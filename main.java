// ============ ALL IMPORTS ============
import algorithms.allocation.MinHeapAllocation;
import algorithms.pricing.DynamicPricing;
import java.util.Scanner;
import models.*;
// =====================================

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("===============================================");
        System.out.println("       SMART PARKING SYSTEM - INTERACTIVE      ");
        System.out.println("===============================================");
        
        // STEP 1: Create Parking Lot
        System.out.print("\nEnter total parking slots: ");
        int totalSlots = scanner.nextInt();
        scanner.nextLine();
        
        ParkingLot parkingLot = new ParkingLot(totalSlots);
        System.out.println("SUCCESS: Parking Lot '" + parkingLot.getLotId() + "' created!");
        
        // STEP 2: Initialize Algorithms
        MinHeapAllocation allocator = new MinHeapAllocation(parkingLot);
        DynamicPricing pricing = new DynamicPricing();
        
        System.out.println("\nAlgorithms Initialized:");
        System.out.println("- MinHeap Allocation: Ready");
        System.out.println("- Dynamic Pricing: Ready");
        
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n============== MAIN MENU ==============");
            System.out.println("1. Park Vehicle");
            System.out.println("2. Exit Vehicle");
            System.out.println("3. Find Vehicle");
            System.out.println("4. Show Parking Status");
            System.out.println("5. Run Parking Optimization");
            System.out.println("6. Test Algorithms");
            System.out.println("7. Exit System");
            System.out.print("\nChoose option (1-7): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    parkVehicleInteractive(scanner, parkingLot, allocator);
                    break;
                case 2:
                    exitVehicleInteractive(scanner, parkingLot);
                    break;
                case 3:
                    findVehicleInteractive(scanner, parkingLot);
                    break;
                case 4:
                    showStatusInteractive(parkingLot, pricing);
                    break;
                case 5:
                    optimizeParkingInteractive(parkingLot);
                    break;
                case 6:
                    testAlgorithmsInteractive(parkingLot, allocator, pricing);
                    break;
                case 7:
                    exit = true;
                    showFinalSummary(parkingLot);
                    break;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
        
        scanner.close();
    }
    
    // ========== 1. PARK VEHICLE ==========
    private static void parkVehicleInteractive(Scanner scanner, ParkingLot parkingLot, MinHeapAllocation allocator) {
        System.out.println("\n========== PARK VEHICLE ==========");
        
        System.out.print("Enter license plate: ");
        String license = scanner.nextLine();
        
        System.out.print("Enter vehicle type (CAR/SUV/BIKE): ");
        String type = scanner.nextLine();
        
        System.out.print("Is VIP? (true/false): ");
        boolean isVIP = scanner.nextBoolean();
        
        System.out.print("Is Electric? (true/false): ");
        boolean isElectric = scanner.nextBoolean();
        scanner.nextLine();
        
        // Create vehicle
        Vehicle vehicle = new Vehicle(license, type, isVIP, isElectric);
        System.out.println("\nVehicle created: " + vehicle);
        
        // Use MinHeap Allocation to find optimal slot
        System.out.println("\n[ALGORITHM] Running MinHeap Allocation...");
        ParkingSlot optimalSlot = allocator.allocateSlot(vehicle);
        
        if (optimalSlot != null) {
            System.out.println("MinHeap suggested: Slot #" + optimalSlot.getSlotId() + 
                             " [" + optimalSlot.getSlotType() + "]");
        }
        
        // Actually park the vehicle using ParkingLot
        System.out.println("\n[SYSTEM] Attempting to park vehicle...");
        Ticket ticket = parkingLot.parkVehicle(vehicle);
        
        if (ticket != null) {
            System.out.println("SUCCESS: Vehicle parked!");
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Allocated Slot: #" + ticket.getSlot().getSlotId());
        }
    }
    
    // ========== 2. EXIT VEHICLE ==========
    private static void exitVehicleInteractive(Scanner scanner, ParkingLot parkingLot) {
        System.out.println("\n========== EXIT VEHICLE ==========");
        
        System.out.print("Enter license plate: ");
        String license = scanner.nextLine();
        
        // Check if vehicle exists
        ParkingSlot slot = parkingLot.findVehicle(license);
        if (slot == null) {
            System.out.println("ERROR: Vehicle not found in parking!");
            return;
        }
        
        System.out.println("\nVehicle found:");
        System.out.println("Slot: #" + slot.getSlotId() + " [" + slot.getSlotType() + "]");
        System.out.println("Base Rate: Rs" + slot.getBasePrice() + "/hour");
        
        // Note: Your ParkingLot.exitVehicle() automatically calculates price
        // using its internal pricing logic
        
        System.out.print("\nConfirm exit? (yes/no): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            double charged = parkingLot.exitVehicle(license);
            System.out.println("SUCCESS: Vehicle exited!");
            System.out.println("Amount charged: Rs" + charged);
        } else {
            System.out.println("Exit cancelled.");
        }
    }
    
    // ========== 3. FIND VEHICLE ==========
    private static void findVehicleInteractive(Scanner scanner, ParkingLot parkingLot) {
        System.out.println("\n========== FIND VEHICLE ==========");
        
        System.out.print("Enter license plate: ");
        String license = scanner.nextLine();
        
        ParkingSlot slot = parkingLot.findVehicle(license);
        
        if (slot != null) {
            System.out.println("\nVEHICLE FOUND:");
            System.out.println("License: " + license);
            System.out.println("Slot: #" + slot.getSlotId() + " [" + slot.getSlotType() + "]");
            System.out.println("Distance: " + slot.getDistanceFromEntrance() + "m");
            System.out.println("Rate: Rs" + slot.getBasePrice() + "/hour");
            
            Vehicle vehicle = slot.getParkedVehicle();
            if (vehicle != null) {
                System.out.println("Type: " + vehicle.getVehicleType());
                System.out.println("VIP: " + vehicle.isVIP());
                System.out.println("Electric: " + vehicle.isElectric());
            }
        } else {
            System.out.println("Vehicle not found!");
        }
    }
    
    // ========== 4. SHOW STATUS ==========
    private static void showStatusInteractive(ParkingLot parkingLot, DynamicPricing pricing) {
        System.out.println("\n========== PARKING STATUS ==========");
        parkingLot.displayStatus();
        
        System.out.println("\n[PRICING INFORMATION]");
        System.out.println(pricing.getCurrentPricingInfo(parkingLot));
    }
    
    // ========== 5. OPTIMIZE PARKING ==========
    private static void optimizeParkingInteractive(ParkingLot parkingLot) {
        System.out.println("\n========== OPTIMIZE PARKING ==========");
        System.out.println("Running parking optimization algorithm...");
        parkingLot.optimizeParking();
        System.out.println("Optimization complete!");
    }
    
    // ========== 6. TEST ALGORITHMS ==========
    private static void testAlgorithmsInteractive(ParkingLot parkingLot, MinHeapAllocation allocator, DynamicPricing pricing) {
        System.out.println("\n========== TEST ALGORITHMS ==========");
        
        System.out.println("\n1. MINHEAP ALLOCATION TEST:");
        System.out.println("Testing with different vehicle types...");
        
        Vehicle[] testVehicles = {
            new Vehicle("TEST001", "CAR", false, false),
            new Vehicle("TEST002", "CAR", true, false),
            new Vehicle("TEST003", "CAR", false, true),
            new Vehicle("TEST004", "CAR", true, true)
        };
        
        for (Vehicle vehicle : testVehicles) {
            System.out.println("\nTesting: " + vehicle);
            ParkingSlot slot = allocator.allocateSlot(vehicle);
            if (slot != null) {
                System.out.println("  Suggested slot: #" + slot.getSlotId() + " [" + slot.getSlotType() + "]");
            }
        }
        
        System.out.println("\n2. DYNAMIC PRICING TEST:");
        System.out.println("Testing price calculation...");
        
        // Create a test ticket
        Vehicle testVehicle = new Vehicle("PRICETEST", "CAR", true, false);
        ParkingSlot testSlot = new ParkingSlot(99, "VIP", 10, 100.0);
        Ticket testTicket = new Ticket(testVehicle, testSlot);
        
        // Calculate price
        double price = pricing.calculateDynamicPrice(testTicket, parkingLot);
        System.out.println("Test Calculation:");
        System.out.println("  Vehicle: VIP Car");
        System.out.println("  Slot: VIP (Rs100/hour)");
        System.out.println("  Calculated Price: Rs" + price);
        
        System.out.println("\n3. ALGORITHM ANALYSIS:");
        parkingLot.displayComplexityAnalysis();
    }
    
    // ========== 7. FINAL SUMMARY ==========
    private static void showFinalSummary(ParkingLot parkingLot) {
        System.out.println("\n========== SYSTEM SUMMARY ==========");
        System.out.println("Parking Lot: " + parkingLot.getLotId());
        System.out.println("Total Slots: " + parkingLot.getTotalSlots());
        System.out.println("Occupied Slots: " + parkingLot.getOccupiedCount());
        System.out.println("Available Slots: " + parkingLot.getAvailableSlotsCount());
        System.out.println("Waiting Queue: " + parkingLot.getWaitingQueueSize());
        System.out.println("Total Revenue: Rs" + String.format("%.2f", parkingLot.getTotalRevenue()));
        System.out.println("Vehicles Served: " + parkingLot.getTotalVehiclesServed());
        System.out.println("Occupancy Rate: " + String.format("%.1f", parkingLot.getOccupancyRate() * 100) + "%");
        System.out.println("\nThank you for using Smart Parking System!");
        System.out.println("===============================================");
    }
}
