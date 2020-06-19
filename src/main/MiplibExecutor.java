package main;

import main.currentVersion.Solver;

public class MiplibExecutor {

    public static long executeSolver(String filename) {
        Solver solver = MPS2Solver.getSolverFromMps(filename);
        long startTime = System.currentTimeMillis();
        if (solver != null)
            solver.startSolver();

        return System.currentTimeMillis() - startTime;
    }
}
