import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.spantus.extr.wordspot.sphinx.linguist.dictionary.KeyworGeneratedDictionary;

import edu.cmu.sphinx.decoder.Decoder;
import edu.cmu.sphinx.decoder.pruner.SimplePruner;
import edu.cmu.sphinx.decoder.scorer.ThreadedAcousticScorer;
import edu.cmu.sphinx.decoder.search.PartitionActiveListFactory;
import edu.cmu.sphinx.decoder.search.SimpleBreadthFirstSearchManager;
import edu.cmu.sphinx.frontend.DataBlocker;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.endpoint.NonSpeechDataFilter;
import edu.cmu.sphinx.frontend.endpoint.SpeechClassifier;
import edu.cmu.sphinx.frontend.endpoint.SpeechMarker;
import edu.cmu.sphinx.frontend.feature.BatchCMN;
import edu.cmu.sphinx.frontend.feature.DeltasFeatureExtractor;
import edu.cmu.sphinx.frontend.filter.Preemphasizer;
import edu.cmu.sphinx.frontend.frequencywarp.MelFrequencyFilterBank;
import edu.cmu.sphinx.frontend.transform.DiscreteCosineTransform;
import edu.cmu.sphinx.frontend.transform.DiscreteFourierTransform;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.frontend.window.RaisedCosineWindower;
import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.linguist.acoustic.UnitManager;
import edu.cmu.sphinx.linguist.acoustic.tiedstate.Sphinx3Loader;
import edu.cmu.sphinx.linguist.acoustic.tiedstate.TiedStateAcousticModel;
import edu.cmu.sphinx.linguist.flat.FlatLinguist;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.LogMath;
import edu.cmu.sphinx.util.props.ConfigurationManager;

/**
 * Command recorder and dispatcher robocode class.
 */
public class RobocodeSpeechServiceImpl {

	// VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
	/**
	 * Main class that will be invoced by OS
	 */
	public static void main(String[] args) {
		RobocodeSpeechServiceImpl robotSpeechServer = new RobocodeSpeechServiceImpl();
		robotSpeechServer.lazyInit();
		robotSpeechServer.listenAndRecognize();
	}

	// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

	private TranslateAndSendService translateAndSendService;
	/**
	 * konfigūracijos tvarkytojas
	 */
	private ConfigurationManager configuration;

	/**
	 * Argumentų inicializacija
	 */
	public void lazyInit() {
		configuration = newConfigurationManager();
		translateAndSendService = new TranslateAndSendService();
	}

	/**
	 * 
	 */
	public void listenAndRecognize() {
		Recognizer recognizer = (Recognizer) configuration.lookup("recognizer");
		recognizer.allocate();
		// start recording using microphone, if it is not found abort program
		Microphone microphone = (Microphone) configuration.lookup("microphone");
		if (!microphone.startRecording()) {
			System.out.println("microphone is not accessable.");
			recognizer.deallocate();
			System.exit(1);
		}

		System.out.println("say what to do.");

		// Sukti amžiną ciklą atpažinimui
		while (true) {
			System.out.println("Start speeking. click Ctrl-C to exit.\n");

			Result resultat = recognizer.recognize();

			if (resultat != null) {
				String bestResult = resultat.getBestFinalResultNoFiller();
				found(bestResult);
			} else {
				System.out.println("I cannot get you.\n");
			}
		}
	}

	protected void found(String bestResult) {
		translateAndSendService.found(bestResult);
		System.out.println("You said: " + bestResult + '\n');
	}

	public ConfigurationManager newConfigurationManager() {
		ConfigurationManager cm = new ConfigurationManager();

		Map<String, Object> logMathMap = new HashMap<String, Object>();
		logMathMap.put("logBase", 1.0001);
		logMathMap.put("useAddTable", Boolean.TRUE);
		cm.addConfigurable(LogMath.class, "logMath", logMathMap);

		// /pipeline

		Map<String, Object> microphoneMap = new HashMap<String, Object>();
		microphoneMap.put("closeBetweenUtterances", "false");
		cm.addConfigurable(Microphone.class, "microphone", microphoneMap);
		cm.addConfigurable(DataBlocker.class, "dataBlocker");
		cm.addConfigurable(SpeechClassifier.class, "speechClassifier");
		cm.addConfigurable(NonSpeechDataFilter.class, "nonSpeechDataFilter");
		cm.addConfigurable(SpeechMarker.class, "speechMarker");
		cm.addConfigurable(Preemphasizer.class, "preemphasizer",
				new HashMap<String, Object>());
		cm.addConfigurable(RaisedCosineWindower.class, "windower",
				new HashMap<String, Object>());
		cm.addConfigurable(DiscreteFourierTransform.class, "fft",
				new HashMap<String, Object>());
		cm.addConfigurable(MelFrequencyFilterBank.class, "melFilterBank",
				new HashMap<String, Object>());
		cm.addConfigurable(DiscreteCosineTransform.class, "dct",
				new HashMap<String, Object>());
		cm.addConfigurable(BatchCMN.class, "batchCMN",
				new HashMap<String, Object>());
		cm.addConfigurable(DeltasFeatureExtractor.class, "featureExtraction",
				new HashMap<String, Object>());

		// /\pipeline

		Map<String, Object> unitManagerMap = new HashMap<String, Object>();
		cm.addConfigurable(UnitManager.class, "unitManager", unitManagerMap);

		Map<String, Object> dictionaryMap = new HashMap<String, Object>();
		dictionaryMap.put("dictionaryPath",
				"resource:/lt.cd_cont_200/dict/robot.dict");
		dictionaryMap.put("fillerPath", "resource:/lt.cd_cont_200/noisedict");
		dictionaryMap.put("unitManager", "unitManager");
		cm.addConfigurable(KeyworGeneratedDictionary.class, "dictionary",
				dictionaryMap);

		Map<String, Object> grammarMap = new HashMap<String, Object>();
		grammarMap.put("logMath", "logMath");
		grammarMap.put("dictionary", "dictionary");
		grammarMap.put("grammarLocation", "resource:/");
		grammarMap.put("grammarName", "lt_robotas");
		grammarMap.put("addSilenceWords", "true");
		cm.addConfigurable(JSGFGrammar.class, "jsgfGrammar", grammarMap);

		Map<String, Object> wsjLoaderMap = new HashMap<String, Object>();
		wsjLoaderMap.put("logMath", "logMath");
		wsjLoaderMap.put("unitManager", "unitManager");
		wsjLoaderMap.put("location", "resource:/lt.cd_cont_200");
		cm.addConfigurable(Sphinx3Loader.class, "wsjLoader", wsjLoaderMap);

		Map<String, Object> wsjMap = new HashMap<String, Object>();
		wsjMap.put("loader", "wsjLoader");
		wsjMap.put("unitManager", "unitManager");
		cm.addConfigurable(TiedStateAcousticModel.class, "wsj", wsjMap);

		Map<String, Object> flatLinguistMap = new HashMap<String, Object>();
		flatLinguistMap.put("logMath", "logMath");
		flatLinguistMap.put("grammar", "jsgfGrammar");
		flatLinguistMap.put("acousticModel", "wsj");
		flatLinguistMap.put("wordInsertionProbability", "0.1");
		flatLinguistMap.put("languageWeight", "8");
		flatLinguistMap.put("unitManager", "unitManager");
		cm.addConfigurable(FlatLinguist.class, "flatLinguist", flatLinguistMap);

		cm.addConfigurable(SimplePruner.class, "trivialPruner");

		Map<String, Object> epFrontEndMap = new HashMap<String, Object>();
		ArrayList<String> pipeline = new ArrayList<String>();
		pipeline.addAll(Arrays.asList("microphone", "dataBlocker",
				"speechClassifier", "speechMarker", "nonSpeechDataFilter",
				"preemphasizer", "windower", "fft", "melFilterBank", "dct",
				"batchCMN", "featureExtraction"));
		epFrontEndMap.put("pipeline", pipeline);
		cm.addConfigurable(FrontEnd.class, "epFrontEnd", epFrontEndMap);

		Map<String, Object> threadedScorerMap = new HashMap<String, Object>();
		threadedScorerMap.put("frontend", "epFrontEnd");
		cm.addConfigurable(ThreadedAcousticScorer.class, "threadedScorer",
				threadedScorerMap);

		Map<String, Object> activeListMap = new HashMap<String, Object>();
		activeListMap.put("logMath", "logMath");
		activeListMap.put("absoluteBeamWidth", "-1");
		activeListMap.put("relativeBeamWidth", "1E-80");
		cm.addConfigurable(PartitionActiveListFactory.class, "activeList",
				activeListMap);

		Map<String, Object> searchManagerMap = new HashMap<String, Object>();
		searchManagerMap.put("logMath", "logMath");
		searchManagerMap.put("linguist", "flatLinguist");
		searchManagerMap.put("pruner", "trivialPruner");
		searchManagerMap.put("scorer", "threadedScorer");
		searchManagerMap.put("activeListFactory", "activeList");
		cm.addConfigurable(SimpleBreadthFirstSearchManager.class,
				"searchManager", searchManagerMap);

		Map<String, Object> decoderMap = new HashMap<String, Object>();
		decoderMap.put("searchManager", "searchManager");
		cm.addConfigurable(Decoder.class, "decoder", decoderMap);

		Map<String, Object> recognizerMap = new HashMap<String, Object>();
		recognizerMap.put("decoder", "decoder");
		cm.addConfigurable(Recognizer.class, "recognizer", recognizerMap);
		return cm;
	}
}
