package edu.cmu.sphinx.sample.dialog.lt.behavior.pubserv;

import java.io.IOException;

import javax.speech.recognition.GrammarException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spantus.exp.synthesis.EspeakMbrollaGeneratorLt;

import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.sample.dialog.lt.behavior.MyBehavior;
import edu.cmu.sphinx.sample.dialog.lt.dto.BehaviorContext;

public class ConfirmationBehavior extends MyBehavior {
	private static final Logger LOG = LoggerFactory
			.getLogger(ConfirmationBehavior.class);
	private BehaviorContext behaviorCtx;

	public ConfirmationBehavior(EspeakMbrollaGeneratorLt speakGenerator,
			BehaviorContext behaviorCtx) {
		super(speakGenerator);
		this.behaviorCtx = behaviorCtx;
	}

	@Override
	public void onEntry() throws IOException, JSGFGrammarParseException,
			JSGFGrammarException {
		super.onEntry();
		
		speak("Ar Jūs sakėte, " + behaviorCtx.getPreviousStatement());
	}

	@Override
	public String onRecognize(Result result) throws GrammarException {
		LOG.debug("onRecognize name: {}", getName());
		String tagString = getTagString(result);
		LOG.debug("onRecognize[{}] paslauga: {}", getName(), tagString);
		if("TRUE".equals(tagString)){
			return behaviorCtx.getNextState();
		}
		return behaviorCtx.getPreviousState();
	}

}
