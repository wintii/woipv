package test;

import main.GurobiExecutor;
import main.MiplibExecutor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiplibExecutorTest {

    private List<String> files = Arrays.asList(
            //"30n20b8",
            //"d10200",
            "d20200"
            //"lectsched-1",
            //"lectsched-1-obj",
            //"lectsched-2",
            //"lectsched-3",
            //"lectsched-4-obj",
            //"mzzv11",
            //"neos16",
            //"neos-686190",
            //"ns1854840",
            //"rococoB10-011000",
            //"rococoC10-001000",
            //"rococoC11-011100",
            //"rococoC12-111000"
    );

    private List<String> output = new ArrayList<>();

    @Test
    public void testMiplibFiles() {
        for (String filename : files) {
            execute(filename);
        }
        for (String o : output) {
            System.out.println(o);
        }
    }

    @Test
    public void test_single() {
        execute("mzzv11");
        for (String o : output) {
            System.out.println(o);
        }
    }

    private void execute(String filename) {
        long time = MiplibExecutor.executeSolver("inputs/miplib/" + filename + ".mps");
        output.add("Case: " + filename + ". Time: " + time + "ms");
    }
}
