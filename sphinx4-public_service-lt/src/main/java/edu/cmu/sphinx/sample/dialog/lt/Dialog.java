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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.sample.dialog.lt.behavior.MyBehavior;
import edu.cmu.sphinx.sample.dialog.lt.behavior.pubserv.CodeBehavior;
import edu.cmu.sphinx.sample.dialog.lt.behavior.pubserv.ConfirmationBehavior;
import edu.cmu.sphinx.sample.dialog.lt.behavior.pubserv.RequestSubmitedBehavior;
import edu.cmu.sphinx.sample.dialog.lt.behavior.pubserv.ServiceRequestBehavior;
import edu.cmu.sphinx.sample.dialog.lt.behavior.pubserv.YearBehavior;
import edu.cmu.sphinx.sample.dialog.lt.dto.BehaviorContext;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;


/**
 * A simple Dialog demo showing a simple speech application built using Sphinx-4 that uses the DialogManager.
 * <p/>
 * This demo uses a DialogManager to manage a set of dialog states. Each dialog state potentially has its own grammar.
 */
public class Dialog {
	
	

    /** Main method for running the Dialog demo. 
     * @throws JSGFGrammarException 
     * @throws JSGFGrammarParseException 
     **/
    public static void main(String[] args) throws JSGFGrammarParseException, JSGFGrammarException {
        try {
            URL url;
            if (args.length > 0) {
                url = new File(args[0]).toURI().toURL();
            } else {
                url = Dialog.class.getResource("dialog.config.xml");
            }
            ConfigurationManager cm = new ConfigurationManager(url);

            DialogManager dialogManager = (DialogManager)
                    cm.lookup("dialogManager");



            System.out.println("\nSveiki atvykę\n");
//            dialogManager.speak("Laba diena");
            BehaviorContext context = new BehaviorContext();
            
            dialogManager.addNode("menu", new MyBehavior(dialogManager.getSpeakGenerator()));
            dialogManager.addNode("news", new MyBehavior(dialogManager.getSpeakGenerator()));
            dialogManager.addNode("code", new CodeBehavior(dialogManager.getSpeakGenerator(), context));
            dialogManager.addNode("year", new YearBehavior(dialogManager.getSpeakGenerator(), context));
            dialogManager.addNode("service_request", new ServiceRequestBehavior(dialogManager.getSpeakGenerator(), context));
            dialogManager.addNode("request_submited", new RequestSubmitedBehavior(dialogManager.getSpeakGenerator(), context));
            dialogManager.addNode("confirmation", new ConfirmationBehavior(dialogManager.getSpeakGenerator(), context));
//            dialogManager.addNode("music", new MyMusicBehavior());

            dialogManager.setInitialNode("code");

            System.out.println("Krauna dialogą ...");

            dialogManager.allocate();


            System.out.println("Vykdo  ...");

            dialogManager.go();

            System.out.println("Apsivalom  ...");

            dialogManager.deallocate();

        } catch (IOException e) {
            System.err.println("Problem when loading Dialog: " + e);
        } catch (PropertyException e) {
            System.err.println("Problem configuring Dialog: " + e);
        }
        System.exit(0);
    }
}






