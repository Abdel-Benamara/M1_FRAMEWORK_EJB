package fr.pantheonsorbonne.ufr27.miage.main;

import java.time.LocalDateTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

public class trainsLauncher {
	public static void main(String[] args) throws DatatypeConfigurationException {
		int nbtrain = 9;

		for (int idTrain = 0; idTrain < nbtrain; idTrain++) {
			Train t = new Train(idTrain);
			t.run();
		}
	}
}
