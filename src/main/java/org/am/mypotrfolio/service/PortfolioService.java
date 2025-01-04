package org.am.mypotrfolio.service;

import org.am.mypotrfolio.domain.NseStock;

public interface PortfolioService {

    NseStock processNseStock(String fileName);
}
