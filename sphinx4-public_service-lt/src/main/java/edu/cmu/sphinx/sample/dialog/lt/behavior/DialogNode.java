package edu.cmu.sphinx.sample.dialog.lt.behavior;

import java.io.IOException;

import javax.speech.recognition.GrammarException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;

/**
 * Represents a node in the dialog
 */
public class   DialogNode {
	private static final Logger LOG = LoggerFactory.getLogger(DialogNode.class);
    private DialogNodeBehavior behavior;
    private String name;
	private Recognizer recognizer;
	private JSGFGrammar grammar;

    /**
     * Creates a dialog node with the given name an application
     * behavior
     *
     * @param name the name of the node
     *
     * @param behavior the application behavor for the node
     *
     */
    public DialogNode(String name, DialogNodeBehavior behavior, Recognizer recognizer, JSGFGrammar grammar) {
        this.behavior = behavior;
        this.name = name;
        this.recognizer = recognizer;
        this.grammar = grammar;
    }


    /**
     * Initializes the node
     */
    
    public void init() {
        behavior.onInit(this);
    }

    /**
     * Enters the node, prepares it for recognition
     * @throws JSGFGrammarException 
     * @throws JSGFGrammarParseException 
     */
    public void enter() throws IOException, JSGFGrammarParseException, JSGFGrammarException {
    	LOG.info("Entering {}", name);
        behavior.onEntry();
        behavior.onReady();
    }

    /**
     * Performs recognition at the node.
     *
     * @return the result tag
     */
    public String recognize() throws GrammarException {
    	LOG.info("Recognize {}", name);
        Result result = recognizer.recognize();
        return behavior.onRecognize(result);
    }

    /**
     * Exits the node
     */
    public void exit() {
    	LOG.info("Exiting {}", name);
        behavior.onExit();
    }

    /**
     * Gets the name of the node
     *
     * @return the name of the node
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the JSGF Grammar for the dialog manager that
     * contains this node
     *
     * @return the grammar
     */
    public JSGFGrammar getGrammar() {
        return grammar;
    }

    /**
     * Traces a message
     *
     * @param msg the message to trace
     */
//    public void trace(String msg) {
//        (msg);
//    }

//    public DialogManager getDialogManager() {
//        return DialogManager.this;
//    }
}
