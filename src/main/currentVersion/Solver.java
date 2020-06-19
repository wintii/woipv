package main.currentVersion;

import java.util.*;

public class Solver {

    private int[][] boundArray;
    private int[][] startBounds;
    Stack<BoundStack> boundStack;
    public Constraint optimization;
    public List<Constraint> constraints = new ArrayList<>();

    private int countVariablesFound = 0;
    boolean[] finalVariables;
    public int variableCount;

    private DecisionHelper decisionHelper;

    public Solver(int variableCount) {
        finalVariables = new boolean[variableCount];
        this.boundArray = new int[variableCount][2];
        this.startBounds = new int[variableCount][2];
        this.variableCount = variableCount;
        this.boundStack = new Stack<>();
        decisionHelper = new DecisionHelper(this);
    }

    // constraint are always <= than a number
    public void addConstraint(Constraint constraint) {
        switch (constraint.getComparison()) {
            case GREATER_EQUAL:
                constraint.setComparison(ComparisonType.LESS_EQUAL);
                addExpression(new Constraint(constraint));
                break;
            case EQUAL:
                constraint.setComparison(ComparisonType.LESS_EQUAL);
                addExpression(constraint);
                addExpression(new Constraint(constraint));
                break;
            case LESS_EQUAL:
                addExpression(constraint);
                break;
        }
    }

    public void setOptimization(Constraint expression) {
        optimization = expression;
    }

    //region add variable with bounds
    public void addVarWithBounds(int index, int lb, int ub) {
        int initialReasonConstraint = -1;
        addInitialLowerBound(index, lb, new HashMap<>(), initialReasonConstraint, ub);
        addInitialUpperBound(index, ub, new HashMap<>(), initialReasonConstraint, lb);
        startBounds[index][0] = lb;
        startBounds[index][1] = ub;
    }

    private void addInitialLowerBound(int index, int bound, HashMap<Integer, ReasonSet> reasonSet, int reasonConstraint, int otherBound) {
        if (positionHasValue(index, 1) && getBoundFromPosition(index, 1) < bound)
            return;

        if (positionHasValue(index, 0) && getBoundFromPosition(index, 0) > bound)
            return;

        addToStack(index, bound, 0, reasonSet, reasonConstraint, otherBound);
    }

    private void addInitialUpperBound(int index, int bound, HashMap<Integer, ReasonSet> reasonSet, int reasonConstraint, int otherBound) {
        if (positionHasValue(index, 0) && getBoundFromPosition(index, 0) > bound)
            return;

        if (positionHasValue(index, 1) && getBoundFromPosition(index, 1) < bound)
            return;

        addToStack(index, bound, 1, reasonSet, reasonConstraint, otherBound);
    }

    private boolean positionHasValue(int position, int upper) {
        // always true with initial bounds
        return boundArray[position][upper] != 0;
    }
    //endregion

    BoundStack getBoundStackValue(int position) {
        return boundStack.get(position - 1);
    }

    public int getBoundFromPosition(int position, int upper) {
        return getBoundStackValue(boundArray[position][upper]).bound;
    }

    public int getSolution(int position) {
        return getBoundFromPosition(position, 0);
    }

    //region add stack position
    private void addToStack(int position, int bound, int upper, HashMap<Integer, ReasonSet> reasonSet, int reasonConstraint, int otherBound) {

        if (bound == otherBound) {
            if (!finalVariables[position]) {
                finalVariables[position] = true;
                countVariablesFound++;
            }
        }

        boundStack.push(new BoundStack(position, upper, bound, boundArray[position][upper], reasonSet, reasonConstraint));
        boundArray[position][upper] = boundStack.size();
    }

    boolean checkAndAddToStack(int position, int bound, int upper, HashMap<Integer, ReasonSet> reasonSet, int reasonConstraint) {
        int previousValue = getPreviousValue(position, upper);
        int otherBound = getPreviousValue(position, 1 - upper);
        if ((upper == 1 && (previousValue <= bound || bound < otherBound)) || (upper == 0 && (bound <= previousValue || bound > otherBound))) {
            return false;
        }

        addToStack(position, bound, upper, reasonSet, reasonConstraint, otherBound);

        return true;
    }

    private int getPreviousValue(int position, int upper) {
        int previousIndex = boundArray[position][upper];
        return getBoundStackValue(previousIndex).bound;
    }
    //endregion

    //region remove stack position
    void removeFromStack(int position) {
        for (int i = boundStack.size(); i >= position; i--) {
            removeLastStackPosition();
        }
    }

    private void removeLastStackPosition() {
        BoundStack bs = boundStack.pop();
        boundArray[bs.index][bs.upper] = bs.previousStackPosition;

        if (finalVariables[bs.index]) {
            finalVariables[bs.index] = false;
            countVariablesFound--;
        }

        decisionHelper.checkRemoveLastDecision(boundStack.size());
    }
    //endregion

    private int indexLastPropagation;
    private int indexConflict;
    private HashMap<Integer, ReasonSet> constraintResonMap;

    //TODO distinguish between top and bottom
    private HashMap<Integer, List<Integer>> usedInConstraint = new HashMap<>();

    public boolean startSolver() {
        System.out.println("solver start");
        indexLastPropagation = 0;
        usedInConstraint.clear();
        for (int i = 0; i < constraints.size(); i++) {
            Constraint constraint = constraints.get(i);
            for (Integer key : constraint.coefficients.keySet()) {
                if (!usedInConstraint.containsKey(key)) {
                    usedInConstraint.put(key, new ArrayList<>());
                }
                usedInConstraint.get(key).add(i);
            }
        }

        boolean satisfiable = true;
        // step 1 to 4 from pag 4 in the paper
        while (true) {
            if (stepOne()) {
                if (stepTwo())
                    break;
                continue;
            }

            if (stepThree()) {
                satisfiable = false;
                break;
            }
            //error conflict analyse
            stepFour();
        }

        System.out.println("Solver finished");
        if (satisfiable && optimization != null) {
            int val = 0;
            for (Map.Entry<Integer, Integer> entry : optimization.coefficients.entrySet()) {
                int key = entry.getKey();
                int coeff = entry.getValue();

                val += coeff * getSolution(key);
            }
            System.out.println("My optimal solution:" + val);
        }
        return satisfiable;
    }

    // find the optimal solution
    public int optimize() {
        boolean satisfiable = true;
        int val = 0;
        while (satisfiable) {
            satisfiable = startSolver();
            if (optimization == null)
                break;

            if (satisfiable) {
                Constraint constr = new Constraint(ComparisonType.LESS_EQUAL);
                for (Map.Entry<Integer, Integer> entry : optimization.coefficients.entrySet()) {
                    int key = entry.getKey();
                    int coeff = entry.getValue();

                    val += coeff * getSolution(key);
                    if (optimization.getComparison() == ComparisonType.MINIMIZE) {
                        constr.setCoefficient(key, coeff);
                    } else {
                        constr.setCoefficient(key, -coeff);
                    }
                }
                if (optimization.getComparison() == ComparisonType.MINIMIZE) {
                    constr.setConstraintValue(val - 1);
                } else {
                    constr.setConstraintValue(-val - 1);
                }
            }
        }
        System.out.println("My optimal solution:" + val);
        return val;
    }

    //region one
    private boolean stepOne() {
        int index = indexLastPropagation % constraints.size();
        Set<Integer> ids = new HashSet<>();

        //TODO Fill with correct ids
        for (int i = 0; i < constraints.size(); i++) {
            ids.add(i);
        }

        do {
            if (ids.contains(index)) {
                Constraint constraint = constraints.get(index);
                long sum = getMinSumOfConstraint(constraint);
                if (overflow == 0 && sum - constraint.getConstraintValue() > 0) {
                    indexConflict = index;
                    return false;
                }
                if (overflow == 1 && sum - constraint.getConstraintValue() < 0) {
                    indexConflict = index;
                    return false;
                }
                if (!allVariableSet()) {
                    // only one solution possible
                    if (sum == constraint.getConstraintValue()) {
                        for (Map.Entry<Integer, Integer> entry : constraint.coefficients.entrySet()) {
                            int key = entry.getKey();

                            int upper = entry.getValue() > 0 ? 0 : 1;
                            if (checkAndAddToStack(key, getBoundFromPosition(key, upper), 1 - upper, constraintResonMap, index)) {
                                indexLastPropagation = index;
                                if (usedInConstraint.containsKey(key))
                                    ids.addAll(usedInConstraint.get(key));
                            }
                        }
                    } else {
                        ids.addAll(findNewPropagation(constraint, index, (int) sum));
                    }
                }
                ids.remove(index);
            }

            /*
            System.out.println(index + "---");
            for (int i = 0; i < boundArray.length; i++) {
                System.out.println(i + " " + getBoundFromPosition(i, 0) + " " + getBoundFromPosition(i, 1));
            }
            */
            index = ++index % constraints.size();
        }
        while (index != indexLastPropagation);

        return true;
    }

    private int overflow = 0;

    private long getMinSumOfConstraint(Constraint constraint) {
        return getMinSumOfConstraint(constraint, -1);
    }

    private long getMinSumOfConstraint(Constraint constraint, int k) {
        long sum = 0;
        overflow = 0;
        constraintResonMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : constraint.coefficients.entrySet()) {
            int key = entry.getKey();
            if (key == k)
                continue;
            int upper = entry.getValue() > 0 ? 0 : 1;
            int value = getBoundFromPosition(key, upper);
            constraintResonMap.put(key, new ReasonSet(key, upper, value));
            long v = entry.getValue() * (long) value;
            sum += v;
        }
        if (sum > Integer.MAX_VALUE)
            overflow = 1;
        if (sum < Integer.MIN_VALUE)
            overflow = -1;
        return sum;
    }

    private Set<Integer> findNewPropagation(Constraint constraint, int index, int sum) {
        Set<Integer> newIds = new HashSet<>();

        for (Integer key : constraint.coefficients.keySet()) {
            if (finalVariables[key]) {
                continue;
            }
            int coeff = constraint.getCoefficient(key);
            if (propagateBound(index, constraint.getConstraintValue(), key, coeff)) {
                indexLastPropagation = index;
                if (usedInConstraint.containsKey(key))
                    newIds.addAll(usedInConstraint.get(key));
            }

        }

        return newIds;
    }

    private boolean propagateBound(int index, int constraintValue, int key, int coeff) {
        int up = coeff >= 0 ? 1 : 0;

        long sum = getMinSumOfConstraint(constraints.get(index), key);
        if (overflow != 0)
            return false;
        int diff = constraintValue - (int) sum;
        int bound = diff / coeff;
        if (diff % coeff != 0) {
            if (up == 0) {
                if ((double) diff / coeff > 0)
                    bound++;
            } else if ((double) diff / coeff < 0) {
                bound--;
            }
        }
        return checkAndAddToStack(key, bound, up, constraintResonMap, index);
    }
    //endregion

    //region two
    private boolean stepTwo() {
        if (allVariableSet())
            return true;

        decisionHelper.doOneDecision();
        return false;
    }

    private boolean allVariableSet() {
        return countVariablesFound == boundArray.length;
    }
    //endregion

    //region three
    private boolean stepThree() {
        return isInfeasible();
    }

    private boolean isInfeasible() {
        if (decisionHelper.decisions.isEmpty()) {
            System.out.println("infeasible");
            return true;
        }
        return false;
    }
    //endregion

    //region four
    private void stepFour() {
        resolveConflict(constraints.get(indexConflict));
    }

    private void resolveConflict(Constraint constraint) {
        List<ConflictSet> CS = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : constraint.coefficients.entrySet()) {
            int key = entry.getKey();
            int upper = entry.getValue() > 0 ? 0 : 1;
            int boundStackPosition = boundArray[key][upper];
            BoundStack bs = getBoundStackValue(boundStackPosition);

            CS.add(new ConflictSet(key, bs.upper, bs.bound, boundStackPosition));
        }

        // conflict analysis
        //System.out.println("new conflict");
        CS.sort(Comparator.comparing(ConflictSet::getStackPosition, Collections.reverseOrder()));

        Constraint C = new Constraint(ComparisonType.LESS_EQUAL);
        // set constaint as new clause to learn
        C.addConstraint(0, constraint, 1);

        do {
            // bound topmost in A
            ConflictSet csTop = CS.get(0);
            BoundStack B = getBoundStackValue(csTop.getStackPosition());

            // CS \ {B}
            CS.remove(csTop);

            // CS U R
            for (Map.Entry<Integer, ReasonSet> entry : B.reasonBounds.entrySet()) {
                ReasonSet rs = entry.getValue();
                boolean found = false;
                for (ConflictSet c : CS) {
                    if (c.isEqual(rs.index, rs.upper)) {
                        if (c.upper == 1 && rs.bound < c.bound) {
                            c.bound = rs.bound;
                        } else if (c.upper == 0 && rs.bound > c.bound) {
                            c.bound = rs.bound;
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    int boundStackPosition = boundArray[rs.index][rs.upper];
                    while (getBoundStackValue(boundStackPosition).bound != rs.bound)
                        boundStackPosition = getBoundStackValue(boundStackPosition).previousStackPosition;
                    CS.add(new ConflictSet(rs.index, rs.upper, rs.bound, boundStackPosition));
                }
            }

            CS.sort(Comparator.comparing(ConflictSet::getStackPosition, Collections.reverseOrder()));

            // find cut elimination between C and B's reason constraint
            // -1 == initial bound
            if (B.reasonConstraint > -1) {
                Constraint B_reason_constraint = constraints.get(B.reasonConstraint);
                int ai = C.getCoefficient(csTop.index);
                int bi = B_reason_constraint.getCoefficient(csTop.index);

                if (ai + bi == 0) {
                    C.addConstraint(1, B_reason_constraint, 1);
                } else if (ai * bi < 0) {
                    int posAi = ai < 0 ? ai * -1 : ai;
                    int posBi = bi < 0 ? bi * -1 : bi;
                    C.addConstraint(posBi, B_reason_constraint, posAi);
                }
            }

            // Early Backjump
            if (constraintAlreadyInConstraints(C) != -1) {
                int decisionNumber = decisionHelper.decisions.size() - 1;
                while (decisionNumber >= 0) {
                    int k = decisionHelper.decisions.get(decisionNumber);

                    boolean foundBound = false;

                    for (Integer key : C.coefficients.keySet()) {
                        constraintResonMap = new HashMap<>();
                        foundBound = checkBoundBeforePosition(key, C, k);

                        if (foundBound)
                            break;
                    }
                    if (foundBound) {

                        BoundStack decision = getBoundStackValue(k);
                        removeFromStack(k);
                        //learn

                        if (addExpression(C))
                            continue;

                        long minSum = getMinSumOfConstraint(C);
                        if (overflow != 0)
                            continue;
                        findNewPropagation(C, constraints.size() - 1, (int) minSum);

                        BoundStack newBound = decision.invert();
                        checkAndAddToStack(newBound.index, newBound.bound, newBound.upper, constraintResonMap, constraints.size() - 1);
                        return;
                    }
                    decisionNumber--;
                }
            }

            if (CS.size() == 1)
                break;
            if (CS.get(1).getStackPosition() < decisionHelper.decisions.peek())
                break;
        } while (true);

        // backjump
        int B_position = CS.size() > 1 ? CS.get(1).getStackPosition() : 0;
        BoundStack lastRemovedDecision = null;
        while (decisionHelper.decisions.size() > 0 &&
                (B_position == 0 || (B_position > 0 && B_position < decisionHelper.decisions.peek()))) {
            if (decisionHelper.decisions.peek() == boundStack.size()) {
                lastRemovedDecision = getBoundStackValue(decisionHelper.decisions.peek()).invert();
            }
            removeLastStackPosition();
        }

        if (lastRemovedDecision != null) {
            checkAndAddToStack(lastRemovedDecision.index, lastRemovedDecision.bound, lastRemovedDecision.upper, new HashMap<>(), -3);
        }

        constraintResonMap = new HashMap<>();
        int upper = C.getCoefficient(CS.get(0).index) > 0 ? 1 : 0;
        for (int i = 1; i < CS.size(); i++) {
            ConflictSet cs = CS.get(i);
            //if (cs.upper != upper)
            constraintResonMap.put(cs.index, new ReasonSet(cs.index, cs.upper, cs.bound));
        }

        //learn constraint
        int constraintIndex = constraintAlreadyInConstraints(C);
        if (constraintIndex != -1) {
            checkAndAddToStack(CS.get(0).index, CS.get(0).bound, upper, constraintResonMap, constraintIndex);
            return;
        }
        if (!addExpression(C))
            return;

        checkAndAddToStack(CS.get(0).index, CS.get(0).bound, upper, constraintResonMap, constraints.size() - 1);
    }

    private boolean addExpression(Constraint newConstraint) {
        if (newConstraint.doConstraintNormalization()) {
            constraints.add(newConstraint);
            return true;
        }
        return false;
    }

    private int constraintAlreadyInConstraints(Constraint newConstraint) {

        for (int i = 0; i < constraints.size(); i++) {
            if (newConstraint.isConstraintEqual(constraints.get(i)))
                return i;
        }
        return -1;
    }

    private boolean checkBoundBeforePosition(int key, Constraint constraint, int decisionPosition) {
        int coeff = constraint.getCoefficient(key);
        int up = coeff >= 0 ? 1 : 0;
        int sum = getMinSumOfConstraintBeforePosition(constraint, key, decisionPosition);
        if (overflow != 0)
            return false;
        int diff = constraint.getConstraintValue() - sum;

        int bound = diff / coeff;
        if (diff % coeff != 0) {
            if (up == 0) {
                if ((double) diff / coeff > 0)
                    bound++;
            } else if ((double) diff / coeff < 0) {
                bound--;
            }
        }
        return checkOnStack(key, bound, up, decisionPosition);
    }

    private int getMinSumOfConstraintBeforePosition(Constraint constraint, int k, int decisionPosition) {
        int sum = 0;
        constraintResonMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : constraint.coefficients.entrySet()) {
            int key = entry.getKey();
            if (key == k)
                continue;
            int upper = entry.getValue() > 0 ? 0 : 1;
            int value = getBoundBeforePosition(key, upper, decisionPosition);
            constraintResonMap.put(key, new ReasonSet(key, upper, value));
            sum += entry.getValue() * value;
        }
        return sum;
    }

    private boolean checkOnStack(int index, int bound, int upper, int decisionPosition) {

        // find bounds before decisionPosition
        int previousBound = getBoundBeforePosition(index, upper, decisionPosition);
        int otherPreviousBound = getBoundBeforePosition(index, 1 - upper, decisionPosition);

        return (upper == 0 || (previousBound > bound && bound >= otherPreviousBound)) && (upper == 1 || (bound > previousBound && bound <= otherPreviousBound));
    }

    private int getBoundBeforePosition(int index, int upper, int decisionPosition) {
        int stackPosition = boundArray[index][upper];
        while (stackPosition >= decisionPosition) {
            stackPosition = getBoundStackValue(stackPosition).previousStackPosition;
        }
        return getBoundStackValue(stackPosition).bound;
    }
    //endregion
}
