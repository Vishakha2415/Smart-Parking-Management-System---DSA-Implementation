package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {
    private String ticketId;
    private Vehicle vehicle;
    private ParkingSlot slot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double priceCharged;
    private boolean isPaid;
    
    public Ticket(Vehicle vehicle, ParkingSlot slot) {
        this.ticketId = "TKT" + UUID.randomUUID().toString().substring(0, 8);
        this.vehicle = vehicle;
        this.slot = slot;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
        this.priceCharged = 0.0;
        this.isPaid = false;
    }
    
    public double getParkingDurationHours() {
        LocalDateTime endTime = (exitTime != null) ? exitTime : LocalDateTime.now();
        long minutes = java.time.Duration.between(entryTime, endTime).toMinutes();
        return minutes / 60.0;
    }
    
    public void completePayment(double price) {
        this.exitTime = LocalDateTime.now();
        this.priceCharged = price;
        this.isPaid = true;
    }
    
    public String getTicketId() {
        return ticketId;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public ParkingSlot getSlot() {
        return slot;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public double getPriceCharged() {
        return priceCharged;
    }
    
    public boolean isPaid() {
        return isPaid;
    }
    
    public void setPriceCharged(double price) {
        this.priceCharged = price;
    }
    
    @Override
    public String toString() {
        return String.format("Ticket %s: %s at Slot %d - Rs%.2f", 
            ticketId, vehicle.getLicensePlate(), slot.getSlotId(), priceCharged);
    }
}
