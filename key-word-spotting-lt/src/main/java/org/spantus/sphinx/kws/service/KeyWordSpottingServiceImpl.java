package org.spantus.sphinx.kws.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spantus.sphinx.kws.dto.KeyWordSpottingDto;
import org.spantus.sphinx.kws.dto.PhonemeRangeSpottingResult;
import org.spantus.sphinx.kws.linguist.flat.KWSFlatLinguist;
import org.spantus.sphinx.kws.linguist.language.grammar.NoSkipGrammar;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.ConfigurationManagerUtils;

/**
 * Based on <a href=
 * "https://github.com/ewilded/KeyWordSpottingTest/blob/master/src/edu/cmu/sphinx/demo/KeyWordSpottingTest/KeyWordSpottingTest.java"
 * >KeyWordSpottingTest</a>
 * 
 * @author mindaugas greibus
 * @author ewilded
 * 
 * 
 */
public class KeyWordSpottingServiceImpl {

	private static final String SPHINX_RESULT = "(\\w+)\\(\\d+\\.\\d+,\\d+\\.\\d+\\)";// family(43.72,44.21)
	private static final Pattern WORD_SPOTED_PATERN = Pattern
			.compile(SPHINX_RESULT);

	private static final Logger LOG = LoggerFactory
			.getLogger(KeyWordSpottingServiceImpl.class);

	// private List<String> audioFiles = new ArrayList<String>();

	// private Map<String, List<String>> wordsToSpot = new HashMap<String,
	// List<String>>();
	// private static String[] PHONEME_RANGE = { "4-7", "8-11", "12-15", "16-20"
	// };
//	private static String[] OUT_OF_GRAMMA_PROBABILITY = { "9E-1", "5E-1",
//			"1E-1", "1E-3", "1E-5", "1E-7", "1E-9", "1E-11", "1E-15", "1E-20",
//			"1E-30", "1E-50", "1E-100", "1E-130", "1E-150", "1E-170", "1E-200" };

	private String language = "en";

	/**
	 * 
	 * @param phonemesRange
	 * @return
	 */
	public Map<String, List<String>> initDictionaries(String dir,
			List<String> phonemesRange) {
		Map<String, List<String>> aWordsToSpot = new HashMap<String, List<String>>();
		File dirFile = new File(dir);
		assertTrue(dirFile.exists(), "Dir does not existis: {0}",
				dirFile.getAbsolutePath());
		for (String phonemesRangeItem : phonemesRange) {
			String searchDictionaryPath = MessageFormat.format(
					"config/{0}/words_to_spot.{1}.txt", getLanguage(),
					phonemesRangeItem);
			File toSpotFile = new File(dirFile, searchDictionaryPath);
			assertTrue(toSpotFile.isFile(), "File does not existis: {0}",
					toSpotFile.getAbsolutePath());
			try {
				aWordsToSpot.put(phonemesRangeItem,
						Files.readLines(toSpotFile, Charsets.UTF_8));
			} catch (IOException e) {
				throw new IllegalArgumentException(
						" Bad thing happend with file: "
								+ toSpotFile.getAbsolutePath(), e);
			}
		}
		return aWordsToSpot;
	}

	/**
	 * 
	 * @param audioTestSetListPath
	 * @return
	 */
	public List<File> gatherAudioPaths(String dir, String audioTestSetListPath) {
		final File dirFile = new File(dir);
		assertTrue(dirFile.exists(), "Dir does not existis: {0}",
				dirFile.getAbsolutePath());
		List<File> audioFiles = new ArrayList<File>();
		try {
			List<String> files = Files.readLines(
					new File(audioTestSetListPath), Charsets.UTF_8);
			Iterable<File> filteredFiles = Iterables.filter(
					Iterables.transform(files, new Function<String, File>() {
						@Override
						public File apply(String anotherAudioFile) {
							return new File(dirFile, anotherAudioFile);
						}
					}), Files.isFile());
			Iterables.addAll(audioFiles, filteredFiles);
		} catch (Exception e) {
			LOG.error("Bad thing happened: ", e);
		}
		LOG.debug("Found {} audio files in the test set.", audioFiles.size());

		return audioFiles;
	}

	/**
	 * 
	 * @param outOfGrammarProbability
	 * @param phonemesRange
	 * @param audioFiles
	 * @param wordsToSpot
	 * @return
	 */
	public Map<String, List<PhonemeRangeSpottingResult>> performTests(
			String dir, List<String> outOfGrammarProbability,
			List<String> phonemesRange, List<String> audioFiles,
			Map<String, List<String>> wordsToSpot) {
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			throw new IllegalArgumentException("Dir does not existis: "
					+ dirFile.getAbsolutePath());
		}

		// separate counters for each of phonemes ranges
		Map<String, List<PhonemeRangeSpottingResult>> precisionRecallGraph = new HashMap<String, List<PhonemeRangeSpottingResult>>(); // phonemes

		Map<String, KeyWordSpottingDto> dtoMap = Maps.newLinkedHashMap();
		

		int j1 = 0;
		for (String outOfGrammarProbabilityItem : outOfGrammarProbability) {
			LOG.debug(
					"\n\nStarting tests with outOfGrammarProbability: {} ({} of {})",
					outOfGrammarProbabilityItem, (++j1),
					outOfGrammarProbability.size());
			List<PhonemeRangeSpottingResult> results = performTestForEachProbability(
					dirFile, phonemesRange, dtoMap, wordsToSpot,
					outOfGrammarProbabilityItem, audioFiles);
			precisionRecallGraph.put(outOfGrammarProbabilityItem, results);
		} // end of foreach over outOfGrammarProbability values
		LOG.debug("Test set has finished.");
		return precisionRecallGraph;
	}

	/**
	 * @param outOfGrammarProbabilityItem
	 * @param wordsToSpot
	 * @param dtoMap
	 * @param phonemesRange
	 * @param dirFile
	 * @return
	 * 
	 */
	private List<PhonemeRangeSpottingResult> performTestForEachProbability(
			File dirFile, List<String> phonemesRange,
			Map<String, KeyWordSpottingDto> dtoMap,
			Map<String, List<String>> wordsToSpot,
			String outOfGrammarProbabilityItem, List<String> audioFiles) {
		LOG.debug("Process outOfGrammarProbabilityItem: {}", outOfGrammarProbabilityItem );
		/*
		 * This has been commented out since it doesn't work anyway:
		 * cm.setGlobalProperty
		 * ("outOfGrammarProbability",outOfGrammarProbability[j]); //
		 * dynamically set current outOfGrammarProbability, other settings are
		 * left as they were set directly in the configuration file System
		 * .out.println("Current outOfGrammarProbability: "+cm.getGlobalProperty
		 * ("outOfGrammarProbability")); Solution: 13:53 < nshm1> you can add
		 * public method in linguist for that I don't know how, where, don't
		 * have time for this, so I just made a bunch of hard-coded
		 * outOfGrammarProbability configs
		 */
		ConfigurationManager cm = newConfigurationManager(dirFile, outOfGrammarProbabilityItem);
		Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
		NoSkipGrammar grammar = extractGrammar(cm);
		createDtos(phonemesRange, dtoMap, wordsToSpot, grammar);
		for (String audioFileItem : audioFiles) {
			LOG.debug("\n\nKWS for file {}", audioFileItem);
			int fileSize = updateDataSourceAnbdGetFileSize(cm, audioFileItem);
			int durationSec = calculateDuration(fileSize);
			List<String> currExpectedResult = updateDtoForEachAudio(dirFile,
					audioFileItem, phonemesRange, dtoMap, durationSec);
			LOG.debug("Expected overall words count: {}",
					currExpectedResult.size());
			String resString = performTestEachAudio(recognizer);
			calculateResults(dirFile, phonemesRange, dtoMap, wordsToSpot,
					currExpectedResult, resString);
		} // end of foreach over all wav files
		LOG.debug("Tests count for outOfGrammarProbability: {} ",
				outOfGrammarProbabilityItem);
		List<PhonemeRangeSpottingResult> results = processAllWavResults(
				phonemesRange, dtoMap);
		return results;
	}
	
	/**
	 * 
	 * @param dirFile
	 * @param outOfGrammarProbabilityItem
	 * @return
	 */
	private ConfigurationManager newConfigurationManager(
			File dirFile, String outOfGrammarProbabilityItem) {

		String cfPath = MessageFormat.format("config/{0}/config.xml",
				getLanguage(), outOfGrammarProbabilityItem);
		File cfFile = new File(dirFile, cfPath);
		ConfigurationManager cm = new ConfigurationManager(
				cfFile.getAbsolutePath());
//		KWSFlatLinguist linguist = (KWSFlatLinguist) cm.lookup("FlatLinguist");
		ConfigurationManagerUtils.setProperty(cm, "FlatLinguist", "outOfGrammarProbability", outOfGrammarProbabilityItem); 
//		linguist.set
		return cm;
	}

	/**
	 * 
	 * @param dirFile
	 * @param phonemesRange
	 * @param dtoMap
	 * @param audioFileItem
	 * @param recognizer
	 * @param wordsToSpot
	 * @param currExpectedResult
	 * @return
	 */
	private String performTestEachAudio(Recognizer recognizer) {
		recognizer.allocate();
		Result result = recognizer.recognize();
		String resString = result.getTimedBestResult(false, true);
		LOG.debug("Result: {}", resString);
		recognizer.deallocate();
		return resString;

	}

	/**
	 * 
	 * @param dirFile
	 * @param audioFileItem
	 * @param phonemesRange
	 * @param dtoMap
	 * @param durationSec
	 * @return
	 */
	private List<String> updateDtoForEachAudio(File dirFile,
			String audioFileItem, List<String> phonemesRange,
			Map<String, KeyWordSpottingDto> dtoMap, int durationSec) {
		List<String> currExpectedResult = null;
		for (String phonemesRangeItem : phonemesRange) {
			KeyWordSpottingDto dto = dtoMap.get(phonemesRangeItem);
			dto.setLocalCorrectHits(0);
			dto.setLocalFalseAlarmsCount(0);
			dto.setLocalNoFillerResultsCount(0);
			dto.setLocalExpectedWordsCount(0);

			currExpectedResult = readResultFileContent(dirFile, audioFileItem,
					phonemesRangeItem);
			assertTrue(currExpectedResult != null,
					"Not info found for {0} {1}", audioFileItem,
					phonemesRangeItem);
			dto.setLocalExpectedWordsCount(currExpectedResult.size());
			if (dto.getLocalExpectedWordsCount() == 0) {
				LOG.warn("WARNING: seems empty, skipping this one.");
				continue;
			}

			LOG.debug("localExpectedWordsCount for phonemesRange[{}]: {}",
					phonemesRangeItem, dto.getLocalExpectedWordsCount());
			dto.setTestsCount(dto.getTestsCount() + 1);
			dto.setDurationSecs(durationSec);
		}
		return currExpectedResult;

	}

	private List<String> readResultFileContent(File dirFile,
			String audioFileItem, String phonemesRangeItem) {
		String fileWoExt = Files.getNameWithoutExtension(audioFileItem);
		String defaultPath = MessageFormat.format(
				"expected/{0}/{1}.result.txt", getLanguage(), fileWoExt);
		String phoneRangeSpecificPath = MessageFormat.format(
				"expected/{0}/{1}.{2}.result.txt", getLanguage(), fileWoExt,
				phonemesRangeItem);
		File phoneRangeSpecificFile = new File(dirFile, phoneRangeSpecificPath);
		File deafultFile = new File(dirFile, defaultPath);
		File resultFile = null;
		if (phoneRangeSpecificFile.isFile()) {
			resultFile = phoneRangeSpecificFile; 
		}else{
			if(deafultFile.isFile()){
				resultFile = deafultFile;
			}else{
				LOG.debug(
						"{} does not exist. all audio files must have at least one expected result file: specific phonemes ranges or deault one",
						phoneRangeSpecificPath);
				return null;
			}
		}

		try {
			List<String> resultFileContent = Files.readLines(resultFile,
					Charsets.UTF_8);
			LOG.debug("{} read {} lines", resultFile.getAbsolutePath(), resultFileContent.size());
			return resultFileContent;
		} catch (IOException e) {
			throw new IllegalArgumentException(" Bad thing happend with file: "
					+ phoneRangeSpecificFile.getAbsolutePath(), e);
		}
	}

	/**
	 * 
	 * @param cm
	 * @param audioFileItem
	 * @return
	 */
	private int updateDataSourceAnbdGetFileSize(ConfigurationManager cm,
			String audioFileItem) {
		LOG.debug("[updateDataSourceAnbdGetFileSize]audioFileItem: {}", audioFileItem );
		AudioFileDataSource dataSource = (AudioFileDataSource) cm
				.lookup("audioFileDataSource");
		int fileSize = 0;
		try {
			dataSource.setAudioFile(new URL("file:" + audioFileItem), null);
			fileSize = (int) new File(audioFileItem).length();
		} catch (IOException e) {
			LOG.error("Exception Error: ", e);
		}
		return fileSize;
	}

	/**
	 * @param currExpectedResult
	 * @param wordsToSpot
	 * @param recognizer
	 * @param dtoMap
	 * @param phonemesRange
	 * @param dirFile
	 * @param resString
	 * 
	 */
	private void calculateResults(File dirFile, List<String> phonemesRange,
			Map<String, KeyWordSpottingDto> dtoMap,
			Map<String, List<String>> wordsToSpot,
			List<String> currExpectedResult, String resString) {
		processRecogntonResults(resString, dtoMap, currExpectedResult,
				phonemesRange, wordsToSpot);

		for (String phonemesRangeItem : phonemesRange) {
			KeyWordSpottingDto dto = dtoMap.get(phonemesRangeItem);
			LOG.debug("Collecting local result for {}", phonemesRangeItem);
			if (dto.getLocalExpectedWordsCount() == 0) {
				LOG.debug(
						"No expected results for this file for {}, skipping.",
						phonemesRangeItem);
				continue; // no tests for this range on current audio
							// file, skip results calculation
			}

			dto.setAccuracy(dto.getAccuracy() + 1);
			dto.setLocalFalseAlarmsCount(dto.getLocalNoFillerResultsCount()
					- dto.getLocalCorrectHits()); // the number of
													// results that
			// did not match to expected
			// list
			if (dto.getLocalFalseAlarmsCount() < 0) {
				dto.setLocalFalseAlarmsCount(0);
			}
			dto.setFalseAlarmsCount(dto.getFalseAlarmsCount()
					+ dto.getLocalFalseAlarmsCount());

			double localAcc = (double) dto.getLocalCorrectHits()
					/ dto.getLocalExpectedWordsCount();

			LOG.debug(
					"Overall spotted results({}): count: {}, localCorrectHits: {}, FalseAlarmsCount {}, localAcc {}",
					phonemesRangeItem, dto.getLocalNoFillerResultsCount(),
					dto.getLocalCorrectHits(), dto.getLocalFalseAlarmsCount(),
					localAcc);

		}
	}

	/**
	 * 
	 * @param phonemesRange
	 * @param outOfGrammarProbability
	 * @param dtoMap
	 * @return
	 */
	private List<PhonemeRangeSpottingResult> processAllWavResults(
			List<String> phonemesRange, Map<String, KeyWordSpottingDto> dtoMap) {
		LOG.debug("Final Results after all wav processed");
		List<PhonemeRangeSpottingResult> results = Lists.newArrayList();
		for (String phonemesRangeItem : phonemesRange) {
			PhonemeRangeSpottingResult result = new PhonemeRangeSpottingResult();
			KeyWordSpottingDto dto = dtoMap.get(phonemesRangeItem);
			result.setPhonemesRangeItem(phonemesRangeItem);
			if (dto.getTestsCount() > 0) {
				LOG.debug("\t Tests count for {} for Test count: {} ",
						phonemesRangeItem, dto.getTestsCount());
				dto.setAccuracy(dto.getAccuracy()
						/ (double) dto.getTestsCount());
				double durationHours = (double) dto.getDurationSecs()
						/ (double) 3600.0;
				result.setAccuracy(dto.getAccuracy());
				Double falseAlarmsCountRatio = ((double) dto
						.getFalseAlarmsCount() / durationHours);
				result.setFalseAlarmsCountRatio(falseAlarmsCountRatio);
				;
			} else {
				result.setAccuracy(Double.NaN);
				result.setFalseAlarmsCountRatio(Double.NaN);
			}
			String resultStr = MessageFormat.format(
					"{0}\t{1}\t{2}\n",
					result.getPhonemesRangeItem(),
					result.getAccuracy(),
					result.getFalseAlarmsCountRatio());
			LOG.debug("Result: {}", resultStr);
			results.add(result);
		}
		return results;
	}

	/**
	 * 
	 * @param resString
	 * @param dtoMap
	 * @param currExpectedResult
	 * @param phonemesRange
	 * @param wordsToSpot
	 */
	private void processRecogntonResults(String resString,
			Map<String, KeyWordSpottingDto> dtoMap,
			List<String> currExpectedResult, List<String> phonemesRange,
			Map<String, List<String>> wordsToSpot) {
		Iterable<String> resultTokens = Splitter.on(" ").omitEmptyStrings()
				.trimResults().split(resString);
		for (String currWordToken : resultTokens) {
			Matcher ma = WORD_SPOTED_PATERN.matcher(currWordToken);
			if (!ma.matches()) {
				continue;
			}
			String currWord = ma.group(1).toUpperCase();
			// find appropriate phonemes range
			for (String phonemesRangeItem : phonemesRange) {
				List<String> wordList = wordsToSpot.get(phonemesRangeItem);
				KeyWordSpottingDto dto = dtoMap.get(phonemesRangeItem);
				if (wordList.contains(currWord)) {
					dto.setLocalNoFillerResultsCount(dto
							.getLocalNoFillerResultsCount() + 1);
					LOG.debug(
							"word[{}] noFillerResultsCount {}. known phonemes range word hit, now let's find out if it's  correct hit",
							currWord, dto.getLocalNoFillerResultsCount());
					Iterator<String> resultIterator = currExpectedResult
							.iterator();
					for (; resultIterator.hasNext();) {
						String resultWord = (String) resultIterator.next();
						if (currWord.equals(resultWord)) {
							dto.setLocalCorrectHits(dto.getLocalCorrectHits() + 1);
							LOG.debug(
									"word[{}] LocalCorrectHits {}. remove the match from the list, so we know how many are left (no timing compared, so there can occur some minor inaccuracies, like negative + false positive of the same word in other section would be treated as a good hit, be aware of this fact)",
									currWord, dto.getLocalCorrectHits());
							resultIterator.remove();
							break;
						}
					}
					break;
				}
			}
		}

	}

	/**
	 * 
	 * @param phonemesRange
	 * @param dtoMap
	 * @param wordsToSpot
	 * @param grammar
	 */
	private void createDtos(List<String> phonemesRange,
			Map<String, KeyWordSpottingDto> dtoMap,
			Map<String, List<String>> wordsToSpot, NoSkipGrammar grammar) {
		for (String phonemesRangeItem : phonemesRange) {
			dtoMap.put(phonemesRangeItem, new KeyWordSpottingDto());
			for (String word : wordsToSpot.get(phonemesRangeItem)) {
				grammar.addKeyword(word);

			}
		}
	}

	/**
	 * grammar, since each phonemesRange has its own list of words to spot (to
	 * make these tests reliable, number of words to spot should be the same for
	 * all phonemes lengths)
	 * 
	 * @param cm
	 * @return
	 */
	private NoSkipGrammar extractGrammar(ConfigurationManager cm) {
		return (NoSkipGrammar) cm.lookup("NoSkipGrammar"); // reinitialize

	}

	/**
	 * 32kilobytes = 1 second (calculate summary duration separately for each
	 * phonemes range)
	 * 
	 * @param fileSize
	 * @return
	 */
	private int calculateDuration(int fileSize) {
		return (int) (fileSize / (32 * 1024));
	}

	/**
	 * 
	 * @param outOfGrammarProbability
	 * @param precisionRecallGraph
	 */
	public void saveResults(String dir, List<String> outOfGrammarProbability,
			Map<String, List<PhonemeRangeSpottingResult>> precisionRecallGraph) {
		LOG.debug("Saving results...");
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			throw new IllegalArgumentException("Dir does not existis: "
					+ dirFile.getAbsolutePath());
		}
		String fname = "precision_recall_graph.dat";
		File saveFile = new File(dirFile, fname);
		BufferedWriter writer;
		try {
			writer = Files.newWriter(saveFile, Charsets.UTF_8);
			// StringBuilder globalSb = new StringBuilder();
			for (String outOfGrammarProbabilityItem : outOfGrammarProbability) {
				try {
					for (int i = 0; i < precisionRecallGraph.get(
							outOfGrammarProbabilityItem).size(); i++) {
						List<PhonemeRangeSpottingResult> rangeResult = precisionRecallGraph
								.get(outOfGrammarProbabilityItem);
						for (PhonemeRangeSpottingResult iResult : rangeResult) {
							String resultStr = MessageFormat.format(
									"{0}\t{1}\t{2}\t{3}\n",
									outOfGrammarProbabilityItem,
									iResult.getPhonemesRangeItem(),
									iResult.getAccuracy(),
									iResult.getFalseAlarmsCountRatio());
							LOG.debug("Result: {}", resultStr);
							writer.write(resultStr);
						}
					}
				} catch (IOException e) {
					LOG.error("Bad thing happend with IO: ", e);
				}
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOG.debug("OK.");

	}

	private void assertTrue(boolean val, String msg, Object... args) {
		if (val == false) {
			throw new IllegalArgumentException(MessageFormat.format(msg, args));
		}
	}

	public String getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return "KeyWordSpottingServiceImpl [language=" + language + "]";
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
