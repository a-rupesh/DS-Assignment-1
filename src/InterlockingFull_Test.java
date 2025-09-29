// InterlockingFull_Test.java (JUnit 4)
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

public class InterlockingFull_Test {

    @Test
    public void passengerBlocksFreightAtCrossing() {
        InterlockingFull net = new InterlockingFull();
        net.addTrain(501, TrainType.FREIGHT, InterlockingFull.Section.S3);
        net.addTrain(601, TrainType.PASSENGER, InterlockingFull.Section.S1);

        Map<Integer, InterlockingFull.Section> intents = new HashMap<>();
        intents.put(501, InterlockingFull.Section.S4); // 3->4
        intents.put(601, InterlockingFull.Section.S5); // 1->5

        Map<Integer, InterlockingFull.Result> r = net.moveCycle(intents);
        assertEquals(InterlockingFull.Result.BLOCK, r.get(501));
        assertEquals(InterlockingFull.Result.MOVED, r.get(601));
    }

    @Test
    public void headOnSwapIsCollision() {
        InterlockingFull net = new InterlockingFull();
        net.addTrain(1, TrainType.PASSENGER, InterlockingFull.Section.S5);
        net.addTrain(2, TrainType.PASSENGER, InterlockingFull.Section.S1);
        Map<Integer, InterlockingFull.Section> intents = new HashMap<>();
        intents.put(1, InterlockingFull.Section.S1);
        intents.put(2, InterlockingFull.Section.S5);
        Map<Integer, InterlockingFull.Result> r = net.moveCycle(intents);
        assertEquals(InterlockingFull.Result.COLLISION, r.get(1));
        assertEquals(InterlockingFull.Result.COLLISION, r.get(2));
    }

    @Test
    public void sameTargetSameTypeLocalDeadlock() {
        InterlockingFull net = new InterlockingFull();
        net.addTrain(10, TrainType.PASSENGER, InterlockingFull.Section.S6);
        net.addTrain(11, TrainType.PASSENGER, InterlockingFull.Section.S9);
        Map<Integer, InterlockingFull.Section> intents = new HashMap<>();
        intents.put(10, InterlockingFull.Section.S9);
        intents.put(11, InterlockingFull.Section.S9);
        Map<Integer, InterlockingFull.Result> r = net.moveCycle(intents);
        assertEquals(InterlockingFull.Result.DEADLOCK_LOCAL, r.get(10));
        assertEquals(InterlockingFull.Result.DEADLOCK_LOCAL, r.get(11));
    }
}
