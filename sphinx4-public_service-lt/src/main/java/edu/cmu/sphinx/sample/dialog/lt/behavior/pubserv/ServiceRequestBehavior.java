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

public class ServiceRequestBehavior extends MyBehavior {
	private static final Logger LOG = LoggerFactory
			.getLogger(ServiceRequestBehavior.class);
	private BehaviorContext numberBehaviorCtx;

	public ServiceRequestBehavior(EspeakMbrollaGeneratorLt speakGenerator,
			BehaviorContext numberBehaviorCtx) {
		super(speakGenerator);
		this.numberBehaviorCtx = numberBehaviorCtx;
	}

	@Override
	public void onEntry() throws IOException, JSGFGrammarParseException,
			JSGFGrammarException {
		super.onEntry();
		numberBehaviorCtx.setPreviousState("service_request");
		speak("Kokios paslaugos ieškote?");
	}

	@Override
	public String onRecognize(Result result) throws GrammarException {
		LOG.debug("onRecognize name: {}", getName());
		String tagString = getTagString(result);
		numberBehaviorCtx.setPreviousStatement(result.getBestFinalResultNoFiller());
		numberBehaviorCtx.setNextState("request_submited");
		LOG.debug("onRecognize[{}] paslauga: {}", getName(), tagString);
		return "confirmation";
	}

}
