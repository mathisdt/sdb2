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
package org.zephyrsoft.sdb2.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.Feature;
import org.zephyrsoft.sdb2.gui.SongCell;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongParser;
import org.zephyrsoft.sdb2.service.IndexerService;
import org.zephyrsoft.sdb2.util.gui.SongCellHighlighter;

/**
 * A {@link ListCellRenderer} for {@link Song} values.
 *
 * @author Mathis Dirksen-Thedens
 */
public class SongCellRenderer implements ListCellRenderer<Song> {
	
	private static final long serialVersionUID = -9042262843850129406L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SongCellRenderer.class);
	
	private JTextComponent filterInputField;
	private SongCellHighlighter highlighter;
	
	public SongCellRenderer(JTextComponent filterInputField) {
		this.filterInputField = filterInputField;
		
		if (Feature.HIGHLIGHT_FILTER_MATCHES.isActive()) {
			highlighter = new SongCellHighlighter();
			updateHighlighter();
			
			this.filterInputField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateHighlighter();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateHighlighter();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					updateHighlighter();
				}
			});
		}
	}
	
	private void updateHighlighter() {
		if (Feature.HIGHLIGHT_FILTER_MATCHES.isActive()) {
			String findRegex = constructFindRegex(filterInputField.getText());
			String replaceRegex = constructReplaceRegex(filterInputField.getText());
			LOG.debug("new find regex: {}", findRegex);
			LOG.debug("new replace regex: {}", replaceRegex);
			highlighter.setFindRegex(findRegex);
			highlighter.setReplaceRegex(replaceRegex);
		}
	}
	
	private String constructFindRegex(String filter) {
		if (StringUtils.isBlank(filter)) {
			return "";
		}
		StringBuilder ret = new StringBuilder();
		ret.append("(?i)^(.*)(");
		String[] filterParts = filter.toLowerCase().split(IndexerService.TERM_SPLIT_REGEX);
		boolean isFirst = true;
		for (String filterPart : filterParts) {
			if (isFirst) {
				isFirst = false;
			} else {
				ret.append(")([^ ]* [^ ]*)(");
			}
			ret.append(filterPart);
		}
		ret.append(")(.*)$");
		return ret.toString();
	}
	
	private String constructReplaceRegex(String filter) {
		if (StringUtils.isBlank(filter)) {
			return "";
		}
		StringBuilder ret = new StringBuilder();
		ret.append("<html>$1<b>");
		String[] filterParts = filter.toLowerCase().split(IndexerService.TERM_SPLIT_REGEX);
		int matchNumber = 2;
		for (@SuppressWarnings("unused")
		String filterPart : filterParts) {
			if (matchNumber > 2) {
				ret.append("</b>$" + matchNumber++ + "<b>");
			}
			ret.append("$" + matchNumber++);
		}
		ret.append("</b>$").append(matchNumber).append("</html>");
		return ret.toString();
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends Song> list, Song value, int index,
		boolean isSelected, boolean cellHasFocus) {
		
		SongCell ret = new SongCell(30);
		ret.setSongTitle(value.getTitle());
		ret.setFirstLine(SongParser.getFirstLyricsLine(value));
		
		ret.setForeground(Color.BLACK);
		if (isSelected) {
			ret.setBackground(new Color(183, 200, 213));
		} else {
			if (index % 2 == 0) {
				ret.setBackground(Color.WHITE);
			} else {
				ret.setBackground(new Color(230, 230, 230));
			}
		}
		
		if (Feature.HIGHLIGHT_FILTER_MATCHES.isActive()) {
			highlighter.highlight(ret);
		}
		
		return ret;
	}
}
