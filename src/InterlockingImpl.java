// InterlockingImpl.java
// JDK 11 compatible. Single-file build that includes a lightweight Petri Net engine
// and a minimal model of the Islington crossover with passenger priority.
//
// How to run in IntelliJ:
//  - Put this file under src/main/java
//  - Right-click 'InterlockingImpl.main' → Run
//
// JUnit tests are under src/test/java (see InterlockingImpl_CrossoverTest).

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

enum TrainType { PASSENGER, FREIGHT }

final class Place {
    final String name;
    private final AtomicInteger tokens = new AtomicInteger(0);
    Place(String name) { this.name = name; }
    int tokens() { return tokens.get(); }
    void add(int n) { tokens.addAndGet(n); }
    boolean tryConsume(int n) {
        while (true) {
            int cur = tokens.get();
            if (cur < n) return false;
            if (tokens.compareAndSet(cur, cur - n)) return true;
        }
    }
    void put(int n) { tokens.addAndGet(n); }
    @Override public String toString() { return name + "[" + tokens() + "]"; }
}

final class ArcIn {
    final Place p; final int w;
    ArcIn(Place p, int w) { this.p = p; this.w = Math.max(1, w); }
    boolean enabled() { return p.tokens() >= w; }
    void consume() { if (!p.tryConsume(w)) throw new IllegalStateException("Underflow at " + p.name); }
}

final class ArcOut {
    final Place p; final int w;
    ArcOut(Place p, int w) { this.p = p; this.w = Math.max(1, w); }
    void produce() { p.put(w); }
}

final class Inhibitor {
    final Place p; final int w;
    Inhibitor(Place p, int w) { this.p = p; this.w = Math.max(1, w); }
    boolean ok() { return p.tokens() < w; }
}

final class Transition {
    final String name;
    final List<ArcIn> ins = new ArrayList<>();
    final List<ArcOut> outs = new ArrayList<>();
    final List<Inhibitor> inhibs = new ArrayList<>();

    Transition(String name) { this.name = name; }
    Transition in(Place p) { ins.add(new ArcIn(p,1)); return this; }
    Transition in(Place p, int w) { ins.add(new ArcIn(p,w)); return this; }
    Transition out(Place p) { outs.add(new ArcOut(p,1)); return this; }
    Transition out(Place p, int w) { outs.add(new ArcOut(p,w)); return this; }
    Transition inhibit(Place p) { inhibs.add(new Inhibitor(p,1)); return this; }
    Transition inhibit(Place p, int w) { inhibs.add(new Inhibitor(p,w)); return this; }

    boolean enabled() {
        for (Inhibitor h: inhibs) if (!h.ok()) return false;
        for (ArcIn a: ins) if (!a.enabled()) return false;
        return true;
    }
    boolean fire() {
        if (!enabled()) return false;
        for (ArcIn a: ins) a.consume();
        for (ArcOut a: outs) a.produce();
        return true;
    }
    @Override public String toString() { return name; }
}

final class PetriNet {
    final Map<String, Place> places = new LinkedHashMap<>();
    final Map<String, Transition> transitions = new LinkedHashMap<>();
    Place place(String name) { return places.computeIfAbsent(name, Place::new); }
    Transition transition(String name) { return transitions.computeIfAbsent(name, Transition::new); }
    String snapshot() {
        StringBuilder sb = new StringBuilder();
        for (Place p: places.values()) sb.append(p).append(" ");
        return sb.toString();
    }
}

public class InterlockingImpl {

    private final PetriNet pn = new PetriNet();

    private final Place P_free = pn.place("P_free");
    private final Place P_pass_wait = pn.place("P_pass_wait");
    private final Place P_freight_wait = pn.place("P_freight_wait");
    private final Place P_pass_in = pn.place("P_pass_in");
    private final Place P_freight_in = pn.place("P_freight_in");
    private final Place P_pass_out = pn.place("P_pass_out");
    private final Place P_freight_out = pn.place("P_freight_out");

    private final Transition T_pass_enter = pn.transition("T_pass_enter");
    private final Transition T_pass_exit = pn.transition("T_pass_exit");
    private final Transition T_freight_enter = pn.transition("T_freight_enter");
    private final Transition T_freight_exit = pn.transition("T_freight_exit");

    public InterlockingImpl() {
        P_free.add(1);
        T_pass_enter.in(P_free).in(P_pass_wait).out(P_pass_in);
        T_pass_exit.in(P_pass_in).out(P_pass_out).out(P_free);
        T_freight_enter.in(P_free).in(P_freight_wait).inhibit(P_pass_wait).out(P_freight_in);
        T_freight_exit.in(P_freight_in).out(P_freight_out).out(P_free);
    }

    public void requestApproach(TrainType type) {
        switch (type) {
            case PASSENGER: P_pass_wait.add(1); break;
            case FREIGHT: P_freight_wait.add(1); break;
        }
    }
    public boolean canEnter(TrainType type) {
        return (type == TrainType.PASSENGER) ? T_pass_enter.enabled() : T_freight_enter.enabled();
    }
    public boolean enter(TrainType type) {
        return (type == TrainType.PASSENGER) ? T_pass_enter.fire() : T_freight_enter.fire();
    }
    public boolean exit(TrainType type) {
        return (type == TrainType.PASSENGER) ? T_pass_exit.fire() : T_freight_exit.fire();
    }
    public String snapshot() { return pn.snapshot(); }

    public static void main(String[] args) {
        InterlockingImpl il = new InterlockingImpl();
        System.out.println("Start: " + il.snapshot());
        il.requestApproach(TrainType.FREIGHT);
        il.requestApproach(TrainType.PASSENGER);
        System.out.println("Passenger can enter? " + il.canEnter(TrainType.PASSENGER));
        System.out.println("Freight can enter? " + il.canEnter(TrainType.FREIGHT));
        il.enter(TrainType.PASSENGER);
        System.out.println("After p-enter: " + il.snapshot());
        il.exit(TrainType.PASSENGER);
        System.out.println("After p-exit : " + il.snapshot());
        il.enter(TrainType.FREIGHT);
        System.out.println("After f-enter: " + il.snapshot());
        il.exit(TrainType.FREIGHT);
        System.out.println("After f-exit : " + il.snapshot());
    }
}
