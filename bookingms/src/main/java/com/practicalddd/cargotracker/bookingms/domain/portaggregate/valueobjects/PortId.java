package com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class PortId {
    private static final Pattern UNLOCODE_PATTERN = Pattern.compile("^[A-Z]{2}[A-Z0-9]{3}$");
    private final String unLocCode;

    public PortId(String unLocCode) {
        validateUnLocCode(unLocCode);
        this.unLocCode = unLocCode.toUpperCase();
    }

    private void validateUnLocCode(String unLocCode) {
        if (unLocCode == null || unLocCode.trim().isEmpty()) {
            throw new IllegalArgumentException("UN/LOCODE cannot be null or empty");
        }
        if (!UNLOCODE_PATTERN.matcher(unLocCode.toUpperCase()).matches()) {
            throw new IllegalArgumentException("Invalid UN/LOCODE format");
        }
    }

    public String getUnLocCode() { return unLocCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortId)) return false;
        PortId portId = (PortId) o;
        return Objects.equals(unLocCode, portId.unLocCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unLocCode);
    }
}
