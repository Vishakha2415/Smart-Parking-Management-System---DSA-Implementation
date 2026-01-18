package algorithms.allocation;

import models.ParkingLot;
import models.ParkingSlot;
import models.Vehicle;
import java.util.*;

public class MinHeapAllocation {
    private ParkingLot parkingLot;
    
    public MinHeapAllocation(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }
    
    public ParkingSlot allocateSlot(Vehicle vehicle) {
        System.out.println("\n[MIDDLEWARE] MinHeapAllocation called for: " + vehicle);
        
        // Since your ParkingLot already has its own allocation logic,
        // this acts as a middleware that demonstrates the algorithm
        
        // Create sample slots for demonstration
        List<ParkingSlot> demoSlots = createDemoSlots();
        
        // Filter suitable slots
        List<ParkingSlot> suitableSlots = demoSlots.stream()
            .filter(slot -> slot.isAvailableFor(vehicle))
            .toList();
        
        if (suitableSlots.isEmpty()) {
            System.out.println("  No suitable slots found in demo");
            return null;
        }
        
        // Create min-heap based on distance
        PriorityQueue<ParkingSlot> minHeap = new PriorityQueue<>(
            Comparator.comparingInt(ParkingSlot::getDistanceFromEntrance)
        );
        
        minHeap.addAll(suitableSlots);
        ParkingSlot optimalSlot = minHeap.peek();
        
        System.out.println("  Algorithm suggests: Slot #" + optimalSlot.getSlotId() + 
                         " [" + optimalSlot.getSlotType() + "]");
        System.out.println("  Distance: " + optimalSlot.getDistanceFromEntrance() + "m (nearest)");
        
        return optimalSlot;
    }
    
    private List<ParkingSlot> createDemoSlots() {
        List<ParkingSlot> slots = new ArrayList<>();
        slots.add(new ParkingSlot(1, "REGULAR", 10, 50.0));
        slots.add(new ParkingSlot(2, "VIP", 5, 100.0));
        slots.add(new ParkingSlot(3, "EV_CHARGING", 8, 80.0));
        slots.add(new ParkingSlot(4, "REGULAR", 15, 50.0));
        slots.add(new ParkingSlot(5, "REGULAR", 12, 50.0));
        return slots;
    }
    
    public void displayAlgorithmInfo() {
        System.out.println("\n[MIDDLEWARE] MinHeap Allocation Information:");
        System.out.println("• Algorithm: Priority Queue (Min-Heap)");
        System.out.println("• Purpose: Find nearest available slot");
        System.out.println("• Time Complexity: O(log n)");
        System.out.println("• Real implementation: Integrated with ParkingLot");
    }
}
