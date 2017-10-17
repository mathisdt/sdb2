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
package org.zephyrsoft.sdb2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.XMLConverter;
import org.zephyrsoft.sdb2.model.statistics.StatisticsModel;
import org.zephyrsoft.util.DateTools;
import org.zephyrsoft.util.gui.ErrorDialog;

import com.google.common.base.Preconditions;

/**
 * Manages the song statistics.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class StatisticsController {
	
	private static final Logger LOG = LoggerFactory.getLogger(StatisticsController.class);
	
	private StatisticsModel statistics = null;
	private final IOController ioController;
	
	public StatisticsController(IOController ioController) {
		this.ioController = ioController;
	}
	
	public void loadStatistics() {
		// TODO move to IOController !?
		LOG.debug("loading statistics from file");
		File file = new File(FileAndDirectoryLocations.getStatisticsFileName());
		try {
			InputStream xmlInputStream = new FileInputStream(file);
			statistics = XMLConverter.fromXMLToStatisticsModel(xmlInputStream);
			xmlInputStream.close();
		} catch (IOException e) {
			LOG.error("could not read statistics from \"" + file.getAbsolutePath() + "\"");
		}
		if (statistics == null) {
			// there was a problem while reading
			statistics = new StatisticsModel();
		}
	}
	
	public void countSongAsPresentedToday(Song song) {
		Preconditions.checkArgument(song != null, "counted song must be different from null");
		LOG.info("counting song \"{}\" as presented today", song.getTitle());
		statistics.addStatisticsEntry(song, DateTools.now());
	}
	
	public synchronized boolean saveStatistics() {
		// TODO move to IOController !?
		File file = new File(FileAndDirectoryLocations.getStatisticsFileName());
		try {
			OutputStream xmlOutputStream = new FileOutputStream(file);
			XMLConverter.fromStatisticsModelToXML(statistics, xmlOutputStream);
			xmlOutputStream.close();
			return true;
		} catch (IOException e) {
			LOG.error("could not write statistics to \"" + file.getAbsolutePath() + "\"");
			return false;
		}
	}
	
	public void exportStatisticsAll(SongsModel songs, File targetExcelFile) {
		// collect basic data
		Map<String, Song> songsByUUID = new HashMap<>();
		for (Song song : songs) {
			songsByUUID.put(song.getUUID(), song);
		}
		List<String> months = statistics.getUsedMonths();
		
		// create a new workbook
		Workbook workbook = new HSSFWorkbook();
		
		// define formats
		CellStyle integerStyle = workbook.createCellStyle();
		DataFormat df = workbook.createDataFormat();
		integerStyle.setDataFormat(df.getFormat("0"));
		CellStyle textStyle = workbook.createCellStyle();
		textStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		CellStyle textBoldStyle = workbook.createCellStyle();
		textBoldStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		org.apache.poi.ss.usermodel.Font font = workbook.createFont();
		font.setColor(org.apache.poi.ss.usermodel.Font.COLOR_RED);
		font.setBold(true);
		textBoldStyle.setFont(font);
		
		for (String month : months) {
			Map<String, Integer> monthStatsByUUID = statistics.getStatisticsForMonth(month);
			Map<Song, Integer> monthStatsBySong = new TreeMap<>();
			for (String uuid : monthStatsByUUID.keySet()) {
				Song song = songs.getByUUID(uuid);
				if (song != null) {
					monthStatsBySong.put(song, monthStatsByUUID.get(uuid));
				} else {
					LOG.info("no song found in database for UUID {}", uuid);
				}
			}
			
			Sheet sheet = workbook.createSheet(month);
			Row row = null;
			
			int rownum = 0;
			
			row = sheet.createRow(rownum);
			
			int cellnum = 0;
			
			addTextCell(row, cellnum++, textBoldStyle, "Presentation Count");
			addTextCell(row, cellnum++, textBoldStyle, "Song Title");
			addTextCell(row, cellnum++, textBoldStyle, "Composer (Music)");
			addTextCell(row, cellnum++, textBoldStyle, "Author (Text)");
			addTextCell(row, cellnum++, textBoldStyle, "Publisher");
			addTextCell(row, cellnum++, textBoldStyle, "Copyright Notes");
			addTextCell(row, cellnum++, textBoldStyle, "Song Lyrics");
			
			rownum++;
			
			for (Song song : monthStatsBySong.keySet()) {
				row = sheet.createRow(rownum);
				
				cellnum = 0;
				
				addIntegerCell(row, cellnum++, integerStyle, monthStatsBySong.get(song));
				addTextCell(row, cellnum++, textStyle, song.getTitle());
				addTextCell(row, cellnum++, textStyle, song.getComposer());
				addTextCell(row, cellnum++, textStyle, song.getAuthorText());
				addTextCell(row, cellnum++, textStyle, song.getPublisher());
				addTextCell(row, cellnum++, textStyle, song.getAdditionalCopyrightNotes());
				addTextCell(row, cellnum++, textStyle, song.getLyrics());
				
				rownum++;
			}
			
			for (int i = 0; i < cellnum; i++) {
				sheet.autoSizeColumn(i);
			}
			sheet.createFreezePane(0, 1);
		}
		
		try (FileOutputStream out = new FileOutputStream(targetExcelFile)) {
			workbook.write(out);
			out.close();
			LOG.info("all statistics exported");
		} catch (IOException e) {
			ErrorDialog.openDialog(null, "Could not export the statistics to:\n" + targetExcelFile.getAbsolutePath()
				+ "\n\nPlease verify that you have write access and the file is not opened by any other program!");
			LOG.warn("could not write statistics to file", e);
		}
		try {
			workbook.close();
		} catch (IOException e) {
			// do nothing
		}
	}
	
	private static void addTextCell(Row row, int cellnum, CellStyle style, String text) {
		Cell cell = addCell(row, cellnum, style);
		cell.setCellValue(text);
	}
	
	private static void addIntegerCell(Row row, int cellnum, CellStyle style, Integer number) {
		Cell cell = addCell(row, cellnum, style);
		cell.setCellValue(number);
	}
	
	private static Cell addCell(Row row, int cellnum, CellStyle style) {
		Cell cell = row.createCell(cellnum);
		cell.setCellStyle(style);
		return cell;
	}
}
