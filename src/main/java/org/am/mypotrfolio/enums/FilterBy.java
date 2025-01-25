package org.am.mypotrfolio.enums;

import java.util.Arrays;
import java.util.Optional;

public enum FilterBy {
    QUANTITY("quantity"),
    SYMBOL("symbol"),
    INVESTED_VALUE("investedValue");

    private final String value;

    FilterBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<FilterBy> fromString(String filter) {
        return Arrays.stream(values())
            .filter(f -> f.getValue().equalsIgnoreCase(filter) || f.name().equalsIgnoreCase(filter))
            .findFirst();
    }
}
