package main;

import gurobi.*;
import main.currentVersion.*;

public class MPS2Solver {

    public static Solver getSolverFromMps(String filename) {

        Solver solver = null;
        try {
            GRBEnv env = new GRBEnv();
            try {
                GRBModel model = new GRBModel(env, filename);
                GRBModel presolvedModel = model.presolve();
                try {
                    if (presolvedModel.getVars().length > 0) {
                        solver = transformModel2Solver(presolvedModel);
                    } else {
                        // presolver already found the only solution
                        solver = transformModel2Solver(model);
                    }
                } finally {
                    presolvedModel.dispose();
                    model.dispose();
                }
            } finally {
                env.dispose();
            }
        } catch (Exception e) {
            System.out.println("Exception while mps loading: " + e.getMessage());
        }
        return solver;
    }

    private static Solver transformModel2Solver(GRBModel model) throws Exception {
        GRBVar[] vars = model.getVars();
        Solver solver = new Solver(vars.length);

        // transform all variable with lower and upper bound
        for (int i = 0; i < vars.length; i++) {
            int lb = (int) vars[i].get(GRB.DoubleAttr.LB);
            int ub = (int) vars[i].get(GRB.DoubleAttr.UB);
            solver.addVarWithBounds(i, lb, ub);
            //System.out.println(i + " " + lb + " " + ub);
        }

        // transform all model constraints
        GRBConstr[] constrs = model.getConstrs();
        for (GRBConstr constr : constrs) {
            char sense = constr.get(GRB.CharAttr.Sense);
            ComparisonType expressionComparison =
                    sense == '<' ? ComparisonType.LESS_EQUAL :
                            sense == '>' ? ComparisonType.GREATER_EQUAL : ComparisonType.EQUAL;

            Constraint expression = new Constraint(expressionComparison);
            for (int i = 0; i < vars.length; i++) {
                int coeff = (int) model.getCoeff(constr, vars[i]);
                expression.setCoefficient(i, coeff);
            }
            expression.setConstraintValue((int) constr.get(GRB.DoubleAttr.RHS));
            solver.addConstraint(expression);
        }

        // transform optimization
        int modelSense = model.get(GRB.IntAttr.ModelSense);
        ComparisonType optimizationComparision = modelSense == 1 ? ComparisonType.MINIMIZE : ComparisonType.MAXIMIZE;

        GRBLinExpr grbExpr = (GRBLinExpr) model.getObjective();
        Constraint optimization = new Constraint(optimizationComparision);
        for (int i = 0; i < grbExpr.size(); i++) {
            GRBVar var = grbExpr.getVar(i);

            // get index of var
            for (int j = 0; j < vars.length; j++)
                if (var.equals(vars[j])) {
                    optimization.setCoefficient(j, (int) grbExpr.getCoeff(i));
                }
        }
        solver.setOptimization(optimization);
        return solver;
    }

    public static void createMpsFromSolver(Solver solver, String filename) {
        try {
            GRBEnv env = new GRBEnv();
            try {
                GRBModel model = new GRBModel(env);
                try {
                    // add all variable with lower and upper bound
                    GRBVar[] vars = new GRBVar[solver.variableCount];
                    for (int i = 0; i < solver.variableCount; i++) {
                        vars[i] = model.addVar(solver.getBoundFromPosition(i, 0), solver.getBoundFromPosition(i, 1), 0, GRB.INTEGER, null);
                    }

                    // add expression as model constraint
                    int j = 0;
                    for (Constraint expression : solver.constraints) {
                        GRBLinExpr expr = new GRBLinExpr();
                        for (int i = 0; i < solver.variableCount; i++) {
                            expr.addTerm(expression.getCoefficient(i), vars[i]);
                        }
                        String v = "exp_" + j++;
                        model.addConstr(expr, GRB.LESS_EQUAL, expression.getConstraintValue(), v);
                    }

                    // transform optimization
                    GRBLinExpr optimization = new GRBLinExpr();
                    boolean hasOptimization = solver.optimization != null;
                    if (hasOptimization) {
                        for (int i = 0; i < solver.variableCount; i++) {
                            int coeff = solver.optimization.getCoefficient(i);
                            if (coeff != 0) {
                                optimization.addTerm(solver.optimization.getCoefficient(i), vars[i]);
                            }
                        }

                        // GRB.MAXIMIZE == -1
                        // GRB.MINIMIZE == 1
                        int modelSense = solver.optimization.getComparison() == ComparisonType.MINIMIZE ? 1 : -1;
                        model.setObjective(optimization, modelSense);
                    }

                    model.write(filename);
                } finally {
                    model.dispose();
                }
            } finally {
                env.dispose();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
