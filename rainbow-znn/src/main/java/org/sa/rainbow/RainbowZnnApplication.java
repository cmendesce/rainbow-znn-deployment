package org.sa.rainbow;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import static java.text.MessageFormat.format;

public class RainbowZnnApplication {

    private static Logger logger = Logger.getLogger(RainbowZnnApplication.class);

    public static void main(String[] args) throws RainbowException {
        String mode = System.getenv("MODE");
        if (mode == null) {
            throw new RainbowException("Rainbow mode was not set. Accepted values: ''master'' or ''delegate''");
        }
        try {
            switch (mode) {
                case "delegate":
                    delegate();
                    break;
                case "master":
                    master();
                    break;
                default:
                    throw new RainbowException(format("Invalid Rainbow mode ''{0}''. Accepted values: ''master'' or ''delegate''.", mode));
            }
        } catch (RainbowException e) {
            logger.error(format("Rainbow " + mode + " could not start. Reason: {0}", e.getMessage()), e);
        }
    }

    private static void delegate() throws RainbowConnectionException {
        RainbowDelegate del = new RainbowDelegate();
        del.initialize();
        del.start();
    }
    private static void master() throws RainbowException {
        RainbowMaster master = new RainbowMaster ();
        master.initialize ();
        master.start ();
        RainbowPortFactory.createMasterCommandPort ();
    }
}
