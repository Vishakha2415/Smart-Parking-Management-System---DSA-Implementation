// File: source/models/Vehicle.java
package models;

import java.time.LocalDateTime;

public class Vehicle {
    private String licensePlate;
    private String vehicleType;
    private boolean isVIP;
    private boolean isElectric;
    private LocalDateTime entryTime;
    
    public Vehicle(String licensePlate, String vehicleType, boolean isVIP, boolean isElectric) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.isVIP = isVIP;
        this.isElectric = isElectric;
        this.entryTime = null;
    }
    
    public void setEntryTime() {
        this.entryTime = LocalDateTime.now();
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public long getParkingDurationMinutes() {
        if (entryTime == null) return 0;
        return java.time.Duration.between(entryTime, LocalDateTime.now()).toMinutes();
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public boolean isVIP() {
        return isVIP;
    }
    
    public boolean isElectric() {
        return isElectric;
    }
    
    @Override
    public String toString() {
        return licensePlate + " [" + vehicleType + "]" + 
               (isVIP ? " 👑" : "") + 
               (isElectric ? " ⚡" : "");
    }
}
