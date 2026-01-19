package models;

public class ParkingSlot {
    private int slotId;
    private String slotType;
    private int distanceFromEntrance;
    private boolean isOccupied;
    private Vehicle parkedVehicle;
    private double basePrice;
    
    public ParkingSlot(int slotId, String slotType, int distanceFromEntrance, double basePrice) {
        this.slotId = slotId;
        this.slotType = slotType;
        this.distanceFromEntrance = distanceFromEntrance;
        this.basePrice = basePrice;
        this.isOccupied = false;
        this.parkedVehicle = null;
    }
    
    public boolean isAvailableFor(Vehicle vehicle) {
        if (isOccupied) return false;
        if (slotType.equals("EV_CHARGING") && !vehicle.isElectric()) return false;
        if (slotType.equals("VIP") && !vehicle.isVIP()) return false;
        return true;
    }
    
    public boolean occupy(Vehicle vehicle) {
        if (!isOccupied && isAvailableFor(vehicle)) {
            this.parkedVehicle = vehicle;
            this.isOccupied = true;
            return true;
        }
        return false;
    }
    
    public void vacate() {
        this.parkedVehicle = null;
        this.isOccupied = false;
    }
    
    public int getSlotId() {
        return slotId;
    }
    
    public String getSlotType() {
        return slotType;
    }
    
    public int getDistanceFromEntrance() {
        return distanceFromEntrance;
    }
    
    public boolean isOccupied() {
        return isOccupied;
    }
    
    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }
    
    public double getBasePrice() {
        return basePrice;
    }
    
    @Override
    public String toString() {
        String status = isOccupied ? "[OCCUPIED]" : "[AVAILABLE]";
        String vehicleInfo = isOccupied ? " (" + parkedVehicle.getLicensePlate() + ")" : "";
        return String.format("%s Slot#%02d [%s] %dm Rs%.0f/hr%s", 
            status, slotId, slotType, distanceFromEntrance, basePrice, vehicleInfo);
    }
}