package org.sa.rainbow.adaptation;

import org.acmestudio.acme.element.IAcmeSystem;
import org.apache.commons.lang.time.StopWatch;
import org.sa.rainbow.core.*;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.health.IRainbowHealthProtocol;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityFunction;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.adaptation.AdaptationManager;
import org.sa.rainbow.stitch.core.StitchScript;
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

    private String m_modelRef;
    private AcmeModelInstance m_model;
    private IModelsManagerPort m_modelsManagerPort;
    private IRainbowAdaptationEnqueuePort<Strategy> m_enqueuePort;
    private List<Stitch> m_repertoire;
    private boolean m_adaptEnabled;

    /**
     * Default Constructor with name for the thread.
     */
    public HighAvaibilityAdaptationManager() {
        super("HighAvaibilityAdaptationManager");

        m_repertoire = new ArrayList<> ();
        setSleepTime (SLEEP_TIME);
    }

    public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize(port);
        this.initConnectors();
    }

    private void initConnectors() throws RainbowConnectionException {
        //this.m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
        //this.m_modelChangePort.subscribe(this.m_modelTypecheckingChanged, this);
        this.m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
    }

    @Override
    protected void log(String txt) {
        m_reportingPort.info (RainbowComponentT.ADAPTATION_MANAGER, txt);
    }

    @Override
    protected void runAction() {
        Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_BEGIN);
        Strategy selectedStrategy = checkAdaptation ();
        Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_END);
        if (selectedStrategy != null) {
            log (">> do strategy: " + selectedStrategy.getName ());
            // strategy args removed...
            Object[] args = new Object[0];
            AdaptationTree<Strategy> at = new AdaptationTree<> (selectedStrategy);
            //m_pendingStrategies.add (at);
            m_enqueuePort.offerAdaptation (at, null);

            String message = MessageFormat.format ("{0,number,#},queuing,{1}\n", new Date().getTime(),
                    selectedStrategy.getName ());
            log(message);
        }

    }

    private Strategy checkAdaptation() {

        for (Stitch stitch : m_repertoire) {
            if (stitch.script.getName().equals("newssite.strategies")) {
                return stitch.script.strategies.get(0);
            }
        }
        return null;
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ADAPTATION_MANAGER;
    }

    @Override
    public void setModelToManage(ModelReference model) {
        m_modelRef = model.getModelName () + ":" + model.getModelType ();

        m_model = (AcmeModelInstance) m_modelsManagerPort.<IAcmeSystem>getModelInstance (model);
        if (m_model == null) {
            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER,
                    MessageFormat.format ("Could not find reference to {0}", model.toString ()));
        }
        m_enqueuePort = RainbowPortFactory.createAdaptationEnqueuePort (model);

        initAdaptationRepertoire ();
    }

    private void initAdaptationRepertoire () {
        File stitchPath = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (),
                Rainbow.instance ().getProperty (RainbowConstants
                        .PROPKEY_SCRIPT_PATH));
        if (stitchPath == null) {
            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER, "The stitchState path is not set!");
        } else if (stitchPath.exists () && stitchPath.isDirectory ()) {
            FilenameFilter ff = new FilenameFilter () { // find only ".s" files
                @Override
                public boolean accept (File dir, String name) {
                    return name.endsWith (".s");
                }
            };
            for (File f : stitchPath.listFiles (ff)) {
                try {
                    // don't duplicate loading of script files
                    Stitch stitch = Ohana.instance ().findStitch (f.getCanonicalPath ());
                    if (stitch == null) {
                        DummyStitchProblemHandler stitchProblemHandler = new DummyStitchProblemHandler ();
                        stitch = Stitch.newInstance (f.getCanonicalPath (), stitchProblemHandler);
                        Ohana.instance ().parseFile (stitch);

                        m_repertoire.add (stitch);
                        log ("Parsed script " + stitch.path);
                    } else {
                        log ("Previously known script " + stitch.path);
                    }
                } catch (IOException e) {
                    m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER,
                            "Obtaining file canonical path failed! " + f.getName (), e);
                }
            }
        }
    }

    @Override
    public void markStrategyExecuted(AdaptationTree<Strategy> strategy) {
        final List<Strategy> strategiesExecuted = new LinkedList<> ();
        final CountDownLatch countdownLatch = new CountDownLatch (1);

        try {
            countdownLatch.await (2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }

        for (Strategy str : strategiesExecuted) {
            String s = str.getName () + ";" + str.outcome ();
            log ("*S* outcome: " + s);
            Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY + s);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        m_reportingPort.info (getComponentType (),
                MessageFormat.format ("Turning adaptation {0}.", (enabled ? "on" : "off")));
        if (!enabled) {
            m_reportingPort.info (getComponentType (), "There is an adaptation in progress. This will finish.");
        }
        m_adaptEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return m_adaptEnabled;
    }

    @Override
    public void dispose() {

    }
}
