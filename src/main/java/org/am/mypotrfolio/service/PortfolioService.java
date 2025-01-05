package org.am.mypotrfolio.service;

import java.util.List;

import org.am.mypotrfolio.domain.NseStock;
import org.springframework.web.multipart.MultipartFile;

public interface PortfolioService {

    List<NseStock> processNseStock(MultipartFile fileName);
    
}
