package test;

import main.MPS2Solver;
import main.currentVersion.*;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Satisfiable {

    @Test
    public void solve_3V_4G() {
        /*
        4*x+3*y+5*z=27
        2*x+y=7
        y+2*z=7
        x+z=4
        2
        3
        2
        */
        int variableCount = 3;
        Solver solver = new Solver(variableCount);

        solver.addVarWithBounds(0, 1, 5);
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
        c2.setConstraintValue(7);
        solver.addConstraint(c2);

        Constraint c3 = new Constraint(ComparisonType.EQUAL);
        c3.setCoefficient(0, 1);
        c3.setCoefficient(2, 1);
        c3.setConstraintValue(4);
        solver.addConstraint(c3);


        Constraint optimization = new Constraint(ComparisonType.MAXIMIZE);
        optimization.setCoefficient(0, 2);
        optimization.setCoefficient(1, 3);
        optimization.setCoefficient(2, 1);
        solver.setOptimization(optimization);

        boolean satisfiable = solver.startSolver();
        assertTrue(satisfiable, "couldn't find a satisfiable solution");
        assertEquals(2, solver.getSolution(0));
        assertEquals(3, solver.getSolution(1));
        assertEquals(2, solver.getSolution(2));
    }

    @Test
    public void solve_2V_3G() {
        /*
        x+y=4
        2*x+y=9
        x+3*y=2
        5
        -1
        */
        int variableCount = 2;
        Solver solver = new Solver(variableCount);

        solver.addVarWithBounds(0, -5, 5);
        solver.addVarWithBounds(1, -5, 5);

        Constraint c0 = new Constraint(ComparisonType.EQUAL);
        c0.setCoefficient(0, 1);
        c0.setCoefficient(1, 1);
        c0.setConstraintValue(4);
        solver.addConstraint(c0);

        Constraint c1 = new Constraint(ComparisonType.EQUAL);
        c1.setCoefficient(0, 2);
        c1.setCoefficient(1, 1);
        c1.setConstraintValue(9);
        solver.addConstraint(c1);

        Constraint c2 = new Constraint(ComparisonType.EQUAL);
        c2.setCoefficient(0, 1);
        c2.setCoefficient(1, 3);
        c2.setConstraintValue(2);
        solver.addConstraint(c2);

        boolean satisfiable = solver.startSolver();
        assertTrue(satisfiable, "couldn't find a satisfiable solution");
        assertEquals(5, solver.getSolution(0));
        assertEquals(-1, solver.getSolution(1));
    }

    @Test
    public void solve_3V_4G_2() {
        /*
        x+y-3*z=-12
        2*x+y=6
        x+3*y=3
        x+y=3
        3
        0
        5
        */
        int variableCount = 3;
        Solver solver = new Solver(variableCount);

        solver.addVarWithBounds(0, 0, 5);
        solver.addVarWithBounds(1, 0, 5);
        solver.addVarWithBounds(2, 0, 5);

        Constraint c0 = new Constraint(ComparisonType.EQUAL);
        c0.setCoefficient(0, 1);
        c0.setCoefficient(1, 1);
        c0.setCoefficient(2, -3);
        c0.setConstraintValue(-12);
        solver.addConstraint(c0);

        Constraint c1 = new Constraint(ComparisonType.EQUAL);
        c1.setCoefficient(0, 2);
        c1.setCoefficient(1, 1);
        c1.setConstraintValue(6);
        solver.addConstraint(c1);

        Constraint c2 = new Constraint(ComparisonType.EQUAL);
        c2.setCoefficient(0, 1);
        c2.setCoefficient(1, 3);
        c2.setConstraintValue(3);
        solver.addConstraint(c2);

        Constraint c3 = new Constraint(ComparisonType.EQUAL);
        c3.setCoefficient(0, 1);
        c3.setCoefficient(1, 1);
        c3.setConstraintValue(3);
        solver.addConstraint(c3);

        boolean satisfiable = solver.startSolver();
        assertTrue(satisfiable, "couldn't find a satisfiable solution");
        assertEquals(3, solver.getSolution(0));
        assertEquals(0, solver.getSolution(1));
        assertEquals(5, solver.getSolution(2));
    }

    @Test
    public void solve_3V_3G_positiv() {
        /*
        x+y=4
        2*x+y=7
        x+3*y=6
        3
        1
        */
        int variableCount = 2;
        Solver solver = new Solver(variableCount);

        solver.addVarWithBounds(0, -5, 5);
        solver.addVarWithBounds(1, -5, 5);

        Constraint c0 = new Constraint(ComparisonType.EQUAL);
        c0.setCoefficient(0, 1);
        c0.setCoefficient(1, 1);
        c0.setConstraintValue(4);
        solver.addConstraint(c0);

        Constraint c1 = new Constraint(ComparisonType.EQUAL);
        c1.setCoefficient(0, 2);
        c1.setCoefficient(1, 1);
        c1.setConstraintValue(7);
        solver.addConstraint(c1);

        Constraint c2 = new Constraint(ComparisonType.EQUAL);
        c2.setCoefficient(0, 1);
        c2.setCoefficient(1, 3);
        c2.setConstraintValue(6);
        solver.addConstraint(c2);

        boolean satisfiable = solver.startSolver();
        assertTrue(satisfiable, "couldn't find a satisfiable solution");
        assertEquals(3, solver.getSolution(0));
        assertEquals(1, solver.getSolution(1));
    }

}