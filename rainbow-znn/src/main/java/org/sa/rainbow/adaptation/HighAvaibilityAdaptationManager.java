package org.sa.rainbow.adaptation;

import org.apache.commons.lang.time.StopWatch;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityFunction;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.adaptation.AdaptationManager;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by carlosmendes on 5/3/18.
 */
public class HighAvaibilityAdaptationManager extends AbstractRainbowRunnable implements IAdaptationManager<Strategy> {

    /**
     * Default Constructor with name for the thread.
     */
    public HighAvaibilityAdaptationManager() {
        super("HighAvaibilityAdaptationManager");
    }

    @Override
    protected void log(String txt) {

    }

    @Override
    protected void runAction() {

    }

    @Override
    public RainbowComponentT getComponentType() {
        return null;
    }

    @Override
    public void setModelToManage(ModelReference modelRef) {

    }

    @Override
    public void markStrategyExecuted(AdaptationTree<Strategy> strategy) {

    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void dispose() {

    }
}
