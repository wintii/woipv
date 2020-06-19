package main.currentVersion;

public class ReasonSet {
    public int index;
    public int upper; // 0=lower, 1=upper
    public int bound;

    public ReasonSet(int index, int upper, int bound) {
        this.index = index;
        this.upper = upper;
        this.bound = bound;
    }

    @Override
    public String toString() {
        return "RS{" +
                "i=" + index +
                ", up=" + upper +
                ", b=" + bound +
                '}';
    }
}
