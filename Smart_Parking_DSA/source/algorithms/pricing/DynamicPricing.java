package algorithms.pricing;

import models.ParkingLot;
import models.Ticket;
import models.Vehicle;
import java.time.LocalDateTime;

public class DynamicPricing {
    
    public double calculateDynamicPrice(Ticket ticket, ParkingLot parkingLot) {
        if (ticket == null || parkingLot == null) {
            return 0.0;
        }
        
        double basePrice = calculateBasePrice(ticket);
        double timeMultiplier = calculateTimeMultiplier(ticket);
        double occupancyMultiplier = calculateOccupancyMultiplier(parkingLot);
        double vehicleMultiplier = calculateVehicleMultiplier(ticket.getVehicle());
        
        double finalPrice = basePrice * timeMultiplier * occupancyMultiplier * vehicleMultiplier;
        
        finalPrice = applyPriceCaps(finalPrice, ticket.getVehicle());
        
        return Math.round(finalPrice * 100.0) / 100.0;
    }
    
    private double calculateBasePrice(Ticket ticket) {
        double hours = ticket.getParkingDurationHours();
        double slotBaseRate = ticket.getSlot().getBasePrice();
        
        if (hours < 1.0) {
            hours = 1.0;
        }
        
        return hours * slotBaseRate;
    }
    
    private double calculateTimeMultiplier(Ticket ticket) {
        LocalDateTime entryTime = ticket.getEntryTime();
        int hour = entryTime.getHour();
        
        boolean isPeak = (hour >= 8 && hour < 10) || (hour >= 17 && hour < 20);
        
        return isPeak ? 1.5 : 1.0;
    }
    
    private double calculateOccupancyMultiplier(ParkingLot parkingLot) {
        double occupancyRate = parkingLot.getOccupancyRate();
        
        if (occupancyRate > 0.8) {
            return 1.4;
        } else if (occupancyRate > 0.6) {
            return 1.2;
        } else if (occupancyRate < 0.3) {
            return 0.8;
        }
        
        return 1.0;
    }
    
    private double calculateVehicleMultiplier(Vehicle vehicle) {
        double multiplier = 1.0;
        
        if (vehicle.isVIP()) {
            multiplier *= 0.8;
        }
        
        if (vehicle.isElectric()) {
            multiplier *= 0.9;
        }
        
        return multiplier;
    }
    
    private double applyPriceCaps(double price, Vehicle vehicle) {
        double minPrice = 20.0;
        double maxDailyRate = 500.0;
        
        if (vehicle.isVIP()) {
            maxDailyRate = 800.0;
        }
        
        if (price < minPrice) {
            return minPrice;
        }
        
        if (price > maxDailyRate) {
            return maxDailyRate;
        }
        
        return price;
    }
    
    public String getCurrentPricingInfo(ParkingLot parkingLot) {
        double occupancy = parkingLot.getOccupancyRate();
        double multiplier = calculateOccupancyMultiplier(parkingLot);
        
        return String.format(
            "[PRICING] Current Pricing Info:\n" +
            "   • Occupancy Rate: %.1f%%\n" +
            "   • Pricing Multiplier: %.2fx\n" +
            "   • Peak Hours (8-10 AM, 5-8 PM): 1.5x\n" +
            "   • VIP Discount: 20%%\n" +
            "   • EV Discount: 10%%",
            occupancy * 100, multiplier
        );
    }
    
    public String getPriceBreakdown(Ticket ticket, ParkingLot parkingLot) {
        double base = calculateBasePrice(ticket);
        double timeMult = calculateTimeMultiplier(ticket);
        double occMult = calculateOccupancyMultiplier(parkingLot);
        double vehicleMult = calculateVehicleMultiplier(ticket.getVehicle());
        double finalPrice = base * timeMult * occMult * vehicleMult;
        
        return String.format(
            "[BREAKDOWN] Price Breakdown:\n" +
            "   • Base Price (%.2f hrs × Rs%.0f/hr): Rs%.2f\n" +
            "   • Time Multiplier (%s): %.2fx\n" +
            "   • Occupancy Multiplier (%.1f%%): %.2fx\n" +
            "   • Vehicle Multiplier: %.2fx\n" +
            "   • Final Price: Rs%.2f",
            ticket.getParkingDurationHours(), 
            ticket.getSlot().getBasePrice(),
            base,
            calculateTimeMultiplier(ticket) > 1.0 ? "Peak" : "Off-Peak",
            timeMult,
            parkingLot.getOccupancyRate() * 100,
            occMult,
            vehicleMult,
            finalPrice
        );
    }
}