/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * Created March 15, 2006, separated from class Stitch April 4, 2006.
 */
package org.sa.rainbow.stitch.core;

import org.acmestudio.acme.element.IAcmeElement;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.error.ArgumentMismatchException;
import org.sa.rainbow.stitch.tactic.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.Stitch;

import java.util.*;



/**
 * Represents a Tactic scoped object parsed from the script.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Tactic extends ScopedEntity implements IEvaluableScope {

    /**
     * Declares the states that the Tactic object might be in during parsing.
     */
    public enum ParseState {
        UNKNOWN, IN_PARAMS, IN_VARS, IN_CONDITION, IN_ACTION, IN_EFFECT, PARSED
    }
    private static final String ATTRIBUTE_ARG_PREF = "_arg_";

    public ParseState state = ParseState.UNKNOWN;

    public List<Var> args = new ArrayList<Var>();
    public List<Expression> conditions = new ArrayList<Expression>();
    public List<Statement> actions = new ArrayList<Statement>();
    public List<Expression> effects = new ArrayList<Expression>();

    private Map<String,Object> m_attributes = new HashMap<String,Object>();

    private ExecutionHistoryModelInstance m_executionHistoryModel;

    /**
     * Main Constructor for a new Tactic object.
     * @param parent  the parent scope
     * @param name    the name of this scope
     * @param stitch  the Stitch evaluation context object
     */
    public Tactic(IScope parent, String name, Stitch stitch) {
        super(parent, name, stitch);
    }

    /**
     * Clones a Tactic object, including just the collection objects, but not
     * deep-copying elements held in the collections.
     */
    @Override
    public synchronized Tactic clone (IScope parent) {
        Tactic newT = new Tactic(parent, m_name, m_stitch/*.clone ()*/);
        copyState(newT);
        return newT;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.ScopedEntity#copyState(org.sa.rainbow.stitch.core.ScopedEntity)
     */
    protected void copyState(Tactic target) {
        super.copyState(target);
        target.state = state;
        target.args = new ArrayList<Var>(args.size ());
        for (Var v :
                args) {
            target.args.add (v.clone ());
        }
        target.conditions = new ArrayList<Expression>(conditions.size ());
        for (Expression c : conditions) {
            target.conditions.add (c.clone (target.parent ()));
        }
        target.actions = new ArrayList<Statement>(actions.size ());
        for (Statement s : actions) {
            final Statement statement = new Statement (m_parent, m_name, m_stitch);
            statement.setAST (s.ast ());
            target.actions.add (statement);
        }
        target.effects = new ArrayList<Expression>(effects.size ());
        for (Expression e : effects) {
            target.effects.add (e.clone (target.parent ()));
        }
        target.m_attributes = new HashMap<String,Object>(m_attributes);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.ScopedEntity#toString()
     */
    @Override
    public String toString() {
        String str = "tactic: name \"" + m_name + "\" {" + "\n\t  vars [";
        for (Var v : m_vars.values()) {
            str += "\n\t\t" + v.toString();
        }
        str += "\n\t  ]\n\t  conditions [";
        for (Expression e : conditions) {
            str += "\n\t\t" + e.toString();
        }
        str += "\n\t  ]\n\t  actions [";
        for (Statement s : actions) {
            str += "\n\t\t" + s.toString();
        }
        str += "\n\t  ]\n\t  effects [";
        for (Expression e : effects) {
            str += "\n\t\t" + e.toString();
        }
        str += "\n\t  ]\n\t  m_attributes " + m_attributes.toString();
        str += "\n\t}";

        return str;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.ScopedEntity#addVar(java.lang.String, org.sa.rainbow.stitch.core.Var)
     */
    @Override
    public boolean addVar(String id, Var var) {
        boolean rv = true;
        switch (state) {
        case IN_VARS:
            rv = super.addVar(id, var);
            break;
        case IN_PARAMS:
            rv = super.addVar(id, var);
            args.add(var);
            break;
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.ScopedEntity#addExpression(org.sa.rainbow.stitch.core.Expression)
     */
    @Override
    public void addExpression(Expression expr) {
        super.addExpression(expr);

        // add expression to either condition or effect block if applicable
        switch (state) {
        case IN_CONDITION:
            addCondition(expr);
            break;
        case IN_EFFECT:
            addEffect(expr);
            break;
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.ScopedEntity#addStatement(org.sa.rainbow.stitch.core.Statement)
     */
    @Override
    public void addStatement(Statement stmt) {
        super.addStatement(stmt);

        // add statement to action block if applicable
        if (state == ParseState.IN_ACTION) {
            addAction(stmt);
        }
    }

    /**
     * Puts a new key-valueObj attribute pair into the attribute map.
     * The value object could be a number literal, or an expression string that
     * is evaluated later at value reading time.
     * 
     * @param name  the label of the attribute, e.g., uD
     * @param vObj  the value literal or expression string of the cost-benefit attribute
     */
    public void putAttribute (String name, Object vObj) {
        if (vObj instanceof Number) {
            m_attributes.put(name, ((Number )vObj).doubleValue());
        } else {
            // otherwise, grab expression string, munge it, and evaluate
            String exprStr = vObj + ";";  // append ending semicolon
            for (int i=0; i < args.size(); ++i) {
                exprStr = exprStr.replace("{" + i + "}", ATTRIBUTE_ARG_PREF + i);
            }
            m_attributes.put(name, exprStr);
        }
    }

    /**
     * Returns the set of keys in the attribute map.
     * @return set of keys
     */
    public Set<String> attributeKeySet () {
        return m_attributes.keySet();
    }

    /**
     * Computes and returns the attribute value for the given key.
     * @param key  name of the attribute
     * @return double value, either of the literal, or as a result of evaluating the value expression
     */
    public double computeAttribute (String key) {
        if (!m_attributes.containsKey(key)) return 0.0;
        Object vObj = m_attributes.get(key);
        if (vObj instanceof Double) return (Double )vObj;

        // otherwise, grab expression string, and parse it
        String exprStr = (String )vObj;
        Expression expr = Ohana.instance().parseExpressionString(exprStr);
        // populate values for required arguments, then evaluate expression
        expr.vars().clear();
        for (int i=0; i < args.size(); ++i) {
            String argName = ATTRIBUTE_ARG_PREF + i;
            if (exprStr.contains(argName)) {  // only create a var if called for
                Var v = new Var();
                v.scope = expr.parent();
                v.name = argName;
                Object o = args.get(i).getValue();  // there should be a value already
                // TODO sloppy treatment of value types!
                if (o instanceof Number) {
                    v.setType("float");  // Stitch float accommodates double and int
                } else if (o instanceof Set<?>){
                    v.setType("Set");
                } else {
                    v.setType("Object");
                }
                v.setValue(o);
                expr.addVar(v.name, v);
            }
        }

        Object rv = expr.evaluate(null);
        if (rv instanceof MyNumber) return ((MyNumber )rv).toJavaNumber().doubleValue();
        else
            return (Double )rv;
    }

    /**
     * Sets the tactic arguments with the supplied array of values.
     * @param argsIn  array of argument values to set
     */
    public void setArgs (Object[] argsIn) {
        if (args.size() > 0 && argsIn != null && argsIn.length == args.size()) {
            int i = 0;
            for (Var argV : args) {
                argV.setValue(argsIn[i++]);
            }
        } else {
            if (argsIn == null)
                throw new ArgumentMismatchException("Argument array is NULL!");
            else if (argsIn.length != args.size())
                throw new ArgumentMismatchException("Argument count does NOT match!");
        }
    }

    /**
     * Evaluates the Tactic, which consists of evaluating the conditions,
     * executing the action block if conditions match, and evaluating the
     * effects to make sure they are met.
     * 
     * @param argsIn  the input arguments
     */
    @Override
    public synchronized Object evaluate (Object[] argsIn) {
        long start = new Date ().getTime ();
        setArgs(argsIn);
        ExecutionHistoryModelInstance modelInstance = m_executionHistoryModel;

        // mark disruption level with model
        double level = computeAttribute("uD");
        if (modelInstance != null) {
            modelInstance.markDisruption (level);
        }

        // create snapshot of model
        stitch().script.freezeModel();

        // clear states of action statements first
        for (Statement stmt : actions) {
            stmt.clearState();
        }

        // process the variables
        for (Var v : vars().values()) {
            if (v.valStmt != null) {
                // evaluate the initial var value
                v.valStmt.evaluate(null);
            }
        }

        // evaluate the condition statements
        if (! checkCondition()) {  // don't continue execute?
            Tool.warn("Tactic condition of applicability NOT met!",null, stitch().stitchProblemHandler);
            // "unfreeze" model
            stitch().script.unfreezeModel();
            if (modelInstance != null)
            {
                modelInstance.markDisruption(0.0);  // reset disruption
            }
            return null;
        }
        // execute the action statements
        for (Statement stmt : actions) {
            stmt.evaluate(null);
            // check and abort if failure occurred
            if (stmt.hasError()) {
                m_hasError = true;
                Tool.error("Statement failed to evaluate! " + stmt.toString(), null,
                        stitch().stitchProblemHandler);
                // TODO:  keep running, or break?
            }
        }

        // evaluate the effect statements
        boolean effectObserved = checkEffect ();
        long end = new Date ().getTime ();

        if (Tool.logger().isInfoEnabled()) {
            Tool.logger().info("Tactic expected effect: " + effectObserved);
        }

        // "unfreeze" model
        stitch().script.unfreezeModel();
        if (modelInstance != null)
        {
            modelInstance.markDisruption(0.0);  // reset disruption
        }
        return null;  // Tactic doesn't return a result
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#estimateMaxTimeCost()
     */
    @Override
    public long estimateAvgTimeCost() {
        long t = 0L;
        // first check if we have history data on tactic execution
        IModelInstance<Map<String, ExecutionHistoryData>> historyModel = Rainbow.instance ().getRainbowMaster ()
                .modelsManager ()
                .getModelInstance (
                        new ModelReference ("tacticExecutionHistoryModel",
                                ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE));
        ExecutionHistoryData ehd = null;
        if (historyModel != null && ((ehd = historyModel.getModelInstance ().get (getQualifiedName ()))) != null) {
            t = (long )ehd.getMeanDuration();
        } else {  // estimate based on action statements
            for (Statement stmt : actions) {
                t += stmt.estimateAvgTimeCost();
            }
        }
        return t;
    }

    /**
     * Evaluates Tactic's conditions of applicability and returns result.
     * 
     * @return boolean  <code>true</code> if conditions of applicability are true, <code>false</code> otherwise.
     */
    public boolean checkCondition () {
        boolean condMet = true;
        for (Expression expr : conditions) {
            expr.clearState();
            condMet &= (Boolean )expr.evaluate(null);
        }
        return condMet;
    }

    /**
     * Evaluates Tactic's effects and returns result.
     *
     * @return boolean  <code>true</code> if effects are realized, <code>false</code> otherwise.
     */
    public boolean checkEffect () {
        boolean effMet = true;
        for (Expression expr : effects) {
            expr.clearState();
            effMet &= (Boolean )expr.evaluate(null);
        }
        return effMet;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.core.IEvaluable#modelElementsUsed()
     */
    @Override
    public Set<? extends IAcmeElement> modelElementsUsed() {
        Set<IAcmeElement> resultSet = new HashSet<IAcmeElement>();
        // calculate "used" only from action statements
        for (Statement stmt : actions) {
            resultSet.addAll(stmt.modelElementsUsed());
        }
        return resultSet;
    }

    protected void addCondition (Expression cond) {
        conditions.add(cond);
    }

    protected void addAction (Statement action) {
        actions.add(action);
    }

    protected void addEffect (Expression effect) {
        effects.add(effect);
    }

    public void setHistoryModel (ExecutionHistoryModelInstance executionHistoryModel) {
        m_executionHistoryModel = executionHistoryModel;
    }

    public void markExecuting (boolean executing) {
        stitch ().markExecuting (executing);
    }

    public boolean isExecuting () {
        return stitch ().isExecuting ();
    }
}