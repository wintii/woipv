package main.currentVersion;

import java.util.Objects;

public class ConflictSet {
    int index;
    int upper;
    int bound;
    private int boundStackPosition;

    public ConflictSet(int index, int upper, int bound, int boundStackPosition) {
        this.index = index;
        this.upper = upper;
        this.bound = bound;
        this.boundStackPosition = boundStackPosition;
    }

    public int getStackPosition() {
        return boundStackPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConflictSet cs = (ConflictSet) o;
        return index == cs.index && upper == cs.upper;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, upper);
    }

    public boolean isEqual(int pos, int up) {
        return index == pos && upper == up;
    }

    @Override
    public String toString() {
        return "CS{" +
                "i=" + index +
                ", up=" + upper +
                ", b=" + bound +
                ", bsp=" + boundStackPosition +
                '}';
    }
}
