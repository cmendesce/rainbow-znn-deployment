package org.sa.rainbow.gui;

import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/**
 * Created by carlosmendes on 5/2/18.
 */
public class Application {

    public static void main (String[] args) throws RainbowException {
        boolean showHelp = false;

        int lastIdx = args.length - 1;
        for (int i = 0; i <= lastIdx; i++) {
            if (args[i].equals ("-h")) {
                showHelp = true;
            } else {
                System.err.println ("Unrecognized or incomplete argument " + args[i]);
                showHelp = true;
            }
        }
        if (showHelp) {
            System.out.println ("Usage:\n" + "  system property options {default}:\n"
                    + "    rainbow.target    name of target configuration {default}\n"
                    + "    rainbow.config    top config directory (org.sa.rainbow.config)\n" + " " +
                    " options: \n"
                    + "    -h          Show this help message\n" + "    -nogui      Don't show " +
                    "the Rainbow GUI\n" + "\n"
                    + "Option defaults are defined in <rainbow.target>/rainbow.properties");
            System.exit (RainbowConstants.EXIT_VALUE_ABORT);
        }

        RainbowGUI gui = new RainbowGUI (null);
        gui.display ();
    }

}
