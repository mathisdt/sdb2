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

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.ExportFormat;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongParser;
import org.zephyrsoft.sdb2.util.ChordSpaceCorrector;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

/**
 * Exports songs in various formats.
 *
 * @author Mathis Dirksen-Thedens
 */
public class ExportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);
	
	private class PageNumbers extends PdfPageEventHelper {
		private Font footerFont = new Font(Font.FontFamily.UNDEFINED, 10, Font.ITALIC);
		
		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			PdfContentByte cb = writer.getDirectContent();
			Phrase footer = new Phrase("- " + document.getPageNumber() + " -", footerFont);
			ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
				footer,
				(document.right() - document.left()) / 2 + document.leftMargin(),
				document.bottom() - 10, 0);
		}
	}
	
	public class TOC extends PdfPageEventHelper {
		
		protected int counter = 0;
		protected List<SimpleEntry<String, SimpleEntry<String, Integer>>> toc = new ArrayList<>();
		
		@Override
		public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
			String name = "dest" + (counter++);
			int page = writer.getPageNumber();
			toc.add(new SimpleEntry<>(text, new SimpleEntry<>(name, page)));
			writer.addNamedDestination(name, page, new PdfDestination(PdfDestination.FITH, rect.getTop()));
		}
		
		public List<SimpleEntry<String, SimpleEntry<String, Integer>>> getTOC() {
			return toc;
		}
	}
	
	private Font titleFont;
	private Font lyricsFont;
	private Font translationFont;
	private Font copyrightFont;
	private ChordSpaceCorrector chordSpaceCorrector;
	
	public ExportService() throws Exception {
		BaseFont baseFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);
		titleFont = new Font(baseFont, 20, Font.BOLD);
		lyricsFont = new Font(baseFont, 12);
		translationFont = new Font(baseFont, 8, Font.ITALIC);
		copyrightFont = new Font(baseFont, 10);
		chordSpaceCorrector = new ChordSpaceCorrector(
			text -> (int) lyricsFont.getBaseFont().getWidthPointKerned(text, lyricsFont.getSize()));
	}
	
	public ByteArrayOutputStream export(ExportFormat exportFormat, Collection<Song> songs) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			TOC toc = new TOC();
			writer.setPageEvent(toc);
			writer.setPageEvent(new PageNumbers());
			document.setMargins(50, 50, 30, 30);
			document.open();
			
			for (Song song : songs) {
				List<SongElement> songElements = SongParser.parse(song, true, true, true);
				
				if (exportFormat.onlySongsWithChords()
					&& songElements.stream().noneMatch(e -> e.getType() == SongElementEnum.CHORDS)) {
					continue;
				}
				
				SongElement previousSongElement = null;
				SongElement previousSongElementForNewLines = null;
				for (SongElement songElement : songElements) {
					// TODO use a Map containing handlers for each type instead of switch!?
					switch (songElement.getType()) {
						case TITLE:
							Chunk chunk = new Chunk(song.getTitle() + "\n");
							chunk.setGenericTag(song.getTitle());
							Paragraph paragraph = paragraph(titleFont);
							paragraph.add(chunk);
							document.add(paragraph);
							
							// insert chord sequence directly after title
							if (exportFormat.areChordsShown() && StringUtils.isNotBlank(getCleanChordSequence(song))) {
								Paragraph chordSequence = paragraph(getCleanChordSequence(song) + "\n\n", lyricsFont);
								chordSequence.setIndentationLeft(30);
								document.add(chordSequence);
							}
							
							break;
						case LYRICS:
							String chordsLine = "";
							if (previousSongElement != null && previousSongElement.getType() == SongElementEnum.CHORDS
								&& exportFormat.areChordsShown()) {
								chordsLine = chordSpaceCorrector.correctChordSpaces(previousSongElement.getContent(), songElement.getContent())
									+ "\n";
							}
							document.add(paragraph(chordsLine + songElement.getContent(), lyricsFont));
							break;
						case TRANSLATION:
							if (exportFormat.isTranslationShown()) {
								document.add(paragraph(songElement.getContent(), translationFont));
							}
							break;
						case COPYRIGHT:
							Paragraph copyright = paragraph(songElement.getContent(), copyrightFont);
							if (previousSongElement != null && previousSongElement.getType() != SongElementEnum.COPYRIGHT) {
								copyright.setSpacingBefore(20);
							}
							document.add(copyright);
							break;
						case NEW_LINE:
							// ignored for export when coming alone, but
							// when there are two NEW_LINE elements in a row, we add a blank line
							if (previousSongElementForNewLines != null
								&& previousSongElementForNewLines.getType() == SongElementEnum.NEW_LINE) {
								document.add(paragraph("\n", lyricsFont));
							}
							break;
						case CHORDS:
							// handled by following LYRICS element
							break;
						default:
							throw new IllegalStateException("unsupported song element type");
					}
					
					if (isBodyElement(songElement) && !songElement.isEmpty()) {
						previousSongElementForNewLines = songElement;
					}
					if (songElement.getType() != SongElementEnum.NEW_LINE) {
						previousSongElement = songElement;
					}
				}
				
				document.newPage();
			}
			
			// output TOC
			document.add(paragraph("Table of Contents", titleFont));
			Chunk dottedLine = new Chunk(new DottedLineSeparator());
			List<SimpleEntry<String, SimpleEntry<String, Integer>>> entries = toc.getTOC();
			for (SimpleEntry<String, SimpleEntry<String, Integer>> entry : entries) {
				String songTitle = entry.getKey();
				String destination = entry.getValue().getKey();
				Integer pageNumber = entry.getValue().getValue();
				
				Paragraph p = paragraph(lyricsFont);
				Chunk title = new Chunk(songTitle);
				title.setAction(PdfAction.gotoLocalPage(destination, false));
				p.add(title);
				p.add(dottedLine);
				Chunk number = new Chunk(String.valueOf(pageNumber));
				number.setAction(PdfAction.gotoLocalPage(destination, false));
				p.add(number);
				document.add(p);
			}
			
			document.close();
		} catch (DocumentException e) {
			throw new RuntimeException("error while creating PDF document", e);
		}
		
		return outputStream;
	}
	
	private boolean isBodyElement(SongElement songElement) {
		return songElement.getType() == SongElementEnum.LYRICS
			|| songElement.getType() == SongElementEnum.CHORDS
			|| songElement.getType() == SongElementEnum.TRANSLATION
			|| songElement.getType() == SongElementEnum.NEW_LINE;
	}
	
	private String getCleanChordSequence(Song song) {
		return song.getChordSequence() == null
			? null
			: song.getChordSequence().replaceAll("^\\p{Space}+", "").replaceAll("\\p{Space}+$", "");
	}
	
	private Paragraph paragraph(String text, Font font) {
		Paragraph paragraph = new Paragraph(text, font);
		paragraph.setExtraParagraphSpace(0);
		paragraph.setPaddingTop(0);
		paragraph.setSpacingBefore(0);
		paragraph.setSpacingAfter(0);
		return paragraph;
	}
	
	private Paragraph paragraph(Font font) {
		Paragraph paragraph = new Paragraph();
		paragraph.setFont(font);
		paragraph.setExtraParagraphSpace(0);
		paragraph.setPaddingTop(0);
		paragraph.setSpacingBefore(0);
		paragraph.setSpacingAfter(0);
		return paragraph;
	}
	
}
