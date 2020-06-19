package main.currentVersion;

import java.util.*;

public class DecisionHelper {
    private final Stack<Integer> ids;
    private Solver solver;

    Stack<Integer> decisions = new Stack<>();

    public DecisionHelper(Solver solver) {
        this.solver = solver;
        ids = new Stack<>();
        ids.add(2);
        ids.add(0);
    }

    void doOneDecision() {
        int index = getDecisionVariable();

        int l = solver.getBoundFromPosition(index, 0);
        int u = solver.getBoundFromPosition(index, 1);
        if (l == u)
            System.err.println("lower and upper are the same");

        int m = (l + u) / 2;
        if (u == m)
            m--;

        //System.out.println("Decision variable: " + (solver.boundStack.size() + 1) + " i:" + index + " l" + l + " u" + u + "-->" + m);
        decisions.add(solver.boundStack.size() + 1);
        if (!solver.checkAndAddToStack(index, m, 1, new HashMap<>(), -2))
            decisions.pop();
    }

    private int getDecisionVariable() {
        //TODO get best index
        int index = 0;
        while (solver.finalVariables[index])
            index++;
        return index;
    }

    private void undoLastDecision() {
        int stackPosition = decisions.peek();
        BoundStack decisionStack = solver.getBoundStackValue(stackPosition);

        solver.removeFromStack(stackPosition);
        solver.checkAndAddToStack(decisionStack.index, decisionStack.bound + 1, 0, new HashMap<>(), -2);
    }

    void checkRemoveLastDecision(int position) {
        if (!decisions.empty() && position < decisions.peek()) {
            //System.out.println("Undo decision variable:" + decisions.peek());
            decisions.pop();
        }
    }
}
