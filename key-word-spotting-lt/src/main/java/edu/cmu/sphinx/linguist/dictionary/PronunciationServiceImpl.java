package edu.cmu.sphinx.linguist.dictionary;

import edu.cmu.sphinx.linguist.acoustic.Unit;

public class PronunciationServiceImpl {
	public Pronunciation createPronunciation(Unit[] units){
		return new Pronunciation(units, null, null, 1.f);
	}

	public void setWord(Pronunciation pronunciation, Word wordObject) {
		pronunciation.setWord(wordObject);
	}
}
