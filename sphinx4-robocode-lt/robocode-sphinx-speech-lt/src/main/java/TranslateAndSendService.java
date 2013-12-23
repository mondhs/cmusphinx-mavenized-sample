import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class TranslateAndSendService {

	public void found(String message) {
		String command = translate(message.toUpperCase());
		send(command);
	}

	private String translate(String message) {
		StringBuilder command = new StringBuilder();
		Iterable<String> wordSequence = Splitter.on(CharMatcher.BREAKING_WHITESPACE).omitEmptyStrings().trimResults().split(message);
		LinkedHashSet<String> wordSet = Sets.newLinkedHashSet(wordSequence);
		String separator = "";
		if(wordSet.contains("VAŽIUOK")||wordSet.contains("VARYK")){
			separator = append(command, separator, "GO");
			if(wordSet.contains("PIRMYN")){
				separator = append(command, separator, "FORWARD");
			}else if(wordSet.contains("ATGAL")){
				separator = append(command, separator, "BACKWARD");
			}
			
			if(wordSet.contains("VIENĄ")){
				separator = append(command, separator, "ONE STEP");
			}else if(wordSet.contains("DU")){
				separator = append(command, separator, "TWO STEPS");
			}else if(wordSet.contains("TRIS")){
				separator = append(command, separator, "THREE STEPS");
			}else if(wordSet.contains("KETURIS")){
				separator = append(command, separator, "FOUR STEPS");
			}else if(wordSet.contains("PENKIS")){
				separator = append(command, separator, "FIVE STEPS");
			}
		}else if(wordSet.contains("SUK")||wordSet.contains("GRĘŽKIS")){
			separator = append(command, separator, "TURN");
			if(wordSet.contains("KAIRĖN")){
				separator = append(command, separator, "LEFT");
			}else if(wordSet.contains("DEŠINĖN")){
				separator = append(command, separator, "RIGHT");
			}
			if(wordSet.contains("TREČIA")){
				separator = append(command, separator, "THREE OCLOCK");
			}else if(wordSet.contains("ŠEŠTA")){
				separator = append(command, separator, "SIX OCLOCK");
			}else if(wordSet.contains("DEVINTA")){
				separator = append(command, separator, "NINE OCLOCK");
			}else if(wordSet.contains("DVYLIKTA")){
				separator = append(command, separator, "TWELVE OCLOCK");
			}
		}else if(wordSet.contains("STEBĖK")){
			separator = append(command, separator, "ENEMY ");
			if(wordSet.contains("TREČIA")){
				separator = append(command, separator, "THREE OCLOCK");
			}else if(wordSet.contains("ŠEŠTA")){
				separator = append(command, separator, "SIX OCLOCK");
			}else if(wordSet.contains("DEVINTA")){
				separator = append(command, separator, "NINE OCLOCK");
			}else if(wordSet.contains("DVYLIKTA")){
				separator = append(command, separator, "TWELVE OCLOCK");
			}
		} else if(wordSet.contains("UGNIS")){
			separator = append(command, separator, "FIRE");
		} else if(wordSet.contains("STOK")){
			separator = append(command, separator, "STOP");
		}
		return command.toString();
	}

	private String append(StringBuilder command, String separator, String instruction) {
		command.append(separator).append(instruction);
		separator = " ";
		return separator;
	}

	protected void send(String command) {
		System.out.println("Sending: " + command);
		try {
			Socket s = new Socket("127.0.0.1", 9090);
			BufferedWriter bufOut = new BufferedWriter(new OutputStreamWriter(
					s.getOutputStream()));
			bufOut.write(command);
			bufOut.flush();
			bufOut.close();
			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
