package Experiment;

import Common.Util;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TestUtil {

    private static Logger logger = Logger.getLogger(TestUtil.class.getName());
    private static FileHandler activeLogFile;

    public static void log(String message) {
        logger.info(message);
    }

    public static FileHandler startFileLogging(String fileName) {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%5$s%6$s%n");

            FileHandler fh = new FileHandler("logs/" + Util.currentDateTime("MM-dd HH:mm") + " " + fileName);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            activeLogFile = fh;

            logger.info(new Date().toString());

            return fh;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void stopFileLogging() {
        logger.removeHandler(activeLogFile);
        activeLogFile.close();
        activeLogFile = null;
    }



}
