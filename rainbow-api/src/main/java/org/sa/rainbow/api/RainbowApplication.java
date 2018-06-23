package org.sa.rainbow.api;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RainbowApplication {

    public static void main(String[] args) {
        SpringApplication.run(RainbowApplication.class, args);
    }

    @Bean
    public RainbowMaster rainbowMaster() throws RainbowException {
        RainbowMaster master = new RainbowMaster ();
        master.initialize ();

        //RainbowDelegate localDelegate = new RainbowDelegate ();
        //localDelegate.initialize ();

        master.start ();
        //localDelegate.start ();

        RainbowPortFactory.createMasterCommandPort ();
        return master;
    }
}
