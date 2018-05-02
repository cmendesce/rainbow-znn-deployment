package org.sa.rainbow.translator.znn.probes;

import org.apache.log4j.Logger;
import org.sa.rainbow.translator.probes.AbstractRunnableProbe;


/**
 * Created by carlosmendes on 4/14/18.
 */
public class ClientProbe extends AbstractRunnableProbe {

    private String[] args;

    public ClientProbe(String id, long sleepTime) {
        super (id, "clientProxy", Kind.JAVA, sleepTime);
        LOGGER = Logger.getLogger (getClass ());
    }

    public ClientProbe(String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        this.args = args;
    }

    @Override
    public void run() {
        LOGGER.info("Running ClientProbe with args " + args);
    }
}
