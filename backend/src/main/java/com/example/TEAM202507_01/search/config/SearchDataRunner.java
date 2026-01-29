package com.example.TEAM202507_01.search.config; // 패키지는 적절히 변경

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.TEAM202507_01.search.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchDataRunner implements ApplicationRunner {

    private final SearchService searchService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // 컨트롤러를 거치지 않고 서비스의 로직을 바로 실행
            searchService.migrateAllData();
            log.info("✅ 엘라스틱서치 데이터 완료");
        } catch (Exception e) {
            log.error("❌ 마이그레이션 실패", e);
        }
    }
}