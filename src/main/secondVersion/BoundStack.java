package main.secondVersion;

import main.currentVersion.ReasonSet;

public class BoundStack {
    public int position;
    public int upper;
    public int bound;
    public int previous;
    public ReasonSet[] reasonSet;
    public int reasonConstraint; // 0 == initial

    public BoundStack(int position, int upper, int bound, int previous, ReasonSet[] reasonSet, int reasonConstraint) {
        this.position = position;
        this.upper = upper;
        this.bound = bound;
        this.previous = previous;
        this.reasonSet = reasonSet;
        this.reasonConstraint = reasonConstraint;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < reasonSet.length; i++) {
            if (reasonSet[i] != null)
                s += i + ":" + reasonSet[i].bound + " ";
        }
        return "BoundStack{" +
                "index=" + position +
                ", upper=" + upper +
                ", bound=" + bound +
                ", previousStackPosition=" + previous +
                ", reasonBounds=" + s +
                ", reasonConstraint=" + reasonConstraint +
                '}';
    }
}
