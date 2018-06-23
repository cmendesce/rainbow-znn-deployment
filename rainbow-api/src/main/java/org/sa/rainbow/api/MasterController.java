package org.sa.rainbow.api;

import org.sa.rainbow.core.RainbowMaster;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping(value = "/master", produces = APPLICATION_JSON_UTF8_VALUE)
public class MasterController {

    private RainbowMaster master;

    public MasterController(RainbowMaster master) {
        this.master = master;
    }

    @GetMapping("/probes/start")
    public ResponseEntity<?> startProbes() {
        master.startProbes();
        return ok(master.id());
    }

    @GetMapping("/delegates")
    public ResponseEntity<?> getDelegates() {
        return ok(master.getExpectedDelegateLocations());
    }
}
