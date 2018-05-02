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
        boolean showGui = true;
        int lastIdx = args.length - 1;
        for (int i = 0; i <= lastIdx; i++) {
            switch (args[i]) {
                case "-h":
                    showHelp = true;
                    break;
                case "-nogui":
                    showGui = false;
                    break;
                default:
                    System.err.println ("Unrecognized or incomplete argument " + args[i]);
                    showHelp = true;
                    break;
            }
        }
        if (showHelp) {
            System.out.println ("Usage:\n" + "  system property options {default}:\n"
                    + "    rainbow.target    name of target configuration {default}\n"
                    + "    rainbow.config    top config directory (org.sa.rainbow.config)\n" + "  options: \n"
                    + "    -h          Show this help message\n" + "    -nogui      Don't show the Rainbow GUI\n" + "\n"
                    + "Option defaults are defined in <rainbow.target>/rainbow.properties");
            System.exit (RainbowConstants.EXIT_VALUE_ABORT);
        }

        RainbowMaster master = new RainbowMaster ();
        if (showGui) {
            RainbowGUI gui = new RainbowGUI (master);
            gui.display ();
        }
        master.initialize ();

        RainbowDelegate localDelegate = new RainbowDelegate ();
        localDelegate.initialize ();

        master.start ();
        localDelegate.start ();

        if (!showGui) {
            RainbowPortFactory.createMasterCommandPort ();
        }

    }

}
