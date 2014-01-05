package edu.cmu.sphinx.sample.dialog.lt.behavior;

import java.io.IOException;

import javax.speech.recognition.GrammarException;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleParse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.speech.engine.recognition.BaseRecognizer;
import com.sun.speech.engine.recognition.BaseRuleGrammar;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.result.Result;


/**
* Provides the default behavior for dialog node. Applications will
* typically extend this class and override methods as appropriate
*/
public class DialogNodeBehavior {
	private static final Logger LOG = LoggerFactory.getLogger(DialogNodeBehavior.class);
    private DialogNode node;

    /**
     * Called during the initialization phase
     *
     * @param node the dialog node that the behavior is attached to
     */
    public void onInit(DialogNode node) {
        this.node = node;
    }

    /**
     * Called when this node becomes the active node
     * @throws JSGFGrammarException 
     * @throws JSGFGrammarParseException 
     */
    public void onEntry() throws IOException, JSGFGrammarParseException, JSGFGrammarException {
    	LOG.info("Entering {}", getName());
    }

    /**
     * Called when this node is ready to perform recognition
     */
    public void onReady() {
    	LOG.info("Ready {}", getName());
    }

    /*
     * Called with the recognition results. Should return a string
     * representing the name of the next node.
     */
    public String onRecognize(Result result) throws GrammarException {
        String tagString =  getTagString(result);
        LOG.info("Recognize result: {}" , result.getBestFinalResultNoFiller());
        LOG.info("Recognize tag   : {}", tagString);
        return tagString;
    }

    /**
     * Called when this node is no lnoger the active node
     */
    public void onExit() {
    	LOG.info("Exiting {}", getName());
    }

    /**
     * Returns the name for this node
     *
     * @return the name of the node
     */
    public String getName() {
        return node.getName();
    }

    /**
     * Returns the string representation of this object
     *
     * @return the string representation of this object
     */
    public String toString() {
        return "Node " + getName();
    }

    /**
     * Retrieves the grammar associated with this ndoe
     *
     * @return the grammar
     */
    public JSGFGrammar getGrammar() {
        return node.getGrammar();
    }

    /**
     * Retrieves the rule parse for the given result
     *
     * @param the recognition result
     * @return the rule parse for the result
     * @throws GrammarException if there is an error while parsing the
     * result
     */
    RuleParse getRuleParse(Result result) throws GrammarException {
        String resultText = result.getBestFinalResultNoFiller();
        BaseRecognizer jsapiRecognizer = new BaseRecognizer(getGrammar().getGrammarManager());
        try {
            jsapiRecognizer.allocate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        RuleGrammar ruleGrammar = new BaseRuleGrammar (jsapiRecognizer, getGrammar().getRuleGrammar());
        RuleParse ruleParse = ruleGrammar.parse(resultText, null);
        return ruleParse;
    }

    /**
     * Gets a space delimited string of tags representing the result
     *
     * @param result the recognition result
     * @return the tag string
     * @throws GrammarException if there is an error while parsing the
     * result
     */
    protected String getTagString(Result result) throws GrammarException {
        RuleParse ruleParse = getRuleParse(result);
        if (ruleParse == null)
            return null;
        String[] tags = ruleParse.getTags();
        if (tags == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (String tag : tags)
            sb.append(tag).append(' ');
        return sb.toString().trim();
    }

}
