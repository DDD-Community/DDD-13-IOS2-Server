package com.bangawo.subway.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 지하철 이동 그래프 — 부팅 시 SubwayGraphLoader 가 load() 호출.
 * dijkstra(source) → Map<stationId, int[]{seconds, transfers}>
 */
@Slf4j
@Component
public class SubwayGraph {

    private Map<Long, List<SubwayEdge>> adjacency = new HashMap<>();
    private volatile boolean loaded = false;

    public void load(List<SubwayEdge> edges) {
        Map<Long, List<SubwayEdge>> graph = new HashMap<>();
        for (SubwayEdge edge : edges) {
            graph.computeIfAbsent(edge.fromStationId(), k -> new ArrayList<>()).add(edge);
        }
        this.adjacency = Collections.unmodifiableMap(graph);
        this.loaded = true;
        log.info("SubwayGraph loaded: {} stations, {} edges", graph.size(), edges.size());
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 단일 출발 다익스트라 결과.
     * @param dist Map<stationId, int[]{seconds, transfers}> — 도달 불가 역은 미포함
     * @param prev Map<stationId, 직전 stationId> — 최단경로 역추적용. source는 키로 존재하지 않음
     */
    public record DijkstraResult(Map<Long, int[]> dist, Map<Long, Long> prev) {
    }

    /**
     * 단일 출발 다익스트라. 비용(소요초/환승수)과 경로복원용 직전역(prev)을 함께 반환.
     */
    public DijkstraResult dijkstra(Long sourceStationId) {
        // dist[stationId] = {totalSeconds, totalTransfers}
        Map<Long, int[]> dist = new HashMap<>();
        // prev[stationId] = 최단경로상 직전 stationId
        Map<Long, Long> prev = new HashMap<>();
        // PQ: {seconds, transfers, stationId}
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));

        dist.put(sourceStationId, new int[]{0, 0});
        pq.offer(new long[]{0L, 0L, sourceStationId});

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            long curSec = cur[0];
            long curTransfers = cur[1];
            long curStation = cur[2];

            int[] best = dist.get(curStation);
            if (best == null || curSec > best[0]) continue;

            List<SubwayEdge> neighbors = adjacency.getOrDefault(curStation, List.of());
            for (SubwayEdge edge : neighbors) {
                long nextSec = curSec + edge.weightSec();
                long nextTransfers = curTransfers + (edge.isTransfer() ? 1 : 0);
                Long nextId = edge.toStationId();

                int[] nextBest = dist.get(nextId);
                if (nextBest == null || nextSec < nextBest[0]) {
                    int[] newDist = {(int) nextSec, (int) nextTransfers};
                    dist.put(nextId, newDist);
                    prev.put(nextId, curStation);
                    pq.offer(new long[]{nextSec, nextTransfers, nextId});
                }
            }
        }

        return new DijkstraResult(dist, prev);
    }

    /**
     * prev 맵으로 출발역→도착역 경로(stationId 순서 리스트)를 복원.
     * - dest == source → [source]
     * - dest 도달 불가(prev에 없고 source도 아님) → [] (빈 리스트)
     */
    public static List<Long> reconstructPath(Map<Long, Long> prev, Long sourceStationId, Long destStationId) {
        if (destStationId == null) {
            return List.of();
        }
        if (destStationId.equals(sourceStationId)) {
            return List.of(sourceStationId);
        }
        if (!prev.containsKey(destStationId)) {
            return List.of();
        }
        LinkedList<Long> path = new LinkedList<>();
        Long cur = destStationId;
        while (cur != null) {
            path.addFirst(cur);
            if (cur.equals(sourceStationId)) {
                break;
            }
            cur = prev.get(cur);
        }
        return path;
    }
}
