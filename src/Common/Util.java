package Common;

import BMAA.Agent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Util {

    public static String currentDateTime() {
        return new SimpleDateFormat("HH:mm dd/MM").format(new Date());
    }

    public static String currentDateTime(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    public static double completionRate(List<Agent> agents) {
        return agents.stream()
                .filter(Agent::atGoal)
                .count() / (double) agents.size();
    }

    public static double averageDoubles(List<Double> list) {
        return list.stream()
                .mapToDouble(num -> num)
                .average()
                .orElse(Double.NaN);
    }

    public static double averageInts(List<Integer> list) {
        return list.stream()
                .mapToInt(num -> num)
                .average()
                .orElse(Double.NaN);
    }

    public static double averageLongs(List<Long> list) {
        return list.stream()
                .mapToLong(num -> num)
                .average()
                .orElse(Double.NaN);
    }
}
