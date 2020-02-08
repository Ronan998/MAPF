package Testing;

import Common.Util;

import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TestUtil {

    private static FileHandler activeLogFile;

    public static FileHandler startFileLogging(Logger logger, String fileName) {
        try {
            FileHandler fh = new FileHandler("logs/" + Util.currentDateTime("MM-dd HH:mm") + fileName);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.info(new Date().toString());
            return fh;
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public static void stopFileLogging() {

    }
}
