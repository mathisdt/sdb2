package org.zephyrsoft.sdb2.service;

import org.junit.Test;

public class ExportServiceTest {
	
	private static final String CHORDS_INPUT = "A       B            C            D                    E               F     A     F A";
	private static final String TEXT_INPUT = "This is a Test which only should demonstrate that the service works as expected.";
	
	private ExportService exportService = new ExportService();
	
	@Test
	public void correctChordSpaces() {
		String result = exportService.correctChordSpaces(CHORDS_INPUT, TEXT_INPUT);
		
		System.out.println(CHORDS_INPUT);
		System.out.println(result);
	}
	
}
