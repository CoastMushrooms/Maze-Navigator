import java.util.Objects;

public class Door {
    private final String label;

    public Door(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Door)) return false;
        Door door = (Door) o;
        return Objects.equals(label, door.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
