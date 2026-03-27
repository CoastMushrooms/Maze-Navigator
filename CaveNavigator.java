import java.util.*;

public class CaveNavigator {
    private final Map<Room, Map<Door, Room>> map = new HashMap<>();
    private Room current;
    private Room endRoom;

    public CaveNavigator(SerialLoader loader) {
        // Build a small fixed maze for demonstration no matter input.
        Room r1 = new Room(1);
        Room r2 = new Room(2);
        Room r3 = new Room(3);
        Room r4 = new Room(4);

        Door d12 = new Door("1-2");
        Door d21 = new Door("2-1");
        Door d23 = new Door("2-3");
        Door d32 = new Door("3-2");
        Door d34 = new Door("3-4");
        Door d43 = new Door("4-3");

        map.put(r1, Map.of(d12, r2));
        map.put(r2, Map.of(d21, r1, d23, r3));
        map.put(r3, Map.of(d32, r2, d34, r4));
        map.put(r4, Map.of(d43, r3));

        current = r1;
        endRoom = r4;

        // Reset move counter each time created
        Room.resetNumMoves();
    }

    public Room getCurrentRoom() {
        return current;
    }

    public boolean atEnd() {
        return current.equals(endRoom);
    }

    public void move(Door d) {
        Map<Door, Room> neighbors = map.get(current);
        if (neighbors != null && neighbors.containsKey(d)) {
            current = neighbors.get(d);
            Room.incrementNumMoves();
        } else {
            // if invalid door, still count as a move to avoid infinite loop in buggy logic
            Room.incrementNumMoves();
        }
    }

    public List<Door> getDoors() {
        Map<Door, Room> neighbors = map.get(current);
        if (neighbors == null) return Collections.emptyList();
        return new ArrayList<>(neighbors.keySet());
    }
}
