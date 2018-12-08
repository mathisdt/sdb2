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
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongParser;

import com.google.common.annotations.VisibleForTesting;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Exports songs in various formats.
 *
 * @author Mathis Dirksen-Thedens
 */
public class ExportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);
	
	private class PageNumbers extends PdfPageEventHelper {
		Font ffont = new Font(Font.FontFamily.UNDEFINED, 10, Font.ITALIC);
		
		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			PdfContentByte cb = writer.getDirectContent();
			Phrase footer = new Phrase("- " + document.getPageNumber() + " -", ffont);
			ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
				footer,
				(document.right() - document.left()) / 2 + document.leftMargin(),
				document.bottom() - 10, 0);
		}
	}
	
	private static final Pattern CHORD_PATTERN = Pattern.compile("(\\s*+)\\b(\\S+)\\b");
	
	private Font titleFont;
	private Font lyricsFont;
	private Font translationFont;
	private Font copyrightFont;
	
	public ExportService() {
		BaseFont baseFont = null;
		try {
			baseFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);
		} catch (Exception e) {
			throw new RuntimeException("error while setting up export service", e);
		}
		titleFont = new Font(baseFont, 20, Font.BOLD);
		lyricsFont = new Font(baseFont, 12);
		translationFont = new Font(baseFont, 8, Font.ITALIC);
		copyrightFont = new Font(baseFont, 10);
	}
	
	public ByteArrayOutputStream export(ExportFormat exportFormat, Collection<Song> songs) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		// TODO if export format is LYRICS_WITH_CHORDS, only export songs which actually have chords?
		// (which would mean to remove all songs that don't have CHORDS elements)
		
		try {
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			writer.setPageEvent(new PageNumbers());
			document.setMargins(50, 50, 30, 30);
			document.open();
			
			// output content overview
			document.add(paragraph("Contents", titleFont));
			Paragraph contents = paragraph(lyricsFont);
			contents.add(new Phrase("\n"));
			for (Song song : songs) {
				contents.add(new Phrase(song.getTitle() + "\n"));
			}
			document.add(contents);
			document.newPage();
			
			for (Song song : songs) {
				List<SongElement> songElements = SongParser.parse(song, true, true);
				
				SongElement previousSongElement = null;
				for (SongElement songElement : songElements) {
					switch (songElement.getType()) {
						case TITLE:
							document.add(paragraph(song.getTitle() + "\n", titleFont));
							break;
						case LYRICS:
							String chordsLine = "";
							if (previousSongElement != null && previousSongElement.getType() == SongElementEnum.CHORDS
								&& exportFormat.areChordsShown()) {
								chordsLine = correctChordSpaces(previousSongElement.getElement(), songElement.getElement()) + "\n";
							}
							document.add(paragraph(chordsLine + songElement.getElement(), lyricsFont));
							break;
						case TRANSLATION:
							if (exportFormat.isTranslationShown()) {
								document.add(paragraph(songElement.getElement(), translationFont));
							}
							break;
						case COPYRIGHT:
							Paragraph copyright = paragraph(songElement.getElement(), copyrightFont);
							if (previousSongElement != null && previousSongElement.getType() != SongElementEnum.COPYRIGHT) {
								copyright.setSpacingBefore(20);
							}
							document.add(copyright);
							break;
						case CHORDS:
							// handled by following LYRICS element
							break;
						case NEW_LINE:
							// ignored for export
							break;
						default:
							throw new IllegalStateException("unsupported song element type");
					}
					
					// NEW_LINE elements are ignored for export
					if (songElement.getType() != SongElementEnum.NEW_LINE) {
						previousSongElement = songElement;
					}
				}
				
				document.newPage();
			}
			
			document.close();
		} catch (DocumentException e) {
			throw new RuntimeException("error while creating PDF document", e);
		}
		
		return outputStream;
	}
	
	@VisibleForTesting
	String correctChordSpaces(String chords, String lyrics) {
		try {
			StringBuilder result = new StringBuilder();
			Matcher matcher = CHORD_PATTERN.matcher(chords);
			while (matcher.find()) {
				String lyricsPart = lyrics.substring(0, matcher.end(1) > lyrics.length() ? lyrics.length() : matcher.end(1));
				while (renderedLength(lyricsPart) > renderedLength(result.toString() + " ")) {
					result.append(" ");
				}
				result.append(matcher.group(2)).append(" ");
			}
			return result.toString();
		} catch (Exception e) {
			throw new IllegalStateException("problem while correcting chord spaces - chord line: '" + chords + "' - lyrics: '" + lyrics + "'", e);
		}
	}
	
	private float renderedLength(String text) {
		return lyricsFont.getBaseFont().getWidthPointKerned(text, lyricsFont.getSize());
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
