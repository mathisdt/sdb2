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

import static org.zephyrsoft.sdb2.model.SongElementEnum.CHORDS;
import static org.zephyrsoft.sdb2.model.SongElementEnum.COPYRIGHT;
import static org.zephyrsoft.sdb2.model.SongElementEnum.LYRICS;
import static org.zephyrsoft.sdb2.model.SongElementEnum.NEW_LINE;
import static org.zephyrsoft.sdb2.model.SongElementEnum.TITLE;
import static org.zephyrsoft.sdb2.model.SongElementEnum.TRANSLATION;
import static org.zephyrsoft.sdb2.model.SongElementMatcher.is;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.ExportFormat;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongElementHistory;
import org.zephyrsoft.sdb2.model.SongElementHistory.SongElementHistoryQueryResult;
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
 * Exports songs as PDF in different variations.
 */
public class ExportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);
	
	private class PageNumbers extends PdfPageEventHelper {
		private final Font footerFont = new Font(Font.FontFamily.UNDEFINED, 10, Font.ITALIC);
		
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
	
	private class TOC extends PdfPageEventHelper {
		
		protected int counter = 0;
		protected final List<SimpleEntry<String, SimpleEntry<String, Integer>>> toc = new ArrayList<>();
		
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
	
	private class ExportInProgress {
		private ExportFormat exportFormat;
		private Document document;
		/** only used in song body (not for title or copyright) */
		private Paragraph currentLine;
		
		public ExportInProgress(ExportFormat exportFormat, Document document) {
			this.exportFormat = exportFormat;
			this.document = document;
		}
		
		public ExportFormat getExportFormat() {
			return exportFormat;
		}
		
		public void setExportFormat(ExportFormat exportFormat) {
			this.exportFormat = exportFormat;
		}
		
		public Document getDocument() {
			return document;
		}
		
		public void setDocument(Document document) {
			this.document = document;
		}
		
		public Paragraph getCurrentLine() {
			return currentLine;
		}
		
		public Paragraph getOrCreateCurrentLine(Supplier<Paragraph> paragraphSupplier) {
			if (currentLine == null) {
				currentLine = paragraphSupplier.get();
			}
			return currentLine;
		}
		
		public void setCurrentLine(Paragraph currentLine) {
			this.currentLine = currentLine;
		}
	}
	
	@FunctionalInterface
	private interface SongElementHandler {
		void handleElement(ExportInProgress exportInProgress, Song song, SongElementHistory history)
			throws DocumentException;
	}
	
	private final Font titleFont;
	private final Font lyricsFont;
	private final Font translationFont;
	private final Font copyrightFont;
	private final ChordSpaceCorrector chordSpaceCorrector;
	private final Map<SongElementEnum, SongElementHandler> songElementHandlers;
	
	public ExportService() throws Exception {
		BaseFont baseFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);
		titleFont = new Font(baseFont, 20, Font.BOLD);
		lyricsFont = new Font(baseFont, 12);
		translationFont = new Font(baseFont, 8, Font.ITALIC);
		copyrightFont = new Font(baseFont, 10);
		
		chordSpaceCorrector = new ChordSpaceCorrector(
			text -> (int) lyricsFont.getBaseFont().getWidthPointKerned(text, lyricsFont.getSize()));
		
		songElementHandlers = buildSongElementHandlerMap();
	}
	
	private Map<SongElementEnum, SongElementHandler> buildSongElementHandlerMap() {
		Map<SongElementEnum, SongElementHandler> handlers = new HashMap<>();
		handlers.put(TITLE, (exportInProgress, song, history) -> {
			Chunk chunk = new Chunk(song.getTitle() + "\n");
			chunk.setGenericTag(song.getTitle());
			Paragraph paragraph = paragraph(titleFont);
			paragraph.add(chunk);
			exportInProgress.getDocument().add(paragraph);
			
			// insert chord sequence directly after title
			if (exportInProgress.getExportFormat().areChordsShown() && StringUtils.isNotBlank(song.getCleanChordSequence())) {
				Paragraph chordSequence = paragraph(song.getCleanChordSequence() + "\n\n", lyricsFont);
				chordSequence.setIndentationLeft(30);
				exportInProgress.getDocument().add(chordSequence);
			}
		});
		handlers.put(LYRICS, (exportInProgress, song, history) -> {
			String chordsLine = "";
			SongElementHistoryQueryResult queryResult = history.query()
				.without(NEW_LINE)
				.lastSeen(is(CHORDS))
				.end();
			
			if (queryResult.isMatched() && exportInProgress.getExportFormat().areChordsShown()) {
				chordsLine = chordSpaceCorrector.correctChordSpaces(queryResult.getMatchedElements().get(0).getContent(),
					history.current().getContent()) + "\n";
			}
			exportInProgress.getOrCreateCurrentLine(() -> paragraph())
				.add(chunk(chordsLine + history.current().getContent(), lyricsFont));
		});
		handlers.put(TRANSLATION, (exportInProgress, song, history) -> {
			if (exportInProgress.getExportFormat().isTranslationShown()) {
				exportInProgress.getOrCreateCurrentLine(() -> paragraph())
					.add(chunk(history.current().getContent(), translationFont));
			}
		});
		handlers.put(NEW_LINE, (exportInProgress, song, history) -> {
			if (exportInProgress.getCurrentLine() != null) {
				if (exportInProgress.getCurrentLine().getChunks() == null || exportInProgress.getCurrentLine().getChunks().isEmpty()) {
					// two empty paragraphs won't render as an empty line,
					// so we have to add a newline to the existing paragraph instead
					exportInProgress.getCurrentLine().add(chunk("\n"));
				}
				exportInProgress.getDocument().add(exportInProgress.getCurrentLine());
			}
			exportInProgress.setCurrentLine(paragraph());
		});
		handlers.put(COPYRIGHT, (exportInProgress, song, history) -> {
			Paragraph copyright = paragraph(history.current().getContent(), copyrightFont);
			SongElementHistoryQueryResult queryResult = history.query()
				.without(NEW_LINE)
				.lastSeen(is(COPYRIGHT))
				.end();
			if (!queryResult.isMatched()) {
				copyright.setSpacingBefore(20);
			}
			exportInProgress.getDocument().add(copyright);
		});
		// CHORDS -> handled by following LYRICS element
		return handlers;
	}
	
	public byte[] export(ExportFormat exportFormat, Collection<Song> songs) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			TOC toc = new TOC();
			Document document = beginDocument(toc, outputStream);
			
			ExportInProgress exportInProgress = new ExportInProgress(exportFormat, document);
			for (Song song : songs) {
				List<SongElement> songElements = SongParser.parse(song, true, true, true);
				
				if (exportFormat.onlySongsWithChords()
					&& songElements.stream().noneMatch(e -> e.getType() == CHORDS)) {
					continue;
				}
				
				SongElementHistory history = new SongElementHistory(songElements);
				for (SongElement songElement : history) {
					SongElementHandler handler = songElementHandlers.get(songElement.getType());
					if (handler != null) {
						handler.handleElement(exportInProgress, song, history);
					}
				}
				
				document.newPage();
			}
			
			appendTableOfContents(document, toc);
			
			document.close();
		} catch (DocumentException e) {
			throw new RuntimeException("error while creating PDF document", e);
		}
		
		return outputStream.toByteArray();
	}
	
	private Document beginDocument(TOC toc, ByteArrayOutputStream outputStream) throws DocumentException {
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		writer.setPageEvent(toc);
		writer.setPageEvent(new PageNumbers());
		document.setMargins(50, 50, 30, 30);
		document.open();
		return document;
	}
	
	private void appendTableOfContents(Document document, TOC toc) throws DocumentException {
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
	}
	
	private Chunk chunk(String text) {
		return new Chunk(text);
	}
	
	private Chunk chunk(String text, Font font) {
		return new Chunk(text, font);
	}
	
	private Paragraph paragraph() {
		Paragraph paragraph = new Paragraph();
		paragraph.setExtraParagraphSpace(0);
		paragraph.setPaddingTop(0);
		paragraph.setSpacingBefore(0);
		paragraph.setSpacingAfter(0);
		return paragraph;
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
		return paragraph(null, font);
	}
	
}
