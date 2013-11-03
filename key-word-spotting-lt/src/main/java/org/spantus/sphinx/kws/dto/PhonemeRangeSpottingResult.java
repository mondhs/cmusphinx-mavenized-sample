package org.spantus.sphinx.kws.dto;

public final class PhonemeRangeSpottingResult{
	String phonemesRangeItem;
	Double accuracy; 
	Double falseAlarmsCountRatio;
	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}
	public Double getFalseAlarmsCountRatio() {
		return falseAlarmsCountRatio;
	}
	public void setFalseAlarmsCountRatio(Double falseAlarmsCountRatio) {
		this.falseAlarmsCountRatio = falseAlarmsCountRatio;
	}
	public String getPhonemesRangeItem() {
		return phonemesRangeItem;
	}
	public void setPhonemesRangeItem(String phonemesRangeItem) {
		this.phonemesRangeItem = phonemesRangeItem;
	}
}