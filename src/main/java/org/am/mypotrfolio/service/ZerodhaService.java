package org.am.mypotrfolio.service;

// Lombok annotations
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

// Project imports
import org.am.mypotrfolio.utils.NseFileBuilder;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.ZerodhaStockPortfolio;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.mapper.PortfolioMapper;
import org.am.mypotrfolio.repo.NseStockRepository;

// Spring imports
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// Jackson imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// Java core imports
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("Zerodha")
@RequiredArgsConstructor
public class ZerodhaService implements PortfolioService {
    private final NseFileBuilder nseFileBuilder;
    private final NseStockRepository nseStockRepository;

    @Override
    @SneakyThrows
    public List<NseStock> processNseStock(MultipartFile file) {
        List<Map<String, String>> fileJson = nseFileBuilder.parseExcel(file, "Zerodha");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(fileJson);

        List<ZerodhaStockPortfolio> stocks = objectMapper.readValue(payload, new TypeReference<List<ZerodhaStockPortfolio>>() {});

        List<NseStock> nseStocks = stocks.stream().map(stock -> {
            var zerodhaStock = PortfolioMapper.INSTANCE.toNseStockFromZerodha(stock);
            var nseEntity = NseStockMapper.INSTANCE.mapNseStockEntity(zerodhaStock);
            nseEntity.setBrokerPlatform("Zerodha");
            nseEntity.setUserId("XMW248");
            nseStockRepository.save(nseEntity);
            return zerodhaStock;
        }).collect(Collectors.toList());
        return nseStocks;
    }
}
