package org.am.mypotrfolio.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.NseStockDetails;
import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.entity.NseStockEntity;
// import org.am.mypotrfolio.exceptions.ApiSubError;
// import org.am.mypotrfolio.exceptions.ApiValidationError;
// import org.am.mypotrfolio.exceptions.BadRequestException;

import org.am.mypotrfolio.enums.FilterBy;
import org.am.mypotrfolio.model.Constant;
import org.am.mypotrfolio.repo.NseStockRepository;
//import org.am.mypotrfolio.service.CompanyMasterData;
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
import org.springframework.web.bind.annotation.RequestMapping;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "Portfolio Management", description = "APIs for managing investment portfolios across different platforms")
@Slf4j
@RequiredArgsConstructor
public class PortfolioController {

    private final DhanService dhanPortfolioService;

    private final MStockService mStockPortfolioService;
    private final NseStockService nsestockService;

    private final ZerodhaService zerodhaPortfolioService;

    private final TestService testPortfolioService;

    //private final CompanyMasterData companyMasterData;

    private final NseStockRepository  nseStockRepository;
    private final TestService testService;
    private final MutualFundService mutualFundService;

    @PostMapping("/dhan")
    @Operation(
        summary = "Upload Dhan Portfolio",
        description = "Upload a portfolio file from Dhan trading platform",
        tags = {"Portfolio Upload"},
        responses = {
            @ApiResponse(
                responseCode = "201", 
                description = "Portfolio uploaded successfully", 
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid file format", 
                content = @Content(mediaType = "application/json")
            )
        }
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    public Flux<List<NseStock>> dhan(
        @Parameter(
            description = "Portfolio file from Dhan platform", 
            required = true, 
            content = @Content(mediaType = "multipart/form-data")
        ) @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Processing Dhan portfolio file: {}", file.getOriginalFilename());
        try {
            var result = dhanPortfolioService.processNseStock(file);
            log.info("Successfully processed Dhan portfolio with {} stocks", result.size());
            return Flux.just(result);
        } catch (Exception e) {
            log.error("Error processing Dhan portfolio file: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/mstock")
    @Operation(
        summary = "Upload MStock Portfolio",
        description = "Upload a portfolio file from MStock trading platform",
        tags = {"Portfolio Upload"},
        responses = {
            @ApiResponse(
                responseCode = "201", 
                description = "Portfolio uploaded successfully", 
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid file format", 
                content = @Content(mediaType = "application/json")
            )
        }
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    public Flux<List<NseStock>> mstock(
        @Parameter(
            description = "Portfolio file from MStock platform", 
            required = true, 
            content = @Content(mediaType = "multipart/form-data")
        ) @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Processing MStock portfolio file: {}", file.getOriginalFilename());
        try {
            var result = mStockPortfolioService.processNseStock(file);
            log.info("Successfully processed MStock portfolio with {} stocks", result.size());
            return Flux.just(result);
        } catch (Exception e) {
            log.error("Error processing MStock portfolio file: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/zerodha")
    @Operation(
        summary = "Upload Zerodha Portfolio",
        description = "Upload a portfolio file from Zerodha trading platform",
        tags = {"Portfolio Upload"},
        responses = {
            @ApiResponse(
                responseCode = "201", 
                description = "Portfolio uploaded successfully", 
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid file format", 
                content = @Content(mediaType = "application/json")
            )
        }
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    public Flux<List<NseStock>> zerodha(
        @Parameter(
            description = "Portfolio file from Zerodha platform", 
            required = true, 
            content = @Content(mediaType = "multipart/form-data")
        ) @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Processing Zerodha portfolio file: {}", file.getOriginalFilename());
        try {
            var result = zerodhaPortfolioService.processNseStock(file);
            log.info("Successfully processed Zerodha portfolio with {} stocks", result.size());
            return Flux.just(result);
        } catch (Exception e) {
            log.error("Error processing Zerodha portfolio file: {}", e.getMessage(), e);
            throw e;
        }
    }

    // @PostMapping("/test")
    // @Operation(
    //     summary = "Test Portfolio Upload",
    //     description = "Test portfolio upload with a sample file",
    //     tags = {"Portfolio Upload"},
    //     responses = {
    //         @ApiResponse(
    //             responseCode = "201", 
    //             description = "Portfolio uploaded successfully", 
    //             content = @Content(mediaType = "application/json")
    //         ),
    //         @ApiResponse(
    //             responseCode = "400", 
    //             description = "Invalid file format", 
    //             content = @Content(mediaType = "application/json")
    //         )
    //     }
    // )
    // @ResponseStatus(code = HttpStatus.CREATED)
    // public void test(
    //     @Parameter(
    //         description = "Portfolio file for testing", 
    //         required = true, 
    //         content = @Content(mediaType = "multipart/form-data")
    //     ) @RequestParam("file") MultipartFile file) throws IOException {
    //     companyMasterData.processCompanyRecords("Company") ;
    // }

    @GetMapping("/portfolio")
    @Operation(
        summary = "Retrieve Portfolio",
        description = "Retrieve portfolio with optional filtering",
        tags = {"Portfolio Retrieval"},
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Portfolio retrieved successfully", 
                content = @Content(
                    mediaType = "application/json", 
                    schema = @Schema(implementation = Map.class)
                )
            )
        }
    )
    public Map<String,NseStock> portfolio(
        @Parameter(
            description = "Filter criteria for portfolio", 
            required = true,
            schema = @Schema(
                type = "string",
                implementation = FilterBy.class,
                allowableValues = {"QUANTITY", "SYMBOL", "INVESTED_VALUE"}
            )
        ) @RequestParam("filterBy") FilterBy filterBy,
        @Parameter(description = "Maximum number of records to return") 
        @RequestParam("maxCount") Integer maxCount
    ) {
        log.info("Retrieving portfolio with filter: {} and maxCount: {}", filterBy, maxCount);
        try {
            var result = testPortfolioService.getNseStocks(filterBy, maxCount);
            log.info("Successfully retrieved {} portfolio entries", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error retrieving portfolio: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/api/v1/portfolio/stocks")
    @Operation(summary = "Get all NSE stock details for a user")
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved stock details",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = NseStockDetails.class))
        )
    )
    public ResponseEntity<List<NseStockDetails>> getUserStocks(
            @Parameter(description = "Username to fetch stocks for") 
            @RequestParam("username") String username) {
        List<NseStockDetails> stockDetails = testService.getAllStocksByUserId(username);
        return ResponseEntity.ok(stockDetails);
    }

    @PostMapping("/download")
    @Operation(
        summary = "Download Routing List",
        description = "Download routing list as an Excel file",
        tags = {"File Download"},
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Routing list downloaded successfully", 
                content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
        }
    )
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<ByteArrayResource> download() throws IOException {
        log.info("Starting routing list download");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(CONTENT_DISPOSITION, Constant.ATTACHMENT_FILENAME_COMPANY_TEMPLATE_XLSX + Constant.EXCEL_FILENAME);
            var resource = testService.generateRoutingListExcel();
            log.info("Successfully generated routing list excel file");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating routing list excel: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/download-portfolio")
    @Operation(
        summary = "Download Portfolio List",
        description = "Download portfolio list as an Excel file",
        tags = {"File Download"},
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Portfolio list downloaded successfully", 
                content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )
        }
    )
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<ByteArrayResource> downloadPortfolio() throws IOException {
        log.info("Starting portfolio list download");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(CONTENT_DISPOSITION, Constant.ATTACHMENT_FILENAME_COMPANY_TEMPLATE_XLSX + Constant.EXCEL_PORTFOLIO_FILENAME);
            var resource = testService.generatePortfolioListExcel();
            log.info("Successfully generated portfolio list excel file");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating portfolio list excel: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/upload-mutualcompany-data/{path}")
    @Operation(
        summary = "Upload Mutual Company Data",
        description = "Upload mutual company data from a file",
        tags = {"File Upload"},
        responses = {
            @ApiResponse(
                responseCode = "201", 
                description = "Mutual company data uploaded successfully"
            )
        }
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    public void UploadPortfolio(
        @Parameter(description = "File path for mutual company data") 
        @PathVariable("path") String path
    ) {
        log.info("Starting mutual company data upload from path: {}", path);
        try {
            mutualFundService.uploadMutualFundFiles(path);
            log.info("Successfully processed mutual company data from path: {}", path);
        } catch (Exception e) {
            log.error("Error processing mutual company data: {}", e.getMessage(), e);
            throw e;
        }
    }
}
