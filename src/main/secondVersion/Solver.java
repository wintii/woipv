package main.secondVersion;

import main.currentVersion.ReasonSet;

import java.util.*;

public class Solver {

    private int[][] boundArray;
    private Stack<BoundStack> boundStack = new Stack<>();
    public Expression optimization;
    public List<Expression> expressions = new ArrayList<>();

    private final int initialReasonConstraint = 0;

    private int countVariablesFound = 0;
    private boolean[] finalVariables;
    public int variableCount;

    private int countNoBound = 0;
    private Stack<Integer> decisions = new Stack<>();

    private int actions = 0;

    private ReasonSet[] currentResonArray;

    public Solver(int variableCount) {
        this.boundArray = new int[variableCount][2];
        finalVariables = new boolean[variableCount];
        this.variableCount = variableCount;
    }

    public void addExpression(Expression exp) {
        switch (exp.comparison) {
            case BIGGER_EQUAL_THAN:
                expressions.add(exp.turnExpression());
                break;
            case EQUAL:
                expressions.add(exp);
                expressions.add(exp.turnExpression());
                break;
            case SMALLER_EQUAL_THAN:
                expressions.add(exp);
                break;
        }
    }

    public void setOptimization(Expression expression){
        optimization = expression;
    }

    public void addVar(int position, int lb, int ub) {
        addInitialLowerBound(position, lb, new ReasonSet[finalVariables.length], initialReasonConstraint);
        addInitialUpperBound(position, ub, new ReasonSet[finalVariables.length], initialReasonConstraint);
        if (lb == ub) {
            finalVariables[position] = true;
            countVariablesFound++;
        }
    }

    private boolean addInitialLowerBound(int position, int bound, ReasonSet[] reasonSet, int reasonConstraint) {
        if (positionHasValue(position, 1) && getValueFromPosition(position, 1) < bound)
            return false;

        if (positionHasValue(position, 0) && getValueFromPosition(position, 0) > bound)
            return true;

        addToStack(position, bound, 0, reasonSet, reasonConstraint);
        return true;
    }

    private boolean addInitialUpperBound(int position, int bound, ReasonSet[] reasonSet, int reasonConstraint) {
        if (positionHasValue(position, 0) && getValueFromPosition(position, 0) > bound)
            return false;

        if (positionHasValue(position, 1) && getValueFromPosition(position, 1) < bound)
            return true;

        addToStack(position, bound, 1, reasonSet, reasonConstraint);
        return true;
    }

    private boolean positionHasValue(int position, int upper) {
        // always true with initial bounds
        return boundArray[position][upper] != 0;
    }

    public int getSolutionValueFromPosition(int position) {
        return getValueFromPosition(position, 0);
    }

    public int getValueFromPosition(int position, int upper) {
        // set start at 0, we start at 1
        return getBoundStackValue(boundArray[position][upper]).bound;
    }

    private int getBoundStackPosition(int position, int upper) {
        return boundArray[position][upper];
    }

    private void addToStack(int position, int bound, int upper, ReasonSet[] reasonSet, int reasonConstraint) {
        //System.out.println(index + (upper > 0 ? "up" : "down") + " " + previousValue + " " + bound);
        boundStack.push(new BoundStack(position, upper, bound, boundArray[position][upper], reasonSet, reasonConstraint));
        //for (int i = 0; i < boundStack.size(); i++)
        int i = boundStack.size() - 1;
        //System.out.println((i + 1) + " " +  getBoundStackValue(i));

        boundArray[position][upper] = boundStack.size();
    }

    private boolean checkAndAddToStack(int position, int bound, int upper, ReasonSet[] reasonSet, int reasonConstraint) {
        int previousValue = getPreviousValue(position, upper);
        int otherBound = getPreviousValue(position, 1 - upper);
        if ((upper == 1 && (previousValue <= bound || bound < otherBound)) || (upper == 0 && (bound <= previousValue || bound > otherBound))) {
            return false;
        }

        if (bound == otherBound) {
            //addToStack(index, bound, 1 - upper, reasonBounds, reasonConstraint);
            finalVariables[position] = true;
            countVariablesFound++;
        }
        addToStack(position, bound, upper, reasonSet, reasonConstraint);

        return true;
    }

    private void removeFromStack(int position) {
        for (int i = boundStack.size(); i >= position; i--) {
            removeFromStack();
        }
    }

    private void removeFromStack() {
        BoundStack bs = boundStack.pop();
        int upper = boundArray[bs.position][1] == boundStack.size() + 1 ? 1 : 0;
        boundArray[bs.position][upper] = bs.previous;
        if (bs.bound == getBoundStackValue(boundArray[bs.position][1 - upper]).bound) {
            finalVariables[bs.position] = false;
            countVariablesFound--;
        }
        if (decisions.peek() > boundStack.size())
            decisions.pop();
    }

    private int getPreviousValue(int position, int upper) {
        int previousIndex = boundArray[position][upper];
        return getBoundStackValue(previousIndex).bound;
    }

    public boolean startSolver() {
/*
        for (main.thirdVersion.Constraint exp : constraints) {
            exp.doConstraintNormalization();
        }
*/
        /*
        System.out.println("Print all constraints");
        for (main.thirdVersion.Constraint exp : constraints) {
            for (int i = 0; i < boundArray.length; i++) {
                System.out.print((exp.coeffsArr[i] >= 0 ? " +" : " ") + exp.coeffsArr[i] + "* x" + i);
            }
            System.out.println(" <= " + exp.bound);
        }
        */

        while (!allExpressionCorrect()) {
            if (allVariableSet()) {
                if (isInfeasible()) {
                    System.out.println("action count " + actions);
                    return false;
                }
                undoLastDecision();
            }

            for (int e = 0; e < expressions.size(); e++) {
                checkVariablesInExpression(expressions.get(e), e);
            }

            /*System.out.println("print stack");
            for (int i = 0; i < boundStack.size(); i++) {
                System.out.println(boundStack.get(boundStack.size() - i - 1));
            }
             */
        }

        System.out.println("Solver finished");
        System.out.println("action count " + actions);
        return true;
    }

    private BoundStack getBoundStackValue(int position) {
        return boundStack.get(position - 1);
    }

    private boolean allExpressionCorrect() {
        for (Expression exp : expressions) {
            int sum = 0;
            for (int i = 0; i < boundArray.length; i++) {
                sum += exp.coeffs[i] * getSolutionValueFromPosition(i);
            }
            if (sum < exp.value)
                return false;
        }
        return true;
    }

    private int getSumOfOtherVariables(Expression exp, int currentVariable) {
        int sum = 0;
        for (int j = 0; j < boundArray.length; j++) {
            if (currentVariable == j || exp.coeffs[j] == 0)
                continue;

            int upper = exp.coeffs[j] > 0 ? 0 : 1;
            int value = getValueFromPosition(j, upper);
            currentResonArray[j] = new ReasonSet(j, upper, value);
            sum += exp.coeffs[j] * value;
        }
        return sum;
    }

    private boolean allVariableSet() {
        return countVariablesFound == boundArray.length;
    }

    private boolean isInfeasible() {
        if (decisions.isEmpty()) {
            System.out.println("infeasible");
            return true;
        }
        return false;
    }

    private void undoLastDecision() {
        int stackPosition = decisions.peek();
        BoundStack decisionStack = getBoundStackValue(stackPosition);

        removeFromStack(stackPosition);
        checkAndAddToStack(decisionStack.position, decisionStack.bound + 1, 0, new ReasonSet[finalVariables.length], -1);
    }

    private void checkVariablesInExpression(Expression exp, int expNumber) {

        System.out.println("---" + expNumber);
        checkConflict(exp, expNumber);

        for (int i = 0; i < boundArray.length; i++) {

            if (finalVariables[i] || exp.coeffs[i] == 0) {
                System.out.println(getValueFromPosition(i, 0) + " " + getValueFromPosition(i, 1));
                continue;
            }
            currentResonArray = new ReasonSet[finalVariables.length];
            findBoundOrMakeDecisionForVariable(i, exp, expNumber);
            actions++;
        }
    }

    private void checkConflict(Expression exp, int expNumber) {
        int sum = 0;
        int boundStackPosition = 0;
        List<Integer> boundStackPositions = new ArrayList<>();
        List<ConflictSet> CS = new ArrayList<>();

        currentResonArray = new ReasonSet[finalVariables.length];
        for (int i = 0; i < boundArray.length; i++) {
            if (exp.coeffs[i] == 0)
                continue;

            int upper = exp.coeffs[i] > 0 ? 1 : 0;
            int value = getValueFromPosition(i, upper);
            currentResonArray[i] = new ReasonSet(i, upper, value);
            sum += exp.coeffs[i] * value;

            int pos = getBoundStackPosition(i, upper);
            boundStackPositions.add(pos);
            CS.add(new ConflictSet(i, upper, exp.coeffs[i]));

            if (pos > boundStackPosition)
                boundStackPosition = pos;
        }

        if (sum > exp.value)
            return;

        if (sum == exp.value) {
            for (int j = 0; j < boundArray.length; j++) {
                if (exp.coeffs[j] == 0)
                    continue;

                int upper = exp.coeffs[j] > 0 ? 1 : 0;
                checkAndAddToStack(j, getValueFromPosition(j, upper), 1 - upper, currentResonArray, expNumber + 1);
                    //System.out.println();
            }
            return;
        }

        // conflict analysis
        //System.out.println("new conflict");
        boundStackPositions.sort(Collections.reverseOrder());
        CS.sort(Comparator.comparing(ConflictSet::getStackPosition, Collections.reverseOrder()));

        Expression newExpression = new Expression(finalVariables.length, Expression.ExpressionComparison.SMALLER_EQUAL_THAN);
        newExpression.addExpression(0, exp, 1);
        do {
            // bound topmost in A
            ConflictSet csTop = CS.get(0);
            BoundStack B = getBoundStackValue(csTop.getStackPosition());
            ReasonSet[] R = B.reasonSet;

            // CS \ {B}
            CS.remove(csTop);

            // CS U R
            for (int i = 0; i < R.length; i++) {
                if (R[i] == null)
                    continue;

                boolean found = false;
                for (ConflictSet c : CS) {
                    if (c.isEqual(R[i].index, R[i].upper)) {
                        if (c.upper == 1 && R[i].bound < c.bound) {
                            c.bound = R[i].bound;
                        } else if (c.upper == 0 && R[i].bound > c.bound) {
                            c.bound = R[i].bound;
                        }
                        found = true;
                        break;
                    }
                }
                if (!found)
                    CS.add(new ConflictSet(R[i].index, R[i].upper, R[i].bound));
            }
            CS.sort(Comparator.comparing(ConflictSet::getStackPosition, Collections.reverseOrder()));
            if (CS.isEmpty()) {
                //System.out.println("CS empty");
                return;
            }

            // find cut elimination between C and B's reason constraint
            // -1 == initial bound
            if (B.reasonConstraint > 0) {
                Expression B_reason_constraint = expressions.get(B.reasonConstraint - 1);
                int ai = newExpression.getCoeff(csTop.position);
                int bi = B_reason_constraint.getCoeff(csTop.position);

                if (ai + bi == 0) {
                    newExpression.addExpression(1, B_reason_constraint, 1);
                } else if (ai * bi < 0) {
                    int posAi = ai < 0 ? ai * -1 : ai;
                    int posBi = bi < 0 ? bi * -1 : bi;
                    newExpression.addExpression(posBi, B_reason_constraint, posAi);
                }
            }

            // Early Backjump
            // TODO, performance boost
            int decisionNumber = decisions.size() - 1;
            while (decisionNumber >= 0) {
                int k = decisions.get(decisionNumber);

                boolean foundBound = false;
                for (int j = 0; j < boundArray.length; j++) {
                    if (finalVariables[j] || exp.coeffs[j] == 0) {
                        continue;
                    }
                    currentResonArray = new ReasonSet[finalVariables.length];
                    foundBound = findBoundForVariable(j, exp, expNumber, k);

                    if (foundBound)
                        break;
                }
                if (foundBound) {
                    removeFromStack(k);
                    //learn
                    expressions.add(newExpression);
                    //System.out.println("new early backjump");
                    //System.out.println(newExpression);
                    return;
                }
                decisionNumber--;
            }

        } while (!decisions.isEmpty() && CS.get(0).getStackPosition() < decisions.peek());

        // backjump
        int B_position = CS.size() > 1 ? CS.get(1).getStackPosition() : 0;
        while (decisions.size() > 0 && ((B_position == 0 && boundStack.peek().previous > 0) || B_position < decisions.peek()))
            removeFromStack();

        currentResonArray = new ReasonSet[finalVariables.length];
        int upper = newExpression.coeffs[CS.get(0).position] > 0 ? 1 : 0;
        for (int i = 1; i < CS.size(); i++) {
            ConflictSet cs = CS.get(i);
            if (cs.upper != upper)
                currentResonArray[cs.position] = new ReasonSet(cs.position, cs.upper, cs.bound);
        }
        checkAndAddToStack(CS.get(0).position, CS.get(0).bound, upper, currentResonArray, expressions.size());

        //learn
        for (Expression ex : expressions)
            if (newExpression.isEqual(ex))
                return;
        expressions.add(newExpression);
        //System.out.println("new expression");
        //System.out.println(newExpression);
    }

    class ConflictSet {
        int position;
        int upper;
        int bound;

        public ConflictSet(int position, int upper, int bound) {
            this.position = position;
            this.upper = upper;
            this.bound = bound;
        }

        public int getStackPosition() {
            return boundArray[position][upper];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConflictSet cs = (ConflictSet) o;
            return position == cs.position &&
                    upper == cs.upper;
        }

        @Override
        public int hashCode() {
            return Objects.hash(position, upper);
        }

        public boolean isEqual(int pos, int up) {
            return position == pos && upper == up;
        }
    }

    private void findBoundOrMakeDecisionForVariable(int position, Expression exp, int expNumber) {
        boolean found = findBoundForVariable(position, exp, expNumber);

        if (found) {
            countNoBound = 0;

        } else {
            countNoBound++;
        }

        if (countNoBound >= 5 && getValueFromPosition(position, 0) != getValueFromPosition(position, 1)) {
            doOneDecision(position);
        }

        System.out.println(getValueFromPosition(position, 0) + " " + getValueFromPosition(position, 1));
    }

    private boolean findBoundForVariable(int position, Expression exp, int expNumber) {
        int up = exp.coeffs[position] >= 0 ? 1 : 0;
        int sum = getSumOfOtherVariables(exp, position);

        int bound = (exp.value - sum) / exp.coeffs[position];
        if ((exp.value - sum) % exp.coeffs[position] != 0) {
            if (up == 0) {
                bound++;
            }
        }
        return checkAndAddToStack(position, bound, up, currentResonArray, expNumber + 1);
    }

    private boolean findBoundForVariable(int position, Expression exp, int expNumber, int decisionPosition) {
        int up = exp.coeffs[position] >= 0 ? 1 : 0;
        int sum = getSumOfOtherVariables(exp, position);

        int bound = (exp.value - sum) / exp.coeffs[position];
        if ((exp.value - sum) % exp.coeffs[position] != 0) {
            if (up == 0) {
                bound++;
            }
        }
        return checkOnStack(position, bound, up, decisionPosition);
    }

    private boolean checkOnStack(int position, int bound, int upper, int decisionPosition) {

        // find bound before decisionPosition
        int stackPosition = boundArray[position][upper];
        while (stackPosition > decisionPosition) {
            stackPosition = getBoundStackValue(stackPosition).previous;
        }
        int previousValue = getBoundStackValue(stackPosition).bound;

        // find other bound before decisionPosition
        stackPosition = boundArray[position][1 - upper];
        while (stackPosition > decisionPosition) {
            stackPosition = getBoundStackValue(stackPosition).previous;
        }
        int otherBound = getBoundStackValue(stackPosition).bound;
        return (upper != 1 || (previousValue > bound && bound >= otherBound)) && (upper != 0 || (bound > previousValue && bound <= otherBound));
    }

    private void doOneDecision(int position) {
        //System.out.println("Decision variable:" + index);
        int l = getValueFromPosition(position, 0);
        int u = getValueFromPosition(position, 1);
        if (l == u)
            System.err.println("lower and upper are the same");

        int m = (l + u) / 2;
        if (u == m)
            m--;
        checkAndAddToStack(position, m, 1, new ReasonSet[finalVariables.length], -1);
        decisions.add(boundStack.size());
        countNoBound = 0;
    }
}
