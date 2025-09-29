// InterlockingFull.java — full movement-cycle skeleton with priority/collision/deadlock
// Place under src/main/java and run tests under src/test/java
import java.util.*;

public class InterlockingFull {

    public enum Section {
        // Passenger side (illustrative IDs):
        S1, S2, S5, S6, S8, S9, S10, S11,
        // Freight side:
        S3, S4, S7,
        // Virtual marker:
        OUT
    }

    public enum Result { MOVED, BLOCK, COLLISION, DEADLOCK_LOCAL }

    public static final class Train {
        public final int id;
        public final TrainType type;
        public Section at;
        public Train(int id, TrainType type, Section at) { this.id=id; this.type=type; this.at=at; }
        public String toString() {
            return id + ":" + type + "@" + at;
        }

    }

    private final Map<Section, Integer> occ = new EnumMap<>(Section.class); // 0=free else trainId
    private final Map<Integer, Train> trains = new HashMap<>();
    private final Map<Section, Set<Section>> next = new EnumMap<>(Section.class);

    private static final class Move {
        final int tid; final Section from, to;
        Move(int tid, Section f, Section t){ this.tid=tid; this.from=f; this.to=t; }
    }

    public InterlockingFull(){
        for (Section s: Section.values()) occ.put(s, 0);

        // Southbound (left→right) passenger examples
        link(Section.S1, Section.S5);
        link(Section.S2, Section.S6);
        link(Section.S5, Section.S8);
        link(Section.S6, Section.S9);
        link(Section.S9, Section.S10);
        link(Section.S10, Section.S11);

        // Freight
        link(Section.S3, Section.S4);
        link(Section.S4, Section.S7);

        // Northbound (right→left) mirrors for simplicity
        link(Section.S5, Section.S1);
        link(Section.S6, Section.S2);
        link(Section.S8, Section.S5);
        link(Section.S9, Section.S6);
        link(Section.S10, Section.S9);
        link(Section.S11, Section.S10);
        link(Section.S4, Section.S3);
        link(Section.S7, Section.S4);
    }

    private void link(Section a, Section b){
        next.computeIfAbsent(a, k->EnumSet.noneOf(Section.class)).add(b);
    }

    // --- Train admin ---
    public void addTrain(int id, TrainType type, Section start){
        if (start!=Section.OUT && occ.get(start)!=0) throw new IllegalStateException("Section busy: "+start);
        Train t = new Train(id, type, start);
        trains.put(id, t);
        if (start!=Section.OUT) occ.put(start, id);
    }
    public Section where(int id){ return trains.get(id).at; }

    // One cycle: desired moves per train (to OUT for exiting). Returns outcome per train.
    public Map<Integer, Result> moveCycle(Map<Integer, Section> intents){
        Map<Integer, Move> proposed = new HashMap<>();
        for (Map.Entry<Integer, Section> e: intents.entrySet()){
            int tid = e.getKey();
            Train t = trains.get(tid);
            if (t==null) continue;
            Section to = e.getValue();
            if (t.at==Section.OUT || to==null) continue;
            if (to!=Section.OUT && !next.getOrDefault(t.at, Set.of()).contains(to)) continue;
            proposed.put(tid, new Move(tid, t.at, to));
        }

        Map<Integer, Result> res = new HashMap<>();

        // Head-on swap detection → COLLISION
        for (Move a: proposed.values()){
            for (Move b: proposed.values()){
                if (a.tid>=b.tid) continue;
                if (a.from==b.to && a.to==b.from && a.to!=Section.OUT){
                    res.put(a.tid, Result.COLLISION);
                    res.put(b.tid, Result.COLLISION);
                }
            }
        }

        // Multiple trains to same target
        Map<Section, List<Move>> byTarget = new EnumMap<>(Section.class);
        for (Move m: proposed.values()){
            if (res.containsKey(m.tid)) continue;
            if (m.to==Section.OUT) continue;
            byTarget.computeIfAbsent(m.to, k->new ArrayList<>()).add(m);
        }
        for (Map.Entry<Section, List<Move>> en: byTarget.entrySet()){
            List<Move> ms = en.getValue();
            if (ms.size()<=1) continue;
            boolean hasP=false, hasF=false;
            for (Move m: ms){
                if (trains.get(m.tid).type==TrainType.PASSENGER) hasP=true; else hasF=true;
            }
            if (hasP && hasF){
                for (Move m: ms) if (trains.get(m.tid).type==TrainType.FREIGHT) res.put(m.tid, Result.BLOCK);
            } else {
                for (Move m: ms) res.put(m.tid, Result.DEADLOCK_LOCAL);
            }
        }

        // Crossing priority: block 3->4 if any 1->5 or 2->6 in same cycle
        for (Move m: proposed.values()){
            if (res.containsKey(m.tid)) continue;
            Train t = trains.get(m.tid);
            if (t.type==TrainType.FREIGHT && m.from==Section.S3 && m.to==Section.S4){
                boolean passengerCrossing = proposed.values().stream().anyMatch(p -> {
                    if (trains.get(p.tid).type!=TrainType.PASSENGER) return false;
                    return (p.from==Section.S1 && p.to==Section.S5) || (p.from==Section.S2 && p.to==Section.S6);
                });
                if (passengerCrossing) res.put(m.tid, Result.BLOCK);
            }
        }

        // Occupancy: cannot enter occupied target (unless swap, already flagged above)
        for (Move m: proposed.values()){
            if (res.containsKey(m.tid)) continue;
            if (m.to!=Section.OUT && occ.get(m.to)!=0){
                res.put(m.tid, Result.DEADLOCK_LOCAL);
            }
        }

        boolean anyMoved = false;
        for (Move m: proposed.values()){
            if (res.containsKey(m.tid)) continue;
            if (m.from!=Section.OUT) occ.put(m.from, 0);
            trains.get(m.tid).at = m.to;
            if (m.to!=Section.OUT) occ.put(m.to, m.tid);
            res.put(m.tid, Result.MOVED);
            anyMoved = true;
        }
        return res;
    }
}
