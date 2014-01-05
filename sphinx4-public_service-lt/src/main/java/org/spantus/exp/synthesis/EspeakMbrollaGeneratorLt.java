package org.spantus.exp.synthesis;

import java.io.IOException;
import java.text.MessageFormat;

public class EspeakMbrollaGeneratorLt {

	public static void main(String[] args) {
		EspeakMbrollaGeneratorLt generator = new EspeakMbrollaGeneratorLt();
		generator.speak("Labas, kaip tau sekas?");
	}

	public void speak(String text) {

		String command = MessageFormat.format("tark-lt {0}", text);
		try {

			// write stdin
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (InterruptedException e) {
			throw new IllegalArgumentException(e);
		}

	}
}
