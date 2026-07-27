package com.livedata.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastItem {
    private String title;          // 방송정보(제목)
    private String platform;   // "네이버쇼핑LIVE"
    private String category;       // 분류
    private String broadcastTime;  // 방송시간
    private String views;          // 조회수/시청률
    private String salesCount;     // 판매량
    private String revenue;        // 매출액
    private String productCount;   // 상품수
}