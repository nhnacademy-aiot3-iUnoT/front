package com.nhnacademy.front.inventory.controller;

import com.nhnacademy.front.inventory.client.EnvironmentApiClient;
import com.nhnacademy.front.inventory.dto.response.ReviewHistoryDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class EnvironmentReviewController {

    private final EnvironmentApiClient environmentApiClient;

    @GetMapping("/inventories/under-reviews")
    public String inventoryReviewPage(){
        return "review/inventory-review";
    }

    @GetMapping("/environment-reviews")
    public String reviewHistoryPage(){
        return "review/review-history";
    }

    @GetMapping("/environment-reviews/{environment-review}")
    public String reviewDetailPage(
            @PathVariable(name = "environment-review") Long environmentReview,
            Model model
    ){
        ReviewHistoryDetailResponse reviewDetail = environmentApiClient.getReviewDetail(environmentReview);
        model.addAttribute("reviewDetail", reviewDetail);

        return "review/review-detail";
    }
}
