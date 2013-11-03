package org.spantus.sphinx.kws.dto;

public class LocalKeyWordSpottingDto {
	/**
	 *  count, accuracy, false alarms
	 */
	int localExpectedWordsCount = 0;
	int noFillerResultsCount = 0;
//	double accuracy = 0;
	int localCorrectHits = 0;
//	int falseAlarmsCount = 0;
	int localFalseAlarmsCount = 0;
	
	public LocalKeyWordSpottingDto() {
	}
	
}