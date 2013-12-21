package org.spantus.sphinx.kws.service.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.spantus.sphinx.kws.dto.PhonemeRangeSpottingResult;
import org.spantus.sphinx.kws.service.KeyWordSpottingServiceImpl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class KeyWordSpottingServiceImplTest {
	private KeyWordSpottingServiceImpl keyWordSpottingServiceImpl;
	
	@Before
	public void onSetup() {
		keyWordSpottingServiceImpl = new KeyWordSpottingServiceImpl();
	}
	@Test
	public void testInitDictionaries() {
		//given
		List<String> phonemesRange = Lists.newArrayList("4-7", "8-11", "12-15" );
		//then
		Map<String, List<String>> result = keyWordSpottingServiceImpl.initDictionaries("./target/test-classes",phonemesRange);
		//when
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(10, result.get("8-11").size());
		Assert.assertEquals(10, result.get("4-7").size());
		Assert.assertEquals(10, result.get("12-15").size());

		Assert.assertEquals("information", result.get("8-11").get(0));
		Assert.assertEquals("from", result.get("4-7").get(0));
		Assert.assertEquals("circumstances", result.get("12-15").get(0));

	}


	
	@Test
	public void testPerformTests_lt() {
		//given
		keyWordSpottingServiceImpl.setLanguage("lt");;
		List<String> outOfGrammarProbability = Lists.newArrayList("9E-1", "5E-1");
//		List<String> outOfGrammarProbability = Lists.newArrayList("9E-1", "5E-1",
//		"1E-1", "1E-3", "1E-5", "1E-7", "1E-9", "1E-11", "1E-15", "1E-20",
//		"1E-30", "1E-50", "1E-100", "1E-130", "1E-150", "1E-170", "1E-200");
		List<String> phonemesRange = Lists.newArrayList("4-7");
		Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
		List<String> audioFiles =  new ArrayList<String>();
		audioFiles.add("./target/test-classes/wav/lt/000-30_1.wav");
		audioFiles.add("./target/test-classes/wav/lt/lietuvos_mbr_test-30_1.wav");
		Map<String, List<String>> wordsToSpot = new LinkedHashMap<String, List<String>>();
		wordsToSpot.put("4-7", Lists.newArrayList(splitter.split("LIETUVOS")));
		
		//then
		Map<String, List<PhonemeRangeSpottingResult>> precisionRecallGraph = keyWordSpottingServiceImpl.performTests("./target/test-classes",outOfGrammarProbability, phonemesRange, audioFiles, wordsToSpot);
//		keyWordSpottingServiceImpl.saveResults("./target/test-classes", outOfGrammarProbability, precisionRecallGraph);
		//when
		Assert.assertEquals("Probabilities", outOfGrammarProbability.size(), precisionRecallGraph.size());
		for (String outOfGrammarProbabilityItem : outOfGrammarProbability) {
			List<PhonemeRangeSpottingResult> resultList = precisionRecallGraph.get(outOfGrammarProbabilityItem);
			Assert.assertEquals("Phonemes", phonemesRange.size(), resultList.size());
			for (PhonemeRangeSpottingResult resultItem : resultList) {
				Assert.assertTrue("Phonemes", phonemesRange.contains(resultItem.getPhonemesRangeItem()));
			} 
			
		}
	}	
	
}
