package com.livedata.hub.service;

import com.livedata.hub.dto.BroadcastItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BroadcastService {

    private static final String TARGET_URL = "http://live.ecomm-data.com/assignment";

    public List<BroadcastItem> getBroadcastList(String type) {
        List<BroadcastItem> itemList = new ArrayList<>();

        try {
            // Target URL의 HTML 문서 가져오기
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            // 테이블 내 행(tr) 요소 추출
            Elements rows = doc.select("table tbody tr");

            for (Element row : rows) {
                // 최대 10개만 수집
                if (itemList.size() >= 10) {
                    break;
                }

                Elements cols = row.select("td");
                if (cols.size() >= 7) {
                    BroadcastItem item = BroadcastItem.builder()
                            .title(cols.get(0).text())
                            .category(cols.get(1).text())
                            .broadcastTime(cols.get(2).text())
                            .views(cols.get(3).text())
                            .salesCount(cols.get(4).text())
                            .revenue(cols.get(5).text())
                            .productCount(cols.get(6).text())
                            .build();

                    itemList.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemList;
    }
}