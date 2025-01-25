package org.am.mypotrfolio.controller;

import org.am.mypotrfolio.service.DhanService;
import org.am.mypotrfolio.service.MStockService;
import org.am.mypotrfolio.service.ZerodhaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WebController {

    private final DhanService dhanService;
    private final MStockService mStockService;
    private final ZerodhaService zerodhaService;

    @GetMapping({"/", "/home"})
    public String home() {
        return "redirect:/home/index";
    }

    @GetMapping("/home/index")
    public String homeIndex() {
        return "index";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload/dhan")
    public String uploadDhanPortfolio(@RequestParam("file") MultipartFile file, Model model) {
        try {
            var result = dhanService.processNseStock(file);
            model.addAttribute("portfolioData", result);
            model.addAttribute("platform", "Dhan");
            return "portfolio-result";
        } catch (Exception e) {
            log.error("Error uploading Dhan portfolio", e);
            model.addAttribute("error", "Failed to upload Dhan portfolio: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/upload/mstock")
    public String uploadMStockPortfolio(@RequestParam("file") MultipartFile file, Model model) {
        try {
            var result = mStockService.processNseStock(file);
            model.addAttribute("portfolioData", result);
            model.addAttribute("platform", "MStock");
            return "portfolio-result";
        } catch (Exception e) {
            log.error("Error uploading MStock portfolio", e);
            model.addAttribute("error", "Failed to upload MStock portfolio: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/upload/zerodha")
    public String uploadZerodhaPortfolio(@RequestParam("file") MultipartFile file, Model model) {
        try {
            var result = zerodhaService.processNseStock(file);
            model.addAttribute("portfolioData", result);
            model.addAttribute("platform", "Zerodha");
            return "portfolio-result";
        } catch (Exception e) {
            log.error("Error uploading Zerodha portfolio", e);
            model.addAttribute("error", "Failed to upload Zerodha portfolio: " + e.getMessage());
            return "error";
        }
    }
}
