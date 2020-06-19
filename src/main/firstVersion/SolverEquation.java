package main.firstVersion;

import main.secondVersion.Expression;
import main.currentVersion.ReasonSet;

import java.util.*;

public class SolverEquation {

    private int[][] boundArray;
    private Stack<BoundStackOld> boundStack = new Stack<>();
    private List<Expression> expressions = new ArrayList<>();

    private final int initialReasonConstraint = 0;

    private int countVariablesFound = 0;
    public boolean[] finalVariables;

    public SolverEquation(int variableCount) {
        this.boundArray = new int[variableCount][2];
        finalVariables = new boolean[variableCount];
    }

    public void addExpression(Expression exp) {
        expressions.add(exp);
    }

    public void addVar(int position, int lb, int ub) {
        addInitialLowerBound(position, lb, new HashSet<>(), initialReasonConstraint);
        addInitialUpperBound(position, ub, new HashSet<>(), initialReasonConstraint);
    }

    private boolean addInitialLowerBound(int position, int bound, Set<ReasonSet> reasonSet, int reasonConstraint) {
        if (positionHasValue(position, 1) && getValueFromPosition(position, 1) < bound)
            return false;

        if (positionHasValue(position, 0) && getValueFromPosition(position, 0) > bound)
            return true;

        addToStack(position, bound, 0, 0, reasonSet, reasonConstraint);
        return true;
    }

    private boolean addInitialUpperBound(int position, int bound, Set<ReasonSet> reasonSet, int reasonConstraint) {
        if (positionHasValue(position, 0) && getValueFromPosition(position, 0) > bound)
            return false;

        if (positionHasValue(position, 1) && getValueFromPosition(position, 1) < bound)
            return true;

        addToStack(position, bound, 1, 0, reasonSet, reasonConstraint);
        return true;
    }

    private boolean positionHasValue(int position, int upper) {
        // always true with initial bounds
        return boundArray[position][upper] != 0;
    }

    private int getValueFromPosition(int position, int upper) {
        // set start at 0, we start at 1
        return boundStack.get(boundArray[position][upper] - 1).bound;
    }

    private void addToStack(int position, int bound, int upper, int previousValue, Set<ReasonSet> reasonSet, int reasonConstraint) {
        //System.out.println(index + (upper > 0 ? "up" : "down") + " " + previousValue + " " + bound);
        boundStack.push(new BoundStackOld(position, bound, boundArray[position][upper], reasonSet, reasonConstraint));
        //for (int i = 0; i < boundStack.size(); i++)
        int i = boundStack.size() - 1;
        boundArray[position][upper] = boundStack.size();
    }

    private void checkAndAddToStack(int position, int bound, int upper, Set<ReasonSet> reasonSet, int reasonConstraint) {
        int previousValue = getPreviousValue(position, upper);
        int otherBound = getPreviousValue(position, 1 - upper);
        if ((upper == 1 && (previousValue <= bound || bound < otherBound)) || (upper == 0 && (bound <= previousValue || bound > otherBound))) {
            return;
        }

        if (bound == otherBound) {
            addToStack(position, bound, 1 - upper, previousValue, reasonSet, reasonConstraint);
            finalVariables[position] = true;
            countVariablesFound++;
        }
        addToStack(position, bound, upper, previousValue, reasonSet, reasonConstraint);
    }

    private void removeFromStack(int position, int upper) {
        BoundStackOld bs = boundStack.pop();
        boundArray[position][upper] = bs.previous;
    }

    private int getPreviousValue(int position, int upper) {
        int previousIndex = boundArray[position][upper];
        return boundStack.get(previousIndex - 1).bound;
    }

    public boolean startSolver() {
/*
        for (main.thirdVersion.Constraint exp : constraints) {
            exp.doConstraintNormalization();
        }
*/

        int counter = 0;
        while (countVariablesFound < boundArray.length && counter < 100) {
            counter++;
            for (int e = 0; e < expressions.size(); e++) {
                Expression exp = expressions.get(e);

                System.out.println("---" + e);

                for (int i = 0; i < boundArray.length; i++) {

                    if (finalVariables[i] || exp.coeffs[i] == 0) {
                        System.out.println(getValueFromPosition(i, 0) + " " + getValueFromPosition(i, 1));
                        continue;
                    }

                    for (int up = 0; up <= 1; up++) {
                        int sum = 0;

                        for (int j = 0; j < boundArray.length; j++) {
                            if (i == j || exp.coeffs[j] == 0)
                                continue;

                            int upper = exp.coeffs[j] > 0 ? 1 : 0;
                            if (up == 1)
                                upper = 1 - upper;

                            sum += exp.coeffs[j] * getValueFromPosition(j, upper);
                        }
                        int bound = (exp.value - sum) / exp.coeffs[i];
                        if ((exp.value - sum) % exp.coeffs[i] > 0) {
                            if (up == 1) {
                                bound--;
                            } else {
                                bound++;
                            }
                        }
                        int u = exp.coeffs[i] > 0 ? up : 1 - up;
                        checkAndAddToStack(i, bound, u, new HashSet<>(), e + 1);
                    }

                    System.out.println(getValueFromPosition(i, 0) + " " + getValueFromPosition(i, 1));
                }
            }
        }

        System.out.println("main.thirdVersion.Solver finished");
        if (counter == 100)
            return false;

        for (Expression exp : expressions) {
            int sum = 0;
            for (int i = 0; i < boundArray.length; i++) {
                sum += exp.coeffs[i] * getSolutionValueFromPosition(i);
            }
            if (sum != exp.value)
                return false;
        }
        return true;
    }

    public int getSolutionValueFromPosition(int position) {
        return getValueFromPosition(position, 0);
    }
}
