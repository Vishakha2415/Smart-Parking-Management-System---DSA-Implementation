package models;

import java.util.*;

public class ParkingLot {
    private PriorityQueue<ParkingSlot> availableSlots;
    private HashMap<String, ParkingSlot> occupiedSlots;
    private HashMap<String, Ticket> activeTickets;
    private Queue<Vehicle> waitingQueue;
    private String lotId;
    private int totalSlots;
    private int occupiedCount;
    private int vipSlots;
    private int evSlots;
    private double totalRevenue;
    private int totalVehiclesServed;
    private static final double BASE_RATE = 50.0;
    private static final double VIP_RATE = 100.0;
    private static final double EV_RATE = 80.0;
    
    public ParkingLot(int totalSlots) {
        this.lotId = "LOT-" + (System.currentTimeMillis() % 1000);
        this.totalSlots = totalSlots;
        this.occupiedCount = 0;
        this.totalRevenue = 0.0;
        this.totalVehiclesServed = 0;
        this.vipSlots = Math.max(1, (int)(totalSlots * 0.1));
        this.evSlots = Math.max(1, (int)(totalSlots * 0.2));
        
        this.availableSlots = new PriorityQueue<>(
            new Comparator<ParkingSlot>() {
                @Override
                public int compare(ParkingSlot s1, ParkingSlot s2) {
                    int distanceCompare = Integer.compare(
                        s1.getDistanceFromEntrance(), 
                        s2.getDistanceFromEntrance()
                    );
                    if (distanceCompare == 0) {
                        return getSlotPriority(s2) - getSlotPriority(s1);
                    }
                    return distanceCompare;
                }
                
                private int getSlotPriority(ParkingSlot slot) {
                    if (slot.getSlotType().equals("VIP")) return 3;
                    if (slot.getSlotType().equals("EV_CHARGING")) return 2;
                    return 1;
                }
            }
        );
        
        this.occupiedSlots = new HashMap<>();
        this.activeTickets = new HashMap<>();
        this.waitingQueue = new LinkedList<>();
        
        initializeSlots();
        System.out.println("Parking Lot '" + lotId + "' initialized with " + totalSlots + " slots");
    }
    
    private void initializeSlots() {
        Random rand = new Random(42);
        int slotNumber = 1;
        
        for (int i = 0; i < vipSlots; i++) {
            int distance = 5 + rand.nextInt(20);
            ParkingSlot slot = new ParkingSlot(slotNumber++, "VIP", distance, VIP_RATE);
            availableSlots.add(slot);
        }
        
        for (int i = 0; i < evSlots; i++) {
            int distance = 15 + rand.nextInt(30);
            ParkingSlot slot = new ParkingSlot(slotNumber++, "EV_CHARGING", distance, EV_RATE);
            availableSlots.add(slot);
        }
        
        while (slotNumber <= totalSlots) {
            int distance = 25 + rand.nextInt(50);
            ParkingSlot slot = new ParkingSlot(slotNumber++, "REGULAR", distance, BASE_RATE);
            availableSlots.add(slot);
        }
    }
    
    public Ticket parkVehicle(Vehicle vehicle) {
        System.out.println("\n[PARKING] Vehicle: " + vehicle.getLicensePlate() + 
                         " Type: " + vehicle.getVehicleType() +
                         (vehicle.isVIP() ? " [VIP]" : "") +
                         (vehicle.isElectric() ? " [EV]" : ""));
        
        if (occupiedSlots.containsKey(vehicle.getLicensePlate())) {
            System.out.println("  ERROR: Vehicle already parked!");
            return null;
        }
        
        ParkingSlot allocatedSlot = findSuitableSlot(vehicle);
        
        if (allocatedSlot == null) {
            waitingQueue.add(vehicle);
            System.out.println("  WAITING: Parking full! Added to queue. Position: " + waitingQueue.size());
            return null;
        }
        
        allocatedSlot.occupy(vehicle);
        occupiedSlots.put(vehicle.getLicensePlate(), allocatedSlot);
        availableSlots.remove(allocatedSlot);
        occupiedCount++;
        vehicle.setEntryTime();
        
        Ticket ticket = new Ticket(vehicle, allocatedSlot);
        activeTickets.put(vehicle.getLicensePlate(), ticket);
        
        System.out.println("  SUCCESS: Allocated Slot #" + allocatedSlot.getSlotId() +
                         " [" + allocatedSlot.getSlotType() + "]" +
                         " Distance: " + allocatedSlot.getDistanceFromEntrance() + "m");
        System.out.println("  Ticket ID: " + ticket.getTicketId());
        
        return ticket;
    }
    
    private ParkingSlot findSuitableSlot(Vehicle vehicle) {
        List<ParkingSlot> checkedSlots = new ArrayList<>();
        ParkingSlot foundSlot = null;
        
        while (!availableSlots.isEmpty()) {
            ParkingSlot slot = availableSlots.poll();
            checkedSlots.add(slot);
            
            if (slot.isAvailableFor(vehicle)) {
                foundSlot = slot;
                break;
            }
        }
        
        availableSlots.addAll(checkedSlots);
        return foundSlot;
    }
    
    public double exitVehicle(String licensePlate) {
        System.out.println("\n[EXITING] Vehicle: " + licensePlate);
        
        if (!occupiedSlots.containsKey(licensePlate)) {
            System.out.println("  ERROR: Vehicle not found in parking lot!");
            return 0.0;
        }
        
        Ticket ticket = activeTickets.get(licensePlate);
        if (ticket == null) {
            System.out.println("  ERROR: No active ticket found!");
            return 0.0;
        }
        
        ParkingSlot slot = occupiedSlots.get(licensePlate);
        double price = calculatePrice(ticket, slot);
        ticket.completePayment(price);
        totalRevenue += price;
        totalVehiclesServed++;
        
        slot.vacate();
        occupiedSlots.remove(licensePlate);
        availableSlots.add(slot);
        occupiedCount--;
        activeTickets.remove(licensePlate);
        
        System.out.println("  SUCCESS: Vehicle exited");
        System.out.println("  - Slot Freed: #" + slot.getSlotId() + " [" + slot.getSlotType() + "]");
        System.out.println("  - Parking Duration: " + String.format("%.2f", ticket.getParkingDurationHours()) + " hours");
        System.out.println("  - Base Rate: Rs" + slot.getBasePrice() + "/hour");
        System.out.println("  - Total Charge: Rs" + String.format("%.2f", price));
        
        processWaitingQueue();
        return price;
    }
    
    private double calculatePrice(Ticket ticket, ParkingSlot slot) {
        double hours = ticket.getParkingDurationHours();
        if (hours < 1.0) hours = 1.0;
        double basePrice = hours * slot.getBasePrice();
        double occupancyRate = getOccupancyRate();
        double multiplier = 1.0;
        
        if (occupancyRate > 0.8) multiplier = 1.5;
        else if (occupancyRate > 0.6) multiplier = 1.2;
        
        if (ticket.getVehicle().isVIP()) multiplier *= 0.8;
        
        return Math.round(basePrice * multiplier * 100.0) / 100.0;
    }
    
    private void processWaitingQueue() {
        if (!waitingQueue.isEmpty() && !availableSlots.isEmpty()) {
            Vehicle nextVehicle = waitingQueue.poll();
            System.out.println("\n[QUEUE] Processing waiting vehicle: " + nextVehicle.getLicensePlate());
            parkVehicle(nextVehicle);
        }
    }
    
    public ParkingSlot findVehicle(String licensePlate) {
        return occupiedSlots.get(licensePlate);
    }
    
    public void optimizeParking() {
        System.out.println("\n[OPTIMIZATION] Checking for better slot allocations...");
        List<Vehicle> toReallocate = new ArrayList<>();
        
        for (Map.Entry<String, ParkingSlot> entry : occupiedSlots.entrySet()) {
            String licensePlate = entry.getKey();
            ParkingSlot currentSlot = entry.getValue();
            Vehicle vehicle = currentSlot.getParkedVehicle();
            ParkingSlot betterSlot = findBetterSlot(currentSlot, vehicle);
            
            if (betterSlot != null) {
                toReallocate.add(vehicle);
                System.out.println("  Found better slot for " + licensePlate + 
                                 ": Slot #" + currentSlot.getSlotId() + 
                                 " -> Slot #" + betterSlot.getSlotId());
            }
        }
        
        for (Vehicle vehicle : toReallocate) {
            reallocateVehicle(vehicle);
        }
        
        if (toReallocate.isEmpty()) {
            System.out.println("  No optimization needed - optimal layout maintained");
        }
    }
    
    private ParkingSlot findBetterSlot(ParkingSlot currentSlot, Vehicle vehicle) {
        List<ParkingSlot> tempList = new ArrayList<>();
        ParkingSlot betterSlot = null;
        
        while (!availableSlots.isEmpty()) {
            ParkingSlot slot = availableSlots.poll();
            tempList.add(slot);
            
            if (slot.isAvailableFor(vehicle) && 
                slot.getDistanceFromEntrance() < currentSlot.getDistanceFromEntrance()) {
                betterSlot = slot;
                break;
            }
        }
        
        availableSlots.addAll(tempList);
        return betterSlot;
    }
    
    private void reallocateVehicle(Vehicle vehicle) {
        ParkingSlot currentSlot = occupiedSlots.get(vehicle.getLicensePlate());
        ParkingSlot betterSlot = findBetterSlot(currentSlot, vehicle);
        
        if (betterSlot != null) {
            currentSlot.vacate();
            betterSlot.occupy(vehicle);
            occupiedSlots.put(vehicle.getLicensePlate(), betterSlot);
            availableSlots.remove(betterSlot);
            availableSlots.add(currentSlot);
            
            System.out.println("  Reallocated " + vehicle.getLicensePlate() + 
                             " from Slot #" + currentSlot.getSlotId() + 
                             " to Slot #" + betterSlot.getSlotId());
        }
    }
    
    public double getOccupancyRate() {
        return (double) occupiedCount / totalSlots;
    }
    
    public void displayStatus() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("PARKING LOT STATUS - " + lotId);
        System.out.println("=".repeat(50));
        
        System.out.println("Capacity: " + totalSlots + " slots");
        System.out.println("Occupied: " + occupiedCount + " slots (" + 
                         String.format("%.1f", getOccupancyRate() * 100) + "%)");
        System.out.println("Available: " + availableSlots.size() + " slots");
        System.out.println("Waiting Queue: " + waitingQueue.size() + " vehicles");
        System.out.println("Total Revenue: Rs" + String.format("%.2f", totalRevenue));
        System.out.println("Vehicles Served: " + totalVehiclesServed);
        
        System.out.println("\nSlot Distribution:");
        System.out.println("  - VIP Slots: " + vipSlots);
        System.out.println("  - EV Charging Slots: " + evSlots);
        System.out.println("  - Regular Slots: " + (totalSlots - vipSlots - evSlots));
        
        System.out.println("\nNearest Available Slots (Top 5):");
        if (availableSlots.isEmpty()) {
            System.out.println("  No available slots");
        } else {
            List<ParkingSlot> nearestSlots = new ArrayList<>(availableSlots);
            nearestSlots.sort(Comparator.comparingInt(ParkingSlot::getDistanceFromEntrance));
            
            for (int i = 0; i < Math.min(5, nearestSlots.size()); i++) {
                ParkingSlot slot = nearestSlots.get(i);
                System.out.println("  " + (i+1) + ". Slot #" + slot.getSlotId() + 
                                 " [" + slot.getSlotType() + "] - " + 
                                 slot.getDistanceFromEntrance() + "m - Rs" + 
                                 slot.getBasePrice() + "/hour");
            }
        }
        
        System.out.println("\nOccupied Vehicles:");
        if (occupiedSlots.isEmpty()) {
            System.out.println("  None");
        } else {
            for (ParkingSlot slot : occupiedSlots.values()) {
                Vehicle vehicle = slot.getParkedVehicle();
                System.out.println("  Slot #" + slot.getSlotId() + 
                                 " [" + slot.getSlotType() + "]: " + 
                                 vehicle.getLicensePlate() + 
                                 (vehicle.isVIP() ? " [VIP]" : "") +
                                 (vehicle.isElectric() ? " [EV]" : "") +
                                 " - " + slot.getDistanceFromEntrance() + "m");
            }
        }
        
        System.out.println("=".repeat(50));
    }
    
    public void displayComplexityAnalysis() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ALGORITHM COMPLEXITY ANALYSIS");
        System.out.println("=".repeat(50));
        
        System.out.println("\nData Structures Used:");
        System.out.println("1. PriorityQueue (Min-Heap) - For nearest slot allocation");
        System.out.println("2. HashMap - For O(1) vehicle/slot lookup");
        System.out.println("3. Queue - For FIFO waiting list management");
        
        System.out.println("\nTime Complexity:");
        System.out.println("• parkVehicle(): O(log n) - Heap insertion/removal");
        System.out.println("• exitVehicle(): O(log n) - Heap update");
        System.out.println("• findVehicle(): O(1) - HashMap lookup");
        System.out.println("• findSuitableSlot(): O(k log n) - where k is checked slots");
        
        System.out.println("\nSpace Complexity:");
        System.out.println("• Overall: O(n) for n slots");
        System.out.println("• Per operation: O(1) additional space");
        
        System.out.println("\nOptimizations:");
        System.out.println("• Min-Heap ensures nearest slot allocation");
        System.out.println("• HashMap provides constant time lookups");
        System.out.println("• Reallocation optimization for better space utilization");
        
        System.out.println("=".repeat(50));
    }
    
    public String getLotId() { return lotId; }
    public int getTotalSlots() { return totalSlots; }
    public int getOccupiedCount() { return occupiedCount; }
    public int getAvailableSlotsCount() { return availableSlots.size(); }
    public int getWaitingQueueSize() { return waitingQueue.size(); }
    public double getTotalRevenue() { return totalRevenue; }
    public int getTotalVehiclesServed() { return totalVehiclesServed; }
    
    public double getCurrentPricingMultiplier() {
        double occupancy = getOccupancyRate();
        if (occupancy > 0.8) return 1.5;
        if (occupancy > 0.6) return 1.2;
        return 1.0;
    }
}