package Benchmark;

import Common.Util;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Result {

//    private int numAgents;

//    private long timeTaken;
    private double completionRate;
//    private List<Integer> completionTimes;
//    private List<Double> travelDistances;

    public Result(int numAgents, long timeTaken, double completionRate,
                  List<Integer> completionTimes, List<Double> travelDistances) {
//        this.numAgents = numAgents;
//        this.timeTaken = timeTaken;
        this.completionRate = completionRate;
//        this.completionTimes = completionTimes;
//        this.travelDistances = travelDistances;
    }

    public Result(double completionRate) {
        this.completionRate = completionRate;
    }
    //----------------------------------------------------------------------------------------------

    public static Result averageResults(Collection<Result> results) {
        double averageCompletionRate = results.stream()
                .mapToDouble(result -> result.getCompletionRate())
                .sum() / (double) results.size();

        return new Result(averageCompletionRate);
    }
//
//    public int getNumAgents() {
//        return numAgents;
//    }
//
//    public long getTimeTaken() {
//        return timeTaken;
//    }

    public double getCompletionRate() {
        return completionRate;
    }

//    public List<Integer> getCompletionTimes() {
//        return completionTimes;
//    }
//
//    public int getAverageCompletionTime() {
//        return completionTimes.stream()
//                .mapToInt(time -> time)
//                .sum() / completionTimes.size();
//    }
//
//    public List<Double> getTravelDistances() {
//        return travelDistances;
//    }
//
//    public Double getAverageTravelDistance() {
//        return travelDistances.stream()
//                .mapToDouble(distance -> distance)
//                .sum() / travelDistances.size();
//    }
//
//    public static Result averageResults(List<Result> results) {
//        double avgCompletionRate = results.stream()
//                .mapToDouble(result -> result.getCompletionRate())
//                .sum() / results.size();
//
//        long avgTimeTaken = results.stream()
//                .mapToLong(result -> result.getTimeTaken())
//                .sum() / results.size();
//
//        int avgCompletionTime = results.stream()
//                .mapToInt(result -> result.getAverageCompletionTime())
//                .sum() / results.size();
//
//        double avgTravelDistance = results.stream().
//                mapToDouble(result -> result.getAverageTravelDistance())
//                .sum() / results.size();
//
//        return new Result(avgTimeTaken, avgCompletionRate);
//    }
//
//    public static Result averageRuns(List<Result> results) {
//        int numAgents = results.get(0).getNumAgents();
//
//        double avgCompletionRate = results.stream()
//                .mapToDouble(result -> result.getCompletionRate())
//                .sum() / results.size();
//
//        long avgTimeTaken = results.stream()
//                .mapToLong(result -> result.getTimeTaken())
//                .sum() / results.size();
//
//        int avgCompletionTime = results.stream()
//                .mapToInt(result -> result.getAverageCompletionTime())
//                .sum() / results.size();
//
//        double avgTravelDistance = results.stream().
//                mapToDouble(result -> result.getAverageTravelDistance())
//                .sum() / results.size();
//
//        return new Result(numAgents, avgTimeTaken, avgCompletionRate);
//    }
}
