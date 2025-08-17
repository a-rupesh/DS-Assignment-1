import org.example.Calculator.Calculator;
import org.junit.jupiter.api.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

//JUnit tests for Calculator services.

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CalculatorTest {

    private static Calculator calc;
    private static String cltA;
    private static String cltB;

    @BeforeAll
    public static void setup() throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        calc = (Calculator) registry.lookup("CalculatorService");
        // Two unique client IDs are created
        cltA = "ClientA-" + UUID.randomUUID();
        cltB = "ClientB-" + UUID.randomUUID();
    }

    //test for push and pop of single client
    @Test
    @Order(1)
    public void testPushAndPopSingleClient() throws Exception {
        calc.pushValue(cltA, 10);
        calc.pushValue(cltA, 20);
        assertFalse(calc.isEmpty(cltA), "Stack shouldn't be empty");
        assertEquals(20, calc.pop(cltA), "Pop should return last pushed value of 20");
        assertEquals(10, calc.pop(cltA), "Pop should return 10");
        assertTrue(calc.isEmpty(cltA), "Stack should be empty");
    }

    //gcd test
    @Test
    @Order(2)
    public void testPushOperationGCD() throws Exception {
        calc.pushValue(cltA, 12);
        calc.pushValue(cltA, 18);
        calc.pushOperation(cltA, "gcd");
        assertEquals(6, calc.pop(cltA), "GCD should give 6 as output");
    }

    //lcm test
    @Test
    @Order(3)
    public void testPushOperationLCM() throws Exception {
        calc.pushValue(cltA, 4);
        calc.pushValue(cltA, 5);
        calc.pushOperation(cltA, "lcm");
        assertEquals(20, calc.pop(cltA), "LCM should be 20");
    }

    //pop delay test (ref: baeldung-https://www.baeldung.com/mockito-delay-stubbed-method-response)
    @Test
    @Order(4)
    public void testDelayPop() throws Exception {
        calc.pushValue(cltA, 99);
        long start = System.currentTimeMillis();
        int val = calc.delayPop(cltA, 1000);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 1000, "delayPop should wait for at least 1000 ms");
        assertEquals(99, val, "delayPop should provide 99 as output");
    }

    //multi client test
    @Test
    @Order(5)
    public void testMultipleClientsSeparateStacks() throws Exception {
        // Values are pushed by client A
        calc.pushValue(cltA, 100);
        calc.pushValue(cltA, 200);
        // Another values are pushed by client B
        calc.pushValue(cltB, 1);
        calc.pushValue(cltB, 2);

        assertEquals(200, calc.pop(cltA), "ClientA should get their respective last value");
        assertEquals(2, calc.pop(cltB), "ClientB should get their respective last value");
    }
}
