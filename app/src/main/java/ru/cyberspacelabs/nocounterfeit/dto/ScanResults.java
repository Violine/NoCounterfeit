package ru.cyberspacelabs.nocounterfeit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 23.03.17.
 */
public class ScanResults {
	private List<ScanResult> history;

	public ScanResults() {
		history = new ArrayList<>();
	}

	public List<ScanResult> getHistory() {
		return history;
	}

	public void setHistory(List<ScanResult> history) {
		this.history = history;
	}
}
