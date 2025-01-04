package org.am.mypotrfolio.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.am.mypotrfolio.model.Constant;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.service.CompanyMasterData;
import org.am.mypotrfolio.service.DhanService;
import org.am.mypotrfolio.service.MStockService;
import org.am.mypotrfolio.service.MutualFundService;
import org.am.mypotrfolio.service.NseStockService;
import org.am.mypotrfolio.service.TestService;
import org.am.mypotrfolio.service.ZerodhaService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PortfolioController {

    private final DhanService dhanPortfolioService;

    private final MStockService mStockPortfolioService;
    private final NseStockService nsestockService;

    private final ZerodhaService zerodhaPortfolioService;

    private final TestService testPortfolioService;

    private final CompanyMasterData companyMasterData;

    private final NseStockRepository  nseStockRepository;
    private final TestService testService;
    private final MutualFundService mutualFundService;

    @PostMapping(path = "/dhan")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void dhan() throws IOException {
        dhanPortfolioService.processNseStock("Dhan") ;
    }

    @PostMapping(path = "/mstock")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void mstock() throws IOException {
        mStockPortfolioService.processNseStock("Dhan") ;
    }

    @PostMapping(path = "/zerodha")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void zerodha() throws IOException {
        zerodhaPortfolioService.processNseStock("Dhan") ;
    }

    @PostMapping(path = "/test")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void test() throws IOException {
        companyMasterData.processCompanyRecords("Company") ;
    }

    @GetMapping(path = "/test1")
    public String test1() {
        return nsestockService.test().toString() ;
    }

    @GetMapping(path = "/portfolio")
    public Map<String,NseStock> portfolio(
            @RequestParam("filterBy") String filterBy,
            @RequestParam("maxCount") Integer maxCount
    ) {
        return testPortfolioService.getNseStocks(filterBy, maxCount) ;
    }

    @GetMapping("/sector-investment")
    public List<SectorInvestmentDTO> getSectorInvestment() {
        return nseStockRepository.findTotalInvestedBySector()
                .stream()
                .sorted(Comparator.comparingDouble(SectorInvestmentDTO::getOverAllPNL))
                .toList();
    }

    @PostMapping("/download")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<ByteArrayResource> download() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_DISPOSITION, Constant.ATTACHMENT_FILENAME_COMPANY_TEMPLATE_XLSX + Constant.EXCEL_FILENAME);
        return new ResponseEntity<>(testService.generateRoutingListExcel(),  headers, HttpStatus.OK);
    }

    @PostMapping("/download-portfolio")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<ByteArrayResource> downloadPortfolio() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_DISPOSITION, Constant.ATTACHMENT_FILENAME_COMPANY_TEMPLATE_XLSX + Constant.EXCEL_PORTFOLIO_FILENAME);
        return new ResponseEntity<>(testService.generatePortfolioListExcel(),  headers, HttpStatus.OK);
    }

    @PostMapping("/upload-mutualcompany-data/{path}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void UploadPortfolio(@PathVariable("path") String path) {
        mutualFundService.uploadMutualFundFiles(path);
    }
}
