
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;


/**
 * Pavyzdinė klasė
 */
public class RobotoValdymas{
	/**
	 * Pagrindinis metodas kiečiamas OS.
	 */
    public static void main(String[] args) {
        //konfigūracijos tvarkytojas
        ConfigurationManager konfiguracija = new ConfigurationManager(RobotoValdymas.class.getResource("lt_robotas.config.xml"));
		
        Recognizer atapzintuvas = (Recognizer) konfiguracija.lookup("recognizer");
        atapzintuvas.allocate();

        // pradėti įrašą per mikrofoną, tam nepavykus baigti programą
        Microphone mikrofonas = (Microphone) konfiguracija.lookup("microphone");
        if (!mikrofonas.startRecording()) {
            System.out.println("Negaliu pasiekti mikrofono.");
            atapzintuvas.deallocate();
            System.exit(1);
        }

        System.out.println("Sakyk: (EIK | VARYK ) [ ( VIENĄ | DU | TRIS | KETURIS | PENKIS ) METRUS ] (PIRMYN | ATGAL) arba (SUK | GRĘŽKIS ) ( KAIRĖN | DEŠINĖN )");

        // Sukti amžiną ciklą atpažinimui
        while (true) {
            System.out.println("Pradėk šnekėti. spausk Ctrl-C nutraukti.\n");

            Result rezultatas = atapzintuvas.recognize();

            if (rezultatas != null) {
                String rezultatoTekstas = rezultatas.getBestFinalResultNoFiller();
                System.out.println("tu sakei: " + rezultatoTekstas + '\n');
            } else {
                System.out.println("Neišgirdau ką pasakėte.\n");
            }
        }
    }
}
