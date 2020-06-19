package main;

import gurobi.*;

public class GurobiExecutor {

    public static long execute(String filename) {
        GRBModel model = loadModelFromFile(filename);
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        if (model != null) {
            try {
                model.set(GRB.IntParam.SolutionLimit, 1);
                model.optimize();
                endTime = System.currentTimeMillis();
                System.out.println("Optimization bound: " + ((GRBLinExpr) model.getObjective()).getValue());

            } catch (GRBException e) {
                System.out.println("Exception while model optimization: " + e.getMessage());
            }
        }
        return endTime - startTime;
    }

    private static GRBModel loadModelFromFile(String filename) {
        try {
            GRBEnv env = new GRBEnv();
            return new GRBModel(env, filename);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
