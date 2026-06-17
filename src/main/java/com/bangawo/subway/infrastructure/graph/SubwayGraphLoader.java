package com.bangawo.subway.infrastructure.graph;

import com.bangawo.subway.domain.SubwayEdge;
import com.bangawo.subway.domain.SubwayEdgeRepository;
import com.bangawo.subway.domain.SubwayGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubwayGraphLoader implements ApplicationRunner {

    private final SubwayEdgeRepository subwayEdgeRepository;
    private final SubwayGraph subwayGraph;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<SubwayEdge> edges = subwayEdgeRepository.findAll();
            if (edges.isEmpty()) {
                log.warn("SubwayGraph: subway_edge 데이터 없음 — 이동부담 계산 불가");
                return;
            }
            subwayGraph.load(edges);
        } catch (Exception e) {
            log.error("SubwayGraph 로드 실패 — 애플리케이션 기동은 계속됨", e);
        }
    }
}
