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
package org.zephyrsoft.sdb2.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.gui.renderer.FilterTypeCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.LanguageCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.ScreenContentsCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.ScreenDisplayCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.SongCellRenderer;
import org.zephyrsoft.sdb2.importer.ImportFromSDBv1;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.LanguageEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.SettingKey;
import org.zephyrsoft.sdb2.model.SettingsModel;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongParser;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.ScreenHelper;
import org.zephyrsoft.util.CustomFileFilter;
import org.zephyrsoft.util.JarTools;
import org.zephyrsoft.util.ResourceTools;
import org.zephyrsoft.util.StringTools;
import org.zephyrsoft.util.gui.ErrorDialog;
import org.zephyrsoft.util.gui.FixedWidthJList;
import org.zephyrsoft.util.gui.ImagePreview;
import org.zephyrsoft.util.gui.ListFilter;
import org.zephyrsoft.util.gui.TransparentComboBoxModel;
import org.zephyrsoft.util.gui.TransparentFilterableListModel;
import org.zephyrsoft.util.gui.TransparentListModel;
import say.swing.JFontChooser;

/**
 * Main window of the application.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainWindow extends JFrame {
	
	private static final Presentable BLANK_SCREEN = new Presentable(null, null);
	
	private static final long serialVersionUID = -6874196690375696416L;
	
	private static final String NEWLINE = "\n";
	private static final String STACKTRACE_INDENTATION = "    ";
	private static final int STACKTRACE_ELEMENT_COUNT = 4;
	
	private static final int TAB_INDEX_EDIT = 0;
	private static final int TAB_INDEX_PRESENT = 1;
	private static final int TAB_INDEX_STATS = 2;
	private static final int TAB_INDEX_SETTINGS = 3;
	
	private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);
	
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	
	private JEditorPane editorLyrics;
	private JTextField textFieldTitle;
	private JComboBox<LanguageEnum> comboBoxLanguage;
	private JTextField textFieldTonality;
	private JTextField textFieldComposer;
	private JTextField textFieldAuthorText;
	private JTextField textFieldAuthorTranslation;
	private JTextField textFieldPublisher;
	private JTextField textFieldAdditionalCopyrightNotes;
	private JTextField textFieldSongNotes;
	private JEditorPane editorChordSequence;
	
	private KeyboardShortcutManager shortcutManager;
	private final MainController controller;
	
	private SettingsModel settingsModel;
	
	private FixedWidthJList<Song> songsList;
	private SongsModel songsModel;
	private TransparentFilterableListModel<Song> songsListModel;
	private Song songsListSelected;
	
	private FixedWidthJList<Song> presentList;
	private SongsModel presentModel;
	private TransparentListModel<Song> presentListModel;
	private Song presentListSelected;
	
	private FixedWidthJList<Song> linkedSongsList;
	private TransparentListModel<Song> linkedSongsListModel;
	
	private JPanel panelSongList;
	private JButton btnClearFilter;
	private JTextField textFieldFilter;
	private JButton btnNewSong;
	private JButton btnDeleteSong;
	private JButton btnSelectSong;
	
	private JSplitPane splitPanePresent;
	private boolean splitPanePresentDividerLocationSet = false;
	private JPanel selectedSongListButtons;
	private JButton btnUp;
	private JButton btnUnselect;
	private JButton btnDown;
	private JPanel panelSectionButtons;
	private GridBagConstraints panelSectionButtonsHints;
	private GridBagConstraints panelSectionButtonsLastRowHints;
	private JButton btnAddLinkedSong;
	private JButton btnRemoveLinkedSong;
	private JButton btnJumpToSelected;
	private JButton btnShowLogo;
	private JButton btnShowBlankScreen;
	private JButton btnPresentSelectedSong;
	private JScrollPane scrollPaneSectionButtons;
	private JButton btnJumpToPresented;
	
	private JLabel lblStatistics;
	private JButton btnExportLyricsOnlyPdfSelected;
	private JButton btnExportCompletePdfSelected;
	private JButton btnExportStatisticsSelected;
	private JButton btnExportLyricsOnlyPdfAll;
	private JButton btnExportCompletePdfAll;
	private JButton btnExportStatisticsAll;
	private JButton btnImportFromSdb1;
	private JLabel lblProgramVersion;
	
	private JButton btnUnlock;
	private JButton btnSelectTitleFont;
	private JButton btnSelectLyricsFont;
	private JButton btnSelectTranslationFont;
	private JButton btnSelectCopyrightFont;
	private JButton btnSelectTextColor;
	private JButton btnSelectBackgroundColor;
	private JButton btnSelectLogo;
	private JSpinner spinnerTopMargin;
	private JSpinner spinnerLeftMargin;
	private JSpinner spinnerRightMargin;
	private JSpinner spinnerBottomMargin;
	private JCheckBox checkboxShowTitle;
	private JSpinner spinnerDistanceTitleText;
	private JSpinner spinnerDistanceTextCopyright;
	private JComboBox<FilterTypeEnum> comboSongListFiltering;
	private JComboBox<GraphicsDevice> comboPresentationScreen1Display;
	private JComboBox<ScreenContentsEnum> comboPresentationScreen1Contents;
	private JComboBox<GraphicsDevice> comboPresentationScreen2Display;
	private JComboBox<ScreenContentsEnum> comboPresentationScreen2Contents;
	private JSpinner spinnerCountAsDisplayedAfter;
	
	private void afterConstruction() {
		// read program version
		String version = JarTools.getAttributeFromManifest(getClass(), "Song-Database-Version");
		if (version == null) {
			// use version without build date and time from properties file
			InputStream propsStream =
				ResourceTools.getInputStream(getClass(), "/org/zephyrsoft/sdb2/version.properties");
			if (propsStream != null) {
				Properties props = new Properties();
				try {
					props.load(propsStream);
					version = props.getProperty("programVersion");
				} catch (IOException e) {
					// swallow exception here and just leave version empty
				}
			}
		}
		lblProgramVersion.setText(version);
		
		// fill in available values for language
		for (LanguageEnum item : LanguageEnum.values()) {
			comboBoxLanguage.addItem(item);
		}
		// fill in available values for filter type
		for (FilterTypeEnum item : FilterTypeEnum.values()) {
			comboSongListFiltering.addItem(item);
		}
		clearSongData();
		// add renderer for language
		comboBoxLanguage.setRenderer(new LanguageCellRenderer());
		// add renderer for filter type
		comboSongListFiltering.setRenderer(new FilterTypeCellRenderer());
		// disable editing fields
		setSongEditingEnabled(false);
		// disable delete and select buttons below songs list
		btnDeleteSong.setEnabled(false);
		btnSelectSong.setEnabled(false);
		// disable buttons beneath the present list
		btnUp.setEnabled(false);
		btnUnselect.setEnabled(false);
		btnDown.setEnabled(false);
		// disable "present this song" and "jump to" buttons
		btnPresentSelectedSong.setEnabled(false);
		btnJumpToSelected.setEnabled(false);
		btnJumpToPresented.setEnabled(false);
		// disable single-song export and stats buttons
		btnExportLyricsOnlyPdfSelected.setEnabled(false);
		btnExportCompletePdfSelected.setEnabled(false);
		btnExportStatisticsSelected.setEnabled(false);
		// create empty songsModel for the "selected songs" list
		presentModel = new SongsModel();
		presentModel.setAutoSort(false);
		presentListModel = presentModel.getListModel();
		presentList.setModel(presentListModel);
		
		// fill in available values for screen contents
		for (ScreenContentsEnum item : ScreenContentsEnum.values()) {
			comboPresentationScreen1Contents.addItem(item);
			comboPresentationScreen2Contents.addItem(item);
		}
		// add renderer for screen contents
		comboPresentationScreen1Contents.setRenderer(new ScreenContentsCellRenderer());
		comboPresentationScreen2Contents.setRenderer(new ScreenContentsCellRenderer());
		comboPresentationScreen1Display.setRenderer(new ScreenDisplayCellRenderer());
		comboPresentationScreen2Display.setRenderer(new ScreenDisplayCellRenderer());
	}
	
	public void setModels(SongsModel songs, SettingsModel settings) {
		this.songsModel = songs;
		this.settingsModel = settings;
		songsListModel = songs.getFilterableListModel();
		songsList.setModel(songsListModel);
		
		// song list filtering
		ListFilter<Song> filter = new ListFilter<Song>() {
			@Override
			public boolean isAccepted(Song object) {
				String filterText = textFieldFilter.getText();
				if (StringTools.isBlank(filterText)) {
					return true;
				} else {
					FilterTypeEnum filterType = (FilterTypeEnum) settingsModel.get(SettingKey.SONG_LIST_FILTER);
					String toSearch = "";
					switch (filterType) {
						case ONLY_LYRICS:
							toSearch = object.getLyrics();
							break;
						case ONLY_TITLE:
							toSearch = object.getTitle();
							break;
						case TITLE_AND_FIRST_LYRICS_LINE:
							toSearch = object.getTitle() + " " + SongParser.getFirstLyricsLine(object);
							break;
						case TITLE_AND_LYRICS:
							toSearch = object.getTitle() + " " + object.getLyrics();
							break;
						default:
							throw new IllegalStateException("unknown song list filter type");
					}
					return toSearch == null
						|| StringTools.toEasilyComparable(toSearch).contains(
							StringTools.toEasilyComparable(textFieldFilter.getText()));
				}
			}
		};
		songsListModel.setFilter(filter);
		textFieldFilter.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				applyFilter();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				applyFilter();
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				applyFilter();
			}
		});
		
		// prepare for settings
		controller.detectScreens();
		comboPresentationScreen1Display.setModel(new TransparentComboBoxModel<GraphicsDevice>(controller.getScreens()));
		comboPresentationScreen2Display.setModel(new TransparentComboBoxModel<GraphicsDevice>(controller.getScreens()));
		
		// load values for instantly displayed settings
		Boolean showTitle = settingsModel.getBoolean(SettingKey.SHOW_TITLE);
		checkboxShowTitle.setSelected(showTitle == null ? false : showTitle.booleanValue());
		setSpinnerValue(spinnerTopMargin, settingsModel.getInteger(SettingKey.TOP_MARGIN));
		setSpinnerValue(spinnerLeftMargin, settingsModel.getInteger(SettingKey.LEFT_MARGIN));
		setSpinnerValue(spinnerRightMargin, settingsModel.getInteger(SettingKey.RIGHT_MARGIN));
		setSpinnerValue(spinnerBottomMargin, settingsModel.getInteger(SettingKey.BOTTOM_MARGIN));
		setSpinnerValue(spinnerDistanceTitleText, settingsModel.getInteger(SettingKey.DISTANCE_TITLE_TEXT));
		setSpinnerValue(spinnerDistanceTextCopyright, settingsModel.getInteger(SettingKey.DISTANCE_TEXT_COPYRIGHT));
		comboSongListFiltering.setSelectedItem(settingsModel.get(SettingKey.SONG_LIST_FILTER));
		comboPresentationScreen1Display.setSelectedItem(ScreenHelper.getScreen(controller.getScreens(),
			(String) settingsModel.get(SettingKey.SCREEN_1_DISPLAY)));
		comboPresentationScreen1Contents.setSelectedItem(settingsModel.get(SettingKey.SCREEN_1_CONTENTS));
		comboPresentationScreen2Display.setSelectedItem(ScreenHelper.getScreen(controller.getScreens(),
			(String) settingsModel.get(SettingKey.SCREEN_2_DISPLAY)));
		comboPresentationScreen2Contents.setSelectedItem(settingsModel.get(SettingKey.SCREEN_2_CONTENTS));
		setSpinnerValue(spinnerCountAsDisplayedAfter, settingsModel.getInteger(SettingKey.SECONDS_UNTIL_COUNTED));
	}
	
	private static void setSpinnerValue(JSpinner spinner, Object value) {
		spinner.setValue(value == null ? 0 : value);
	}
	
	protected void handleSettingsUnlock() {
		// reload screens
		controller.detectScreens();
		
		// enable controls
		setSettingsEnabled(true);
	}
	
	protected void handleSettingsSaveAndLock() {
		if (settingsModel != null) {
			commitSpinners(spinnerTopMargin, spinnerLeftMargin, spinnerRightMargin, spinnerBottomMargin,
				spinnerDistanceTitleText, spinnerDistanceTextCopyright, spinnerCountAsDisplayedAfter);
			// disable controls
			setSettingsEnabled(false);
			// copy changed settings to the model
			settingsModel.put(SettingKey.SHOW_TITLE, checkboxShowTitle.getModel().isSelected());
			settingsModel.put(SettingKey.TOP_MARGIN, spinnerTopMargin.getValue());
			settingsModel.put(SettingKey.LEFT_MARGIN, spinnerLeftMargin.getValue());
			settingsModel.put(SettingKey.RIGHT_MARGIN, spinnerRightMargin.getValue());
			settingsModel.put(SettingKey.BOTTOM_MARGIN, spinnerBottomMargin.getValue());
			settingsModel.put(SettingKey.DISTANCE_TITLE_TEXT, spinnerDistanceTitleText.getValue());
			settingsModel.put(SettingKey.DISTANCE_TEXT_COPYRIGHT, spinnerDistanceTextCopyright.getValue());
			settingsModel.put(SettingKey.SONG_LIST_FILTER, comboSongListFiltering.getSelectedItem());
			settingsModel.put(SettingKey.SCREEN_1_DISPLAY,
				ScreenHelper.getScreenId((GraphicsDevice) comboPresentationScreen1Display.getSelectedItem()));
			settingsModel.put(SettingKey.SCREEN_1_CONTENTS, comboPresentationScreen1Contents.getSelectedItem());
			settingsModel.put(SettingKey.SCREEN_2_DISPLAY,
				ScreenHelper.getScreenId((GraphicsDevice) comboPresentationScreen2Display.getSelectedItem()));
			settingsModel.put(SettingKey.SCREEN_2_CONTENTS, comboPresentationScreen2Contents.getSelectedItem());
			settingsModel.put(SettingKey.SECONDS_UNTIL_COUNTED, spinnerCountAsDisplayedAfter.getValue());
			// copying is not necessary for fonts, colors and the logo file name
			// because those settings are only stored directly in the model
			
			// apply settings
			// TODO
		}
	}
	
	/**
	 * Let the user select a font and save it into the {@link SettingsModel}.
	 * 
	 * @param target the target setting for the newly selected font
	 * @return {@code true} if the font was changed, {@code false} else
	 */
	private boolean selectFont(SettingKey target) {
		JFontChooser fontChooser = new JFontChooser();
		// take care of JFontChoosers quirky resource bundle mechanism
		// TODO add code AND RESOURCE BUNDLES to handle other languages
		fontChooser.setLocale(Locale.US);
		Font font = settingsModel.getFont(target);
		if (font != null) {
			fontChooser.setSelectedFont(font);
		}
		int result = fontChooser.showDialog(this);
		if (result == JFontChooser.OK_OPTION) {
			settingsModel.put(target, fontChooser.getSelectedFont());
			return true;
		} else {
			return false;
		}
	}
	
	protected void handleSelectTitleFont() {
		selectFont(SettingKey.TITLE_FONT);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectLyricsFont() {
		selectFont(SettingKey.LYRICS_FONT);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectTranslationFont() {
		selectFont(SettingKey.TRANSLATION_FONT);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectCopyrightFont() {
		selectFont(SettingKey.COPYRIGHT_FONT);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	private boolean selectColor(SettingKey target, Color defaultColor, String title) {
		Color color = settingsModel.getColor(target);
		if (color == null) {
			color = defaultColor;
		}
		Color result = JColorChooser.showDialog(this, title, color);
		if (result != null) {
			settingsModel.put(target, result);
			return true;
		} else {
			return false;
		}
	}
	
	protected void handleSelectTextColor() {
		selectColor(SettingKey.TEXT_COLOR, Color.WHITE, "Select Text Color");
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectBackgroundColor() {
		selectColor(SettingKey.BACKGROUND_COLOR, Color.BLACK, "Select Background Color");
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectLogo() {
		JFileChooser fileChooser = new JFileChooser();
		String pathname = settingsModel.getString(SettingKey.LOGO_FILE);
		if (pathname != null) {
			File currentFile = new File(pathname);
			if (currentFile.isFile() && currentFile.canRead()) {
				fileChooser.setSelectedFile(currentFile);
			}
		}
		fileChooser.setApproveButtonText("Select");
		fileChooser.setDialogTitle("Select Logo");
		
		// set a custom file filter but also keep the "accept all" filter
		fileChooser.setFileFilter(new CustomFileFilter("Images", ".png", ".jpg", ".jpeg", ".tif", ".tiff", ".gif"));
		
		// add the preview pane
		fileChooser.setAccessory(new ImagePreview(fileChooser));
		
		int returnVal = fileChooser.showDialog(this, null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (file.isFile() && file.canRead()) {
				settingsModel.put(SettingKey.LOGO_FILE, file.getAbsolutePath());
				// TODO perhaps apply the new settings?
			} else {
				// error: can't access file
				showErrorDialog("Couldn't access the file:\n" + file.getAbsolutePath());
			}
		}
		setSettingsEnabled(true);
	}
	
	private static void commitSpinners(JSpinner... spinners) {
		for (JSpinner spinner : spinners) {
			try {
				spinner.commitEdit();
			} catch (ParseException pe) {
				// edited value is invalid: spinner.getValue() will return the last valid value
			}
		}
	}
	
	private void setSettingsEnabled(boolean enabled) {
		setEnabledIfNotNull(btnSelectTitleFont, enabled);
		setEnabledIfNotNull(btnSelectLyricsFont, enabled);
		setEnabledIfNotNull(btnSelectTranslationFont, enabled);
		setEnabledIfNotNull(btnSelectCopyrightFont, enabled);
		setEnabledIfNotNull(btnSelectTextColor, enabled);
		setEnabledIfNotNull(btnSelectBackgroundColor, enabled);
		setEnabledIfNotNull(btnSelectLogo, enabled);
		setEnabledIfNotNull(checkboxShowTitle, enabled);
		setEnabledIfNotNull(spinnerTopMargin, enabled);
		setEnabledIfNotNull(spinnerLeftMargin, enabled);
		setEnabledIfNotNull(spinnerRightMargin, enabled);
		setEnabledIfNotNull(spinnerBottomMargin, enabled);
		setEnabledIfNotNull(spinnerDistanceTitleText, enabled);
		setEnabledIfNotNull(spinnerDistanceTextCopyright, enabled);
		setEnabledIfNotNull(comboSongListFiltering, enabled);
		setEnabledIfNotNull(comboPresentationScreen1Display, enabled);
		setEnabledIfNotNull(comboPresentationScreen1Contents, enabled);
		setEnabledIfNotNull(comboPresentationScreen2Display, enabled);
		setEnabledIfNotNull(comboPresentationScreen2Contents, enabled);
		setEnabledIfNotNull(spinnerCountAsDisplayedAfter, enabled);
		// disable the "unlock" button when enabling the other controls
		// (and the other way around)
		setEnabledIfNotNull(btnUnlock, !enabled);
	}
	
	private static void setEnabledIfNotNull(JComponent component, boolean enabled) {
		if (component != null) {
			component.setEnabled(enabled);
		}
	}
	
	protected void handleSongsListSelectionChanged(ListSelectionEvent e) {
		// only the last event in a row should fire these actions (check valueIsAdjusting)
		if (!e.getValueIsAdjusting()) {
			saveSong();
			songsListSelected = songsList.getSelectedValue();
			loadSong();
		}
	}
	
	private void saveSong() {
		if (songsListSelected != null) {
			saveSongData(songsListSelected);
			clearSongData();
			setSongEditingEnabled(false);
			// disable buttons
			btnDeleteSong.setEnabled(false);
			btnSelectSong.setEnabled(false);
			btnExportLyricsOnlyPdfSelected.setEnabled(false);
			btnExportCompletePdfSelected.setEnabled(false);
			btnExportStatisticsSelected.setEnabled(false);
		}
	}
	
	private void loadSong() {
		if (songsListSelected != null) {
			loadSongData(songsListSelected);
			setSongEditingEnabled(true);
			// enable buttons
			btnDeleteSong.setEnabled(true);
			btnSelectSong.setEnabled(true);
			btnExportLyricsOnlyPdfSelected.setEnabled(true);
			btnExportCompletePdfSelected.setEnabled(true);
			btnExportStatisticsSelected.setEnabled(true);
		}
	}
	
	private void applyFilter() {
		saveSong();
		songsListModel.refilter();
		songsListSelected = songsList.getSelectedValue();
		loadSong();
	}
	
	protected void handlePresentListSelectionChanged(ListSelectionEvent e) {
		// only the last event in a row should fire these actions (check valueIsAdjusting)
		if (!e.getValueIsAdjusting()) {
			presentListSelected = presentList.getSelectedValue();
			if (presentListSelected != null) {
				// enable buttons beneath the present list
				btnUp.setEnabled(presentList.getSelectedIndex() > 0);
				btnUnselect.setEnabled(true);
				btnDown.setEnabled(presentList.getSelectedIndex() < presentModel.getSize() - 1);
				// enable "present this song" and "jump to selected" buttons
				btnPresentSelectedSong.setEnabled(true);
				btnJumpToSelected.setEnabled(true);
			} else {
				// disable buttons beneath the present list
				btnUp.setEnabled(false);
				btnUnselect.setEnabled(false);
				btnDown.setEnabled(false);
				// disable "present this song" and "jump to selected" buttons
				btnPresentSelectedSong.setEnabled(false);
				btnJumpToSelected.setEnabled(false);
			}
		}
	}
	
	protected void handleSongDataFocusLost() {
		saveSongData(songsListSelected);
	}
	
	/**
	 * Stores all data contained in the GUI elements.
	 * 
	 * @param song the songsModel object to which the data should be written
	 */
	private synchronized void saveSongData(Song song) {
		LOG.debug("saveSongData");
		song.setLyrics(editorLyrics.getText());
		song.setTitle(textFieldTitle.getText());
		song.setLanguage((LanguageEnum) comboBoxLanguage.getSelectedItem());
		song.setTonality(textFieldTonality.getText());
		song.setComposer(textFieldComposer.getText());
		song.setAuthorText(textFieldAuthorText.getText());
		song.setAuthorTranslation(textFieldAuthorTranslation.getText());
		song.setPublisher(textFieldPublisher.getText());
		song.setAdditionalCopyrightNotes(textFieldAdditionalCopyrightNotes.getText());
		song.setSongNotes(textFieldSongNotes.getText());
		song.setChordSequence(editorChordSequence.getText());
		song.setLinkedSongs(linkedSongsListModel.getAllElements());
		// now put the songs in the right order again (the title could be edited)
		songsModel.sortAndUpdateView();
	}
	
	/**
	 * Deletes all values contained in the GUI elements of the song editing tab.
	 */
	private void clearSongData() {
		setTextAndRewind(editorLyrics, "");
		setTextAndRewind(textFieldTitle, "");
		comboBoxLanguage.setSelectedItem(null);
		setTextAndRewind(textFieldTonality, "");
		setTextAndRewind(textFieldComposer, "");
		setTextAndRewind(textFieldAuthorText, "");
		setTextAndRewind(textFieldAuthorTranslation, "");
		setTextAndRewind(textFieldPublisher, "");
		setTextAndRewind(textFieldAdditionalCopyrightNotes, "");
		setTextAndRewind(textFieldSongNotes, "");
		setTextAndRewind(editorChordSequence, "");
		linkedSongsList.setModel(new TransparentListModel<Song>(Collections.<Song> emptyList()));
	}
	
	/**
	 * Enables or disables all GUI elements of the song editing tab.
	 */
	private void setSongEditingEnabled(boolean state) {
		editorLyrics.setEnabled(state);
		textFieldTitle.setEnabled(state);
		comboBoxLanguage.setEnabled(state);
		textFieldTonality.setEnabled(state);
		textFieldComposer.setEnabled(state);
		textFieldAuthorText.setEnabled(state);
		textFieldAuthorTranslation.setEnabled(state);
		textFieldPublisher.setEnabled(state);
		textFieldAdditionalCopyrightNotes.setEnabled(state);
		textFieldSongNotes.setEnabled(state);
		editorChordSequence.setEnabled(state);
		linkedSongsList.setEnabled(state);
		btnAddLinkedSong.setEnabled(state);
		btnRemoveLinkedSong.setEnabled(state);
		if (state) {
			editorLyrics.setCaretPosition(0);
			textFieldFilter.requestFocusInWindow();
		}
	}
	
	/**
	 * Reads song data and puts the values into the GUI elements.
	 * 
	 * @param song the songsModel object which should be read
	 */
	private synchronized void loadSongData(final Song song) {
		LOG.debug("loadSongData");
		setTextAndRewind(editorLyrics, song.getLyrics());
		setTextAndRewind(textFieldTitle, song.getTitle());
		comboBoxLanguage.setSelectedItem(song.getLanguage());
		setTextAndRewind(textFieldTonality, song.getTonality());
		setTextAndRewind(textFieldComposer, song.getComposer());
		setTextAndRewind(textFieldAuthorText, song.getAuthorText());
		setTextAndRewind(textFieldAuthorTranslation, song.getAuthorTranslation());
		setTextAndRewind(textFieldPublisher, song.getPublisher());
		setTextAndRewind(textFieldAdditionalCopyrightNotes, song.getAdditionalCopyrightNotes());
		setTextAndRewind(textFieldSongNotes, song.getSongNotes());
		setTextAndRewind(editorChordSequence, song.getChordSequence());
		linkedSongsListModel = new TransparentListModel<Song>(song.getLinkedSongs());
		linkedSongsList.setModel(linkedSongsListModel);
	}
	
	private static void setTextAndRewind(JTextComponent textComponent, String textToSet) {
		textComponent.setText(textToSet);
		textComponent.setCaretPosition(0);
	}
	
	protected void handleWindowClosing() {
		if (songsListSelected != null) {
			saveSongData(songsListSelected);
		}
		handleSettingsSaveAndLock();
		boolean mayClose = controller.prepareClose();
		if (mayClose) {
			setVisible(false);
			dispose();
			controller.shutdown();
		}
	}
	
	protected void handleSongNew() {
		Song song = new Song(StringTools.createUUID());
		songsModel.addSong(song);
		applyFilter();
		songsList.setSelectedValue(song, true);
	}
	
	protected void handleSongDelete() {
		if (songsListSelected != null) {
			Song songToDelete = songsListSelected;
			songsList.removeSelectionInterval(0, songsModel.getSize() - 1);
			songsModel.removeSong(songToDelete);
			applyFilter();
		}
	}
	
	protected void handleSongSelect() {
		if (songsListSelected != null) {
			presentModel.addSong(songsListSelected);
			tabbedPane.setSelectedIndex(TAB_INDEX_PRESENT);
			presentList.requestFocusInWindow();
			setDefaultDividerLocation();
			presentList.setSelectedIndex(presentModel.getSize() - 1);
		}
	}
	
	private void setDefaultDividerLocation() {
		if (!splitPanePresentDividerLocationSet) {
			int desiredMinimumWidth = panelSongList.getWidth() + selectedSongListButtons.getWidth();
			if (splitPanePresent.getDividerLocation() < desiredMinimumWidth) {
				splitPanePresent.setDividerLocation(desiredMinimumWidth);
			}
			splitPanePresentDividerLocationSet = true;
		}
	}
	
	protected void handleSongUnselect() {
		if (presentListSelected != null) {
			// use index because there could be multiple instances of one song in the list
			presentModel.removeSong(presentList.getSelectedIndex());
			presentList.clearSelection();
		}
	}
	
	protected void handleSongUp() {
		if (presentListSelected != null) {
			// use index because there could be multiple instances of one song in the list
			int newIndex = presentList.getSelectedIndex() - 1;
			if (newIndex < 0) {
				throw new IllegalStateException("song is already first in list");
			}
			Song song = presentModel.removeSong(presentList.getSelectedIndex());
			presentModel.insertSong(newIndex, song);
			presentList.setSelectedIndex(newIndex);
		}
	}
	
	protected void handleSongDown() {
		if (presentListSelected != null) {
			// use index because there could be multiple instances of one song in the list
			int newIndex = presentList.getSelectedIndex() + 1;
			if (newIndex >= presentModel.getSize()) {
				throw new IllegalStateException("song is already last in list");
			}
			Song song = presentModel.removeSong(presentList.getSelectedIndex());
			presentModel.insertSong(newIndex, song);
			presentList.setSelectedIndex(newIndex);
		}
	}
	
	protected void handleJumpToSelectedSong() {
		if (presentListSelected != null) {
			if (!songsListModel.contains(presentListSelected)) {
				textFieldFilter.setText("");
			}
			songsList.setSelectedValue(presentListSelected, true);
		}
	}
	
	protected void handleJumpToPresentedSong() {
		Song currentlyPresentedSong = controller.getCurrentlyPresentedSong();
		if (currentlyPresentedSong != null) {
			presentList.setSelectedValue(currentlyPresentedSong, true);
			handleJumpToSelectedSong();
		}
	}
	
	protected void handleSongPresent() {
		boolean success = controller.present(new Presentable(presentListSelected, null));
		if (success) {
			clearSectionButtons();
			List<AddressablePart> parts = controller.getParts();
			int partIndex = 0;
			for (AddressablePart part : parts) {
				PartButtonGroup buttonGroup = new PartButtonGroup(part, partIndex, controller);
				panelSectionButtons.add(buttonGroup, panelSectionButtonsHints);
				partIndex++;
			}
			
			// add empty component to consume any space that is left (so the parts appear at the top of the scrollpane
			// view)
			panelSectionButtons.add(new JLabel(""), panelSectionButtonsLastRowHints);
			
			panelSectionButtons.revalidate();
			panelSectionButtons.repaint();
			btnJumpToPresented.setEnabled(true);
		}
	}
	
	protected void handleBlankScreen() {
		boolean success = controller.present(BLANK_SCREEN);
		if (success) {
			clearSectionButtons();
			btnJumpToPresented.setEnabled(false);
		}
	}
	
	protected void handleLogoPresent() {
		boolean success = controller.present(new Presentable(null, loadLogo()));
		if (success) {
			clearSectionButtons();
			btnJumpToPresented.setEnabled(false);
		}
	}
	
	private void clearSectionButtons() {
		panelSectionButtons.removeAll();
		panelSectionButtons.revalidate();
		panelSectionButtons.repaint();
	}
	
	private Image loadLogo() {
		String logoPath = settingsModel.getString(SettingKey.LOGO_FILE);
		if (logoPath != null && !logoPath.equals("")) {
			File logoFile = new File(logoPath);
			if (logoFile.isFile() && logoFile.canRead()) {
				Image logo = null;
				try {
					logo = ImageIO.read(logoFile);
				} catch (IOException ex) {
					handleError(ex);
				}
				return logo;
			}
		}
		return null;
	}
	
	protected void handleImportFromSDBv1() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("choose file to import");
		CustomFileFilter filter = new CustomFileFilter("SDB 1.x database files", ".sdb");
		chooser.addChoosableFileFilter(filter);
		int iValue = chooser.showOpenDialog(this);
		
		if (iValue == JFileChooser.APPROVE_OPTION) {
			List<Song> imported = null;
			ImportFromSDBv1 importer = new ImportFromSDBv1();
			imported = importer.loadFromFile(chooser.getSelectedFile());
			if (imported != null) {
				for (Song song : imported) {
					songsModel.addSong(song);
				}
				applyFilter();
			}
		}
	}
	
	private final void defineShortcuts() {
		shortcutManager = new KeyboardShortcutManager();
		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusManager.addKeyEventDispatcher(shortcutManager);
		
		shortcutManager.add(new KeyboardShortcut(KeyEvent.VK_ESCAPE, false, false, false) {
			@Override
			public void doAction() {
				LOG.debug("escape action");
				if (textFieldFilter.isFocusOwner()) {
					// empty the search field
					textFieldFilter.setText("");
				} else {
					// focus the search field:
					// NOT using requestFocusInWindow() because then the presentation
					// could prevent the field from being focused (if the user configured
					// a presentation to be on the primary display)
					textFieldFilter.requestFocus();
				}
			}
		});
	}
	
	public void handleError(Throwable ex) {
		StringBuilder ret = new StringBuilder();
		ret.append(ex.getMessage());
		ret.append(NEWLINE);
		ret.append(NEWLINE);
		buildStackTraceText(ex, ret);
		showErrorDialog(ret.toString());
	}
	
	public void showErrorDialog(String text) {
		ErrorDialog.openDialog(this, text);
	}
	
	private static void buildStackTraceText(Throwable ex, StringBuilder sb) {
		sb.append(ex.getClass().getCanonicalName());
		sb.append(": ");
		sb.append(ex.getMessage());
		sb.append(NEWLINE);
		if (ex.getStackTrace() != null) {
			for (int i = 0; i < STACKTRACE_ELEMENT_COUNT && i < ex.getStackTrace().length; i++) {
				sb.append(STACKTRACE_INDENTATION);
				sb.append(ex.getStackTrace()[i].toString());
				sb.append(NEWLINE);
			}
		}
		if (ex.getCause() != null && ex.getCause() != ex) {
			sb.append("CAUSED BY:" + NEWLINE);
			buildStackTraceText(ex.getCause(), sb);
		}
	}
	
	public MainWindow(MainController mainController) {
		setIconImages(getIconsFromResources(getClass()));
		setTitle("Song Database");
		controller = mainController;
		defineShortcuts();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					handleWindowClosing();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				try {
					// on every window deactivation, lock the settings again
					handleSettingsSaveAndLock();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		calculateAndSetBounds();
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder(null);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		panelSongList = new JPanel();
		panelSongList.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPane.setLeftComponent(panelSongList);
		panelSongList.setLayout(new BorderLayout(0, 0));
		
		JPanel panelFilter = new JPanel();
		panelFilter.setBorder(null);
		panelSongList.add(panelFilter, BorderLayout.NORTH);
		GridBagLayout gbl_panelFilter = new GridBagLayout();
		gbl_panelFilter.columnWidths = new int[] {39, 114, 22, 0};
		gbl_panelFilter.rowHeights = new int[] {22, 0};
		gbl_panelFilter.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panelFilter.rowWeights = new double[] {0.0, Double.MIN_VALUE};
		panelFilter.setLayout(gbl_panelFilter);
		
		JLabel lblFilter = new JLabel("Filter:");
		GridBagConstraints gbc_lblFilter = new GridBagConstraints();
		gbc_lblFilter.anchor = GridBagConstraints.WEST;
		gbc_lblFilter.insets = new Insets(0, 0, 0, 5);
		gbc_lblFilter.gridx = 0;
		gbc_lblFilter.gridy = 0;
		panelFilter.add(lblFilter, gbc_lblFilter);
		
		textFieldFilter = new JTextField();
		GridBagConstraints gbc_textFieldFilter = new GridBagConstraints();
		gbc_textFieldFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldFilter.insets = new Insets(0, 0, 0, 5);
		gbc_textFieldFilter.gridx = 1;
		gbc_textFieldFilter.gridy = 0;
		panelFilter.add(textFieldFilter, gbc_textFieldFilter);
		textFieldFilter.setColumns(10);
		
		btnClearFilter = new JButton("");
		btnClearFilter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textFieldFilter.setText("");
				textFieldFilter.requestFocusInWindow();
			}
		});
		btnClearFilter.setMargin(new Insets(0, 0, 0, 0));
		btnClearFilter.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/clear.gif"));
		GridBagConstraints gbc_btnClearFilter = new GridBagConstraints();
		gbc_btnClearFilter.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearFilter.gridx = 2;
		gbc_btnClearFilter.gridy = 0;
		panelFilter.add(btnClearFilter, gbc_btnClearFilter);
		
		JScrollPane scrollPaneSongList = new JScrollPane();
		scrollPaneSongList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelSongList.add(scrollPaneSongList, BorderLayout.CENTER);
		
		songsList = new FixedWidthJList<Song>();
		songsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (e.getClickCount() >= 2 && songsListSelected != null) {
						// double-clicked: put into present list
						handleSongSelect();
					}
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		songsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		((DefaultListSelectionModel) songsList.getSelectionModel())
			.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					try {
						handleSongsListSelectionChanged(e);
					} catch (Throwable ex) {
						handleError(ex);
					}
				}
			});
		scrollPaneSongList.setViewportView(songsList);
		songsList.setCellRenderer(new SongCellRenderer());
		
		JPanel panelSongListButtons = new JPanel();
		panelSongList.add(panelSongListButtons, BorderLayout.SOUTH);
		GridBagLayout gbl_panelSongListButtons = new GridBagLayout();
		gbl_panelSongListButtons.columnWidths = new int[] {0, 0, 0};
		gbl_panelSongListButtons.rowHeights = new int[] {26};
		gbl_panelSongListButtons.columnWeights = new double[] {0.0, 0.0, 0.0};
		gbl_panelSongListButtons.rowWeights = new double[] {0.0};
		panelSongListButtons.setLayout(gbl_panelSongListButtons);
		
		btnNewSong = new JButton("New");
		btnNewSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSongNew();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnNewSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/newHighlighter.gif"));
		GridBagConstraints gbc_btnNewSong = new GridBagConstraints();
		gbc_btnNewSong.fill = GridBagConstraints.VERTICAL;
		gbc_btnNewSong.anchor = GridBagConstraints.WEST;
		gbc_btnNewSong.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewSong.gridx = 0;
		gbc_btnNewSong.gridy = 0;
		panelSongListButtons.add(btnNewSong, gbc_btnNewSong);
		
		btnDeleteSong = new JButton("Delete");
		btnDeleteSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSongDelete();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnDeleteSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/deleteHighlighter.gif"));
		GridBagConstraints gbc_btnDeleteSong = new GridBagConstraints();
		gbc_btnDeleteSong.fill = GridBagConstraints.VERTICAL;
		gbc_btnDeleteSong.anchor = GridBagConstraints.WEST;
		gbc_btnDeleteSong.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSong.gridx = 1;
		gbc_btnDeleteSong.gridy = 0;
		panelSongListButtons.add(btnDeleteSong, gbc_btnDeleteSong);
		
		btnSelectSong = new JButton("Select");
		btnSelectSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSongSelect();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnSelectSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/month-up.png"));
		GridBagConstraints gbc_btnSelectSong = new GridBagConstraints();
		gbc_btnSelectSong.fill = GridBagConstraints.VERTICAL;
		gbc_btnSelectSong.anchor = GridBagConstraints.EAST;
		gbc_btnSelectSong.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectSong.gridx = 2;
		gbc_btnSelectSong.gridy = 0;
		panelSongListButtons.add(btnSelectSong, gbc_btnSelectSong);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// on every tab switch, lock the settings again
				handleSettingsSaveAndLock();
			}
		});
		tabbedPane.setBorder(null);
		splitPane.setRightComponent(tabbedPane);
		
		JPanel panelEdit = new JPanel();
		panelEdit.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Edit Song", null, panelEdit, null);
		GridBagLayout gbl_panelEdit = new GridBagLayout();
		gbl_panelEdit.columnWidths = new int[] {9999, 9999, 9999};
		gbl_panelEdit.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelEdit.columnWeights = new double[] {1.0, 1.0, 1.0};
		gbl_panelEdit.rowWeights =
			new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panelEdit.setLayout(gbl_panelEdit);
		
		JLabel lblLyricsAndChords = new JLabel("Lyrics and Chords");
		GridBagConstraints gbc_lblLyricsAndChords = new GridBagConstraints();
		gbc_lblLyricsAndChords.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblLyricsAndChords.gridwidth = 3;
		gbc_lblLyricsAndChords.insets = new Insets(0, 0, 5, 0);
		gbc_lblLyricsAndChords.gridx = 0;
		gbc_lblLyricsAndChords.gridy = 0;
		panelEdit.add(lblLyricsAndChords, gbc_lblLyricsAndChords);
		
		JScrollPane scrollPaneLyrics = new JScrollPane();
		GridBagConstraints gbc_scrollPaneLyrics = new GridBagConstraints();
		gbc_scrollPaneLyrics.weighty = 7.0;
		gbc_scrollPaneLyrics.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneLyrics.gridwidth = 3;
		gbc_scrollPaneLyrics.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPaneLyrics.gridx = 0;
		gbc_scrollPaneLyrics.gridy = 1;
		panelEdit.add(scrollPaneLyrics, gbc_scrollPaneLyrics);
		
		editorLyrics = new JEditorPane();
		editorLyrics.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		editorLyrics
			.setFont(new Font("Monospaced", editorLyrics.getFont().getStyle(), editorLyrics.getFont().getSize()));
		editorLyrics.setBackground(Color.WHITE);
		scrollPaneLyrics.setViewportView(editorLyrics);
		
		JLabel lblTitle = new JLabel("Title");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 2;
		panelEdit.add(lblTitle, gbc_lblTitle);
		
		JLabel lblComposer = new JLabel("Composer (Music)");
		GridBagConstraints gbc_lblComposer = new GridBagConstraints();
		gbc_lblComposer.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblComposer.insets = new Insets(0, 0, 5, 5);
		gbc_lblComposer.gridx = 1;
		gbc_lblComposer.gridy = 2;
		panelEdit.add(lblComposer, gbc_lblComposer);
		
		JLabel lblPublisher = new JLabel("Publisher");
		GridBagConstraints gbc_lblPublisher = new GridBagConstraints();
		gbc_lblPublisher.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPublisher.insets = new Insets(0, 0, 5, 0);
		gbc_lblPublisher.gridx = 2;
		gbc_lblPublisher.gridy = 2;
		panelEdit.add(lblPublisher, gbc_lblPublisher);
		
		textFieldTitle = new JTextField();
		textFieldTitle.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldTitle = new GridBagConstraints();
		gbc_textFieldTitle.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTitle.gridx = 0;
		gbc_textFieldTitle.gridy = 3;
		panelEdit.add(textFieldTitle, gbc_textFieldTitle);
		textFieldTitle.setColumns(10);
		
		textFieldComposer = new JTextField();
		textFieldComposer.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldComposer = new GridBagConstraints();
		gbc_textFieldComposer.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldComposer.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldComposer.gridx = 1;
		gbc_textFieldComposer.gridy = 3;
		panelEdit.add(textFieldComposer, gbc_textFieldComposer);
		textFieldComposer.setColumns(10);
		
		textFieldPublisher = new JTextField();
		textFieldPublisher.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldPublisher = new GridBagConstraints();
		gbc_textFieldPublisher.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldPublisher.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPublisher.gridx = 2;
		gbc_textFieldPublisher.gridy = 3;
		panelEdit.add(textFieldPublisher, gbc_textFieldPublisher);
		textFieldPublisher.setColumns(10);
		
		JLabel lblLanguage = new JLabel("Language");
		GridBagConstraints gbc_lblLanguage = new GridBagConstraints();
		gbc_lblLanguage.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblLanguage.insets = new Insets(0, 0, 5, 5);
		gbc_lblLanguage.gridx = 0;
		gbc_lblLanguage.gridy = 4;
		panelEdit.add(lblLanguage, gbc_lblLanguage);
		
		JLabel lblAuthorText = new JLabel("Author (Text)");
		GridBagConstraints gbc_lblAuthorText = new GridBagConstraints();
		gbc_lblAuthorText.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAuthorText.insets = new Insets(0, 0, 5, 5);
		gbc_lblAuthorText.gridx = 1;
		gbc_lblAuthorText.gridy = 4;
		panelEdit.add(lblAuthorText, gbc_lblAuthorText);
		
		JLabel lblAdditionalCopyrightNotes = new JLabel("Additional Copyright Notes");
		GridBagConstraints gbc_lblAdditionalCopyrightNotes = new GridBagConstraints();
		gbc_lblAdditionalCopyrightNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAdditionalCopyrightNotes.insets = new Insets(0, 0, 5, 0);
		gbc_lblAdditionalCopyrightNotes.gridx = 2;
		gbc_lblAdditionalCopyrightNotes.gridy = 4;
		panelEdit.add(lblAdditionalCopyrightNotes, gbc_lblAdditionalCopyrightNotes);
		
		comboBoxLanguage = new JComboBox<LanguageEnum>();
		comboBoxLanguage.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_comboBoxLanguage = new GridBagConstraints();
		gbc_comboBoxLanguage.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxLanguage.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxLanguage.gridx = 0;
		gbc_comboBoxLanguage.gridy = 5;
		panelEdit.add(comboBoxLanguage, gbc_comboBoxLanguage);
		
		textFieldAuthorText = new JTextField();
		textFieldAuthorText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldAuthorText = new GridBagConstraints();
		gbc_textFieldAuthorText.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldAuthorText.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldAuthorText.gridx = 1;
		gbc_textFieldAuthorText.gridy = 5;
		panelEdit.add(textFieldAuthorText, gbc_textFieldAuthorText);
		textFieldAuthorText.setColumns(10);
		
		textFieldAdditionalCopyrightNotes = new JTextField();
		textFieldAdditionalCopyrightNotes.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldAdditionalCopyrightNotes = new GridBagConstraints();
		gbc_textFieldAdditionalCopyrightNotes.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldAdditionalCopyrightNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldAdditionalCopyrightNotes.gridx = 2;
		gbc_textFieldAdditionalCopyrightNotes.gridy = 5;
		panelEdit.add(textFieldAdditionalCopyrightNotes, gbc_textFieldAdditionalCopyrightNotes);
		textFieldAdditionalCopyrightNotes.setColumns(10);
		
		JLabel lblTonality = new JLabel("Tonality");
		GridBagConstraints gbc_lblTonality = new GridBagConstraints();
		gbc_lblTonality.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTonality.insets = new Insets(0, 0, 5, 5);
		gbc_lblTonality.gridx = 0;
		gbc_lblTonality.gridy = 6;
		panelEdit.add(lblTonality, gbc_lblTonality);
		
		JLabel lblAuthorTranslation = new JLabel("Author (Translation)");
		GridBagConstraints gbc_lblAuthorTranslation = new GridBagConstraints();
		gbc_lblAuthorTranslation.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAuthorTranslation.insets = new Insets(0, 0, 5, 5);
		gbc_lblAuthorTranslation.gridx = 1;
		gbc_lblAuthorTranslation.gridy = 6;
		panelEdit.add(lblAuthorTranslation, gbc_lblAuthorTranslation);
		
		JLabel lblSongNotes = new JLabel("Song Notes (not shown in presentation)");
		GridBagConstraints gbc_lblSongNotes = new GridBagConstraints();
		gbc_lblSongNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSongNotes.insets = new Insets(0, 0, 5, 0);
		gbc_lblSongNotes.gridx = 2;
		gbc_lblSongNotes.gridy = 6;
		panelEdit.add(lblSongNotes, gbc_lblSongNotes);
		
		textFieldTonality = new JTextField();
		textFieldTonality.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldTonality = new GridBagConstraints();
		gbc_textFieldTonality.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTonality.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTonality.gridx = 0;
		gbc_textFieldTonality.gridy = 7;
		panelEdit.add(textFieldTonality, gbc_textFieldTonality);
		textFieldTonality.setColumns(10);
		
		textFieldAuthorTranslation = new JTextField();
		textFieldAuthorTranslation.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
					
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldAuthorTranslation = new GridBagConstraints();
		gbc_textFieldAuthorTranslation.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldAuthorTranslation.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldAuthorTranslation.gridx = 1;
		gbc_textFieldAuthorTranslation.gridy = 7;
		panelEdit.add(textFieldAuthorTranslation, gbc_textFieldAuthorTranslation);
		textFieldAuthorTranslation.setColumns(10);
		
		textFieldSongNotes = new JTextField();
		textFieldSongNotes.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_textFieldSongNotes = new GridBagConstraints();
		gbc_textFieldSongNotes.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldSongNotes.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSongNotes.gridx = 2;
		gbc_textFieldSongNotes.gridy = 7;
		panelEdit.add(textFieldSongNotes, gbc_textFieldSongNotes);
		textFieldSongNotes.setColumns(10);
		
		JLabel lblChordSequence = new JLabel("Chord Sequence");
		GridBagConstraints gbc_lblChordSequence = new GridBagConstraints();
		gbc_lblChordSequence.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblChordSequence.gridwidth = 2;
		gbc_lblChordSequence.insets = new Insets(0, 0, 5, 5);
		gbc_lblChordSequence.gridx = 0;
		gbc_lblChordSequence.gridy = 8;
		panelEdit.add(lblChordSequence, gbc_lblChordSequence);
		
		JLabel lblLinkedSongs = new JLabel("Linked Songs");
		GridBagConstraints gbc_lblLinkedSongs = new GridBagConstraints();
		gbc_lblLinkedSongs.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblLinkedSongs.insets = new Insets(0, 0, 5, 0);
		gbc_lblLinkedSongs.gridx = 2;
		gbc_lblLinkedSongs.gridy = 8;
		panelEdit.add(lblLinkedSongs, gbc_lblLinkedSongs);
		
		JScrollPane scrollPaneChordSequence = new JScrollPane();
		GridBagConstraints gbc_scrollPaneChordSequence = new GridBagConstraints();
		gbc_scrollPaneChordSequence.gridheight = 2;
		gbc_scrollPaneChordSequence.weighty = 1.0;
		gbc_scrollPaneChordSequence.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneChordSequence.gridwidth = 2;
		gbc_scrollPaneChordSequence.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneChordSequence.gridx = 0;
		gbc_scrollPaneChordSequence.gridy = 9;
		panelEdit.add(scrollPaneChordSequence, gbc_scrollPaneChordSequence);
		
		editorChordSequence = new JEditorPane();
		editorChordSequence.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		editorChordSequence.setFont(new Font("Monospaced", editorChordSequence.getFont().getStyle(),
			editorChordSequence.getFont().getSize()));
		scrollPaneChordSequence.setViewportView(editorChordSequence);
		editorChordSequence.setBackground(Color.WHITE);
		
		JScrollPane scrollPaneLinkedSongsList = new JScrollPane();
		GridBagConstraints gbc_scrollPaneLinkedSongsList = new GridBagConstraints();
		gbc_scrollPaneLinkedSongsList.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPaneLinkedSongsList.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneLinkedSongsList.gridx = 2;
		gbc_scrollPaneLinkedSongsList.gridy = 9;
		panelEdit.add(scrollPaneLinkedSongsList, gbc_scrollPaneLinkedSongsList);
		
		linkedSongsList = new FixedWidthJList<Song>();
		scrollPaneLinkedSongsList.setViewportView(linkedSongsList);
		
		JPanel panelLinkedSongs = new JPanel();
		GridBagConstraints gbc_panelLinkedSongs = new GridBagConstraints();
		gbc_panelLinkedSongs.fill = GridBagConstraints.BOTH;
		gbc_panelLinkedSongs.gridx = 2;
		gbc_panelLinkedSongs.gridy = 10;
		panelEdit.add(panelLinkedSongs, gbc_panelLinkedSongs);
		GridBagLayout gbl_panelLinkedSongs = new GridBagLayout();
		gbl_panelLinkedSongs.columnWidths = new int[] {0, 0};
		gbl_panelLinkedSongs.rowHeights = new int[] {26};
		gbl_panelLinkedSongs.columnWeights = new double[] {1.0, 1.0};
		gbl_panelLinkedSongs.rowWeights = new double[] {0.0};
		panelLinkedSongs.setLayout(gbl_panelLinkedSongs);
		
		btnAddLinkedSong = new JButton("Add");
		btnAddLinkedSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// TODO
					
					// now save the edited song
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnAddLinkedSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/newHighlighter.gif"));
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.BOTH;
		gbc_btnAdd.insets = new Insets(0, 0, 0, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		panelLinkedSongs.add(btnAddLinkedSong, gbc_btnAdd);
		
		btnRemoveLinkedSong = new JButton("Remove");
		btnRemoveLinkedSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// TODO
					
					// now save the edited song
					handleSongDataFocusLost();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnRemoveLinkedSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/deleteHighlighter.gif"));
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.fill = GridBagConstraints.BOTH;
		gbc_btnRemove.gridx = 1;
		gbc_btnRemove.gridy = 0;
		panelLinkedSongs.add(btnRemoveLinkedSong, gbc_btnRemove);
		
		JPanel panelPresent = new JPanel();
		tabbedPane.addTab("Present Songs", null, panelPresent, null);
		panelPresent.setLayout(new BorderLayout(0, 0));
		
		splitPanePresent = new JSplitPane();
		splitPanePresent.setBorder(null);
		panelPresent.add(splitPanePresent, BorderLayout.CENTER);
		
		JPanel panelPresentLeft = new JPanel();
		panelPresentLeft.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPanePresent.setLeftComponent(panelPresentLeft);
		panelPresentLeft.setLayout(new BorderLayout(0, 0));
		splitPanePresent.setDividerLocation(-1);
		
		JScrollPane scrollPanePresentSongList = new JScrollPane();
		scrollPanePresentSongList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelPresentLeft.add(scrollPanePresentSongList, BorderLayout.CENTER);
		
		presentList = new FixedWidthJList<Song>();
		presentList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				try {
					handlePresentListSelectionChanged(e);
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		presentList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (e.getClickCount() >= 2 && presentListSelected != null) {
						// double-clicked: present this song
						handleSongPresent();
					}
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		presentList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					try {
						handleSongUnselect();
					} catch (Throwable ex) {
						handleError(ex);
					}
				}
			}
		});
		presentList.setCellRenderer(new SongCellRenderer());
		scrollPanePresentSongList.setViewportView(presentList);
		
		selectedSongListButtons = new JPanel();
		panelPresentLeft.add(selectedSongListButtons, BorderLayout.EAST);
		GridBagLayout gbl_panelSelectedSongListButtons = new GridBagLayout();
		gbl_panelSelectedSongListButtons.columnWidths = new int[] {0};
		gbl_panelSelectedSongListButtons.rowHeights = new int[] {0, 0, 0, 0, 0};
		gbl_panelSelectedSongListButtons.columnWeights = new double[] {0.0};
		gbl_panelSelectedSongListButtons.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0};
		selectedSongListButtons.setLayout(gbl_panelSelectedSongListButtons);
		
		btnUp = new JButton("");
		btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSongUp();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnUp.setToolTipText("Up");
		btnUp.setIcon(ResourceTools.getIcon(getClass(), "/javax/swing/plaf/metal/icons/sortUp.png"));
		GridBagConstraints gbc_btnUp = new GridBagConstraints();
		gbc_btnUp.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUp.anchor = GridBagConstraints.SOUTH;
		gbc_btnUp.insets = new Insets(0, 0, 5, 0);
		gbc_btnUp.gridx = 0;
		gbc_btnUp.gridy = 1;
		selectedSongListButtons.add(btnUp, gbc_btnUp);
		
		btnUnselect = new JButton("");
		btnUnselect.setToolTipText("Unselect");
		btnUnselect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSongUnselect();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnUnselect.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/JXErrorPane16.png"));
		GridBagConstraints gbc_btnUnselect = new GridBagConstraints();
		gbc_btnUnselect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUnselect.insets = new Insets(0, 0, 5, 0);
		gbc_btnUnselect.anchor = GridBagConstraints.NORTH;
		gbc_btnUnselect.gridx = 0;
		gbc_btnUnselect.gridy = 2;
		selectedSongListButtons.add(btnUnselect, gbc_btnUnselect);
		
		btnDown = new JButton("");
		btnDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSongDown();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		btnDown.setIcon(ResourceTools.getIcon(getClass(), "/javax/swing/plaf/metal/icons/sortDown.png"));
		btnDown.setToolTipText("Down");
		GridBagConstraints gbc_btnDown = new GridBagConstraints();
		gbc_btnDown.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDown.anchor = GridBagConstraints.NORTH;
		gbc_btnDown.gridx = 0;
		gbc_btnDown.gridy = 3;
		selectedSongListButtons.add(btnDown, gbc_btnDown);
		
		btnJumpToSelected = new JButton("Jump to selected song");
		btnJumpToSelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleJumpToSelectedSong();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		panelPresentLeft.add(btnJumpToSelected, BorderLayout.SOUTH);
		
		JPanel panelPresentRight = new JPanel();
		panelPresentRight.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPanePresent.setRightComponent(panelPresentRight);
		panelPresentRight.setLayout(new BorderLayout(0, 0));
		
		JPanel panelPresentationButtons = new JPanel();
		panelPresentRight.add(panelPresentationButtons, BorderLayout.CENTER);
		GridBagLayout gbl_panelPresentationButtons = new GridBagLayout();
		gbl_panelPresentationButtons.columnWidths = new int[] {0, 0};
		gbl_panelPresentationButtons.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
		gbl_panelPresentationButtons.columnWeights = new double[] {1.0, Double.MIN_VALUE};
		gbl_panelPresentationButtons.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panelPresentationButtons.setLayout(gbl_panelPresentationButtons);
		
		btnShowLogo = new JButton("Show logo");
		btnShowLogo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleLogoPresent();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnShowLogo = new GridBagConstraints();
		gbc_btnShowLogo.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnShowLogo.insets = new Insets(0, 0, 5, 0);
		gbc_btnShowLogo.gridx = 0;
		gbc_btnShowLogo.gridy = 0;
		panelPresentationButtons.add(btnShowLogo, gbc_btnShowLogo);
		
		btnShowBlankScreen = new JButton("Blank screen");
		btnShowBlankScreen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleBlankScreen();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnShowBlankScreen = new GridBagConstraints();
		gbc_btnShowBlankScreen.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnShowBlankScreen.insets = new Insets(0, 0, 5, 0);
		gbc_btnShowBlankScreen.gridx = 0;
		gbc_btnShowBlankScreen.gridy = 1;
		panelPresentationButtons.add(btnShowBlankScreen, gbc_btnShowBlankScreen);
		
		btnPresentSelectedSong = new JButton("Present selected song");
		btnPresentSelectedSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSongPresent();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnPresentSelectedSong = new GridBagConstraints();
		gbc_btnPresentSelectedSong.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPresentSelectedSong.insets = new Insets(0, 0, 5, 0);
		gbc_btnPresentSelectedSong.gridx = 0;
		gbc_btnPresentSelectedSong.gridy = 2;
		panelPresentationButtons.add(btnPresentSelectedSong, gbc_btnPresentSelectedSong);
		
		JLabel lblSections = new JLabel("Sections:");
		GridBagConstraints gbc_lblSections = new GridBagConstraints();
		gbc_lblSections.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSections.insets = new Insets(0, 0, 5, 0);
		gbc_lblSections.gridx = 0;
		gbc_lblSections.gridy = 3;
		panelPresentationButtons.add(lblSections, gbc_lblSections);
		
		scrollPaneSectionButtons = new JScrollPane();
		GridBagConstraints gbc_scrollPaneSectionButtons = new GridBagConstraints();
		gbc_scrollPaneSectionButtons.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneSectionButtons.gridx = 0;
		gbc_scrollPaneSectionButtons.gridy = 4;
		panelPresentationButtons.add(scrollPaneSectionButtons, gbc_scrollPaneSectionButtons);
		
		panelSectionButtons = new JPanel();
		scrollPaneSectionButtons.setViewportView(panelSectionButtons);
		scrollPaneSectionButtons.getVerticalScrollBar().setUnitIncrement(30);
		panelSectionButtonsHints = new GridBagConstraints();
		panelSectionButtonsHints.gridx = 0;
		panelSectionButtonsHints.gridy = GridBagConstraints.RELATIVE;
		panelSectionButtonsHints.weightx = 1.0;
		panelSectionButtonsHints.weighty = GridBagConstraints.RELATIVE;
		panelSectionButtonsHints.fill = GridBagConstraints.HORIZONTAL;
		panelSectionButtonsLastRowHints = new GridBagConstraints();
		panelSectionButtonsLastRowHints.gridx = 0;
		panelSectionButtonsLastRowHints.gridy = GridBagConstraints.RELATIVE;
		panelSectionButtonsLastRowHints.weightx = 1.0;
		panelSectionButtonsLastRowHints.weighty = 1.0;
		panelSectionButtonsLastRowHints.fill = GridBagConstraints.BOTH;
		panelSectionButtons.setLayout(new GridBagLayout());
		
		btnJumpToPresented = new JButton("Jump to presented song");
		btnJumpToPresented.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleJumpToPresentedSong();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		panelPresentRight.add(btnJumpToPresented, BorderLayout.SOUTH);
		
		JPanel panelImportExportStatistics = new JPanel();
		panelImportExportStatistics.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Import / Export / Statistics", null, panelImportExportStatistics, null);
		GridBagLayout gbl_panelImportExportStatistics = new GridBagLayout();
		gbl_panelImportExportStatistics.columnWidths = new int[] {0, 70, 0, 0};
		gbl_panelImportExportStatistics.rowHeights = new int[] {30, 0, 0, 30, 30, 0, 0, 0, 30, 0, 30, 30, 0};
		gbl_panelImportExportStatistics.columnWeights = new double[] {1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panelImportExportStatistics.rowWeights =
			new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelImportExportStatistics.setLayout(gbl_panelImportExportStatistics);
		
		JLabel lblSelectedSong2 = new JLabel("Selected Song");
		GridBagConstraints gbc_lblSelectedSong2 = new GridBagConstraints();
		gbc_lblSelectedSong2.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSelectedSong2.gridwidth = 3;
		gbc_lblSelectedSong2.anchor = GridBagConstraints.SOUTH;
		gbc_lblSelectedSong2.insets = new Insets(0, 0, 5, 0);
		gbc_lblSelectedSong2.gridx = 0;
		gbc_lblSelectedSong2.gridy = 0;
		panelImportExportStatistics.add(lblSelectedSong2, gbc_lblSelectedSong2);
		
		btnExportLyricsOnlyPdfSelected = new JButton("Export lyrics-only PDF");
		GridBagConstraints gbc_btnExportLyricsOnlyPdfSelected = new GridBagConstraints();
		gbc_btnExportLyricsOnlyPdfSelected.anchor = GridBagConstraints.NORTH;
		gbc_btnExportLyricsOnlyPdfSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportLyricsOnlyPdfSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportLyricsOnlyPdfSelected.gridx = 0;
		gbc_btnExportLyricsOnlyPdfSelected.gridy = 1;
		panelImportExportStatistics.add(btnExportLyricsOnlyPdfSelected, gbc_btnExportLyricsOnlyPdfSelected);
		
		lblStatistics = new JLabel("<STATISTICS>");
		GridBagConstraints gbc_lblStatistics = new GridBagConstraints();
		gbc_lblStatistics.anchor = GridBagConstraints.NORTH;
		gbc_lblStatistics.gridheight = 3;
		gbc_lblStatistics.insets = new Insets(0, 0, 5, 0);
		gbc_lblStatistics.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblStatistics.gridx = 2;
		gbc_lblStatistics.gridy = 1;
		panelImportExportStatistics.add(lblStatistics, gbc_lblStatistics);
		
		btnExportCompletePdfSelected = new JButton("Export complete PDF");
		GridBagConstraints gbc_btnExportCompletePdfSelected = new GridBagConstraints();
		gbc_btnExportCompletePdfSelected.anchor = GridBagConstraints.NORTH;
		gbc_btnExportCompletePdfSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportCompletePdfSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportCompletePdfSelected.gridx = 0;
		gbc_btnExportCompletePdfSelected.gridy = 2;
		panelImportExportStatistics.add(btnExportCompletePdfSelected, gbc_btnExportCompletePdfSelected);
		
		btnExportStatisticsSelected = new JButton("Export statistics");
		GridBagConstraints gbc_btnExportStatisticsSelected = new GridBagConstraints();
		gbc_btnExportStatisticsSelected.anchor = GridBagConstraints.NORTH;
		gbc_btnExportStatisticsSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportStatisticsSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportStatisticsSelected.gridx = 0;
		gbc_btnExportStatisticsSelected.gridy = 3;
		panelImportExportStatistics.add(btnExportStatisticsSelected, gbc_btnExportStatisticsSelected);
		
		JLabel lblAllSongs2 = new JLabel("All Songs");
		GridBagConstraints gbc_lblAllSongs2 = new GridBagConstraints();
		gbc_lblAllSongs2.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAllSongs2.insets = new Insets(0, 0, 5, 0);
		gbc_lblAllSongs2.gridwidth = 3;
		gbc_lblAllSongs2.anchor = GridBagConstraints.SOUTH;
		gbc_lblAllSongs2.gridx = 0;
		gbc_lblAllSongs2.gridy = 4;
		panelImportExportStatistics.add(lblAllSongs2, gbc_lblAllSongs2);
		
		btnExportLyricsOnlyPdfAll = new JButton("Export lyrics-only PDF");
		GridBagConstraints gbc_btnExportLyricsOnlyPdfAll = new GridBagConstraints();
		gbc_btnExportLyricsOnlyPdfAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportLyricsOnlyPdfAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportLyricsOnlyPdfAll.gridx = 0;
		gbc_btnExportLyricsOnlyPdfAll.gridy = 5;
		panelImportExportStatistics.add(btnExportLyricsOnlyPdfAll, gbc_btnExportLyricsOnlyPdfAll);
		
		btnExportCompletePdfAll = new JButton("Export complete PDF");
		GridBagConstraints gbc_btnExportCompletePdfAll = new GridBagConstraints();
		gbc_btnExportCompletePdfAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportCompletePdfAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportCompletePdfAll.gridx = 0;
		gbc_btnExportCompletePdfAll.gridy = 6;
		panelImportExportStatistics.add(btnExportCompletePdfAll, gbc_btnExportCompletePdfAll);
		
		btnExportStatisticsAll = new JButton("Export statistics");
		GridBagConstraints gbc_btnExportStatisticsAll = new GridBagConstraints();
		gbc_btnExportStatisticsAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportStatisticsAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportStatisticsAll.gridx = 0;
		gbc_btnExportStatisticsAll.gridy = 7;
		panelImportExportStatistics.add(btnExportStatisticsAll, gbc_btnExportStatisticsAll);
		
		JLabel lblImportingSongs = new JLabel("Importing Songs");
		GridBagConstraints gbc_lblImportingSongs = new GridBagConstraints();
		gbc_lblImportingSongs.gridwidth = 3;
		gbc_lblImportingSongs.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblImportingSongs.anchor = GridBagConstraints.SOUTH;
		gbc_lblImportingSongs.insets = new Insets(0, 0, 5, 0);
		gbc_lblImportingSongs.gridx = 0;
		gbc_lblImportingSongs.gridy = 8;
		panelImportExportStatistics.add(lblImportingSongs, gbc_lblImportingSongs);
		
		btnImportFromSdb1 = new JButton("Import from SDB 1.x");
		btnImportFromSdb1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleImportFromSDBv1();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnImportFromSdb1 = new GridBagConstraints();
		gbc_btnImportFromSdb1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnImportFromSdb1.insets = new Insets(0, 0, 5, 5);
		gbc_btnImportFromSdb1.gridx = 0;
		gbc_btnImportFromSdb1.gridy = 9;
		panelImportExportStatistics.add(btnImportFromSdb1, gbc_btnImportFromSdb1);
		
		JLabel lblProgramVersionTitle = new JLabel("Program Version");
		lblProgramVersionTitle.setFont(new Font("DejaVu Sans", Font.ITALIC, 12));
		GridBagConstraints gbc_lblProgramVersionTitle = new GridBagConstraints();
		gbc_lblProgramVersionTitle.gridwidth = 3;
		gbc_lblProgramVersionTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblProgramVersionTitle.anchor = GridBagConstraints.SOUTH;
		gbc_lblProgramVersionTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblProgramVersionTitle.gridx = 0;
		gbc_lblProgramVersionTitle.gridy = 10;
		panelImportExportStatistics.add(lblProgramVersionTitle, gbc_lblProgramVersionTitle);
		
		lblProgramVersion = new JLabel("<PROGRAM VERSION>");
		lblProgramVersion.setBorder(new EmptyBorder(0, 0, 0, 0));
		lblProgramVersion.setFont(new Font("DejaVu Sans", Font.ITALIC, 12));
		GridBagConstraints gbc_lblProgramVersion = new GridBagConstraints();
		gbc_lblProgramVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblProgramVersion.anchor = GridBagConstraints.NORTH;
		gbc_lblProgramVersion.gridwidth = 3;
		gbc_lblProgramVersion.gridx = 0;
		gbc_lblProgramVersion.gridy = 11;
		panelImportExportStatistics.add(lblProgramVersion, gbc_lblProgramVersion);
		
		JPanel panelSettings = new JPanel();
		panelSettings.setBorder(null);
		tabbedPane.addTab("Global Settings", null, panelSettings, null);
		panelSettings.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneSettings = new JScrollPane();
		scrollPaneSettings.setBorder(null);
		panelSettings.add(scrollPaneSettings);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		scrollPaneSettings.setViewportBorder(null);
		scrollPaneSettings.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0};
		gbl_panel.rowWeights =
			new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		btnUnlock = new JButton("Unlock");
		btnUnlock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSettingsUnlock();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnUnlock = new GridBagConstraints();
		gbc_btnUnlock.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUnlock.gridwidth = 3;
		gbc_btnUnlock.insets = new Insets(0, 0, 5, 5);
		gbc_btnUnlock.gridx = 1;
		gbc_btnUnlock.gridy = 1;
		panel.add(btnUnlock, gbc_btnUnlock);
		
		JLabel lblTitleFont = new JLabel("Title font");
		GridBagConstraints gbc_lblTitleFont = new GridBagConstraints();
		gbc_lblTitleFont.anchor = GridBagConstraints.EAST;
		gbc_lblTitleFont.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitleFont.gridx = 1;
		gbc_lblTitleFont.gridy = 2;
		panel.add(lblTitleFont, gbc_lblTitleFont);
		
		btnSelectTitleFont = new JButton("Select...");
		btnSelectTitleFont.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSelectTitleFont();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnSelectTitleFont = new GridBagConstraints();
		gbc_btnSelectTitleFont.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTitleFont.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTitleFont.gridx = 3;
		gbc_btnSelectTitleFont.gridy = 2;
		panel.add(btnSelectTitleFont, gbc_btnSelectTitleFont);
		
		JLabel lblLyricsFont = new JLabel("Lyrics font");
		GridBagConstraints gbc_lblLyricsFont = new GridBagConstraints();
		gbc_lblLyricsFont.anchor = GridBagConstraints.EAST;
		gbc_lblLyricsFont.insets = new Insets(0, 0, 5, 5);
		gbc_lblLyricsFont.gridx = 1;
		gbc_lblLyricsFont.gridy = 3;
		panel.add(lblLyricsFont, gbc_lblLyricsFont);
		
		btnSelectLyricsFont = new JButton("Select...");
		btnSelectLyricsFont.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSelectLyricsFont();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnSelectLyricsFont = new GridBagConstraints();
		gbc_btnSelectLyricsFont.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectLyricsFont.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectLyricsFont.gridx = 3;
		gbc_btnSelectLyricsFont.gridy = 3;
		panel.add(btnSelectLyricsFont, gbc_btnSelectLyricsFont);
		
		JLabel lblTranslationFont = new JLabel("Translation font");
		GridBagConstraints gbc_lblTranslationFont = new GridBagConstraints();
		gbc_lblTranslationFont.anchor = GridBagConstraints.EAST;
		gbc_lblTranslationFont.insets = new Insets(0, 0, 5, 5);
		gbc_lblTranslationFont.gridx = 1;
		gbc_lblTranslationFont.gridy = 4;
		panel.add(lblTranslationFont, gbc_lblTranslationFont);
		
		btnSelectTranslationFont = new JButton("Select...");
		btnSelectTranslationFont.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSelectTranslationFont();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnSelectTranslationFont = new GridBagConstraints();
		gbc_btnSelectTranslationFont.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTranslationFont.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTranslationFont.gridx = 3;
		gbc_btnSelectTranslationFont.gridy = 4;
		panel.add(btnSelectTranslationFont, gbc_btnSelectTranslationFont);
		
		JLabel lblCopyrightFont = new JLabel("Copyright font");
		GridBagConstraints gbc_lblCopyrightFont = new GridBagConstraints();
		gbc_lblCopyrightFont.anchor = GridBagConstraints.EAST;
		gbc_lblCopyrightFont.insets = new Insets(0, 0, 5, 5);
		gbc_lblCopyrightFont.gridx = 1;
		gbc_lblCopyrightFont.gridy = 5;
		panel.add(lblCopyrightFont, gbc_lblCopyrightFont);
		
		btnSelectCopyrightFont = new JButton("Select...");
		btnSelectCopyrightFont.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSelectCopyrightFont();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnSelectCopyrightFont = new GridBagConstraints();
		gbc_btnSelectCopyrightFont.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectCopyrightFont.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectCopyrightFont.gridx = 3;
		gbc_btnSelectCopyrightFont.gridy = 5;
		panel.add(btnSelectCopyrightFont, gbc_btnSelectCopyrightFont);
		
		JLabel lblTextColor = new JLabel("Text color");
		GridBagConstraints gbc_lblTextColor = new GridBagConstraints();
		gbc_lblTextColor.anchor = GridBagConstraints.EAST;
		gbc_lblTextColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblTextColor.gridx = 1;
		gbc_lblTextColor.gridy = 6;
		panel.add(lblTextColor, gbc_lblTextColor);
		
		btnSelectTextColor = new JButton("Select...");
		btnSelectTextColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSelectTextColor();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnSelectTextColor = new GridBagConstraints();
		gbc_btnSelectTextColor.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTextColor.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTextColor.gridx = 3;
		gbc_btnSelectTextColor.gridy = 6;
		panel.add(btnSelectTextColor, gbc_btnSelectTextColor);
		
		JLabel lblBackgroundColor = new JLabel("Background color");
		GridBagConstraints gbc_lblBackgroundColor = new GridBagConstraints();
		gbc_lblBackgroundColor.anchor = GridBagConstraints.EAST;
		gbc_lblBackgroundColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblBackgroundColor.gridx = 1;
		gbc_lblBackgroundColor.gridy = 7;
		panel.add(lblBackgroundColor, gbc_lblBackgroundColor);
		
		btnSelectBackgroundColor = new JButton("Select...");
		btnSelectBackgroundColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSelectBackgroundColor();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnSelectBackgroundColor = new GridBagConstraints();
		gbc_btnSelectBackgroundColor.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectBackgroundColor.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectBackgroundColor.gridx = 3;
		gbc_btnSelectBackgroundColor.gridy = 7;
		panel.add(btnSelectBackgroundColor, gbc_btnSelectBackgroundColor);
		
		JLabel lblLogo = new JLabel("Logo");
		GridBagConstraints gbc_lblLogo = new GridBagConstraints();
		gbc_lblLogo.anchor = GridBagConstraints.EAST;
		gbc_lblLogo.insets = new Insets(0, 0, 5, 5);
		gbc_lblLogo.gridx = 1;
		gbc_lblLogo.gridy = 8;
		panel.add(lblLogo, gbc_lblLogo);
		
		btnSelectLogo = new JButton("Select...");
		btnSelectLogo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					handleSelectLogo();
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		});
		GridBagConstraints gbc_btnSelectLogo = new GridBagConstraints();
		gbc_btnSelectLogo.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectLogo.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectLogo.gridx = 3;
		gbc_btnSelectLogo.gridy = 8;
		panel.add(btnSelectLogo, gbc_btnSelectLogo);
		
		JLabel lblTopMargin = new JLabel("Top margin");
		GridBagConstraints gbc_lblTopMargin = new GridBagConstraints();
		gbc_lblTopMargin.anchor = GridBagConstraints.EAST;
		gbc_lblTopMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblTopMargin.gridx = 1;
		gbc_lblTopMargin.gridy = 9;
		panel.add(lblTopMargin, gbc_lblTopMargin);
		
		spinnerTopMargin = new JSpinner();
		GridBagConstraints gbc_spinnerTopMargin = new GridBagConstraints();
		gbc_spinnerTopMargin.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerTopMargin.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerTopMargin.gridx = 3;
		gbc_spinnerTopMargin.gridy = 9;
		panel.add(spinnerTopMargin, gbc_spinnerTopMargin);
		
		JLabel lblLeftMargin = new JLabel("Left margin");
		GridBagConstraints gbc_lblLeftMargin = new GridBagConstraints();
		gbc_lblLeftMargin.anchor = GridBagConstraints.EAST;
		gbc_lblLeftMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblLeftMargin.gridx = 1;
		gbc_lblLeftMargin.gridy = 10;
		panel.add(lblLeftMargin, gbc_lblLeftMargin);
		
		spinnerLeftMargin = new JSpinner();
		GridBagConstraints gbc_spinnerLeftMargin = new GridBagConstraints();
		gbc_spinnerLeftMargin.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerLeftMargin.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerLeftMargin.gridx = 3;
		gbc_spinnerLeftMargin.gridy = 10;
		panel.add(spinnerLeftMargin, gbc_spinnerLeftMargin);
		
		JLabel lblRightMargin = new JLabel("Right margin");
		GridBagConstraints gbc_lblRightMargin = new GridBagConstraints();
		gbc_lblRightMargin.anchor = GridBagConstraints.EAST;
		gbc_lblRightMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblRightMargin.gridx = 1;
		gbc_lblRightMargin.gridy = 11;
		panel.add(lblRightMargin, gbc_lblRightMargin);
		
		spinnerRightMargin = new JSpinner();
		GridBagConstraints gbc_spinnerRightMargin = new GridBagConstraints();
		gbc_spinnerRightMargin.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerRightMargin.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerRightMargin.gridx = 3;
		gbc_spinnerRightMargin.gridy = 11;
		panel.add(spinnerRightMargin, gbc_spinnerRightMargin);
		
		JLabel lblBottomMargin = new JLabel("Bottom margin");
		GridBagConstraints gbc_lblBottomMargin = new GridBagConstraints();
		gbc_lblBottomMargin.anchor = GridBagConstraints.EAST;
		gbc_lblBottomMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblBottomMargin.gridx = 1;
		gbc_lblBottomMargin.gridy = 12;
		panel.add(lblBottomMargin, gbc_lblBottomMargin);
		
		spinnerBottomMargin = new JSpinner();
		GridBagConstraints gbc_spinnerBottomMargin = new GridBagConstraints();
		gbc_spinnerBottomMargin.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerBottomMargin.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerBottomMargin.gridx = 3;
		gbc_spinnerBottomMargin.gridy = 12;
		panel.add(spinnerBottomMargin, gbc_spinnerBottomMargin);
		
		JLabel lblShowTitle = new JLabel("Show title in presentation");
		lblShowTitle.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblShowTitle = new GridBagConstraints();
		gbc_lblShowTitle.anchor = GridBagConstraints.EAST;
		gbc_lblShowTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblShowTitle.gridx = 1;
		gbc_lblShowTitle.gridy = 13;
		panel.add(lblShowTitle, gbc_lblShowTitle);
		
		checkboxShowTitle = new JCheckBox("");
		GridBagConstraints gbc_checkboxShowTitle = new GridBagConstraints();
		gbc_checkboxShowTitle.insets = new Insets(0, 0, 5, 5);
		gbc_checkboxShowTitle.gridx = 3;
		gbc_checkboxShowTitle.gridy = 13;
		panel.add(checkboxShowTitle, gbc_checkboxShowTitle);
		
		JLabel lblDistanceBetweenTitle = new JLabel("Distance between title and text");
		GridBagConstraints gbc_lblDistanceBetweenTitle = new GridBagConstraints();
		gbc_lblDistanceBetweenTitle.anchor = GridBagConstraints.EAST;
		gbc_lblDistanceBetweenTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblDistanceBetweenTitle.gridx = 1;
		gbc_lblDistanceBetweenTitle.gridy = 14;
		panel.add(lblDistanceBetweenTitle, gbc_lblDistanceBetweenTitle);
		
		spinnerDistanceTitleText = new JSpinner();
		GridBagConstraints gbc_spinnerDistanceTitleText = new GridBagConstraints();
		gbc_spinnerDistanceTitleText.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerDistanceTitleText.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerDistanceTitleText.gridx = 3;
		gbc_spinnerDistanceTitleText.gridy = 14;
		panel.add(spinnerDistanceTitleText, gbc_spinnerDistanceTitleText);
		
		JLabel lblDistanceBetweenText = new JLabel("Distance between text and copyright");
		GridBagConstraints gbc_lblDistanceBetweenText = new GridBagConstraints();
		gbc_lblDistanceBetweenText.anchor = GridBagConstraints.EAST;
		gbc_lblDistanceBetweenText.insets = new Insets(0, 0, 5, 5);
		gbc_lblDistanceBetweenText.gridx = 1;
		gbc_lblDistanceBetweenText.gridy = 15;
		panel.add(lblDistanceBetweenText, gbc_lblDistanceBetweenText);
		
		spinnerDistanceTextCopyright = new JSpinner();
		GridBagConstraints gbc_spinnerDistanceTextCopyright = new GridBagConstraints();
		gbc_spinnerDistanceTextCopyright.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerDistanceTextCopyright.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerDistanceTextCopyright.gridx = 3;
		gbc_spinnerDistanceTextCopyright.gridy = 15;
		panel.add(spinnerDistanceTextCopyright, gbc_spinnerDistanceTextCopyright);
		
		JLabel lblSongListFiltering = new JLabel("Song list filter");
		GridBagConstraints gbc_lblSongListFiltering = new GridBagConstraints();
		gbc_lblSongListFiltering.anchor = GridBagConstraints.EAST;
		gbc_lblSongListFiltering.insets = new Insets(0, 0, 5, 5);
		gbc_lblSongListFiltering.gridx = 1;
		gbc_lblSongListFiltering.gridy = 16;
		panel.add(lblSongListFiltering, gbc_lblSongListFiltering);
		
		comboSongListFiltering = new JComboBox<FilterTypeEnum>();
		GridBagConstraints gbc_comboSongListFiltering = new GridBagConstraints();
		gbc_comboSongListFiltering.insets = new Insets(0, 0, 5, 5);
		gbc_comboSongListFiltering.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboSongListFiltering.gridx = 3;
		gbc_comboSongListFiltering.gridy = 16;
		panel.add(comboSongListFiltering, gbc_comboSongListFiltering);
		
		JLabel lblPresentationScreen1Display = new JLabel("Presentation screen 1 display");
		GridBagConstraints gbc_lblPresentationScreen1Display = new GridBagConstraints();
		gbc_lblPresentationScreen1Display.anchor = GridBagConstraints.EAST;
		gbc_lblPresentationScreen1Display.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresentationScreen1Display.gridx = 1;
		gbc_lblPresentationScreen1Display.gridy = 17;
		panel.add(lblPresentationScreen1Display, gbc_lblPresentationScreen1Display);
		
		comboPresentationScreen1Display = new JComboBox<GraphicsDevice>();
		GridBagConstraints gbc_comboPresentationScreen1Display = new GridBagConstraints();
		gbc_comboPresentationScreen1Display.insets = new Insets(0, 0, 5, 5);
		gbc_comboPresentationScreen1Display.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboPresentationScreen1Display.gridx = 3;
		gbc_comboPresentationScreen1Display.gridy = 17;
		panel.add(comboPresentationScreen1Display, gbc_comboPresentationScreen1Display);
		
		JLabel lblPresentationScreen1Contents = new JLabel("Presentation screen 1 contents");
		GridBagConstraints gbc_lblPresentationScreen1Contents = new GridBagConstraints();
		gbc_lblPresentationScreen1Contents.anchor = GridBagConstraints.EAST;
		gbc_lblPresentationScreen1Contents.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresentationScreen1Contents.gridx = 1;
		gbc_lblPresentationScreen1Contents.gridy = 18;
		panel.add(lblPresentationScreen1Contents, gbc_lblPresentationScreen1Contents);
		
		comboPresentationScreen1Contents = new JComboBox<ScreenContentsEnum>();
		GridBagConstraints gbc_comboPresentationScreen1Contents = new GridBagConstraints();
		gbc_comboPresentationScreen1Contents.insets = new Insets(0, 0, 5, 5);
		gbc_comboPresentationScreen1Contents.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboPresentationScreen1Contents.gridx = 3;
		gbc_comboPresentationScreen1Contents.gridy = 18;
		panel.add(comboPresentationScreen1Contents, gbc_comboPresentationScreen1Contents);
		
		JLabel lblPresentationScreen2Display = new JLabel("Presentation screen 2 display");
		GridBagConstraints gbc_lblPresentationScreen2Display = new GridBagConstraints();
		gbc_lblPresentationScreen2Display.anchor = GridBagConstraints.EAST;
		gbc_lblPresentationScreen2Display.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresentationScreen2Display.gridx = 1;
		gbc_lblPresentationScreen2Display.gridy = 19;
		panel.add(lblPresentationScreen2Display, gbc_lblPresentationScreen2Display);
		
		comboPresentationScreen2Display = new JComboBox<GraphicsDevice>();
		GridBagConstraints gbc_comboPresentationScreen2Display = new GridBagConstraints();
		gbc_comboPresentationScreen2Display.insets = new Insets(0, 0, 5, 5);
		gbc_comboPresentationScreen2Display.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboPresentationScreen2Display.gridx = 3;
		gbc_comboPresentationScreen2Display.gridy = 19;
		panel.add(comboPresentationScreen2Display, gbc_comboPresentationScreen2Display);
		
		JLabel lblPresentationScreen2Contents = new JLabel("Presentation screen 2 contents");
		GridBagConstraints gbc_lblPresentationScreen2Contents = new GridBagConstraints();
		gbc_lblPresentationScreen2Contents.anchor = GridBagConstraints.EAST;
		gbc_lblPresentationScreen2Contents.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresentationScreen2Contents.gridx = 1;
		gbc_lblPresentationScreen2Contents.gridy = 20;
		panel.add(lblPresentationScreen2Contents, gbc_lblPresentationScreen2Contents);
		
		comboPresentationScreen2Contents = new JComboBox<ScreenContentsEnum>();
		GridBagConstraints gbc_comboPresentationScreen2Contents = new GridBagConstraints();
		gbc_comboPresentationScreen2Contents.insets = new Insets(0, 0, 5, 5);
		gbc_comboPresentationScreen2Contents.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboPresentationScreen2Contents.gridx = 3;
		gbc_comboPresentationScreen2Contents.gridy = 20;
		panel.add(comboPresentationScreen2Contents, gbc_comboPresentationScreen2Contents);
		
		JLabel lblSecondsToCount = new JLabel("Seconds to count a song as displayed after");
		GridBagConstraints gbc_lblSecondsToCount = new GridBagConstraints();
		gbc_lblSecondsToCount.anchor = GridBagConstraints.EAST;
		gbc_lblSecondsToCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblSecondsToCount.gridx = 1;
		gbc_lblSecondsToCount.gridy = 21;
		panel.add(lblSecondsToCount, gbc_lblSecondsToCount);
		
		spinnerCountAsDisplayedAfter = new JSpinner();
		GridBagConstraints gbc_spinnerCountAsDisplayedAfter = new GridBagConstraints();
		gbc_spinnerCountAsDisplayedAfter.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerCountAsDisplayedAfter.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerCountAsDisplayedAfter.gridx = 3;
		gbc_spinnerCountAsDisplayedAfter.gridy = 21;
		panel.add(spinnerCountAsDisplayedAfter, gbc_spinnerCountAsDisplayedAfter);
		
		afterConstruction();
	}
	
	public static List<Image> getIconsFromResources(Class<?> classToUse) {
		return Arrays.asList(ResourceTools.getImage(classToUse, "/org/zephyrsoft/sdb2/icon-128.png"),
			ResourceTools.getImage(classToUse, "/org/zephyrsoft/sdb2/icon-64.png"),
			ResourceTools.getImage(classToUse, "/org/zephyrsoft/sdb2/icon-32.png"),
			ResourceTools.getImage(classToUse, "/org/zephyrsoft/sdb2/icon-16.png"));
	}
	
	private void calculateAndSetBounds() {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle maximumWindowBounds = env.getMaximumWindowBounds();
		Rectangle screenSize = env.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		if (maximumWindowBounds.width != screenSize.width || maximumWindowBounds.height != screenSize.height) {
			setBounds(maximumWindowBounds);
		} else {
			// fallback
			setBounds(50, 50, 965, 659);
		}
	}
	
	public JTextField getTextFieldTitle() {
		return textFieldTitle;
	}
	
	public JComboBox<LanguageEnum> getComboBoxLanguage() {
		return comboBoxLanguage;
	}
	
	public JTextField getTextFieldTonality() {
		return textFieldTonality;
	}
	
	public JEditorPane getEditorLyrics() {
		return editorLyrics;
	}
	
	public JTextField getTextFieldComposer() {
		return textFieldComposer;
	}
	
	public JTextField getTextFieldAuthorText() {
		return textFieldAuthorText;
	}
	
	public JTextField getTextFieldAuthorTranslation() {
		return textFieldAuthorTranslation;
	}
	
	public JTextField getTextFieldPublisher() {
		return textFieldPublisher;
	}
	
	public JTextField getTextFieldAdditionalCopyrightNotes() {
		return textFieldAdditionalCopyrightNotes;
	}
	
	public JTextField getTextFieldSongNotes() {
		return textFieldSongNotes;
	}
	
	public JEditorPane getEditorChordSequence() {
		return editorChordSequence;
	}
	
	public JList<?> getSongList() {
		return songsList;
	}
	
	public JTextField getTextFieldFilter() {
		return textFieldFilter;
	}
	
	public JList<?> getPresentSongList() {
		return presentList;
	}
	
	public JPanel getPanelSectionButtons() {
		return panelSectionButtons;
	}
	
	public JLabel getLblStatistics() {
		return lblStatistics;
	}
}
