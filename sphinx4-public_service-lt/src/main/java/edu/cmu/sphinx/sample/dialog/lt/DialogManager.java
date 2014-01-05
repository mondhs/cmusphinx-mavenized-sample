/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */

package edu.cmu.sphinx.sample.dialog.lt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.speech.recognition.GrammarException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spantus.exp.synthesis.EspeakMbrollaGeneratorLt;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.sample.dialog.lt.behavior.DialogNode;
import edu.cmu.sphinx.sample.dialog.lt.behavior.DialogNodeBehavior;
import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;



/**
 * The DialogManager is a component that is used to manage speech
 * dialogs.  A speech dialog is represented as a graph of dialog
 * nodes. The dialog manager maintains an active node. When a node is
 * active it is directing the recognition process. Typically a dialog
 * node will define the current active grammar. The recognition result
 * is typically used to direct the dialog manager to select the next
 * active node. An application can easily customize the behavior at
 * each active node.
 */
public class DialogManager implements Configurable {
	private static final Logger LOG = LoggerFactory.getLogger(DialogManager.class);
	EspeakMbrollaGeneratorLt speakGenerator;
    /**
     * The property that defines the name of the grammar component 
     * to be used by this dialog manager
     */
    @S4Component(type = JSGFGrammar.class)
    public final static String PROP_JSGF_GRAMMAR = "jsgfGrammar";

    /**
     * The property that defines the name of the microphone to be used 
     * by this dialog manager
     */
    @S4Component(type = Microphone.class)
    public final static String PROP_MICROPHONE = "microphone";

    /**
     * The property that defines the name of the recognizer to be used by
     * this dialog manager
     */
    @S4Component(type = Recognizer.class)
    public final static String PROP_RECOGNIZER = "recognizer";

    // ------------------------------------
    // Configuration data
    // ------------------------------------
    private JSGFGrammar grammar;
    private Recognizer recognizer;
        private Microphone microphone;

    // ------------------------------------
    // local data
    // ------------------------------------
    private DialogNode initialNode;
    private Map<String, DialogNode> nodeMap = new HashMap<String, DialogNode>();
    private String name;

    /*
    * (non-Javadoc)
    *
    * @see edu.cmu.sphinx.util.props.Configurable#newProperties(edu.cmu.sphinx.util.props.PropertySheet)
    */
    public void newProperties(PropertySheet ps) throws PropertyException {
    	speakGenerator = new EspeakMbrollaGeneratorLt();
        grammar = 
            (JSGFGrammar) ps.getComponent(PROP_JSGF_GRAMMAR);
        microphone = 
            (Microphone) ps.getComponent(PROP_MICROPHONE);
        recognizer = 
            (Recognizer) ps.getComponent(PROP_RECOGNIZER);
    }


    /**
     * Adds a new node to the dialog manager. The dialog manager
     * maintains a set of dialog nodes. When a new node is added the
     * application specific beh
     *
     * @param name the name of the node
     * @param behavior the application specified behavior for the node
     */
    public void addNode(String name, DialogNodeBehavior behavior) {
        DialogNode node = new DialogNode(name, behavior,recognizer,grammar);
        putNode(node);
    }

    /**
     * Sets the name of the initial node for the dialog manager
     *
     * @param name the name of the initial node. Must be the name of a
     * previously added dialog node.
     */
    public void setInitialNode(String name) {
        if (getNode(name) == null) {
            throw new IllegalArgumentException("Unknown node " + name);
        }
        initialNode = getNode(name);
    }

    /**
     * Gets the recognizer and the dialog nodes ready to run
     *
     * @throws IOException if an error occurs while allocating the
     * recognizer.
     */
    public void allocate() throws IOException {
        recognizer.allocate();

        for (DialogNode node : nodeMap.values()) {
            node.init();
        }
    }

    /**
     * Releases all resources allocated by the dialog manager
     */
    public void deallocate() {
        recognizer.deallocate();
    }

    /**
     * Invokes the dialog manager. The dialog manager begin to process
     * the dialog states starting at the initial node. This method
     * will not return until the dialog manager is finished processing
     * states
     * @throws JSGFGrammarException 
     * @throws JSGFGrammarParseException 
     */
    public void go() throws JSGFGrammarParseException, JSGFGrammarException {
        DialogNode lastNode = null;
        DialogNode curNode = initialNode;

        try {
	    if (microphone.startRecording()) {
                while (true) {

                    if (curNode != lastNode) {
                        if (lastNode != null) {
                            lastNode.exit();
                        }
                        curNode.enter();
                        lastNode = curNode;
                    } 
                    String nextStateName  = curNode.recognize();
                    if (nextStateName == null || nextStateName.isEmpty()) {
                        continue;
                    } else {
                        DialogNode node = nodeMap.get(nextStateName);
                        if (node == null) {
                        	LOG.warn("Can't transition to unknown state {}", nextStateName);
                        } else {
                            curNode = node;
                        }
                    }
                }
            } else {
            	LOG.error("Can't start the microphone");
            }
        } catch (GrammarException ge) {
        	LOG.error("grammar problem in state " + curNode.getName(), ge);
        } catch (IOException ioe) {
        	LOG.error("problem loading grammar in state " + curNode.getName(), ioe);
        }
    }


    /**
     * Returns the name of this component
     *
     * @return the name of the component.
     */
    public String getName() {
        return name;
    }


    /**
     * Gets the dialog node with the given name
     *
     * @param name the name of the node
     */
    private DialogNode getNode(String name) {
        return nodeMap.get(name);
    }

    /**
     * Puts a node into the node map
     *
     * @param node the node to place into the node map
     */
    private void putNode(DialogNode node) {
        nodeMap.put(node.getName(), node);
    }


    public Recognizer getRecognizer() {
        return recognizer;
    }

    /**
     * Sets the recognizer
     *
     * @param recognizer the recognizer
     */
    public void setRecognizer(Recognizer recognizer) {
        this.recognizer = recognizer;
    }
    
    public void speak(String text){
    	speakGenerator.speak(text);
    }


	public EspeakMbrollaGeneratorLt getSpeakGenerator() {
		return speakGenerator;
	}


}



