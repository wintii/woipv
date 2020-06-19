package main.currentVersion;

import java.util.*;
import java.util.stream.Collectors;

public class Constraint {
    public HashMap<Integer, Integer> coefficients;
    private int constraintValue;
    private ComparisonType comparison;

    // new constraint
    public Constraint(ComparisonType comparison) {
        this.coefficients = new HashMap<>();
        this.comparison = comparison;
    }

    // inverted constraint
    public Constraint(Constraint constraint) {
        this.coefficients = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : constraint.coefficients.entrySet()) {
            coefficients.put(entry.getKey(), -entry.getValue());
        }
        this.constraintValue = -constraint.constraintValue;
        this.comparison = ComparisonType.LESS_EQUAL;
    }

    public int getCoefficient(int key) {
        if (coefficients.containsKey(key))
            return coefficients.get(key);
        return 0;
    }

    public void setCoefficient(int key, int value) {
        if (value != 0) {
            coefficients.put(key, value);
        } else {
            coefficients.remove(key);
        }
    }

    public int getConstraintValue() {
        return constraintValue;
    }

    public void setConstraintValue(int constraintValue) {
        this.constraintValue = constraintValue;
    }

    public ComparisonType getComparison() {
        return comparison;
    }

    public void setComparison(ComparisonType comparison) {
        this.comparison = comparison;
    }

    public void addConstraint(int i, Constraint constraint, int j) {
        Set<Integer> ids = new HashSet<>();
        ids.addAll(coefficients.keySet());
        ids.addAll(constraint.coefficients.keySet());
        for (int index : ids) {
            setCoefficient(index, i * getCoefficient(index) + j * constraint.getCoefficient(index));
        }
        setConstraintValue(i * getConstraintValue() + j * constraint.getConstraintValue());
    }

    @Override
    public String toString() {
        String mapAsString = coefficients
                .keySet()
                .stream()
                .map(key -> key + ":" + coefficients.get(key))
                .collect(Collectors.joining(", ", "{", "}"));

        return "C{" +
                "coeff=" + mapAsString +
                ", val<=" + getConstraintValue() +
                '}';
    }

    public boolean isConstraintEqual(Constraint c) {
        if (getConstraintValue() != c.getConstraintValue())
            return false;

        for (Map.Entry<Integer, Integer> entry : coefficients.entrySet()) {
            if (entry.getValue() != c.getCoefficient(entry.getKey()))
                return false;
        }

        return true;
    }

    // https://www.geeksforgeeks.org/gcd-two-array-numbers/
    public boolean doConstraintNormalization() {
        for (int value : coefficients.values()) {
            /*
            if (value > 0 && value >> 28 > 0 )
                return false;
            if (value < 0 && ~(value >> 28) > 0 )
                return false;
             */
        }
        int result = constraintValue;
        for (Integer value : coefficients.values()) {
            result = gcd(value, result);

            if (result == 1) {
                return true;
            }
        }

        if (result < 0)
            result = -result;
        for (Map.Entry<Integer, Integer> entry : coefficients.entrySet()) {
            entry.setValue(entry.getValue() / result);
        }
        constraintValue /= result;
        return true;
    }

    private int gcd(int a, int b) {
        if (a == 0)
            return b;
        return gcd(b % a, a);
    }
}