package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class Location {
    private static final Pattern UNLOCODE_PATTERN = Pattern.compile("^[A-Z]{2}[A-Z0-9]{3}$");
    
    // Lista de portos conhecidos
    private static final Set<String> KNOWN_SEAPORTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "USNYC", "NLRTM", "GBLON", "JPTYO", "SGSIN", 
        "DEHAM", "CNHKG", "USLGB", "CNPVG", "HKHKG"
    )));
    
    private final String unLocCode;
    
    public Location(String unLocCode) {
        if (unLocCode == null || unLocCode.trim().isEmpty()) {
            throw new IllegalArgumentException("UN/LOCODE cannot be null or empty");
        }
        
        String normalized = unLocCode.trim().toUpperCase();
        
        if (!UNLOCODE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid UN/LOCODE format: %s. Expected format: 2 letters + 3 alphanumeric characters", unLocCode)
            );
        }
        
        this.unLocCode = normalized;
    }
    
    public String getUnLocCode() { 
        return this.unLocCode; 
    }
    
    public boolean isSeaport() {
        // Usar lista conhecida de portos
        return KNOWN_SEAPORTS.contains(unLocCode);
    }
    
    public boolean isAirport() {
        // Lógica simplificada
        // Aeroportos geralmente terminam com 3 letras, mas não há regra fixa
        if (unLocCode != null && unLocCode.length() >= 3) {
            char thirdChar = unLocCode.charAt(2);
            return Character.isLetter(thirdChar);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return Objects.equals(unLocCode, location.unLocCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unLocCode);
    }
    
    @Override
    public String toString() {
        return unLocCode;
    }
}
