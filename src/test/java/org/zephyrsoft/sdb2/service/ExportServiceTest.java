/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.zephyrsoft.sdb2.model.ExportFormat;
import org.zephyrsoft.sdb2.model.Song;

import com.google.common.collect.Lists;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

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
		song1.setLyrics("A     B          X\nLyrics of Song 1\n[Translation of Song 1]");
		song2.setLyrics("[Intro of a part of Song 2]\nA     B          Y\nLyrics of Song 2");
		song3.setLyrics("Lyrics of Song 3");
		songs = Lists.newArrayList(song1, song2, song3);
	}
	
	@Test
	public void exportAll() throws Exception {
		ByteArrayOutputStream exported = exportService.export(new ExportFormat(true, true, false), songs);
		
		PdfReader reader = new PdfReader(exported.toByteArray());
		assertEquals(reader.getNumberOfPages(), 4);
		
		String page1 = PdfTextExtractor.getTextFromPage(reader, 1);
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertTrue(page1.contains("Translation of Song 1"));
		assertTrue(page1.matches("(?s)^.*A +B +X.*$"));
		
		String page2 = PdfTextExtractor.getTextFromPage(reader, 2);
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertTrue(page2.contains("Intro of a part of Song 2"));
		assertTrue(page2.matches("(?s)^.*A +B +Y.*$"));
		
		String page3 = PdfTextExtractor.getTextFromPage(reader, 3);
		assertTrue(page3.contains("Lyrics of Song 3"));
	}
	
	@Test
	public void exportOnlyWithChords() throws Exception {
		ByteArrayOutputStream exported = exportService.export(new ExportFormat(true, true, true), songs);
		
		PdfReader reader = new PdfReader(exported.toByteArray());
		assertEquals(reader.getNumberOfPages(), 3);
		
		String page1 = PdfTextExtractor.getTextFromPage(reader, 1);
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertTrue(page1.contains("Translation of Song 1"));
		assertTrue(page1.matches("(?s)^.*A +B +X.*$"));
		
		String page2 = PdfTextExtractor.getTextFromPage(reader, 2);
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertTrue(page2.contains("Intro of a part of Song 2"));
		assertTrue(page2.matches("(?s)^.*A +B +Y.*$"));
	}
	
	@Test
	public void exportWithoutChords() throws Exception {
		ByteArrayOutputStream exported = exportService.export(new ExportFormat(true, false, false), songs);
		
		PdfReader reader = new PdfReader(exported.toByteArray());
		assertEquals(reader.getNumberOfPages(), 4);
		
		String page1 = PdfTextExtractor.getTextFromPage(reader, 1);
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertTrue(page1.contains("Translation of Song 1"));
		assertFalse(page1.matches("(?s)^.*A +B +X.*$"));
		
		String page2 = PdfTextExtractor.getTextFromPage(reader, 2);
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertTrue(page2.contains("Intro of a part of Song 2"));
		assertFalse(page2.matches("(?s)^.*A +B +Y.*$"));
		
		String page3 = PdfTextExtractor.getTextFromPage(reader, 3);
		assertTrue(page3.contains("Lyrics of Song 3"));
	}
	
	@Test
	public void exportWithoutChordsAndTranslation() throws Exception {
		ByteArrayOutputStream exported = exportService.export(new ExportFormat(false, false, false), songs);
		
		PdfReader reader = new PdfReader(exported.toByteArray());
		assertEquals(reader.getNumberOfPages(), 4);
		
		String page1 = PdfTextExtractor.getTextFromPage(reader, 1);
		assertTrue(page1.contains("Lyrics of Song 1"));
		assertFalse(page1.contains("Translation of Song 1"));
		assertFalse(page1.matches("(?s)^.*A +B +X.*$"));
		
		String page2 = PdfTextExtractor.getTextFromPage(reader, 2);
		assertTrue(page2.contains("Lyrics of Song 2"));
		assertFalse(page2.contains("Intro of a part of Song 2"));
		assertFalse(page2.matches("(?s)^.*A +B +Y.*$"));
		
		String page3 = PdfTextExtractor.getTextFromPage(reader, 3);
		assertTrue(page3.contains("Lyrics of Song 3"));
	}
	
}
