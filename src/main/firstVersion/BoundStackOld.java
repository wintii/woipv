package main.firstVersion;

import main.currentVersion.ReasonSet;

import java.util.Set;

public class BoundStackOld {
    public int position;
    public int bound;
    public int previous;
    public Set<ReasonSet> reasonSet;
    public int reasonConstraint; // 0 == initial

    public BoundStackOld(int position, int bound, int previous, Set<ReasonSet> reasonSet, int reasonConstraint) {
        this.position = position;
        this.bound = bound;
        this.previous = previous;
        this.reasonSet = reasonSet;
        this.reasonConstraint = reasonConstraint;
    }

    @Override
    public String toString() {
        return "BoundStack{" +
                "index=" + position +
                ", bound=" + bound +
                ", previousStackPosition=" + previous +
                ", reasonBounds=" + reasonSet +
                ", reasonConstraint=" + reasonConstraint +
                '}';
    }
}
