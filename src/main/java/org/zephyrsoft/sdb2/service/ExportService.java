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

import org.zephyrsoft.sdb2.model.Song;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
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
	
	public ByteArrayOutputStream export(ExportFormat exportFormat, Collection<Song> songs) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			writer.setPageEvent(new PageNumbers());
			document.setMargins(50, 50, 30, 30);
			document.open();
			
			// TODO output content overview
			document.add(new Paragraph("Hello World!"));
			
			// TODO output song(s)
			// TODO => if export format is LYRICS_WITH_CHORDS, only export songs which actually have chords?
			
			document.close();
		} catch (DocumentException de) {
			throw new RuntimeException("error while creating PDF document", de);
		}
		
		return outputStream;
	}
	
}
