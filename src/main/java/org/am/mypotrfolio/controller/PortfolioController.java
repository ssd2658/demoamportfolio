package org.am.mypotrfolio.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.entity.NseStockEntity;
// import org.am.mypotrfolio.exceptions.ApiSubError;
// import org.am.mypotrfolio.exceptions.ApiValidationError;
// import org.am.mypotrfolio.exceptions.BadRequestException;

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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
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
    public Flux<List<NseStock>> dhan(@RequestParam("file") MultipartFile file) throws IOException {
        // if (file.isEmpty()) {
        //   return Mono.defer(() -> Mono.error(new BadRequestException("Bad")));
        // }
        return Flux.just(dhanPortfolioService.processNseStock(file));
    }

    @PostMapping(path = "/mstock")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Flux<List<NseStock>> mstock(@RequestParam("file") MultipartFile file) throws IOException {
        return Flux.just(mStockPortfolioService.processNseStock(file));
    }

    @PostMapping(path = "/zerodha")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Flux<List<NseStock>> zerodha(@RequestParam("file") MultipartFile file) throws IOException {
        return Flux.just(zerodhaPortfolioService.processNseStock(file));
    }

    @PostMapping(path = "/test")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void test(@RequestParam("file") MultipartFile file) throws IOException {
        companyMasterData.processCompanyRecords("Company") ;
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

    // private ApiSubError buildApiValidationError(String fieldName,String rejectedValue, String message,String errorCode) {
    //     return new ApiValidationError(fieldName, rejectedValue, message);
    // }

    // private RuntimeException buildApiValidationError(String value, String code, String errorMessage) {
    // ApiSubError apiSubError = new ApiValidationError(null, value, errorMessage);
    
    // if (HttpStatusCode.valueOf(code).equals(HttpStatusCode)) {
    //     return new AccessDeniedException(BookingDataValidation.FORBIDDEN_ERROR_MESSAGE);
    // } else {
    //     return new BadRequestException(null, new ArrayList<>(List.of(apiSubError)), BOOKING_DATA_ERROR);
    // }
    //}

}
