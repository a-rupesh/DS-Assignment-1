// InterlockingImpl_CrossoverTest.java (JUnit 4)
import org.junit.*;
import static org.junit.Assert.*;

public class InterlockingImpl_CrossoverTest {

    @Test
    public void passengerGetsPriorityOverFreight() {
        InterlockingImpl il = new InterlockingImpl();
        // Freight arrives first
        il.requestApproach(TrainType.FREIGHT);
        Assert.assertTrue(il.canEnter(TrainType.FREIGHT));

        // Passenger arrives — should now block freight enter
        il.requestApproach(TrainType.PASSENGER);
        Assert.assertTrue(il.canEnter(TrainType.PASSENGER));
        Assert.assertFalse("Freight must be blocked when passenger waiting", il.canEnter(TrainType.FREIGHT));

        // Passenger goes through
        Assert.assertTrue(il.enter(TrainType.PASSENGER));
        Assert.assertFalse(il.enter(TrainType.FREIGHT)); // blocked while block is occupied
        Assert.assertTrue(il.exit(TrainType.PASSENGER));

        // Now freight can take the block
        Assert.assertTrue(il.canEnter(TrainType.FREIGHT));
        Assert.assertTrue(il.enter(TrainType.FREIGHT));
        Assert.assertTrue(il.exit(TrainType.FREIGHT));
    }

    @Test
    public void noCollision_sameBlockMutualExclusion() {
        InterlockingImpl il = new InterlockingImpl();
        il.requestApproach(TrainType.PASSENGER);
        il.requestApproach(TrainType.FREIGHT);
        Assert.assertTrue(il.enter(TrainType.PASSENGER));
        // Once passenger is inside, freight cannot enter
        Assert.assertFalse(il.enter(TrainType.FREIGHT));
        Assert.assertTrue(il.exit(TrainType.PASSENGER));
        Assert.assertTrue(il.enter(TrainType.FREIGHT));
        Assert.assertTrue(il.exit(TrainType.FREIGHT));
    }
}
