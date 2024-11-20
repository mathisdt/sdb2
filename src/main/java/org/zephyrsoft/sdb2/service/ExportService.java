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

import static org.zephyrsoft.sdb2.model.SongElementEnum.CHORDS;
import static org.zephyrsoft.sdb2.model.SongElementEnum.COPYRIGHT;
import static org.zephyrsoft.sdb2.model.SongElementEnum.LYRICS;
import static org.zephyrsoft.sdb2.model.SongElementEnum.NEW_LINE;
import static org.zephyrsoft.sdb2.model.SongElementEnum.TITLE;
import static org.zephyrsoft.sdb2.model.SongElementEnum.TRANSLATION;
import static org.zephyrsoft.sdb2.model.SongElementMatcher.is;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.zephyrsoft.sdb2.model.ExportFormat;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;
import org.zephyrsoft.sdb2.model.SongElementHistory;
import org.zephyrsoft.sdb2.model.SongElementHistory.SongElementHistoryQueryResult;
import org.zephyrsoft.sdb2.model.SongParser;
import org.zephyrsoft.sdb2.util.ChordSpaceCorrector;
import org.zephyrsoft.sdb2.util.TextRendererNonTrimming;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEvent;
import com.itextpdf.kernel.pdf.event.AbstractPdfDocumentEventHandler;
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.DottedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AbstractElement;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TabAlignment;

/**
 * Exports songs as PDF in different variations.
 */
public class ExportService {
	
	private class PageNumbers extends AbstractPdfDocumentEventHandler {
		@Override
		public void onAcceptedEvent(AbstractPdfDocumentEvent event) {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfPage page = docEvent.getPage();
			int pageNum = docEvent.getDocument().getPageNumber(page);
			PdfCanvas canvas = new PdfCanvas(page);
			canvas.beginText();
			canvas.setFontAndSize(baseFont, 10);
			Rectangle pageSize = page.getPageSizeWithRotation();
			canvas.moveText(pageSize.getWidth() / 2 - 10, 15);
			canvas.showText(String.format("- %d -", pageNum));
			canvas.endText();
			canvas.stroke();
			canvas.release();
		}
	}
	
	private class ExportInProgress {
		private final ExportFormat exportFormat;
		private final Document document;
		private final List<TocEntry> toc = new ArrayList<>();
		/** only used in song body (not for title or copyright) */
		private Paragraph currentLine;
		
		public ExportInProgress(ExportFormat exportFormat, Document document) {
			this.exportFormat = exportFormat;
			this.document = document;
		}
		
		public ExportFormat getExportFormat() {
			return exportFormat;
		}
		
		public Document getDocument() {
			return document;
		}
		
		public List<TocEntry> getToc() {
			return toc;
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
		void handleElement(ExportInProgress exportInProgress, Song song, SongElementHistory history);
	}
	
	@FunctionalInterface
	private interface AttributeSetter {
		void apply(AbstractElement<?> element);
	}
	
	private AttributeSetter titleAttributes;
	private AttributeSetter lyricsAttributes;
	private AttributeSetter translationAttributes;
	private AttributeSetter copyrightAttributes;
	private ChordSpaceCorrector chordSpaceCorrector;
	private Map<SongElementEnum, SongElementHandler> songElementHandlers;
	private PdfFont baseFont;
	
	private void init() throws Exception {
		baseFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_EMBEDDED);
		PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_EMBEDDED);
		PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_EMBEDDED);
		titleAttributes = e -> {
			e.setFont(boldFont);
			e.setFontSize(20);
		};
		lyricsAttributes = e -> {
			e.setFont(baseFont);
			e.setFontSize(12);
		};
		translationAttributes = e -> {
			e.setFont(italicFont);
			e.setFontSize(8);
		};
		copyrightAttributes = e -> {
			e.setFont(italicFont);
			e.setFontSize(10);
		};
		
		chordSpaceCorrector = new ChordSpaceCorrector(
			text -> (int) baseFont.getWidth(text, 12));
		
		songElementHandlers = buildSongElementHandlerMap();
	}
	
	private Map<SongElementEnum, SongElementHandler> buildSongElementHandlerMap() {
		Map<SongElementEnum, SongElementHandler> handlers = new EnumMap<>(SongElementEnum.class);
		handlers.put(TITLE, titleHandler());
		handlers.put(LYRICS, lyricsHandler());
		handlers.put(TRANSLATION, translationHandler());
		handlers.put(NEW_LINE, newlineHandler());
		handlers.put(COPYRIGHT, copyrightHandler());
		// CHORDS don't have a handler, they are handled by the following LYRICS element
		return handlers;
	}
	
	private SongElementHandler titleHandler() {
		return (exportInProgress, song, history) -> {
			Text chunk = new Text(song.getTitle() + "\n");
			exportInProgress.getToc().add(new TocEntry(song.getUUID(), song.getTitle(),
				exportInProgress.getDocument().getPdfDocument().getNumberOfPages()));
			Paragraph paragraph = paragraph(titleAttributes);
			paragraph.setDestination(song.getUUID());
			paragraph.setMarginBottom(15);
			paragraph.add(chunk);
			exportInProgress.getDocument().add(paragraph);
			
			if (exportInProgress.getExportFormat().areChordsShown() && StringUtils.isNotBlank(song.getCleanChordSequence())) {
				// insert chord sequence directly after title
				Paragraph chordSequence = paragraph(song.getCleanChordSequence() + "\n\n", lyricsAttributes);
				chordSequence.setPaddingLeft(30);
				exportInProgress.getDocument().add(chordSequence);
			}
		};
	}
	
	private SongElementHandler lyricsHandler() {
		return (exportInProgress, song, history) -> {
			String chordsLine = "";
			SongElementHistoryQueryResult queryResult = history.query()
				.without(NEW_LINE)
				.lastSeen(is(CHORDS))
				.end();
			
			if (queryResult.isMatched() && exportInProgress.getExportFormat().areChordsShown()) {
				chordsLine = chordSpaceCorrector.correctChordSpaces(queryResult.getMatchedElements().get(0).getContent(),
					history.current().getContent()) + "\n";
			}
			Paragraph currentLine = exportInProgress.getOrCreateCurrentLine(this::paragraph);
			if (history.current().getIndentation() > 0) {
				currentLine.setPaddingLeft(calculateIndentation(history.current()));
			}
			currentLine.add(text(chordsLine + history.current().getContent(), lyricsAttributes));
		};
	}
	
	private SongElementHandler translationHandler() {
		return (exportInProgress, song, history) -> {
			if (exportInProgress.getExportFormat().isTranslationShown()) {
				Paragraph currentLine = exportInProgress.getOrCreateCurrentLine(this::paragraph);
				if (history.current().getIndentation() > 0) {
					currentLine.setPaddingLeft(calculateIndentation(history.current()));
				}
				currentLine.add(text(history.current().getContent(), translationAttributes));
			}
		};
	}
	
	private Text text(String text, AttributeSetter attributes) {
		Text result = new Text(text);
		attributes.apply(result);
		result.setNextRenderer(new TextRendererNonTrimming(result));
		return result;
	}
	
	private SongElementHandler newlineHandler() {
		return (exportInProgress, song, history) -> {
			if (exportInProgress.getCurrentLine() != null) {
				if (history.query()
					.lastSeen(is(NEW_LINE))
					.end().isMatched()) {
					// two empty paragraphs won't render as an empty line (iText-specific behaviour),
					// so we have to add a newline to the existing paragraph instead
					exportInProgress.getCurrentLine().add("\n");
				}
				exportInProgress.getDocument().add(exportInProgress.getCurrentLine());
			}
			exportInProgress.setCurrentLine(paragraph());
		};
	}
	
	private SongElementHandler copyrightHandler() {
		return (exportInProgress, song, history) -> {
			Paragraph copyright = paragraph(history.current().getContent(), copyrightAttributes);
			SongElementHistoryQueryResult queryResult = history.query()
				.without(NEW_LINE)
				.lastSeen(is(COPYRIGHT))
				.end();
			if (!queryResult.isMatched()) {
				copyright.setPaddingTop(20);
			}
			exportInProgress.getDocument().add(copyright);
		};
	}
	
	private int calculateIndentation(SongElement element) {
		return element.getIndentation() * 12;
	}
	
	public byte[] export(ExportFormat exportFormat, Collection<Song> songs) throws Exception {
		init();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			Document document = beginDocument(outputStream);
			
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
				
				document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
			}
			
			appendTableOfContents(document, exportInProgress.getToc());
			
			document.close();
		} catch (Exception e) {
			throw new RuntimeException("error while creating PDF document", e);
		}
		
		return outputStream.toByteArray();
	}
	
	private static record TocEntry(String technicalName, String title, Integer pageNumber) {
		// nothing here
	}
	
	private Document beginDocument(ByteArrayOutputStream outputStream) {
		PdfDocument pdf = new PdfDocument(new PdfWriter(outputStream));
		pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new PageNumbers());
		Document doc = new Document(pdf);
		doc.setMargins(50, 50, 30, 30);
		return doc;
	}
	
	private void appendTableOfContents(Document document, List<TocEntry> toc) {
		document.add(paragraph("Table of Contents", titleAttributes));
		for (TocEntry tocEntry : toc) {
			String songTitle = tocEntry.title();
			String technicalName = tocEntry.technicalName();
			Integer pageNumber = tocEntry.pageNumber();
			
			Paragraph p = paragraph(lyricsAttributes);
			p.addTabStops(List.of(new TabStop(580, TabAlignment.RIGHT, new DottedLine())))
				.add(songTitle)
				.add(new Tab())
				.add(String.valueOf(pageNumber))
				.setAction(PdfAction.createGoTo(technicalName));
			document.add(p);
		}
	}
	
	private Paragraph paragraph() {
		Paragraph paragraph = new Paragraph();
		paragraph.setMultipliedLeading(1);
		paragraph.setMargin(0);
		paragraph.setPadding(0);
		return paragraph;
	}
	
	private Paragraph paragraph(AttributeSetter attributes) {
		Paragraph paragraph = paragraph();
		attributes.apply(paragraph);
		return paragraph;
	}
	
	private Paragraph paragraph(String text, AttributeSetter attributes) {
		Paragraph paragraph = paragraph(attributes);
		paragraph.add(text);
		return paragraph;
	}
	
}
