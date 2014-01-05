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

public class CodeBehavior extends MyBehavior {
	private static final Logger LOG = LoggerFactory
			.getLogger(CodeBehavior.class);
	private BehaviorContext numberBehaviorCtx;

	public CodeBehavior(EspeakMbrollaGeneratorLt speakGenerator,
			BehaviorContext numberBehaviorCtx) {
		super(speakGenerator);
		this.numberBehaviorCtx = numberBehaviorCtx;
	}

	@Override
	public void onEntry() throws IOException, JSGFGrammarParseException,
			JSGFGrammarException {
		super.onEntry();
		numberBehaviorCtx.setPreviousState("code");
		speak("Pasakykite kodą iš 3 skaičių");
	}

	@Override
	public String onRecognize(Result result) throws GrammarException {
		String tagString = getTagString(result);
		String code = tagString.replaceAll(" ", "");
		numberBehaviorCtx.setCode(code);
		numberBehaviorCtx.setPreviousStatement(code);
		numberBehaviorCtx.setNextState("year");
		LOG.debug("onRecognize[{}] code: {}", getName(), tagString);
		return "confirmation";
	}

}
