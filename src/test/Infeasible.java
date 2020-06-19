package test;

import main.currentVersion.*;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Infeasible {

    @Test
    public void not_solve_2V_2G() {
        /*
        x+y=8
        2*x+y=9
        */
        int variableCount = 2;
        Solver solver = new Solver(variableCount);

        solver.addVarWithBounds(0, 1, 2);
        solver.addVarWithBounds(1, -5, 5);

        Constraint c0 = new Constraint(ComparisonType.EQUAL);
        c0.setCoefficient(0, 1);
        c0.setCoefficient(1, 1);
        c0.setConstraintValue(8);
        solver.addConstraint(c0);

        Constraint c1 = new Constraint(ComparisonType.EQUAL);
        c1.setCoefficient(0, 2);
        c1.setCoefficient(1, 1);
        c1.setConstraintValue(9);
        solver.addConstraint(c1);

        boolean satisfiable = solver.startSolver();
        assertFalse(satisfiable, "should be infeasible");
    }

    @Test
    public void not_solve_3V_4G() {
        /*
        4*x+3*y+5*z=27
        2*x+y=7
        y+2*z=8
        x+z=4
        */
        int variableCount = 3;
        Solver solver = new Solver(variableCount);

        solver.addVarWithBounds(0, 0, 5);
        solver.addVarWithBounds(1, 0, 5);
        solver.addVarWithBounds(2, 0, 5);

        Constraint c0 = new Constraint(ComparisonType.EQUAL);
        c0.setCoefficient(0, 4);
        c0.setCoefficient(1, 3);
        c0.setCoefficient(2, 5);
        c0.setConstraintValue(27);
        solver.addConstraint(c0);

        Constraint c1 = new Constraint(ComparisonType.EQUAL);
        c1.setCoefficient(0, 2);
        c1.setCoefficient(1, 1);
        c1.setConstraintValue(7);
        solver.addConstraint(c1);

        Constraint c2 = new Constraint(ComparisonType.EQUAL);
        c2.setCoefficient(1, 1);
        c2.setCoefficient(2, 2);
        c2.setConstraintValue(8);
        solver.addConstraint(c2);

        Constraint c3 = new Constraint(ComparisonType.EQUAL);
        c3.setCoefficient(0, 1);
        c3.setCoefficient(2, 1);
        c3.setConstraintValue(4);
        solver.addConstraint(c3);

        boolean satisfiable = solver.startSolver();
        assertFalse(satisfiable, "should be infeasible");
    }
}