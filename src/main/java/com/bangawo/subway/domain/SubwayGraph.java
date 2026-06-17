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
     * 단일 출발 다익스트라.
     * @return Map<stationId, int[]{seconds, transfers}> — 도달 불가 역은 포함되지 않음
     */
    public Map<Long, int[]> dijkstra(Long sourceStationId) {
        // dist[stationId] = {totalSeconds, totalTransfers}
        Map<Long, int[]> dist = new HashMap<>();
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
                    pq.offer(new long[]{nextSec, nextTransfers, nextId});
                }
            }
        }

        return dist;
    }
}
