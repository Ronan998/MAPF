package Benchmark;

import java.util.List;

/**
 * Utility class to hold information relating to benchmarks
 */
public class Benchmark {

    // Common benchmark information
    public static final List<Integer> BMAA_AGENT_COUNTS = List.of(25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
            600, 800, 1000, 1200, 1400, 1600, 1800, 2000);
    public static final List<String> BMAA_TEST_MAPS =
            List.of("maps/BGII-AR0414SR (320*280).map",
                    "maps/BGII-AR0414SR (512*512).map",
                    "maps/BGII-AR0504SR (512*512).map",
                    "maps/BGII-AR0701SR (512*512).map",
                    "maps/WCIII-blastedlands (512*512).map",
                    "maps/WCIII-duskwood (512*512).map",
                    "maps/WCIII-golemsinthemist (512*512).map",
                    "maps/DAO-lak304d (193*193).map",
                    "maps/DAO-lak307d (84*84).map",
                    "maps/DAO-lgt300d (747*531).map");

    public static final List<Integer> TIME_LIMITS =
            List.of(1000, 1250, 1500, 1750,
                    2000, 2250, 2500, 2750,
                    3000, 3250, 3500, 3750,
                    4000, 4250, 4500, 4750,
                    5000, 5250, 5500, 5750,
                    6000, 6250, 6500, 6750,
                    7000, 7250, 7500, 7750,
                    8000, 8250, 8500, 8750,
                    9000, 9250, 9500, 9750,
                    10000, 12500, 15000, 17500,
                    20000, 22500, 25000, 27500,
                    30000, 32500, 35000, 37500,
                    40000, 42500, 45000, 47500,
                    50000, 60000, 70000);


    /**
     * Private constructor so no one can instantiate this  class
     */
    private Benchmark() {
    }

}
