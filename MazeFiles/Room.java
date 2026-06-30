import java.util.Objects;

public class Room {
    private final int id;
    private static int numMoves = 0;

    public Room(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public static int getNumMoves() {
        return numMoves;
    }

    public static void incrementNumMoves() {
        numMoves++;
    }

    public static void resetNumMoves() {
        numMoves = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return id == room.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room(" + id + ")";
    }
}
