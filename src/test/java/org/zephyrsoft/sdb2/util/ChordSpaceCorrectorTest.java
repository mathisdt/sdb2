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
package org.zephyrsoft.sdb2.util;

import static org.junit.Assert.assertEquals;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;

public class ChordSpaceCorrectorTest {
	
	private static final String CHORDS_INPUT = "A       B            C            D                    Em              F     A     F A";
	private static final String TEXT_INPUT = "This is a Test which only should demonstrate that the service works as expected.";
	private static final String EXPECTED_OUTPUT_PDF = "A        B                   C                   D                              Em                    F         A  F A";
	private static final String EXPECTED_OUTPUT_PRESENTATION = "A        B                   C                   D                                Em                    F         A  F A";
	
	@Test
	public void correctChordSpacesForPdf() throws Exception {
		PdfFont baseFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_EMBEDDED);
		ChordSpaceCorrector chordSpaceCorrector = new ChordSpaceCorrector(
			text -> (int) baseFont.getWidth(text, 12));
		
		String result = chordSpaceCorrector.correctChordSpaces(CHORDS_INPUT, TEXT_INPUT);
		assertEquals(EXPECTED_OUTPUT_PDF, result);
	}
	
	@Test
	public void correctChordSpacesForPresentation() {
		java.awt.Font lyricsFont = new java.awt.Font("Dialog", java.awt.Font.BOLD + java.awt.Font.ITALIC, 36);
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		FontMetrics fm = g.getFontMetrics(lyricsFont);
		
		ChordSpaceCorrector chordSpaceCorrector = new ChordSpaceCorrector(text -> fm.stringWidth(text));
		String result = chordSpaceCorrector.correctChordSpaces(CHORDS_INPUT, TEXT_INPUT);
		assertEquals(EXPECTED_OUTPUT_PRESENTATION, result);
	}
	
}
