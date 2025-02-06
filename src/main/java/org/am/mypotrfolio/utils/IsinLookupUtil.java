package org.am.mypotrfolio.utils;

import lombok.RequiredArgsConstructor;
import org.am.mypotrfolio.entity.NseSecurityEntity;
import org.am.mypotrfolio.repo.NseSecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IsinLookupUtil {
    private static final Logger log = LoggerFactory.getLogger(IsinLookupUtil.class);
    private final NseSecurityRepository nseSecurityRepository;

    public Optional<Map<String, String>> findIsinBySecurityName(String securityName) {
        if (securityName == null || securityName.trim().isEmpty()) {
            return Optional.empty();
        }

        // Try exact match first
        Optional<NseSecurityEntity> exactMatch = nseSecurityRepository.findBestMatchBySearchParam(securityName);
        if (exactMatch.isPresent()) {
            log.info("Found ISIN {} for security {} through exact match", 
                exactMatch.get().getIsin(), securityName);
            return Optional.of(Map.of(
                    "security_id", exactMatch.get().getSecurityId(),
                    "isin", exactMatch.get().getIsin()
            ));
        }

        // Try fuzzy match if exact match fails
        var fuzzyMatches = nseSecurityRepository.findBySecurityNameFuzzy(securityName);
        if (!fuzzyMatches.isEmpty()) {
            log.info("Found ISIN {} for security {} through fuzzy match", 
                fuzzyMatches.get(0).getIsin(), securityName);
            return Optional.of(Map.of(
                    "security_id", fuzzyMatches.get(0).getSecurityId(),
                    "isin", fuzzyMatches.get(0).getIsin()
            ));
        }

        log.warn("No ISIN found for security: {}", securityName);
        return Optional.empty();
    }
}
