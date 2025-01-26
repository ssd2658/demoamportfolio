package org.am.mypotrfolio.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.am.mypotrfolio.domain.BrokerPortfolioSummary;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.NseStockDetails;
import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.enums.FilterBy;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.service.DhanService;
import org.am.mypotrfolio.service.MStockService;
import org.am.mypotrfolio.service.TestService;
import org.am.mypotrfolio.service.ZerodhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class WebController {

    @Autowired
    private DhanService dhanService;
    @Autowired
    private MStockService mStockService;
    @Autowired
    private ZerodhaService zerodhaService;
    @Autowired
    private TestService testPortfolioService;
    @Autowired
    private NseStockRepository nseStockRepository;

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

    @GetMapping("/portfolio-view")
    public String viewPortfolio(
        @RequestParam(value = "brokerPlatform", required = false) String brokerPlatform, 
        Model model
    ) {
        try {
            // Fetch all stock details
            List<NseStockDetails> nseStockDetails = testPortfolioService.getAllStocks();

            // Log broker platforms for debugging
            log.info("Total stocks loaded: {}", nseStockDetails.size());
            nseStockDetails.stream()
                .filter(stock -> stock.getBrokerPlatform() != null)
                .collect(Collectors.groupingBy(NseStockDetails::getBrokerPlatform))
                .forEach((platform, stocks) -> 
                    log.info("Broker Platform: {}, Stocks Count: {}", platform, stocks.size())
                );

            // Group stocks by broker platform and calculate summary
            Map<String, BrokerPortfolioSummary> brokerSummaries = nseStockDetails.stream()
                .filter(stock -> stock.getBrokerPlatform() != null)
                .collect(Collectors.groupingBy(
                    NseStockDetails::getBrokerPlatform,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        brokerStocks -> {
                            double totalInvested = brokerStocks.stream()
                                .mapToDouble(NseStockDetails::getInvestedValue)
                                .sum();
                            
                            double currentValue = brokerStocks.stream()
                                .mapToDouble(NseStockDetails::getCurrentValue)
                                .sum();
                            
                            double profitLoss = currentValue - totalInvested;
                            
                            double percentageChange = totalInvested != 0 ? 
                                (profitLoss / totalInvested) * 100 : 0.0;
                            
                            return BrokerPortfolioSummary.builder()
                                .brokerPlatform(brokerStocks.get(0).getBrokerPlatform())
                                .totalInvested(totalInvested)
                                .currentValue(currentValue)
                                .profitLoss(profitLoss)
                                .percentageChange(percentageChange)
                                .build();
                        }
                    )
                ));

            // Convert map to list for Thymeleaf rendering
            List<BrokerPortfolioSummary> brokerPlatforms = new ArrayList<>(brokerSummaries.values());

            // Log broker platforms for debugging
            log.info("Broker Platforms Count: {}", brokerPlatforms.size());
            brokerPlatforms.forEach(platform -> 
                log.info("Platform: {}, Total Invested: {}", platform.getBrokerPlatform(), platform.getTotalInvested())
            );

            // Add attributes to model
            model.addAttribute("brokerPlatforms", brokerPlatforms);
            model.addAttribute("nseStockDetails", nseStockDetails);

            return "portfolio-view";

        } catch (Exception e) {
            log.error("Error in portfolio view", e);
            model.addAttribute("error", "Unable to load portfolio. Please try again.");
            return "portfolio-view";
        }
    }

    @GetMapping("/view-portfolio")
    public String viewPortfolioOld(
        @RequestParam(value = "brokerPlatform", required = false) String brokerPlatform, 
        Model model
    ) {
        try {
            // Fetch portfolio data from a service
            List<NseStockDetails> allStockDetails = testPortfolioService.getAllStocks();
            
            // Ensure allStockDetails is not null
            allStockDetails = allStockDetails != null ? allStockDetails : Collections.emptyList();
            
            // Get unique broker platforms for dropdown
            List<String> brokerPlatforms = allStockDetails.stream()
                .filter(stock -> stock.getBrokerPlatform() != null)
                .map(NseStockDetails::getBrokerPlatform)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            // Filter stocks by broker platform if specified
            List<NseStockDetails> nseStockDetails = brokerPlatform != null && !brokerPlatform.isEmpty()
                ? allStockDetails.stream()
                    .filter(stock -> brokerPlatform.equalsIgnoreCase(stock.getBrokerPlatform()))
                    .collect(Collectors.toList())
                : allStockDetails;
            
            if (nseStockDetails.isEmpty()) {
                // No data scenario
                model.addAttribute("nseStockDetails", Collections.emptyList());
                model.addAttribute("brokerPlatforms", brokerPlatforms);
                model.addAttribute("selectedBrokerPlatform", brokerPlatform);
                return "portfolio-view";
            }

            // // Calculate portfolio metrics with null-safe operations
            // double totalInvestment = nseStockDetails.stream()
            //     .mapToDouble(stock -> stock.getInvestedValue() != 0.0 ? stock.getInvestedValue() : 0.0)
            //     .sum();

            // double currentPortfolioValue = nseStockDetails.stream()
            //     .mapToDouble(stock -> Optional.ofNullable(stock.getCurrentValue()).orElse(0.0))
            //     .sum();

            // double dailyReturnChange = nseStockDetails.stream()
            //     .mapToDouble(stock -> stock.getReturnChange() != 0.0 ? stock.getReturnChange() : 0.0)
            //     .sum();

            // // Safely calculate profit/loss and percentage change
            // double totalProfitLoss = currentPortfolioValue - totalInvestment;
            // double totalPercentChange = totalInvestment != 0.0 
            //     ? ((currentPortfolioValue - totalInvestment) / totalInvestment) * 100 
            //     : 0.0;

            // // Add attributes for view
            // model.addAttribute("nseStockDetails", nseStockDetails);
            // model.addAttribute("brokerPlatforms", brokerPlatforms);
            // model.addAttribute("selectedBrokerPlatform", brokerPlatform);
            // model.addAttribute("totalInvestment", totalInvestment);
            // model.addAttribute("currentPortfolioValue", currentPortfolioValue);
            // model.addAttribute("totalProfitLoss", totalProfitLoss);
            // model.addAttribute("totalPercentChange", totalPercentChange);
            // model.addAttribute("dailyReturnChange", dailyReturnChange);

            setPortfolioCommonData(nseStockDetails, model);

            return "portfolio-view";

        } catch (Exception e) {
            log.error("Error fetching portfolio data", e);
            model.addAttribute("error", "Unable to fetch portfolio data. Please try again.");
            model.addAttribute("nseStockDetails", Collections.emptyList());
            model.addAttribute("brokerPlatforms", Collections.emptyList());
            return "portfolio-view";
        }
    }

    @GetMapping("/broker-portfolio-details")
    public String brokerPortfolioDetails(@RequestParam(value = "brokerPlatform", required = true) String brokerPlatform, 
                                         @RequestParam(value = "page", required = false) Integer page,
                                         @RequestParam(value = "sortBy", required = false) String sortBy,
                                         @RequestParam(value = "sortOrder", required = false) String sortOrder,
                                         Model model) {
        try {
            log.info("Fetching broker portfolio details for platform: {}", brokerPlatform);
            
            // Fetch stocks for specific broker platform
            List<NseStockDetails> brokerStocks = testPortfolioService.getAllStocks(brokerPlatform);

            if (brokerStocks == null || brokerStocks.isEmpty()) {
                log.warn("No stocks found for broker platform: {}", brokerPlatform);
                model.addAttribute("errorMessage", "No portfolio data found for " + brokerPlatform);
                return "broker-portfolio-details";
            }
            setPortfolioCommonData(brokerStocks, model);
            double currentPortfolioValue = brokerStocks.stream()
            .mapToDouble(stock -> Optional.ofNullable(stock.getCurrentValue()).orElse(0.0))
            .sum();
            // Pagination for sector investments
            int pageSize = 10;
            int pageNumber = page != null && page >= 0 ? page : 0;
            
            List<SectorInvestmentDTO> sectorInvestments = nseStockRepository.getSectorInvestments(brokerPlatform);
            
            // Validate and adjust page number
            int totalElements = sectorInvestments.size();
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            pageNumber = Math.min(pageNumber, Math.max(0, totalPages - 1));
            
            // Paginate the list manually
            List<SectorInvestmentDTO> paginatedSectorInvestments = sectorInvestments.stream()
                .skip((long) pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
            
            // Create Page object
            Pageable pageable = PageRequest.of(
                pageNumber, 
                pageSize, 
                Sort.by(sortBy != null ? sortBy : "industry").ascending()
            );
            
            Page<SectorInvestmentDTO> sectorInvestmentsPage = new PageImpl<>(
                paginatedSectorInvestments,
                pageable,
                totalElements
            );
            
            log.info("Sector Investments Page - Total Elements: {}, Total Pages: {}", 
                     sectorInvestmentsPage.getTotalElements(), 
                     sectorInvestmentsPage.getTotalPages());
            
            if (sectorInvestmentsPage != null) {
                model.addAttribute("sectorInvestments", sectorInvestmentsPage.getContent());
                model.addAttribute("sectorInvestmentsPage", sectorInvestmentsPage);
                model.addAttribute("currentPage", pageNumber);
                model.addAttribute("totalPages", sectorInvestmentsPage.getTotalPages());
                model.addAttribute("currentSortBy", sortBy != null ? sortBy : "industry");
                model.addAttribute("currentSortOrder", "ASC");
                model.addAttribute("brokerPlatform", brokerPlatform);
            } else {
                log.error("Sector investments page is null");
                model.addAttribute("errorMessage", "Failed to load sector investments page");
            }

            // Top performing and worst performing stocks
            List<NseStockDetails> topPerformingStocks = brokerStocks.stream()
                .sorted(Comparator.comparing(NseStockDetails::getPercentChange).reversed())
                .limit(5)
                .collect(Collectors.toList());

            List<NseStockDetails> worstPerformingStocks = brokerStocks.stream()
                .sorted(Comparator.comparing(NseStockDetails::getPercentChange))
                .limit(5)
                .collect(Collectors.toList());

            model.addAttribute("topPerformingStocks", topPerformingStocks);
            model.addAttribute("worstPerformingStocks", worstPerformingStocks);

            return "broker-portfolio-details";

        } catch (Exception e) {
            log.error("Error fetching broker portfolio details for {}", brokerPlatform, e);
            model.addAttribute("errorMessage", "Unable to fetch portfolio details: " + e.getMessage());
            return "broker-portfolio-details";
        }
    }

    private void setPortfolioCommonData(List<NseStockDetails> nseStockDetails, Model model) {

         // Calculate portfolio metrics with null-safe operations
         double totalInvestment = nseStockDetails.stream()
         .mapToDouble(stock -> stock.getInvestedValue() != 0.0 ? stock.getInvestedValue() : 0.0)
         .sum();

     double currentPortfolioValue = nseStockDetails.stream()
         .mapToDouble(stock -> Optional.ofNullable(stock.getCurrentValue()).orElse(0.0))
         .sum();

     double dailyReturnChange = nseStockDetails.stream()
         .mapToDouble(stock -> stock.getReturnChange() != 0.0 ? stock.getReturnChange() : 0.0)
         .sum();

     // Safely calculate profit/loss and percentage change
     double totalProfitLoss = currentPortfolioValue - totalInvestment;
     double totalPercentChange = totalInvestment != 0.0 
         ? ((currentPortfolioValue - totalInvestment) / totalInvestment) * 100 
         : 0.0;

     // Add attributes for view
     model.addAttribute("brokerStocks", nseStockDetails);
     model.addAttribute("nseStockDetails", nseStockDetails);
     model.addAttribute("totalInvestment", totalInvestment);
     model.addAttribute("currentPortfolioValue", currentPortfolioValue);
     model.addAttribute("totalProfitLoss", totalProfitLoss);
     model.addAttribute("totalPercentChange", totalPercentChange);
     model.addAttribute("dailyReturnChange", dailyReturnChange);

    }
}
