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

public class YearBehavior extends MyBehavior {
	private static final Logger LOG = LoggerFactory
			.getLogger(YearBehavior.class);
	private BehaviorContext numberBehaviorCtx;

	public YearBehavior(EspeakMbrollaGeneratorLt speakGenerator,
			BehaviorContext numberBehaviorCtx) {
		super(speakGenerator);
		this.numberBehaviorCtx = numberBehaviorCtx;
	}

	@Override
	public void onEntry() throws IOException, JSGFGrammarParseException,
			JSGFGrammarException {
		super.onEntry();
		speak("Sakykite gimimo metus iš 4 skaičių");
		numberBehaviorCtx.setPreviousState("year");
	}

	@Override
	public String onRecognize(Result result) throws GrammarException {
		LOG.debug("onRecognize name: {}", getName());
		String tagString = getTagString(result);
		String year = tagString.replaceAll(" ", "");
		numberBehaviorCtx.setYear(year);
		numberBehaviorCtx.setPreviousStatement(year);
		numberBehaviorCtx.setNextState("service_request");
		LOG.debug("onRecognize[{}] year: {}", getName(), tagString);
		return "confirmation";
	}

}
