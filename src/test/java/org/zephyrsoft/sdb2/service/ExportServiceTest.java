/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License 3.0 as published by
 * the Free Software Foundation.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License 3.0 for more details.
 *
 * You should have received a copy of the GNU General Public License 3.0
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.zephyrsoft.sdb2.model.ExportFormat;
import org.zephyrsoft.sdb2.model.Song;

import com.google.common.collect.Lists;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

public class ExportServiceTest {
	
	private ExportService exportService;
	private List<Song> songs;
	
	@Before
	public void setup() throws Exception {
		exportService = new ExportService();
		Song song1 = new Song("abcde-10000");
		Song song2 = new Song("abcde-20000");
		Song song3 = new Song("abcde-30000");
		song1.setTitle("Test-Song 1");
		song2.setTitle("Test-Song 2");
		song3.setTitle("Test-Song 3");
		song1.setLyrics("""
			A     B          X
			Lyrics of Song 1
			[Translation of Song 1]""");
		song2.setLyrics("""
			[Intro of a part of Song 2]
			A     B          Y
			Lyrics of Song 2""");
		song3.setLyrics("""
			Lyrics of Song 3
			
			Second paragraph of Song 3""");
		songs = Lists.newArrayList(song1, song2, song3);
	}
	
	@Test
	public void exportAll() throws Exception {
		byte[] exported = exportService.export(new ExportFormat(true, true, false), songs);
		
		PdfDocument document = new PdfDocument(new PdfReader(new ByteArrayInputStream(exported)));
		assertEquals(4, document.getNumberOfPages());
		
		String page1 = PdfTextExtractor.getTextFromPage(document.getPage(1));
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertTrue(page1.contains("Translation of Song 1"));
		assertTrue(page1.matches("(?s)^.*A +B +X.*$"));
		assertTrue(page1.contains("- 1 -"));
		
		String page2 = PdfTextExtractor.getTextFromPage(document.getPage(2));
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertTrue(page2.contains("Intro of a part of Song 2"));
		assertTrue(page2.matches("(?s)^.*A +B +Y.*$"));
		assertTrue(page2.contains("- 2 -"));
		
		String page3 = PdfTextExtractor.getTextFromPage(document.getPage(3));
		assertTrue(page3.contains("Lyrics of Song 3"));
		assertTrue(page3.contains("- 3 -"));
	}
	
	@Test
	public void exportOnlyWithChords() throws Exception {
		byte[] exported = exportService.export(new ExportFormat(true, true, true), songs);
		
		PdfDocument document = new PdfDocument(new PdfReader(new ByteArrayInputStream(exported)));
		assertEquals(3, document.getNumberOfPages());
		
		String page1 = PdfTextExtractor.getTextFromPage(document.getPage(1));
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertTrue(page1.contains("Translation of Song 1"));
		assertTrue(page1.matches("(?s)^.*A +B +X.*$"));
		assertTrue(page1.contains("- 1 -"));
		
		String page2 = PdfTextExtractor.getTextFromPage(document.getPage(2));
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertTrue(page2.contains("Intro of a part of Song 2"));
		assertTrue(page2.matches("(?s)^.*A +B +Y.*$"));
		assertTrue(page2.contains("- 2 -"));
	}
	
	@Test
	public void exportWithoutChords() throws Exception {
		byte[] exported = exportService.export(new ExportFormat(true, false, false), songs);
		
		PdfDocument document = new PdfDocument(new PdfReader(new ByteArrayInputStream(exported)));
		assertEquals(4, document.getNumberOfPages());
		
		String page1 = PdfTextExtractor.getTextFromPage(document.getPage(1));
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertTrue(page1.contains("Translation of Song 1"));
		assertFalse(page1.matches("(?s)^.*A +B +X.*$"));
		assertTrue(page1.contains("- 1 -"));
		
		String page2 = PdfTextExtractor.getTextFromPage(document.getPage(2));
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertTrue(page2.contains("Intro of a part of Song 2"));
		assertFalse(page2.matches("(?s)^.*A +B +Y.*$"));
		assertTrue(page2.contains("- 2 -"));
		
		String page3 = PdfTextExtractor.getTextFromPage(document.getPage(3));
		assertTrue(page3.contains("Lyrics of Song 3"));
		assertTrue(page3.contains("- 3 -"));
	}
	
	@Test
	public void exportWithoutChordsAndTranslation() throws Exception {
		byte[] exported = exportService.export(new ExportFormat(false, false, false), songs);
		
		PdfDocument document = new PdfDocument(new PdfReader(new ByteArrayInputStream(exported)));
		assertEquals(4, document.getNumberOfPages());
		
		String page1 = PdfTextExtractor.getTextFromPage(document.getPage(1));
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertFalse(page1.contains("Translation of Song 1"));
		assertFalse(page1.matches("(?s)^.*A +B +X.*$"));
		assertTrue(page1.contains("- 1 -"));
		
		String page2 = PdfTextExtractor.getTextFromPage(document.getPage(2));
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertFalse(page2.contains("Intro of a part of Song 2"));
		assertFalse(page2.matches("(?s)^.*A +B +Y.*$"));
		assertTrue(page2.contains("- 2 -"));
		
		String page3 = PdfTextExtractor.getTextFromPage(document.getPage(3));
		assertTrue(page3.contains("Lyrics of Song 3"));
		assertTrue(page3.contains("- 3 -"));
	}
	
}
