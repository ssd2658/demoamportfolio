package org.am.mypotrfolio.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AssetType {
    EQUITY("Equity", true),
    DEBT("Debt", false);

    private final String assetType;
    private final boolean isRisky;
}
