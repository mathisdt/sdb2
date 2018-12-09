package org.zephyrsoft.sdb2.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExportServiceTest {
	
	private static final String CHORDS_INPUT = "A       B            C            D                    Em              F     A     F A";
	private static final String TEXT_INPUT = "This is a Test which only should demonstrate that the service works as expected.";
	private static final String EXPECTED_OUTPUT = "A        B                   C                   D                              Em                    F        A   F A";
	
	private ExportService exportService = new ExportService();
	
	@Test
	public void correctChordSpaces() {
		String result = exportService.correctChordSpaces(CHORDS_INPUT, TEXT_INPUT);
		assertEquals(EXPECTED_OUTPUT, result);
	}
	
}
