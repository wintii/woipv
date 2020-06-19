package main.currentVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BoundStack {
    public int index;
    public int upper; // 0=lower, 1=upper
    public int bound;
    public int previousStackPosition;
    public HashMap<Integer, ReasonSet> reasonBounds;
    public int reasonConstraint; // initial = -1

    public BoundStack(int index, int upper, int bound, int previousStackPosition, HashMap<Integer, ReasonSet> reasonBounds, int reasonConstraint) {
        this.index = index;
        this.upper = upper;
        this.bound = bound;
        this.previousStackPosition = previousStackPosition;

        // remove current index from reason bounds
        this.reasonBounds = new HashMap<>();
        for (Map.Entry<Integer, ReasonSet> entry : reasonBounds.entrySet()) {
            if (index != entry.getKey())
                this.reasonBounds.put(entry.getKey(), entry.getValue());
        }
        this.reasonConstraint = reasonConstraint;
    }

    @Override
    public String toString() {

        String mapAsString = reasonBounds.keySet().stream()
                .map(key -> key + "=" + reasonBounds.get(key))
                .collect(Collectors.joining(", ", "{", "}"));

        return "BoundStack{" +
                "index=" + index +
                ", upper=" + upper +
                ", bound=" + bound +
                ", previousStackPosition=" + previousStackPosition +
                ", reasonBounds=" + mapAsString +
                ", reasonConstraint=" + reasonConstraint +
                '}';
    }

    public BoundStack invert() {
        if (upper == 1)
            return new BoundStack(index, 0, bound + 1, previousStackPosition, reasonBounds, reasonConstraint);
        return new BoundStack(index, 1, bound - 1, previousStackPosition, reasonBounds, reasonConstraint);
    }
}
