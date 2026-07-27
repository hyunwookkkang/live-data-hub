package com.livedata.hub.service;

import com.livedata.hub.dto.BroadcastItem;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BroadcastService {

    private static final String TARGET_URL = "https://live.ecomm-data.com/api/assignment/list";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<BroadcastItem> getBroadcastList(String type) {

        String apiType = "shopping".equalsIgnoreCase(type) ? "hs" : "lb";

        List<BroadcastItem> itemList = new ArrayList<>();

        try {

            // API에 보낼 JSON Body 생성
            String jsonRequestBody = String.format("{\"type\":\"%s\"}", apiType);

            // POST 요청 전송 후 JSON 응답(body) 수집
            String responseJson = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Content-Type", "application/json")
                    .requestBody(jsonRequestBody)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute()
                    .body();

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode listNode = rootNode.get("list");

            if (listNode != null && listNode.isArray()) {
                for (JsonNode node : listNode) {
                    if (itemList.size() >= 10) {
                        break; // 최대 10개만 수집
                    }



                    BroadcastItem item;

                    if ("lb".equals(apiType)) {

                        String cid = getTextOrDefault(node, "cid");
                        //System.out.println("방송 제목: " + getTextOrDefault(node, "title") + " | cid 코드: " + cid);
                        String platformId = getTextOrDefault(node, "platform_id");

                        String platformName = switch (platformId) {
                            case "naver" -> "네이버쇼핑LIVE";
                            case "kakao" -> "카카오쇼핑LIVE";
                            case "cj", "cjonstyle" -> "CJ온스타일";
                            case "hyundai", "hmall", "shora" -> "현대Hmall 쇼라";
                            case "gmarket", "glive" -> "G라이브";
                            case "11st" -> "11번가 라이브11";
                            default -> "네이버쇼핑LIVE"; // 기본값
                        };

                        item = BroadcastItem.builder()
                                .title(getTextOrDefault(node, "title"))
                                .platform(platformName)
                                .category(getNaverCategoryName(cid))
                                .broadcastTime(formatTime(getTextOrDefault(node, "datetime_start")))
                                .views(getTextOrDefault(node, "visit_cnt"))
                                .salesCount(getTextOrDefault(node, "sales_cnt"))
                                .revenue(getTextOrDefault(node, "sales_amt"))
                                .productCount(getTextOrDefault(node, "product_cnt"))
                                .build();
                    } else {
                        // 홈쇼핑 데이터 매핑
                        String categoryName = "-";
                        if (node.has("cat") && node.get("cat").has("cat_name")) {
                            categoryName = node.get("cat").get("cat_name").asText();
                        }

                        String platformName = getTextOrDefault(node, "platform_name");

                        item = BroadcastItem.builder()
                                .title(getTextOrDefault(node, "hsshow_title"))
                                .platform(platformName)
                                .category(categoryName)
                                .broadcastTime(formatTime(getTextOrDefault(node, "hsshow_datetime_start")))
                                .views(getTextOrDefault(node, "visit_cnt"))
                                .salesCount(getTextOrDefault(node, "sales_cnt"))
                                .revenue(getTextOrDefault(node, "sales_amt"))
                                .productCount(getTextOrDefault(node, "item_cnt"))
                                .build();
                    }

                    itemList.add(item);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemList;
    }

    private String getTextOrDefault(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            String val = node.get(fieldName).asText();
            return val.isEmpty() ? "-" : val;
        }
        return "-";
    }

    // 시간 문자열 변환 메서드
    private String formatTime(String rawTime) {
        if ("-".equals(rawTime) || rawTime == null || rawTime.length() < 10) {
            return rawTime;
        }

        try {
            String cleanTime = rawTime.replaceAll("[^0-9]", "");

            String year, month, day, hour, minute;

            if (cleanTime.length() >= 12) { // YYYYMMDDHHmm
                year = cleanTime.substring(2, 4);
                month = cleanTime.substring(4, 6);
                day = cleanTime.substring(6, 8);
                hour = cleanTime.substring(8, 10);
                minute = cleanTime.substring(10, 12);
            } else if (cleanTime.length() >= 10) { // YYMMDDHHmm
                year = cleanTime.substring(0, 2);
                month = cleanTime.substring(2, 4);
                day = cleanTime.substring(4, 6);
                hour = cleanTime.substring(6, 8);
                minute = cleanTime.substring(8, 10);
            } else {
                return rawTime;
            }

            // 날짜 기반 요일 계산
            int fullYear = Integer.parseInt("20" + year);
            int m = Integer.parseInt(month);
            int d = Integer.parseInt(day);

            java.time.LocalDate date = java.time.LocalDate.of(fullYear, m, d);
            String dayOfWeek = switch (date.getDayOfWeek()) {
                case MONDAY -> "월";
                case TUESDAY -> "화";
                case WEDNESDAY -> "수";
                case THURSDAY -> "목";
                case FRIDAY -> "금";
                case SATURDAY -> "토";
                case SUNDAY -> "일";
            };

            return String.format("%s.%s.%s (%s)\n%s:%s", year, month, day, dayOfWeek, hour, minute);

        } catch (Exception e) {
            return rawTime;
        }
    }

    // 네이버 cid 번호를 카테고리명으로 변환
    private String getNaverCategoryName(String cid) {
        if ("-".equals(cid) || cid == null) return "디지털/가전";

        switch (cid) {
            case "50000102":
                return "가구/인테리어";

            case "50000148":
                return "식품";

            case "50000092":
            case "50000209":
            case "50000210":
            case "50000212":
            case "50000213":
                return "디지털/가전";
        }

        return "디지털/가전";
    }

}