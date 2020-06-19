package test;

import main.currentVersion.*;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Example5 {

    @Test
    public void ex5() {
        /*
        x-3*y-3*z <= 1
        -2x+3y+2z <= -2
        3*x-3*y+2*z <= -1
        */
        int variableCount = 3;
        Solver solver = new Solver(variableCount);

        solver.addVarWithBounds(0, -2, 3);
        solver.addVarWithBounds(1, 1, 4);
        solver.addVarWithBounds(2, -2, 2);

        Constraint c0 = new Constraint(ComparisonType.LESS_EQUAL);
        c0.setCoefficient(0, 1);
        c0.setCoefficient(1, -3);
        c0.setCoefficient(2, -3);
        c0.setConstraintValue(1);
        solver.addConstraint(c0);

        Constraint c1 = new Constraint(ComparisonType.LESS_EQUAL);
        c1.setCoefficient(0, -2);
        c1.setCoefficient(1, 3);
        c1.setCoefficient(2, 2);
        c1.setConstraintValue(-2);
        solver.addConstraint(c1);

        Constraint c2 = new Constraint(ComparisonType.LESS_EQUAL);
        c2.setCoefficient(0, 3);
        c2.setCoefficient(1, -3);
        c2.setCoefficient(2, 2);
        c2.setConstraintValue(-1);
        solver.addConstraint(c2);

        boolean satisfiable = solver.startSolver();
        assertFalse(satisfiable, "should be infeasible");
    }

}