package org.spantus.sphinx.kws.dto;

public class KeyWordSpottingDto {
	/**
	 *  count, accuracy, false alarms
	 */
	int testsCount = 0;
	int localExpectedWordsCount = 0;
	int localNoFillerResultsCount = 0;
	double accuracy = 0;
	int localCorrectHits = 0;
	int falseAlarmsCount = 0;
	int localFalseAlarmsCount = 0;
	
	public KeyWordSpottingDto() {
		setTestsCount(0);
		setFalseAlarmsCount(0);
		setAccuracy(0d);
		setDurationSecs(0);
	}
	
	/**
	 * summary duration in milliseconds
	 */
	double durationSecs = 0;
	public int getTestsCount() {
		return testsCount;
	}
	public void setTestsCount(int testsCount) {
		this.testsCount = testsCount;
	}
	public int getLocalExpectedWordsCount() {
		return localExpectedWordsCount;
	}
	public void setLocalExpectedWordsCount(int localExpectedWordsCount) {
		this.localExpectedWordsCount = localExpectedWordsCount;
	}
	public int getLocalNoFillerResultsCount() {
		return localNoFillerResultsCount;
	}
	public void setLocalNoFillerResultsCount(int noFillerResultsCount) {
		this.localNoFillerResultsCount = noFillerResultsCount;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public int getLocalCorrectHits() {
		return localCorrectHits;
	}
	public void setLocalCorrectHits(int localCorrectHits) {
		this.localCorrectHits = localCorrectHits;
	}
	public int getFalseAlarmsCount() {
		return falseAlarmsCount;
	}
	public void setFalseAlarmsCount(int falseAlarmsCount) {
		this.falseAlarmsCount = falseAlarmsCount;
	}
	public int getLocalFalseAlarmsCount() {
		return localFalseAlarmsCount;
	}
	public void setLocalFalseAlarmsCount(int localFalseAlarmsCount) {
		this.localFalseAlarmsCount = localFalseAlarmsCount;
	}
	public double getDurationSecs() {
		return durationSecs;
	}
	public void setDurationSecs(double durationSecs) {
		this.durationSecs = durationSecs;
	}
}
