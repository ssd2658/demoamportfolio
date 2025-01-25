package org.am.mypotrfolio.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.NseStockDetails;
import org.am.mypotrfolio.enums.FilterBy;
import org.am.mypotrfolio.service.DhanService;
import org.am.mypotrfolio.service.MStockService;
import org.am.mypotrfolio.service.TestService;
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
    private final TestService testPortfolioService;

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

    @PostMapping("/upload-portfolio")
    public String uploadPortfolio(@RequestParam("platform") String platform,
                                  @RequestParam("file") MultipartFile file,
                                  Model model) {
        try {
            switch (platform.toLowerCase()) {
                case "dhan":
                    return uploadDhanPortfolio(file, model);
                case "mstock":
                    return uploadMStockPortfolio(file, model);
                case "zerodha":
                    return uploadZerodhaPortfolio(file, model);
                default:
                    model.addAttribute("error", "Invalid platform selected");
                    return "error";
            }
        } catch (Exception e) {
            log.error("Error uploading portfolio", e);
            model.addAttribute("error", "Failed to upload portfolio: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/view-portfolio")
    public String viewPortfolio(Model model) {
        try {
            // Fetch portfolio data from a service
            List<NseStockDetails> portfolioData = testPortfolioService.getAllStocks();
            
            if (portfolioData == null || portfolioData.isEmpty()) {
                // No data scenario
                model.addAttribute("portfolioData", Collections.emptyList());
                return "portfolio-view";
            }

            // Calculate summary metrics
            double totalInvestment = portfolioData.stream()
                .mapToDouble(NseStockDetails::getTotalInvestment)
                .sum();

            double currentPortfolioValue = portfolioData.stream()
                .mapToDouble(NseStockDetails::getCurrentValue)
                .sum();

            double dailyReturnChange = portfolioData.stream()
                .mapToDouble(NseStockDetails::getReturnChange)
                .sum();

            double totalProfitLoss = Double.parseDouble(String.format("%.2f", currentPortfolioValue - totalInvestment));

            // Calculate percentage change
            double totalPercentChange = totalInvestment > 0 
                ? Double.parseDouble(String.format("%.2f", ((currentPortfolioValue - totalInvestment) / totalInvestment) * 100)) 
                : 0.0;

            // Calculate daily return change
            // double dailyTotalInvestment = portfolioData.stream()
            //     .mapToDouble(stock -> stock.getQuantity() * stock.getOpenPrice())
            //     .sum();
            
            // double dailyTotalCurrentValue = portfolioData.stream()
            //     .mapToDouble(stock -> stock.getQuantity() * stock.getCurrentPrice())
            //     .sum();
            
            // double dailyReturnChange = dailyTotalInvestment > 0 
            //     ? Double.parseDouble(String.format("%.2f", (dailyTotalCurrentValue - dailyTotalInvestment))) 
            //     : 0.0;

            // double dailyPercentChange = dailyTotalInvestment > 0 
            //     ? Double.parseDouble(String.format("%.2f", ((dailyTotalCurrentValue - dailyTotalInvestment) / dailyTotalInvestment) * 100)) 
            //     : 0.0;

            // Add attributes for view
            model.addAttribute("portfolioData", portfolioData);
            model.addAttribute("totalInvestment", totalInvestment);
            model.addAttribute("currentPortfolioValue", currentPortfolioValue);
            model.addAttribute("totalProfitLoss", totalProfitLoss);
            model.addAttribute("totalPercentChange", totalPercentChange);
            model.addAttribute("dailyReturnChange", dailyReturnChange);
            //model.addAttribute("dailyPercentChange", dailyPercentChange);

            return "portfolio-view";
        } catch (Exception e) {
            log.error("Error fetching portfolio data", e);
            model.addAttribute("error", "Unable to fetch portfolio data. Please try again.");
            model.addAttribute("portfolioData", Collections.emptyList());
            return "portfolio-view";
        }
    }
}
