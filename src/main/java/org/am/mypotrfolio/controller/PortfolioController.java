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
        return Flux.just(dhanPortfolioService.processNseStock(file));
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
        return Flux.just(mStockPortfolioService.processNseStock(file));
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
        return Flux.just(zerodhaPortfolioService.processNseStock(file));
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
        return testPortfolioService.getNseStocks(filterBy, maxCount);
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
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_DISPOSITION, Constant.ATTACHMENT_FILENAME_COMPANY_TEMPLATE_XLSX + Constant.EXCEL_FILENAME);
        return new ResponseEntity<>(testService.generateRoutingListExcel(),  headers, HttpStatus.OK);
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
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_DISPOSITION, Constant.ATTACHMENT_FILENAME_COMPANY_TEMPLATE_XLSX + Constant.EXCEL_PORTFOLIO_FILENAME);
        return new ResponseEntity<>(testService.generatePortfolioListExcel(),  headers, HttpStatus.OK);
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
        mutualFundService.uploadMutualFundFiles(path);
    }
}
