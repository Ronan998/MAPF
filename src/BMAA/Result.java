package BMAA;

import java.time.Duration;

public class Result {

    private final double completionRate;
    private final int flowtime;
    private final int makespan;


    private final int numAgents;
    private final Duration runtime;

    public Result(double completionRate, int flowtime, int makespan, int numAgents, Duration runtime) {
        this.completionRate = completionRate;
        this.flowtime = flowtime;
        this.makespan = makespan;
        this.numAgents = numAgents;
        this.runtime = runtime;
    }
}
