package com.livedata.hub.controller;

import com.livedata.hub.dto.BroadcastItem;
import com.livedata.hub.service.BroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BroadcastController {

    private final BroadcastService broadcastService;

    @GetMapping("/")
    public String index(@RequestParam(name = "type", defaultValue = "live") String type, Model model) {
        // Service로 데이터 10개 수집
        List<BroadcastItem> broadcastList = broadcastService.getBroadcastList(type);

        // HTML에 데이터 전달
        model.addAttribute("broadcastList", broadcastList);
        model.addAttribute("currentType", type);

        return "index"; // templates/index.html
    }
}