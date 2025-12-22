package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DeadlinePolicy {
    public static final int MIN_HOURS_FOR_BOOKING = 24;
    public static final int URGENT_THRESHOLD_DAYS = 7;
    public static final int CRITICAL_THRESHOLD_HOURS = 48;
    
    // TEMPOS MÍNIMOS REALISTAS BASEADOS NA ROTA
    public static final int MIN_DAYS_DOMESTIC = 3;       // Rotas domésticas
    public static final int MIN_DAYS_REGIONAL = 7;       // Rotas regionais (ex: Europa-Europa)
    public static final int MIN_DAYS_INTERCONTINENTAL = 14; // Rotas intercontinentais
    
    private final LocalDateTime deadline;
    private final LocalDateTime referenceTime;
    private final boolean isIntercontinental;
    
    public DeadlinePolicy(LocalDateTime deadline, boolean isIntercontinental) {
        this(deadline, LocalDateTime.now(), isIntercontinental);
    }
    
    public DeadlinePolicy(LocalDateTime deadline, LocalDateTime referenceTime, boolean isIntercontinental) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }
        if (referenceTime == null) {
            throw new IllegalArgumentException("Reference time cannot be null");
        }
        
        this.deadline = deadline;
        this.referenceTime = referenceTime;
        this.isIntercontinental = isIntercontinental;
    }
    
    public boolean isValidForBooking() {
        long daysUntilDeadline = ChronoUnit.DAYS.between(referenceTime, deadline);
        
        if (isIntercontinental) {
            return daysUntilDeadline >= MIN_DAYS_INTERCONTINENTAL;
        } else {
            // Para simplificação, consideraremos tudo não-intercontinental como regional
            // No futuro, deve ter lógica para distinguir doméstico vs regional
            return daysUntilDeadline >= MIN_DAYS_REGIONAL;
        }
    }
    
    public boolean isUrgent() {
        long daysUntilDeadline = ChronoUnit.DAYS.between(referenceTime, deadline);
        
        if (isIntercontinental) {
            // Para rotas intercontinentais, considerar urgente se faltar menos de 21 dias
            return daysUntilDeadline < 21;
        } else {
            return daysUntilDeadline < URGENT_THRESHOLD_DAYS;
        }
    }
    
    public boolean isCritical() {
        long hoursUntilDeadline = ChronoUnit.HOURS.between(referenceTime, deadline);
        return hoursUntilDeadline < CRITICAL_THRESHOLD_HOURS;
    }
    
    public long getRemainingHours() {
        return ChronoUnit.HOURS.between(referenceTime, deadline);
    }
    
    public long getRemainingDays() {
        return ChronoUnit.DAYS.between(referenceTime, deadline);
    }
    
    public String getPriorityCategory() {
        if (getRemainingHours() < CRITICAL_THRESHOLD_HOURS) {
            return "CRITICAL";
        } else if (isUrgent()) {
            return "URGENT";
        } else {
            return "STANDARD";
        }
    }
    
    public String getMinimumRequiredDays() {
        if (isIntercontinental) {
            return MIN_DAYS_INTERCONTINENTAL + " dias (rota intercontinental)";
        } else {
            return MIN_DAYS_REGIONAL + " dias (rota regional)";
        }
    }
    
    public boolean isIntercontinental() {
        return isIntercontinental;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeadlinePolicy)) return false;
        DeadlinePolicy that = (DeadlinePolicy) o;
        return isIntercontinental == that.isIntercontinental &&
               Objects.equals(deadline, that.deadline) &&
               Objects.equals(referenceTime, that.referenceTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(deadline, referenceTime, isIntercontinental);
    }
}
