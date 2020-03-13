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


    /**
     * Private constructor so no one can instantiate this  class
     */
    private Benchmark() {
    }

}
