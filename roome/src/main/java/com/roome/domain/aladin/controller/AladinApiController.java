package com.roome.domain.aladin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api")
@Tag(name = "알라딘 API", description = "알라딘 도서 검색 관련 API")
public class AladinApiController {

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    private final WebClient webClient;

    public AladinApiController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://www.aladin.co.kr").build();
    }

    @Operation(summary = "도서 검색", description = "알라딘 서점 API를 이용하여 도서를 검색합니다.")
    @GetMapping("/aladin/search")
    public ResponseEntity<String> searchBooks(
            @Parameter(description = "검색 키워드", required = true) @RequestParam String keyword,
            @Parameter(description = "검색 타입 (Keyword, Title, Author, Publisher 등)", required = false) @RequestParam(required = false, defaultValue = "Keyword") String queryType,
            @Parameter(description = "검색 결과 수", required = false) @RequestParam(required = false, defaultValue = "10") int maxResults,
            @Parameter(description = "검색 시작 위치", required = false) @RequestParam(required = false, defaultValue = "1") int start,
            @Parameter(description = "검색 대상 (Book, Music, DVD 등)", required = false) @RequestParam(required = false, defaultValue = "Book") String searchTarget,
            @Parameter(description = "표지 크기 (Big, MidBig, Mid, Small, Mini)", required = false) @RequestParam(required = false, defaultValue = "Big") String cover) {

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ttb/api/ItemSearch.aspx")
                        .queryParam("ttbkey", aladinApiKey)
                        .queryParam("Query", keyword)
                        .queryParam("QueryType", queryType)
                        .queryParam("MaxResults", maxResults)
                        .queryParam("start", start)
                        .queryParam("SearchTarget", searchTarget)
                        .queryParam("output", "js")
                        .queryParam("Version", "20131101")
                        .queryParam("Cover", cover)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok(response);
    }
}