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
package org.zephyrsoft.sdb2.gui;

import static org.zephyrsoft.sdb2.model.VirtualScreen.SCREEN_A;
import static org.zephyrsoft.sdb2.model.VirtualScreen.SCREEN_B;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.FileAndDirectoryLocations;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.gui.KeyboardShortcut.Modifiers;
import org.zephyrsoft.sdb2.gui.renderer.FilterTypeCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.ScreenContentsCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.ScreenDisplayCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.SongCellRenderer;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.ExportFormat;
import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.SelectableDisplay;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.VirtualScreen;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.ScreenHelper;
import org.zephyrsoft.sdb2.presenter.UIScroller;
import org.zephyrsoft.sdb2.remote.PatchController;
import org.zephyrsoft.sdb2.remote.RemoteStatus;
import org.zephyrsoft.sdb2.service.ExportService;
import org.zephyrsoft.sdb2.service.FieldName;
import org.zephyrsoft.sdb2.service.IndexType;
import org.zephyrsoft.sdb2.service.IndexerService;
import org.zephyrsoft.sdb2.service.IndexerService.OnIndexChangeListener;
import org.zephyrsoft.sdb2.util.CustomFileFilter;
import org.zephyrsoft.sdb2.util.ResourceTools;
import org.zephyrsoft.sdb2.util.StringTools;
import org.zephyrsoft.sdb2.util.VersionTools;
import org.zephyrsoft.sdb2.util.VersionTools.VersionUpdate;
import org.zephyrsoft.sdb2.util.gui.ErrorDialog;
import org.zephyrsoft.sdb2.util.gui.FixedWidthJList;
import org.zephyrsoft.sdb2.util.gui.ImagePreview;
import org.zephyrsoft.sdb2.util.gui.ListFilter;
import org.zephyrsoft.sdb2.util.gui.TransparentComboBoxModel;
import org.zephyrsoft.sdb2.util.gui.TransparentFilterableListModel;
import org.zephyrsoft.sdb2.util.gui.TransparentListModel;

import com.google.common.io.Files;

import say.swing.JFontChooser;

/**
 * Main window of the application.
 */
public class MainWindow extends JFrame implements UIScroller, OnIndexChangeListener {
	
	private static final String PROBLEM_WHILE_SAVING = """
		There was a problem while saving the data.
		
		Please examine the log file at:
		""";
	
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
	
	private Container glassPane;
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JPanel buttonPanel;
	
	private JEditorPane editorLyrics;
	private JTextField textFieldTitle;
	private JComboBox<String> comboBoxLanguage;
	private JTextField textFieldTonality;
	private JTextField textFieldComposer;
	private JTextField textFieldAuthorText;
	private JTextField textFieldAuthorTranslation;
	private JTextField textFieldPublisher;
	private JTextField textFieldAdditionalCopyrightNotes;
	private JTextField textFieldTempo;
	private JEditorPane editorChordSequence;
	private JEditorPane editorDrumNotes;
	private JEditorPane editorSongNotes;
	
	private KeyboardShortcutManager keyboardShortcutManager;
	private final MainController controller;
	private IndexerService indexer;
	private ExportService exportService;
	
	private SettingsModel settingsModel;
	
	private FixedWidthJList<Song> songsList;
	private SongsModel songsModel;
	private TransparentFilterableListModel<Song> songsListModel;
	private Collection<Song> songsListFiltered;
	private Song selectedSong;
	private String selectNewSongWithUUID;
	
	private FixedWidthJList<Song> presentList;
	private SongsModel presentModel;
	private TransparentListModel<Song> presentListModel;
	private Song presentListSelected;
	
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
	private List<PartButtonGroup> listSectionButtons = new ArrayList<>();
	private GridBagConstraints panelSectionButtonsHints;
	private GridBagConstraints panelSectionButtonsLastRowHints;
	private JButton btnJumpToSelected;
	private JButton btnShowLogo;
	private JButton btnShowBlankScreen;
	private JButton btnPresentSelectedSong;
	private JScrollPane scrollPaneSectionButtons;
	private JButton btnJumpToPresented;
	private JButton btnExportPdfSelected;
	private JButton btnExportPdfAll;
	private JButton btnExportStatisticsAll;
	private JButton btnImportFromSdb1;
	private JLabel lblProgramVersion;
	
	private JButton btnUnlock;
	private JButton btnSelectTitleFont1;
	private JButton btnSelectTitleFont2;
	private JButton btnSelectTitleFontBoth;
	private JButton btnSelectLyricsFont1;
	private JButton btnSelectLyricsFont2;
	private JButton btnSelectLyricsFontBoth;
	private JButton btnSelectTranslationFont1;
	private JButton btnSelectTranslationFont2;
	private JButton btnSelectTranslationFontBoth;
	private JButton btnSelectCopyrightFont1;
	private JButton btnSelectCopyrightFont2;
	private JButton btnSelectCopyrightFontBoth;
	private JButton btnSelectChordSequenceFont1;
	private JButton btnSelectChordSequenceFont2;
	private JButton btnSelectChordSequenceFontBoth;
	private JButton btnSelectTextColor1;
	private JButton btnSelectBackgroundColor1;
	private JButton btnSelectLogo;
	private JSpinner spinnerTopMargin;
	private JSpinner spinnerLeftMargin;
	private JSpinner spinnerRightMargin;
	private JSpinner spinnerBottomMargin;
	private JCheckBox checkboxShowTitle;
	private JSpinner spinnerDistanceTitleText;
	private JSpinner spinnerDistanceTextCopyright;
	private JComboBox<FilterTypeEnum> comboSongListFiltering;
	private JComboBox<SelectableDisplay> comboPresentationScreen1Display;
	private JComboBox<ScreenContentsEnum> comboPresentationScreen1Contents;
	private JComboBox<SelectableDisplay> comboPresentationScreen2Display;
	private JComboBox<ScreenContentsEnum> comboPresentationScreen2Contents;
	private JSpinner spinnerCountAsDisplayedAfter;
	private JButton btnSlideShowDirectory;
	private JSpinner spinnerSlideShowSeconds;
	private JCheckBox checkboxRemoteEnabled;
	private JTextField textFieldRemoteServer;
	private JTextField textFieldRemoteUsername;
	private JTextField textFieldRemotePrefix;
	private JTextField textFieldRemoteRoom;
	private JTextField textFieldRemotePassword;
	private JLabel lblRemoteUsername;
	private JLabel lblRemotePassword;
	private JLabel lblRemoteServer;
	private JLabel lblRemoteEnabled;
	
	private JButton saveButton;
	
	private SongCellRenderer songCellRenderer;
	private JLabel lblSlideShowDirectory;
	private JLabel lblSlideShowSeconds;
	private JButton btnSlideshow;
	private JCheckBox chckbxWithTranslation;
	private JCheckBox chckbxWithChords;
	private JCheckBox chckbxOnlyExportSongs;
	private JLabel labelExportStatisctics;
	private JButton btnSelectTextColor2;
	private JButton btnSelectBackgroundColor2;
	private JButton btnSelectTextColorBoth;
	private JButton btnSelectBackgroundColorBoth;
	private JLabel lblMinimizeScrolling;
	private JCheckBox checkboxMinimizeScrolling1;
	private JCheckBox checkboxMinimizeScrolling2;
	
	private JLabel lblRemotePrefix;
	
	private JLabel lblRemoteRoom;
	
	private JLabel lblStatus;
	private JLabel lblGitCommitHash;
	private JLabel lblFadeTime;
	private JSpinner spinnerFadeTime;
	
	@Override
	public List<PartButtonGroup> getUIParts() {
		return listSectionButtons;
	}
	
	private void afterConstruction() {
		MainController.initAnimationTimer();
		
		// read program version
		lblProgramVersion.setText(VersionTools.getCurrent());
		lblGitCommitHash.setText(VersionTools.getGitCommitHash());
		
		// fill in available values for filter type
		for (FilterTypeEnum item : FilterTypeEnum.values()) {
			comboSongListFiltering.addItem(item);
		}
		clearSongData();
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
		btnExportPdfSelected.setEnabled(false);
		// create empty songsModel for the "selected songs" list
		presentModel = new SongsModel();
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
		
		// add keyboard comfort to the filter field
		textFieldFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT) {
					presentList.requestFocusInWindow();
				} else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP) {
					songsList.moveSelectionUp();
					textFieldFilter.requestFocusInWindow();
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN) {
					songsList.moveSelectionDown();
					textFieldFilter.requestFocusInWindow();
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleSongSelect();
				}
			}
		});
		
		// add keyboard comfort to the global songs list
		songsList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT) {
					presentList.requestFocusInWindow();
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleSongSelect();
				}
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
				char keyChar = e.getKeyChar();
				
				if (keyChar != KeyEvent.CHAR_UNDEFINED) {
					if (Character.isLetterOrDigit(keyChar)
						|| Character.getType(keyChar) == Character.CONNECTOR_PUNCTUATION
						|| Character.getType(keyChar) == Character.MATH_SYMBOL
						|| Character.getType(keyChar) == Character.OTHER_SYMBOL
						|| Character.getType(keyChar) == Character.DASH_PUNCTUATION
						|| Character.getType(keyChar) == Character.START_PUNCTUATION
						|| Character.getType(keyChar) == Character.END_PUNCTUATION
						|| Character.getType(keyChar) == Character.SPACE_SEPARATOR
						|| Character.getType(keyChar) == Character.OTHER_PUNCTUATION) {
						// forward to filter field
						textFieldFilter.setText(textFieldFilter.getText() + keyChar);
					} else if (keyChar == (char) 8 && textFieldFilter.getText().length() > 0) {
						// strip last character from filter field
						textFieldFilter.setText(textFieldFilter.getText().substring(0,
							textFieldFilter.getText().length() - 1));
					}
				}
			}
		});
		
		// add keyboard comfort to the present songs list
		presentList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT) {
					songsList.requestFocusInWindow();
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleSongPresent();
				}
			}
		});
		
		// lock setting controls to prevent accidental changes
		tabbedPane.addChangeListener(e -> {
			// on every tab switch, lock the settings again
			handleSettingsSaveAndLock();
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
		
		indexer.onIndexChange(this);
		setModels(controller.getSongs(), controller.getSettings());
		setVisible(true);
		textFieldFilter.requestFocusInWindow();
		checkForUpdateAsync();
	}
	
	private void checkForUpdateAsync() {
		Runnable task = () -> {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// do nothing
			}
			
			final VersionUpdate updateAvailable = VersionTools.getLatest();
			if (updateAvailable != null) {
				final JLabel updateLabel = new JLabel("new version available with timestamp "
					+ updateAvailable.getVersionTimestamp());
				updateLabel.setForeground(Color.BLUE);
				updateLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
				updateLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent event) {
						if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(
							Desktop.Action.BROWSE)) {
							try {
								LOG.info("starting browse action for {}", updateAvailable.getWebUrl());
								Desktop.getDesktop().browse(new URI(updateAvailable.getWebUrl()));
							} catch (Exception e) {
								LOG.warn("could not start browsing", e);
							}
						} else {
							LOG.warn("browsing not supported on this platform");
						}
					}
				});
				SwingUtilities.invokeLater(() -> {
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.weightx = 1.0;
					gbc.weighty = 1.0;
					gbc.fill = GridBagConstraints.NONE;
					gbc.insets = new Insets(5, 20, 5, 20);
					gbc.anchor = GridBagConstraints.NORTHEAST;
					glassPane.add(updateLabel, gbc, 0);
					glassPane.revalidate();
				});
			}
		};
		Thread thread = new Thread(task);
		thread.start();
	}
	
	public void reloadModels(SongsModel songs, SettingsModel settings) {
		SwingUtilities.invokeLater(() -> setModels(songs, settings));
		
	}
	
	public void setModels(SongsModel songs, SettingsModel settings) {
		this.songsModel = songs;
		this.settingsModel = settings;
		songsListModel = songs.getFilterableListModel();
		songsList.setModel(songsListModel);
		
		// If some song changes, index changes, update the list and load the current song again
		songsModel.addSongsModelListener(() -> {
			indexAllSongs();
		});
		// start indexing once after initialization
		indexAllSongs();
		
		// song list filtering
		ListFilter<Song> filter = song -> {
			String filterText = textFieldFilter.getText();
			if (StringTools.isBlank(filterText)) {
				return true;
			} else {
				return songsListFiltered.contains(song);
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
		updateScreenModels();
		ScreenHelper.addChangeListener(() -> SwingUtilities.invokeLater(this::updateScreenModels));
		
		// load values for instantly displayed settings
		updateFontButtons();
		Boolean showTitle = settingsModel.get(SettingKey.SHOW_TITLE, Boolean.class);
		checkboxShowTitle.setSelected(showTitle != null && showTitle.booleanValue());
		Boolean minimalScrolling1 = settingsModel.get(SettingKey.MINIMAL_SCROLLING, Boolean.class);
		checkboxMinimizeScrolling1.setSelected(minimalScrolling1 != null && minimalScrolling1.booleanValue());
		Boolean minimalScrolling2 = settingsModel.get(SettingKey.MINIMAL_SCROLLING_2, Boolean.class);
		checkboxMinimizeScrolling2.setSelected(minimalScrolling2 != null && minimalScrolling2.booleanValue());
		setSpinnerValue(spinnerTopMargin, settingsModel.get(SettingKey.TOP_MARGIN, Integer.class));
		setSpinnerValue(spinnerLeftMargin, settingsModel.get(SettingKey.LEFT_MARGIN, Integer.class));
		setSpinnerValue(spinnerRightMargin, settingsModel.get(SettingKey.RIGHT_MARGIN, Integer.class));
		setSpinnerValue(spinnerBottomMargin, settingsModel.get(SettingKey.BOTTOM_MARGIN, Integer.class));
		setSpinnerValue(spinnerDistanceTitleText, settingsModel.get(SettingKey.DISTANCE_TITLE_TEXT, Integer.class));
		setSpinnerValue(spinnerDistanceTextCopyright, settingsModel.get(SettingKey.DISTANCE_TEXT_COPYRIGHT,
			Integer.class));
		comboSongListFiltering.setSelectedItem(settingsModel.get(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.class));
		comboPresentationScreen1Contents.setSelectedItem(settingsModel.get(SettingKey.SCREEN_1_CONTENTS, ScreenContentsEnum.class));
		comboPresentationScreen2Contents.setSelectedItem(settingsModel.get(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.class));
		setSpinnerValue(spinnerCountAsDisplayedAfter, settingsModel.get(SettingKey.SECONDS_UNTIL_COUNTED, Integer.class));
		setSpinnerValue(spinnerSlideShowSeconds, settingsModel.get(SettingKey.SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE, Integer.class));
		setSpinnerValue(spinnerFadeTime, settingsModel.get(SettingKey.FADE_TIME, Integer.class));
		
		checkboxRemoteEnabled.setSelected(settingsModel.get(SettingKey.REMOTE_ENABLED, Boolean.class));
		textFieldRemoteServer.setText(settingsModel.get(SettingKey.REMOTE_SERVER, String.class));
		textFieldRemoteUsername.setText(settingsModel.get(SettingKey.REMOTE_USERNAME, String.class));
		textFieldRemotePassword.setText(settingsModel.get(SettingKey.REMOTE_PASSWORD, String.class));
		textFieldRemotePrefix.setText(settingsModel.get(SettingKey.REMOTE_PREFIX, String.class));
		textFieldRemoteRoom.setText(settingsModel.get(SettingKey.REMOTE_NAMESPACE, String.class));
	}
	
	private void updateScreenModels() {
		controller.detectScreens();
		comboPresentationScreen1Display.setModel(new TransparentComboBoxModel<>(controller.getScreens()));
		comboPresentationScreen2Display.setModel(new TransparentComboBoxModel<>(controller.getScreens()));
		comboPresentationScreen1Display.setSelectedItem(ScreenHelper.getScreen(controller.getScreens(),
			settingsModel.get(SettingKey.SCREEN_1_DISPLAY, Integer.class)));
		comboPresentationScreen2Display.setSelectedItem(ScreenHelper.getScreen(controller.getScreens(),
			settingsModel.get(SettingKey.SCREEN_2_DISPLAY, Integer.class)));
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
		// only if the "unlock" button is disabled (which means that the settings were just edited)
		if (btnUnlock != null && !btnUnlock.isEnabled() && settingsModel != null) {
			commitSpinners(spinnerTopMargin, spinnerLeftMargin, spinnerRightMargin, spinnerBottomMargin,
				spinnerDistanceTitleText, spinnerDistanceTextCopyright, spinnerCountAsDisplayedAfter, spinnerSlideShowSeconds,
				spinnerFadeTime);
			// disable controls
			// copy changed settings to the model
			settingsModel.put(SettingKey.MINIMAL_SCROLLING, checkboxMinimizeScrolling1.getModel().isSelected());
			settingsModel.put(SettingKey.MINIMAL_SCROLLING_2, checkboxMinimizeScrolling2.getModel().isSelected());
			settingsModel.put(SettingKey.SHOW_TITLE, checkboxShowTitle.getModel().isSelected());
			settingsModel.put(SettingKey.TOP_MARGIN, spinnerTopMargin.getValue());
			settingsModel.put(SettingKey.LEFT_MARGIN, spinnerLeftMargin.getValue());
			settingsModel.put(SettingKey.RIGHT_MARGIN, spinnerRightMargin.getValue());
			settingsModel.put(SettingKey.BOTTOM_MARGIN, spinnerBottomMargin.getValue());
			settingsModel.put(SettingKey.DISTANCE_TITLE_TEXT, spinnerDistanceTitleText.getValue());
			settingsModel.put(SettingKey.DISTANCE_TEXT_COPYRIGHT, spinnerDistanceTextCopyright.getValue());
			settingsModel.put(SettingKey.SONG_LIST_FILTER, comboSongListFiltering.getSelectedItem());
			Integer screenOneIndex = comboPresentationScreen1Display.getSelectedItem() == null
				? null
				: ((SelectableDisplay) comboPresentationScreen1Display.getSelectedItem()).getIndex();
			settingsModel.put(SettingKey.SCREEN_1_DISPLAY, screenOneIndex);
			settingsModel.put(SettingKey.SCREEN_1_CONTENTS, comboPresentationScreen1Contents.getSelectedItem());
			Integer screenTwoIndex = comboPresentationScreen2Display.getSelectedItem() == null
				? null
				: ((SelectableDisplay) comboPresentationScreen2Display.getSelectedItem()).getIndex();
			settingsModel.put(SettingKey.SCREEN_2_DISPLAY, screenTwoIndex);
			settingsModel.put(SettingKey.SCREEN_2_CONTENTS, comboPresentationScreen2Contents.getSelectedItem());
			settingsModel.put(SettingKey.SECONDS_UNTIL_COUNTED, spinnerCountAsDisplayedAfter.getValue());
			settingsModel.put(SettingKey.SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE, spinnerSlideShowSeconds.getValue());
			settingsModel.put(SettingKey.FADE_TIME, spinnerFadeTime.getValue());
			
			settingsModel.put(SettingKey.REMOTE_ENABLED, checkboxRemoteEnabled.getModel().isSelected());
			settingsModel.put(SettingKey.REMOTE_SERVER, textFieldRemoteServer.getText());
			settingsModel.put(SettingKey.REMOTE_PASSWORD, textFieldRemotePassword.getText());
			settingsModel.put(SettingKey.REMOTE_USERNAME, textFieldRemoteUsername.getText());
			settingsModel.put(SettingKey.REMOTE_NAMESPACE, textFieldRemoteRoom.getText());
			settingsModel.put(SettingKey.REMOTE_PREFIX, textFieldRemotePrefix.getText());
			// copying is not necessary for fonts, colors, the logo file and the slide show directory
			// because those settings are only stored directly in the model
			
			// apply settings
			controller.settingsChanged();
		}
		setSettingsEnabled(false);
	}
	
	private void indexAllSongs() {
		indexer.index(IndexType.ALL_SONGS, songsModel.getSongs());
	}
	
	@Override
	public void onIndexChange() {
		comboBoxLanguage.removeAllItems();
		Set<String> languages = new HashSet<>();
		// Fill in used languages:
		for (Song song : songsModel.getSongs()) {
			if (song.getLanguage() != null && !song.getLanguage().isBlank()) {
				languages.add(song.getLanguage());
			}
		}
		languages.forEach(comboBoxLanguage::addItem);
		
		SwingUtilities.invokeLater(() -> {
			LOG.debug("Loading songsmodel changes into ui..");
			
			Song newSelectedSong = selectNewSongWithUUID != null ? songsModel.getByUUID(selectNewSongWithUUID) : null;
			if (newSelectedSong != null) {
				selectNewSongWithUUID = null;
				// Open song, if just added with handleNewSong
				setSelectedSong(newSelectedSong, false);
			} else if (selectedSong != null) {
				// If current opened song differs, reopen it.
				// This means overwrite current editor changes without saving them.
				newSelectedSong = songsModel.getByUUID(selectedSong.getUUID());
				if (!selectedSong.equals(newSelectedSong)) {
					if (newSelectedSong == null) {
						// Song is deleted:
						setSelectedSong(null, false);
					} else {
						// Song is updated
						newSelectedSong = rebaseChanges(newSelectedSong);
						setSelectedSong(newSelectedSong, false);
					}
				}
			}
			
			// Update songlist / make it visible:
			if (songsListFiltered != null && selectedSong != null && textFieldFilter != null && !textFieldFilter.getText().isBlank()
				&& !songsListFiltered.contains(selectedSong)) {
				textFieldFilter.setText("");
			} else {
				applyFilter();
			}
		});
	}
	
	private Song rebaseChanges(Song songFromRemote) {
		if (!isSongDataChanged()) {
			return songFromRemote;
		}
		
		Song songFromGui = songFromGUI();
		if (songFromGui.equals(songFromRemote)) {
			return songFromRemote;
		}
		
		Song localPatch = PatchController.patch(songFromGui, selectedSong);
		if (localPatch.isEmpty()) {
			return songFromRemote;
		}
		
		// Merge/Remove equal patches:
		Song remotePatch = PatchController.patch(songFromRemote, selectedSong);
		Song mergedPatch = PatchController.mergePatches(remotePatch, localPatch);
		return PatchController.applyPatch(selectedSong, mergedPatch);
	}
	
	private void setSelectedSong(Song song, boolean save) {
		// Save Song:
		if (save && selectedSong != null) {
			saveSongWithoutChangingGUI();
		}
		
		// Update selected song, if its already open (uuid is the same), do not rewind cursors.
		boolean rewindCursors = song == null || selectedSong == null || !StringTools.equalsWithNullAsEmpty(selectedSong.getUUID(), song.getUUID());
		
		LOG.debug("setSelectedSong: {} selected before={}", song, selectedSong);
		selectedSong = song;
		
		if (selectedSong == null) {
			clearSongData();
			setSongEditingEnabled(false);
			// disable buttons
			btnDeleteSong.setEnabled(false);
			btnSelectSong.setEnabled(false);
			btnExportPdfSelected.setEnabled(false);
		} else {
			loadSongData(selectedSong, rewindCursors);
			if (rewindCursors) {
				setSongEditingEnabled(true);
				// enable buttons
				btnDeleteSong.setEnabled(true);
				btnSelectSong.setEnabled(true);
				btnExportPdfSelected.setEnabled(true);
			}
		}
	}
	
	/**
	 * Let the user select a font, save it into the {@link SettingsModel} (if changed) and re-enable the settings tab.
	 *
	 * @param targets
	 *            the target setting for the newly selected font
	 */
	private void selectFont(SettingKey... targets) {
		Font font = settingsModel.get(targets[0], Font.class);
		Font initialFontForDialog = font != null
			? settingsModel.get(targets[0], Font.class)
			: new Font("Dialog", Font.BOLD | Font.ITALIC, 56);
		JFontChooser fontChooser = new JFontChooser();
		fontChooser.setSelectedFont(initialFontForDialog);
		int result = fontChooser.showDialog(this);
		if (result == JFontChooser.OK_OPTION) {
			Font selectedFont = fontChooser.getSelectedFont();
			if (selectedFont != null) {
				for (SettingKey target : targets) {
					settingsModel.put(target, selectedFont);
				}
			}
		}
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
		updateFontButtons();
	}
	
	private boolean selectColor(Color defaultColor, String title, SettingKey... targets) {
		Color color = settingsModel.get(targets[0], Color.class);
		if (color == null) {
			color = defaultColor;
		}
		Color result = JColorChooser.showDialog(this, title, color);
		if (result != null) {
			for (SettingKey target : targets) {
				settingsModel.put(target, result);
			}
			return true;
		} else {
			return false;
		}
	}
	
	protected void handleSelectTextColor1() {
		selectColor(Color.WHITE, "Select Text Color (Screen 1)", SettingKey.TEXT_COLOR);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectTextColor2() {
		selectColor(Color.WHITE, "Select Text Color (Screen 2)", SettingKey.TEXT_COLOR_2);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectTextColorBoth() {
		selectColor(Color.WHITE, "Select Text Color (Both Screens)", SettingKey.TEXT_COLOR, SettingKey.TEXT_COLOR_2);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectBackgroundColor1() {
		selectColor(Color.BLACK, "Select Background Color (Screen 1)", SettingKey.BACKGROUND_COLOR);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectBackgroundColor2() {
		selectColor(Color.BLACK, "Select Background Color (Screen 2)", SettingKey.BACKGROUND_COLOR_2);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectBackgroundColorBoth() {
		selectColor(Color.BLACK, "Select Background Color (Both Screens)", SettingKey.BACKGROUND_COLOR, SettingKey.BACKGROUND_COLOR_2);
		// TODO perhaps apply the new settings?
		setSettingsEnabled(true);
	}
	
	protected void handleSelectLogo() {
		JFileChooser fileChooser = new JFileChooser();
		String pathname = settingsModel.get(SettingKey.LOGO_FILE, String.class);
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
	
	protected void handleSelectSlideShowDirectory() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		String pathname = settingsModel.get(SettingKey.SLIDE_SHOW_DIRECTORY, String.class);
		if (pathname != null) {
			File currentValue = new File(pathname);
			if (currentValue.isDirectory() && currentValue.canRead()) {
				fileChooser.setSelectedFile(currentValue);
			}
		}
		fileChooser.setApproveButtonText("Select");
		fileChooser.setDialogTitle("Select Slide Show Directory");
		
		// set a custom file filter and don't keep the "accept all" filter
		fileChooser.setFileFilter(new CustomFileFilter("Directories"));
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		int returnVal = fileChooser.showDialog(this, null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (file.isDirectory() && file.canRead()) {
				settingsModel.put(SettingKey.SLIDE_SHOW_DIRECTORY, file.getAbsolutePath());
				// TODO perhaps apply the new settings?
			} else {
				// error: can't access dir
				showErrorDialog("Couldn't access the directory:\n" + file.getAbsolutePath());
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
		setEnabledIfNotNull(btnSelectTitleFont1, enabled);
		setEnabledIfNotNull(btnSelectTitleFont2, enabled);
		setEnabledIfNotNull(btnSelectTitleFontBoth, enabled);
		setEnabledIfNotNull(btnSelectLyricsFont1, enabled);
		setEnabledIfNotNull(btnSelectLyricsFont2, enabled);
		setEnabledIfNotNull(btnSelectLyricsFontBoth, enabled);
		setEnabledIfNotNull(btnSelectTranslationFont1, enabled);
		setEnabledIfNotNull(btnSelectTranslationFont2, enabled);
		setEnabledIfNotNull(btnSelectTranslationFontBoth, enabled);
		setEnabledIfNotNull(btnSelectCopyrightFont1, enabled);
		setEnabledIfNotNull(btnSelectCopyrightFont2, enabled);
		setEnabledIfNotNull(btnSelectCopyrightFontBoth, enabled);
		setEnabledIfNotNull(btnSelectChordSequenceFont1, enabled);
		setEnabledIfNotNull(btnSelectChordSequenceFont2, enabled);
		setEnabledIfNotNull(btnSelectChordSequenceFontBoth, enabled);
		setEnabledIfNotNull(btnSelectTextColor1, enabled);
		setEnabledIfNotNull(btnSelectTextColor2, enabled);
		setEnabledIfNotNull(btnSelectTextColorBoth, enabled);
		setEnabledIfNotNull(btnSelectBackgroundColor1, enabled);
		setEnabledIfNotNull(btnSelectBackgroundColor2, enabled);
		setEnabledIfNotNull(btnSelectBackgroundColorBoth, enabled);
		setEnabledIfNotNull(btnSelectLogo, enabled);
		setEnabledIfNotNull(checkboxShowTitle, enabled);
		setEnabledIfNotNull(checkboxMinimizeScrolling1, enabled);
		setEnabledIfNotNull(checkboxMinimizeScrolling2, enabled);
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
		setEnabledIfNotNull(btnSlideShowDirectory, enabled);
		setEnabledIfNotNull(spinnerSlideShowSeconds, enabled);
		setEnabledIfNotNull(spinnerFadeTime, enabled);
		setEnabledIfNotNull(checkboxRemoteEnabled, enabled);
		setEnabledIfNotNull(textFieldRemoteServer, enabled);
		setEnabledIfNotNull(textFieldRemoteUsername, enabled);
		setEnabledIfNotNull(textFieldRemotePassword, enabled);
		setEnabledIfNotNull(textFieldRemotePrefix, enabled);
		setEnabledIfNotNull(textFieldRemoteRoom, enabled);
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
			Song song = songsList.getSelectedValue();
			LOG.debug("SongListselectionChange to {}", song);
			setSelectedSong(song, true);
		}
	}
	
	private void saveSongWithoutChangingGUI() {
		if (!isSongDataChanged()) {
			return;
		}
		
		LOG.debug("saveSongData: {}", selectedSong.getTitle());
		controller.getSongsController().updateSong(songFromGUI());
	}
	
	private void applyFilter() {
		// Filter:
		final String filterText = textFieldFilter.getText();
		FieldName[] fieldsToSearch = settingsModel.get(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.class).getFields();
		songsListFiltered = indexer.search(IndexType.ALL_SONGS, filterText, fieldsToSearch);
		songsListModel.refilter();
		
		// Find and select currently opened song:
		SwingUtilities.invokeLater(() -> {
			songsList.setValueIsAdjusting(true);
			if (songsListFiltered.contains(selectedSong) || StringTools.isBlank(filterText)) {
				songsList.setSelectedValue(selectedSong, true);
			} else {
				songsList.clearSelection();
				songsList.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
			}
		});
	}
	
	protected void handlePresentListSelectionChanged(ListSelectionEvent e) {
		// only the last event in a row should fire these actions (check valueIsAdjusting)
		if (!e.getValueIsAdjusting()) {
			updateButtonsForPresentListSelection();
		}
	}
	
	protected void updateButtonsForPresentListSelection() {
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
	
	private boolean isSongDataChanged() {
		return selectedSong != null && (!StringTools.equalsWithNullAsEmpty(selectedSong.getLyrics(), editorLyrics.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getTitle(), textFieldTitle.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getLanguage(), (String) comboBoxLanguage.getEditor().getItem())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getTonality(), textFieldTonality.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getComposer(), textFieldComposer.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getAuthorText(), textFieldAuthorText.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getAuthorTranslation(), textFieldAuthorTranslation.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getPublisher(), textFieldPublisher.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getAdditionalCopyrightNotes(), textFieldAdditionalCopyrightNotes
				.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getTempo(), textFieldTempo.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getChordSequence(), editorChordSequence.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getDrumNotes(), editorDrumNotes.getText())
			|| !StringTools.equalsWithNullAsEmpty(selectedSong.getSongNotes(), editorSongNotes.getText()));
	}
	
	private Song songFromGUI() {
		Song song = new Song(selectedSong);
		
		if (!StringTools.equalsWithNullAsEmpty(song.getLyrics(), editorLyrics.getText())) {
			song.setLyrics(editorLyrics.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getTitle(), textFieldTitle.getText())) {
			song.setTitle(textFieldTitle.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getLanguage(), (String) comboBoxLanguage.getEditor().getItem())) {
			song.setLanguage((String) comboBoxLanguage.getEditor().getItem());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getTonality(), textFieldTonality.getText())) {
			song.setTonality(textFieldTonality.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getComposer(), textFieldComposer.getText())) {
			song.setComposer(textFieldComposer.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getAuthorText(), textFieldAuthorText.getText())) {
			song.setAuthorText(textFieldAuthorText.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getAuthorTranslation(), textFieldAuthorTranslation.getText())) {
			song.setAuthorTranslation(textFieldAuthorTranslation.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getPublisher(), textFieldPublisher.getText())) {
			song.setPublisher(textFieldPublisher.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getAdditionalCopyrightNotes(), textFieldAdditionalCopyrightNotes
			.getText())) {
			song.setAdditionalCopyrightNotes(textFieldAdditionalCopyrightNotes.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getTempo(), textFieldTempo.getText())) {
			song.setTempo(textFieldTempo.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getChordSequence(), editorChordSequence.getText())) {
			song.setChordSequence(editorChordSequence.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getDrumNotes(), editorDrumNotes.getText())) {
			song.setDrumNotes(editorDrumNotes.getText());
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getSongNotes(), editorSongNotes.getText())) {
			song.setSongNotes(editorSongNotes.getText());
		}
		return song;
	}
	
	/**
	 * Deletes all values contained in the GUI elements of the song editing tab.
	 */
	private void clearSongData() {
		LOG.debug("clearSongData");
		setText(editorLyrics, "", true);
		setText(textFieldTitle, "", true);
		comboBoxLanguage.setSelectedItem(null);
		setText(textFieldTonality, "", true);
		setText(textFieldComposer, "", true);
		setText(textFieldAuthorText, "", true);
		setText(textFieldAuthorTranslation, "", true);
		setText(textFieldPublisher, "", true);
		setText(textFieldAdditionalCopyrightNotes, "", true);
		setText(textFieldTempo, "", true);
		setText(editorChordSequence, "", true);
		setText(editorDrumNotes, "", true);
		setText(editorSongNotes, "", true);
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
		textFieldTempo.setEnabled(state);
		editorChordSequence.setEnabled(state);
		editorDrumNotes.setEnabled(state);
		editorSongNotes.setEnabled(state);
		if (state) {
			editorLyrics.setCaretPosition(0);
		}
	}
	
	/**
	 * Reads song data and puts the values into the GUI elements.
	 *
	 * @param song
	 *            the songsModel object which should be read
	 */
	private synchronized void loadSongData(final Song song, final boolean rewind) {
		LOG.debug("loadSongData: {} {}", song.getTitle(), song.getUUID());
		setText(editorLyrics, song.getLyrics(), rewind);
		setText(textFieldTitle, song.getTitle(), rewind);
		((JTextField) comboBoxLanguage.getEditor().getEditorComponent()).setText(song.getLanguage());
		if (rewind) {
			((JTextField) comboBoxLanguage.getEditor().getEditorComponent()).setCaretPosition(0);
		}
		setText(textFieldTonality, song.getTonality(), rewind);
		setText(textFieldComposer, song.getComposer(), rewind);
		setText(textFieldAuthorText, song.getAuthorText(), rewind);
		setText(textFieldAuthorTranslation, song.getAuthorTranslation(), rewind);
		setText(textFieldPublisher, song.getPublisher(), rewind);
		setText(textFieldAdditionalCopyrightNotes, song.getAdditionalCopyrightNotes(), rewind);
		setText(textFieldTempo, song.getTempo(), rewind);
		setText(editorChordSequence, song.getChordSequence(), rewind);
		setText(editorDrumNotes, song.getDrumNotes(), rewind);
		setText(editorSongNotes, song.getSongNotes(), rewind);
	}
	
	private static void setText(JTextComponent textComponent, String textToSet, boolean rewind) {
		textComponent.setText(textToSet);
		if (rewind) {
			textComponent.setCaretPosition(0);
		}
	}
	
	protected void handleWindowClosing() {
		saveSongWithoutChangingGUI();
		handleSettingsSaveAndLock();
		boolean mayClose = controller.prepareClose();
		if (mayClose) {
			setVisible(false);
			dispose();
			controller.shutdown();
		} else {
			showErrorDialog(PROBLEM_WHILE_SAVING + FileAndDirectoryLocations.getLogDir());
		}
	}
	
	protected void handleSave() {
		saveSongWithoutChangingGUI();
		handleSettingsSaveAndLock();
		controller.saveAll();
	}
	
	protected void handleSongNew() {
		Song song = new Song(StringTools.createUUID());
		song.setTitle("New Song");
		song.setLyrics("Put your lyrics here");
		selectNewSongWithUUID = song.getUUID();
		controller.getSongsController().updateSong(song);
	}
	
	protected void handleSongDelete() {
		if (selectedSong != null) {
			controller.getSongsController().removeSong(selectedSong);
		}
	}
	
	protected void handleSongSelect() {
		if (selectedSong != null) {
			presentModel.addSong(selectedSong);
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
			presentModel.moveSong(presentList.getSelectedIndex(), newIndex);
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
			presentModel.moveSong(presentList.getSelectedIndex(), newIndex);
			presentList.setSelectedIndex(newIndex);
		}
	}
	
	protected void handleJumpToSelectedSong() {
		if (presentListSelected != null) {
			textFieldFilter.setText("");
			Song songInSongs = songsModel.getByUUID(presentListSelected.getUUID());
			if (songInSongs != null) {
				songsList.setSelectedValue(songInSongs, true);
			}
		}
	}
	
	protected void handleJumpToPresentedSong() {
		Song currentlyPresentedSong = controller.getCurrentlyPresentedSong();
		if (currentlyPresentedSong != null) {
			List<Song> songs = presentListModel.getAllElements();
			
			// Search for new equal songs starting with last used song position:
			int startIndex = presentList.getSelectedIndex() + 1;
			for (int i = 0; i < songs.size(); i++) {
				int p = (i + startIndex) % songs.size();
				if (songs.get(p).equals(currentlyPresentedSong)) {
					presentList.setSelectedValue(songs.get(p), true);
					handleJumpToSelectedSong();
					break;
				}
			}
		}
	}
	
	/**
	 * A present function, which can be called by a remote controller.
	 */
	public void present(Song song) {
		Song currentlyPresentedSong = controller.getCurrentlyPresentedSong();
		if (currentlyPresentedSong != null && !currentlyPresentedSong.equals(song)
			|| currentlyPresentedSong == null && song != null) {
			if (song != null) {
				presentSong(song);
			} else {
				handleBlankScreen();
			}
		}
	}
	
	/**
	 * A update playlist function, which can be called by a remote controller.
	 */
	public void updatePlaylist(SongsModel playlist) {
		Song selection = presentList.getSelectedValue();
		getPresentModel().update(playlist);
		if (selection != null && selection.equals(presentList.getSelectedValue())) {
			updateButtonsForPresentListSelection();
		} else if (!playlist.getSongs().contains(selection)) {
			presentList.clearSelection();
		} else {
			presentList.setSelectedValue(selection, true);
		}
		setDefaultDividerLocation();
	}
	
	/**
	 * A setActiveLine function, which can be called by a remote controller.
	 */
	public void setActiveLine(Integer part, Integer line) {
		controller.contentChange(() -> {
			if (!getUIParts().isEmpty()) {
				Boolean showTitle = settingsModel.get(SettingKey.SHOW_TITLE, Boolean.class);
				int noTitlePart = showTitle ? part : Math.max(part - 1, 0);
				getUIParts().get(noTitlePart).setActiveLine(line);
			}
		});
	}
	
	protected void handleSongPresent() {
		presentSong(presentListSelected);
	}
	
	protected void presentSong(Song song) {
		boolean success = controller.present(new Presentable(song, null));
		controller.contentChange(() -> controller.stopSlideShow());
		controller.contentChange(() -> {
			if (success) {
				clearSectionButtons();
				List<AddressablePart> parts = controller.getParts();
				Boolean showTitle = settingsModel.get(SettingKey.SHOW_TITLE, Boolean.class);
				int partIndex = showTitle ? 0 : 1;
				for (AddressablePart part : parts) {
					PartButtonGroup buttonGroup = new PartButtonGroup(part, partIndex, controller, this);
					panelSectionButtons.add(buttonGroup, panelSectionButtonsHints);
					listSectionButtons.add(buttonGroup);
					partIndex++;
				}
				
				// mark first line as active
				if (!listSectionButtons.isEmpty()) {
					listSectionButtons.get(0).setActiveLine(0);
				}
				
				// add empty component to consume any space that is left (so the parts appear at the top of the
				// scrollpane view)
				panelSectionButtons.add(new JLabel(""), panelSectionButtonsLastRowHints);
				
				panelSectionButtons.revalidate();
				panelSectionButtons.repaint();
				btnJumpToPresented.setEnabled(true);
			}
		});
	}
	
	protected void handleBlankScreen() {
		boolean success = controller.present(BLANK_SCREEN);
		controller.stopSlideShow();
		if (success) {
			clearSectionButtons();
			btnJumpToPresented.setEnabled(false);
		}
	}
	
	protected void handleLogoPresent() {
		boolean success = controller.present(new Presentable(null, controller.loadLogo()));
		controller.stopSlideShow();
		if (success) {
			clearSectionButtons();
			btnJumpToPresented.setEnabled(false);
		}
	}
	
	protected void handleSlideShowPresent() {
		controller.stopSlideShow();
		boolean success = controller.presentSlideShow();
		if (success) {
			clearSectionButtons();
			btnJumpToPresented.setEnabled(false);
		}
	}
	
	private void clearSectionButtons() {
		listSectionButtons.clear();
		panelSectionButtons.removeAll();
		panelSectionButtons.revalidate();
		panelSectionButtons.repaint();
	}
	
	private final void defineShortcuts() {
		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusManager.addKeyEventDispatcher(keyboardShortcutManager);
		
		keyboardShortcutManager.add(new KeyboardShortcut(KeyEvent.VK_ESCAPE, Modifiers.NONE, () -> {
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
		}));
		
		keyboardShortcutManager.add(new KeyboardShortcut(KeyEvent.VK_S, Modifiers.CTRL, () -> {
			LOG.debug("ctrl-s action");
			handleSaveAll();
		}));
		
		// TODO use single Shortcut (without ctrl), put an option in the global settings tab
		keyboardShortcutManager.add(new KeyboardShortcut(KeyEvent.VK_P, Modifiers.CTRL, () -> {
			LOG.debug("ctrl-p action");
			if (presentListSelected != null) {
				handleSongPresent();
			}
		}));
		
		// TODO use single Shortcut (without ctrl), put an option in the global settings tab
		keyboardShortcutManager.add(new KeyboardShortcut(KeyEvent.VK_B, Modifiers.CTRL, () -> {
			LOG.debug("ctrl-b action");
			handleBlankScreen();
		}));
		
		keyboardShortcutManager.add(new KeyboardShortcut(KeyEvent.VK_R, Modifiers.CTRL, () -> {
			LOG.debug("ctrl-r action");
			handleSaveAll();
			new Thread(() -> controller.initRemoteController()).start();
		}));
	}
	
	private void handleSaveAll() {
		saveSongWithoutChangingGUI();
		boolean success = controller.saveAll();
		if (!success) {
			showErrorDialog(PROBLEM_WHILE_SAVING
				+ FileAndDirectoryLocations.getLogDir());
		}
	}
	
	public void handleError(Throwable ex) {
		LOG.error("handled exception", ex);
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
	
	public MainWindow(MainController mainController, KeyboardShortcutManager keyboardShortcutManager,
		IndexerService indexer, ExportService exportService) {
		controller = mainController;
		this.keyboardShortcutManager = keyboardShortcutManager;
		this.indexer = indexer;
		this.exportService = exportService;
		controller.setMainWindow(this);
		setIconImages(getIconsFromResources(getClass()));
		setTitle("Song Database");
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
		
		// MARK Edit Panel
		panelSongList = new JPanel();
		panelSongList.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPane.setLeftComponent(panelSongList);
		panelSongList.setLayout(new BorderLayout(0, 0));
		
		JPanel panelFilter = new JPanel();
		panelFilter.setBorder(null);
		panelSongList.add(panelFilter, BorderLayout.NORTH);
		GridBagLayout gblPanelFilter = new GridBagLayout();
		gblPanelFilter.columnWidths = new int[] { 39, 114, 22, 0 };
		gblPanelFilter.rowHeights = new int[] { 22, 0 };
		gblPanelFilter.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblPanelFilter.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFilter.setLayout(gblPanelFilter);
		
		JLabel lblFilter = new JLabel("Filter:");
		GridBagConstraints gbcLblFilter = new GridBagConstraints();
		gbcLblFilter.anchor = GridBagConstraints.WEST;
		gbcLblFilter.insets = new Insets(0, 0, 0, 5);
		gbcLblFilter.gridx = 0;
		gbcLblFilter.gridy = 0;
		panelFilter.add(lblFilter, gbcLblFilter);
		
		textFieldFilter = new JTextField();
		GridBagConstraints gbcTextFieldFilter = new GridBagConstraints();
		gbcTextFieldFilter.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldFilter.insets = new Insets(0, 0, 0, 5);
		gbcTextFieldFilter.gridx = 1;
		gbcTextFieldFilter.gridy = 0;
		panelFilter.add(textFieldFilter, gbcTextFieldFilter);
		textFieldFilter.setColumns(10);
		
		btnClearFilter = new JButton("");
		btnClearFilter.addActionListener(safeAction(e -> {
			textFieldFilter.setText("");
			textFieldFilter.requestFocusInWindow();
		}));
		btnClearFilter.setMargin(new Insets(0, 0, 0, 0));
		btnClearFilter.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/clear.gif"));
		GridBagConstraints gbcBtnClearFilter = new GridBagConstraints();
		gbcBtnClearFilter.anchor = GridBagConstraints.NORTHWEST;
		gbcBtnClearFilter.gridx = 2;
		gbcBtnClearFilter.gridy = 0;
		panelFilter.add(btnClearFilter, gbcBtnClearFilter);
		
		JScrollPane scrollPaneSongList = new JScrollPane();
		scrollPaneSongList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelSongList.add(scrollPaneSongList, BorderLayout.CENTER);
		
		songsList = new FixedWidthJList<>();
		songsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (e.getClickCount() >= 2 && selectedSong != null) {
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
			.addListSelectionListener(e -> {
				try {
					handleSongsListSelectionChanged(e);
				} catch (Throwable ex) {
					handleError(ex);
				}
			});
		scrollPaneSongList.setViewportView(songsList);
		songCellRenderer = new SongCellRenderer(textFieldFilter);
		songsList.setCellRenderer(songCellRenderer);
		
		JPanel panelSongListButtons = new JPanel();
		panelSongList.add(panelSongListButtons, BorderLayout.SOUTH);
		GridBagLayout gblPanelSongListButtons = new GridBagLayout();
		gblPanelSongListButtons.columnWidths = new int[] { 0, 0, 0 };
		gblPanelSongListButtons.rowHeights = new int[] { 26 };
		gblPanelSongListButtons.columnWeights = new double[] { 0.0, 0.0, 0.0 };
		gblPanelSongListButtons.rowWeights = new double[] { 0.0 };
		panelSongListButtons.setLayout(gblPanelSongListButtons);
		
		btnNewSong = new JButton("New");
		btnNewSong.addActionListener(safeAction(e -> handleSongNew()));
		btnNewSong.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/newHighlighter.gif"));
		GridBagConstraints gbcBtnNewSong = new GridBagConstraints();
		gbcBtnNewSong.fill = GridBagConstraints.VERTICAL;
		gbcBtnNewSong.anchor = GridBagConstraints.WEST;
		gbcBtnNewSong.insets = new Insets(0, 0, 5, 5);
		gbcBtnNewSong.gridx = 0;
		gbcBtnNewSong.gridy = 0;
		panelSongListButtons.add(btnNewSong, gbcBtnNewSong);
		
		btnDeleteSong = new JButton("Delete");
		btnDeleteSong.addActionListener(safeAction(e -> handleSongDelete()));
		btnDeleteSong.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/deleteHighlighter.gif"));
		GridBagConstraints gbcBtnDeleteSong = new GridBagConstraints();
		gbcBtnDeleteSong.fill = GridBagConstraints.VERTICAL;
		gbcBtnDeleteSong.anchor = GridBagConstraints.WEST;
		gbcBtnDeleteSong.insets = new Insets(0, 0, 5, 5);
		gbcBtnDeleteSong.gridx = 1;
		gbcBtnDeleteSong.gridy = 0;
		panelSongListButtons.add(btnDeleteSong, gbcBtnDeleteSong);
		
		btnSelectSong = new JButton("Select");
		btnSelectSong.addActionListener(safeAction(e -> handleSongSelect()));
		btnSelectSong.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/month-up.png"));
		GridBagConstraints gbcBtnSelectSong = new GridBagConstraints();
		gbcBtnSelectSong.fill = GridBagConstraints.VERTICAL;
		gbcBtnSelectSong.anchor = GridBagConstraints.EAST;
		gbcBtnSelectSong.insets = new Insets(0, 0, 5, 0);
		gbcBtnSelectSong.gridx = 2;
		gbcBtnSelectSong.gridy = 0;
		panelSongListButtons.add(btnSelectSong, gbcBtnSelectSong);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		splitPane.setRightComponent(tabbedPane);
		
		JPanel panelEdit = new JPanel();
		panelEdit.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Edit Song", null, panelEdit, null);
		GridBagLayout gblPanelEdit = new GridBagLayout();
		gblPanelEdit.columnWidths = new int[] { 9999, 9999, 9999 };
		gblPanelEdit.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanelEdit.columnWeights = new double[] { 1.0, 1.0, 1.0 };
		gblPanelEdit.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
			Double.MIN_VALUE };
		panelEdit.setLayout(gblPanelEdit);
		
		JLabel lblLyricsAndChords = new JLabel("Lyrics and Chords");
		GridBagConstraints gbcLblLyricsAndChords = new GridBagConstraints();
		gbcLblLyricsAndChords.fill = GridBagConstraints.HORIZONTAL;
		gbcLblLyricsAndChords.gridwidth = 3;
		gbcLblLyricsAndChords.insets = new Insets(0, 0, 5, 0);
		gbcLblLyricsAndChords.gridx = 0;
		gbcLblLyricsAndChords.gridy = 0;
		panelEdit.add(lblLyricsAndChords, gbcLblLyricsAndChords);
		
		JScrollPane scrollPaneLyrics = new JScrollPane();
		GridBagConstraints gbcScrollPaneLyrics = new GridBagConstraints();
		gbcScrollPaneLyrics.weighty = 7.0;
		gbcScrollPaneLyrics.fill = GridBagConstraints.BOTH;
		gbcScrollPaneLyrics.gridwidth = 3;
		gbcScrollPaneLyrics.insets = new Insets(0, 0, 5, 0);
		gbcScrollPaneLyrics.gridx = 0;
		gbcScrollPaneLyrics.gridy = 1;
		panelEdit.add(scrollPaneLyrics, gbcScrollPaneLyrics);
		
		editorLyrics = new JEditorPane();
		editorLyrics
			.setFont(new Font("Monospaced", editorLyrics.getFont().getStyle(), editorLyrics.getFont().getSize()));
		editorLyrics.setBackground(Color.WHITE);
		scrollPaneLyrics.setViewportView(editorLyrics);
		
		JLabel lblTitle = new JLabel("Title");
		GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTitle.insets = new Insets(0, 0, 5, 5);
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 2;
		panelEdit.add(lblTitle, gbcLblTitle);
		
		JLabel lblComposer = new JLabel("Composer (Music)");
		GridBagConstraints gbcLblComposer = new GridBagConstraints();
		gbcLblComposer.fill = GridBagConstraints.HORIZONTAL;
		gbcLblComposer.insets = new Insets(0, 0, 5, 5);
		gbcLblComposer.gridx = 1;
		gbcLblComposer.gridy = 2;
		panelEdit.add(lblComposer, gbcLblComposer);
		
		JLabel lblPublisher = new JLabel("Publisher");
		GridBagConstraints gbcLblPublisher = new GridBagConstraints();
		gbcLblPublisher.fill = GridBagConstraints.HORIZONTAL;
		gbcLblPublisher.insets = new Insets(0, 0, 5, 0);
		gbcLblPublisher.gridx = 2;
		gbcLblPublisher.gridy = 2;
		panelEdit.add(lblPublisher, gbcLblPublisher);
		
		textFieldTitle = new JTextField();
		GridBagConstraints gbcTextFieldTitle = new GridBagConstraints();
		gbcTextFieldTitle.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldTitle.gridx = 0;
		gbcTextFieldTitle.gridy = 3;
		panelEdit.add(textFieldTitle, gbcTextFieldTitle);
		textFieldTitle.setColumns(10);
		
		textFieldComposer = new JTextField();
		GridBagConstraints gbcTextFieldComposer = new GridBagConstraints();
		gbcTextFieldComposer.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldComposer.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldComposer.gridx = 1;
		gbcTextFieldComposer.gridy = 3;
		panelEdit.add(textFieldComposer, gbcTextFieldComposer);
		textFieldComposer.setColumns(10);
		
		textFieldPublisher = new JTextField();
		GridBagConstraints gbcTextFieldPublisher = new GridBagConstraints();
		gbcTextFieldPublisher.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldPublisher.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldPublisher.gridx = 2;
		gbcTextFieldPublisher.gridy = 3;
		panelEdit.add(textFieldPublisher, gbcTextFieldPublisher);
		textFieldPublisher.setColumns(10);
		
		JLabel lblLanguage = new JLabel("Language");
		GridBagConstraints gbcLblLanguage = new GridBagConstraints();
		gbcLblLanguage.fill = GridBagConstraints.HORIZONTAL;
		gbcLblLanguage.insets = new Insets(0, 0, 5, 5);
		gbcLblLanguage.gridx = 0;
		gbcLblLanguage.gridy = 4;
		panelEdit.add(lblLanguage, gbcLblLanguage);
		
		JLabel lblAuthorText = new JLabel("Author (Text)");
		GridBagConstraints gbcLblAuthorText = new GridBagConstraints();
		gbcLblAuthorText.fill = GridBagConstraints.HORIZONTAL;
		gbcLblAuthorText.insets = new Insets(0, 0, 5, 5);
		gbcLblAuthorText.gridx = 1;
		gbcLblAuthorText.gridy = 4;
		panelEdit.add(lblAuthorText, gbcLblAuthorText);
		
		JLabel lblAdditionalCopyrightNotes = new JLabel("Additional Copyright Notes");
		GridBagConstraints gbcLblAdditionalCopyrightNotes = new GridBagConstraints();
		gbcLblAdditionalCopyrightNotes.fill = GridBagConstraints.HORIZONTAL;
		gbcLblAdditionalCopyrightNotes.insets = new Insets(0, 0, 5, 0);
		gbcLblAdditionalCopyrightNotes.gridx = 2;
		gbcLblAdditionalCopyrightNotes.gridy = 4;
		panelEdit.add(lblAdditionalCopyrightNotes, gbcLblAdditionalCopyrightNotes);
		
		comboBoxLanguage = new JComboBox<>();
		comboBoxLanguage.setEditable(true);
		GridBagConstraints gbcComboBoxLanguage = new GridBagConstraints();
		gbcComboBoxLanguage.insets = new Insets(0, 0, 5, 5);
		gbcComboBoxLanguage.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBoxLanguage.gridx = 0;
		gbcComboBoxLanguage.gridy = 5;
		panelEdit.add(comboBoxLanguage, gbcComboBoxLanguage);
		
		textFieldAuthorText = new JTextField();
		GridBagConstraints gbcTextFieldAuthorText = new GridBagConstraints();
		gbcTextFieldAuthorText.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldAuthorText.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldAuthorText.gridx = 1;
		gbcTextFieldAuthorText.gridy = 5;
		panelEdit.add(textFieldAuthorText, gbcTextFieldAuthorText);
		textFieldAuthorText.setColumns(10);
		
		textFieldAdditionalCopyrightNotes = new JTextField();
		GridBagConstraints gbcTextFieldAdditionalCopyrightNotes = new GridBagConstraints();
		gbcTextFieldAdditionalCopyrightNotes.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldAdditionalCopyrightNotes.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldAdditionalCopyrightNotes.gridx = 2;
		gbcTextFieldAdditionalCopyrightNotes.gridy = 5;
		panelEdit.add(textFieldAdditionalCopyrightNotes, gbcTextFieldAdditionalCopyrightNotes);
		textFieldAdditionalCopyrightNotes.setColumns(10);
		
		JLabel lblTonality = new JLabel("Tonality");
		GridBagConstraints gbcLblTonality = new GridBagConstraints();
		gbcLblTonality.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTonality.insets = new Insets(0, 0, 5, 5);
		gbcLblTonality.gridx = 0;
		gbcLblTonality.gridy = 6;
		panelEdit.add(lblTonality, gbcLblTonality);
		
		JLabel lblAuthorTranslation = new JLabel("Author (Translation)");
		GridBagConstraints gbcLblAuthorTranslation = new GridBagConstraints();
		gbcLblAuthorTranslation.fill = GridBagConstraints.HORIZONTAL;
		gbcLblAuthorTranslation.insets = new Insets(0, 0, 5, 5);
		gbcLblAuthorTranslation.gridx = 1;
		gbcLblAuthorTranslation.gridy = 6;
		panelEdit.add(lblAuthorTranslation, gbcLblAuthorTranslation);
		
		JLabel lblTempo = new JLabel("Tempo");
		GridBagConstraints gbcLblTempo = new GridBagConstraints();
		gbcLblTempo.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTempo.insets = new Insets(0, 0, 5, 0);
		gbcLblTempo.gridx = 2;
		gbcLblTempo.gridy = 6;
		panelEdit.add(lblTempo, gbcLblTempo);
		
		textFieldTonality = new JTextField();
		GridBagConstraints gbcTextFieldTonality = new GridBagConstraints();
		gbcTextFieldTonality.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldTonality.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldTonality.gridx = 0;
		gbcTextFieldTonality.gridy = 7;
		panelEdit.add(textFieldTonality, gbcTextFieldTonality);
		textFieldTonality.setColumns(10);
		
		textFieldAuthorTranslation = new JTextField();
		GridBagConstraints gbcTextFieldAuthorTranslation = new GridBagConstraints();
		gbcTextFieldAuthorTranslation.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldAuthorTranslation.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldAuthorTranslation.gridx = 1;
		gbcTextFieldAuthorTranslation.gridy = 7;
		panelEdit.add(textFieldAuthorTranslation, gbcTextFieldAuthorTranslation);
		textFieldAuthorTranslation.setColumns(10);
		
		textFieldTempo = new JTextField();
		GridBagConstraints gbcTextFieldSongNotes = new GridBagConstraints();
		gbcTextFieldSongNotes.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldSongNotes.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldSongNotes.gridx = 2;
		gbcTextFieldSongNotes.gridy = 7;
		panelEdit.add(textFieldTempo, gbcTextFieldSongNotes);
		textFieldTempo.setColumns(10);
		
		JLabel lblChordSequence = new JLabel("Chord Sequence");
		GridBagConstraints gbcLblChordSequence = new GridBagConstraints();
		gbcLblChordSequence.fill = GridBagConstraints.HORIZONTAL;
		gbcLblChordSequence.gridwidth = 1;
		gbcLblChordSequence.insets = new Insets(0, 0, 5, 5);
		gbcLblChordSequence.gridx = 0;
		gbcLblChordSequence.gridy = 8;
		panelEdit.add(lblChordSequence, gbcLblChordSequence);
		
		JScrollPane scrollPaneChordSequence = new JScrollPane();
		GridBagConstraints gbcScrollPaneChordSequence = new GridBagConstraints();
		gbcScrollPaneChordSequence.gridheight = 2;
		gbcScrollPaneChordSequence.weighty = 1.0;
		gbcScrollPaneChordSequence.fill = GridBagConstraints.BOTH;
		gbcScrollPaneChordSequence.gridwidth = 1;
		gbcScrollPaneChordSequence.insets = new Insets(0, 0, 0, 5);
		gbcScrollPaneChordSequence.gridx = 0;
		gbcScrollPaneChordSequence.gridy = 9;
		panelEdit.add(scrollPaneChordSequence, gbcScrollPaneChordSequence);
		
		editorChordSequence = new JEditorPane();
		editorChordSequence.setFont(new Font("Monospaced", editorChordSequence.getFont().getStyle(),
			editorChordSequence.getFont().getSize()));
		scrollPaneChordSequence.setViewportView(editorChordSequence);
		editorChordSequence.setBackground(Color.WHITE);
		
		JLabel lblDrumNotes = new JLabel("Drum notes");
		GridBagConstraints gbcLblDrumNotes = new GridBagConstraints();
		gbcLblDrumNotes.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDrumNotes.gridwidth = 1;
		gbcLblDrumNotes.insets = new Insets(0, 0, 5, 5);
		gbcLblDrumNotes.gridx = 1;
		gbcLblDrumNotes.gridy = 8;
		panelEdit.add(lblDrumNotes, gbcLblDrumNotes);
		
		JScrollPane scrollPaneDrumNotes = new JScrollPane();
		GridBagConstraints gbcScrollPaneDrumNotes = new GridBagConstraints();
		gbcScrollPaneDrumNotes.gridheight = 2;
		gbcScrollPaneDrumNotes.weighty = 1.0;
		gbcScrollPaneDrumNotes.fill = GridBagConstraints.BOTH;
		gbcScrollPaneDrumNotes.gridwidth = 1;
		gbcScrollPaneDrumNotes.insets = new Insets(0, 0, 0, 5);
		gbcScrollPaneDrumNotes.gridx = 1;
		gbcScrollPaneDrumNotes.gridy = 9;
		panelEdit.add(scrollPaneDrumNotes, gbcScrollPaneDrumNotes);
		
		editorDrumNotes = new JEditorPane();
		editorDrumNotes.setFont(new Font("Monospaced", editorDrumNotes.getFont().getStyle(),
			editorDrumNotes.getFont().getSize()));
		scrollPaneDrumNotes.setViewportView(editorDrumNotes);
		editorDrumNotes.setBackground(Color.WHITE);
		
		JLabel lblSongNotes = new JLabel("Song Notes (not shown in presentation)");
		GridBagConstraints gbcLblSongNotes = new GridBagConstraints();
		gbcLblSongNotes.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSongNotes.gridwidth = 1;
		gbcLblSongNotes.insets = new Insets(0, 0, 5, 5);
		gbcLblSongNotes.gridx = 2;
		gbcLblSongNotes.gridy = 8;
		panelEdit.add(lblSongNotes, gbcLblSongNotes);
		
		JScrollPane scrollPaneSongNotes = new JScrollPane();
		GridBagConstraints gbcScrollPaneSongNotes = new GridBagConstraints();
		gbcScrollPaneSongNotes.gridheight = 2;
		gbcScrollPaneSongNotes.weighty = 1.0;
		gbcScrollPaneSongNotes.fill = GridBagConstraints.BOTH;
		gbcScrollPaneSongNotes.gridwidth = 1;
		gbcScrollPaneSongNotes.insets = new Insets(0, 0, 0, 5);
		gbcScrollPaneSongNotes.gridx = 2;
		gbcScrollPaneSongNotes.gridy = 9;
		panelEdit.add(scrollPaneSongNotes, gbcScrollPaneSongNotes);
		
		editorSongNotes = new JEditorPane();
		scrollPaneSongNotes.setViewportView(editorSongNotes);
		editorSongNotes.setBackground(Color.WHITE);
		
		for (JComponent v : new JComponent[] {
			textFieldTonality,
			textFieldAdditionalCopyrightNotes,
			textFieldAuthorText,
			textFieldAuthorTranslation,
			textFieldComposer,
			textFieldPublisher,
			textFieldTempo,
			textFieldTitle,
			(JTextField) comboBoxLanguage.getEditor().getEditorComponent(),
			editorLyrics,
			editorChordSequence,
			editorSongNotes,
			editorDrumNotes
		}) {
			v.addFocusListener(new FocusAdapter() {
				
				@Override
				public void focusLost(FocusEvent e) {
					try {
						saveSongWithoutChangingGUI();
					} catch (Throwable ex) {
						handleError(ex);
					}
				}
			});
		}
		
		// MARK Present Panel
		JPanel panelPresent = new JPanel();
		tabbedPane.addTab("Present Songs", null, panelPresent, null);
		panelPresent.setLayout(new BorderLayout(0, 0));
		
		splitPanePresent = new JSplitPane();
		splitPanePresent.setBorder(null);
		panelPresent.add(splitPanePresent, BorderLayout.CENTER);
		
		// left part, filterable list of all songs
		JPanel panelPresentLeft = new JPanel();
		panelPresentLeft.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPanePresent.setLeftComponent(panelPresentLeft);
		panelPresentLeft.setLayout(new BorderLayout(0, 0));
		splitPanePresent.setDividerLocation(-1);
		
		JScrollPane scrollPanePresentSongList = new JScrollPane();
		scrollPanePresentSongList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelPresentLeft.add(scrollPanePresentSongList, BorderLayout.CENTER);
		
		presentList = new FixedWidthJList<>();
		presentList.addListSelectionListener(e -> {
			try {
				handlePresentListSelectionChanged(e);
			} catch (Throwable ex) {
				handleError(ex);
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
		presentList.setCellRenderer(songCellRenderer);
		scrollPanePresentSongList.setViewportView(presentList);
		
		selectedSongListButtons = new JPanel();
		panelPresentLeft.add(selectedSongListButtons, BorderLayout.EAST);
		GridBagLayout gblPanelSelectedSongListButtons = new GridBagLayout();
		gblPanelSelectedSongListButtons.columnWidths = new int[] { 0 };
		gblPanelSelectedSongListButtons.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gblPanelSelectedSongListButtons.columnWeights = new double[] { 0.0 };
		gblPanelSelectedSongListButtons.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0 };
		selectedSongListButtons.setLayout(gblPanelSelectedSongListButtons);
		
		btnUp = new JButton("");
		btnUp.addActionListener(safeAction(e -> handleSongUp()));
		btnUp.setToolTipText("Up");
		btnUp.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/sortUp.png"));
		GridBagConstraints gbcBtnUp = new GridBagConstraints();
		gbcBtnUp.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnUp.anchor = GridBagConstraints.SOUTH;
		gbcBtnUp.insets = new Insets(0, 0, 5, 0);
		gbcBtnUp.gridx = 0;
		gbcBtnUp.gridy = 1;
		selectedSongListButtons.add(btnUp, gbcBtnUp);
		
		btnUnselect = new JButton("");
		btnUnselect.setToolTipText("Unselect");
		btnUnselect.addActionListener(safeAction(e -> handleSongUnselect()));
		btnUnselect.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/JXErrorPane16.png"));
		GridBagConstraints gbcBtnUnselect = new GridBagConstraints();
		gbcBtnUnselect.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnUnselect.insets = new Insets(0, 0, 5, 0);
		gbcBtnUnselect.anchor = GridBagConstraints.NORTH;
		gbcBtnUnselect.gridx = 0;
		gbcBtnUnselect.gridy = 2;
		selectedSongListButtons.add(btnUnselect, gbcBtnUnselect);
		
		btnDown = new JButton("");
		btnDown.addActionListener(safeAction(e -> handleSongDown()));
		btnDown.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/sortDown.png"));
		btnDown.setToolTipText("Down");
		GridBagConstraints gbcBtnDown = new GridBagConstraints();
		gbcBtnDown.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnDown.anchor = GridBagConstraints.NORTH;
		gbcBtnDown.gridx = 0;
		gbcBtnDown.gridy = 3;
		selectedSongListButtons.add(btnDown, gbcBtnDown);
		
		btnJumpToSelected = new JButton("Jump to selected song");
		btnJumpToSelected.addActionListener(safeAction(e -> handleJumpToSelectedSong()));
		panelPresentLeft.add(btnJumpToSelected, BorderLayout.SOUTH);
		
		// right part, controls and active song
		JPanel panelPresentRight = new JPanel();
		panelPresentRight.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPanePresent.setRightComponent(panelPresentRight);
		panelPresentRight.setLayout(new BorderLayout(0, 0));
		
		JPanel panelPresentationButtons = new JPanel();
		panelPresentRight.add(panelPresentationButtons, BorderLayout.CENTER);
		
		GridBagLayout gblPanelPresentationButtons = new GridBagLayout();
		gblPanelPresentationButtons.columnWidths = new int[] { 0, 0, 0, 0 };
		// gblPanelPresentationButtons.rowHeights = new int[] { 1, 1, 1, 0, 0, 0 };
		gblPanelPresentationButtons.columnWeights = new double[] { 0.5d, 0.5d, 0.5d, 0.5d };
		gblPanelPresentationButtons.rowWeights = new double[] { 0d, 0d, 0d, 0d, 1d };
		panelPresentationButtons.setLayout(gblPanelPresentationButtons);
		
		btnPresentSelectedSong = new JButton("Present selected song");
		btnPresentSelectedSong.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/play.png"));
		btnPresentSelectedSong.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnPresentSelectedSong.setHorizontalTextPosition(SwingConstants.CENTER);
		btnPresentSelectedSong.addActionListener(safeAction(e -> handleSongPresent()));
		GridBagConstraints gbcBtnPresentSelectedSong = new GridBagConstraints();
		gbcBtnPresentSelectedSong.fill = GridBagConstraints.BOTH;
		gbcBtnPresentSelectedSong.insets = new Insets(10, 0, 15, 5);
		gbcBtnPresentSelectedSong.gridheight = 3;
		// gbcBtnPresentSelectedSong.ipadx = 20;
		gbcBtnPresentSelectedSong.ipady = 80;
		gbcBtnPresentSelectedSong.gridx = 0;
		gbcBtnPresentSelectedSong.gridy = 0;
		btnPresentSelectedSong.setPreferredSize(new Dimension(64, 64));
		panelPresentationButtons.add(btnPresentSelectedSong, gbcBtnPresentSelectedSong);
		
		btnShowBlankScreen = new JButton("Blank screen");
		btnShowBlankScreen.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/stop.png"));
		btnShowBlankScreen.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnShowBlankScreen.setHorizontalTextPosition(SwingConstants.CENTER);
		btnShowBlankScreen.addActionListener(safeAction(e -> handleBlankScreen()));
		GridBagConstraints gbcBtnShowBlankScreen = new GridBagConstraints();
		gbcBtnShowBlankScreen.fill = GridBagConstraints.BOTH;
		gbcBtnShowBlankScreen.insets = new Insets(10, 0, 15, 5);
		gbcBtnShowBlankScreen.gridheight = 3;
		// gbcBtnShowBlankScreen.ipadx = 30;
		gbcBtnShowBlankScreen.ipady = 80;
		gbcBtnShowBlankScreen.gridx = 1;
		gbcBtnShowBlankScreen.gridy = 0;
		btnShowBlankScreen.setPreferredSize(new Dimension(64, 64));
		panelPresentationButtons.add(btnShowBlankScreen, gbcBtnShowBlankScreen);
		
		btnShowLogo = new JButton("Show logo");
		btnShowLogo.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/picture.png"));
		btnShowLogo.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnShowLogo.setHorizontalTextPosition(SwingConstants.CENTER);
		btnShowLogo.addActionListener(safeAction(e -> handleLogoPresent()));
		GridBagConstraints gbcBtnShowLogo = new GridBagConstraints();
		gbcBtnShowLogo.fill = GridBagConstraints.BOTH;
		gbcBtnShowLogo.insets = new Insets(10, 0, 15, 5);
		gbcBtnShowLogo.gridheight = 3;
		// gbcBtnShowLogo.ipadx = 40;
		gbcBtnShowLogo.ipady = 80;
		gbcBtnShowLogo.gridx = 2;
		gbcBtnShowLogo.gridy = 0;
		btnShowLogo.setPreferredSize(new Dimension(64, 64));
		panelPresentationButtons.add(btnShowLogo, gbcBtnShowLogo);
		
		btnSlideshow = new JButton("Slide Show");
		btnSlideshow.setIcon(ResourceTools.getIcon(getClass(), "/org/zephyrsoft/sdb2/movie.png"));
		btnSlideshow.setVerticalTextPosition(SwingConstants.BOTTOM);
		btnSlideshow.setHorizontalTextPosition(SwingConstants.CENTER);
		btnSlideshow.addActionListener(safeAction(e -> handleSlideShowPresent()));
		GridBagConstraints gbcBtnSlideshow = new GridBagConstraints();
		gbcBtnSlideshow.fill = GridBagConstraints.BOTH;
		gbcBtnSlideshow.gridheight = 3;
		gbcBtnSlideshow.insets = new Insets(10, 0, 15, 5);
		gbcBtnSlideshow.ipady = 80;
		gbcBtnSlideshow.gridx = 3;
		gbcBtnSlideshow.gridy = 0;
		btnSlideshow.setPreferredSize(new Dimension(64, 64));
		panelPresentationButtons.add(btnSlideshow, gbcBtnSlideshow);
		
		JLabel lblSections = new JLabel("Sections:");
		GridBagConstraints gbcLblSections = new GridBagConstraints();
		gbcLblSections.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSections.insets = new Insets(0, 0, 5, 5);
		gbcLblSections.gridx = 0;
		gbcLblSections.gridy = 3;
		panelPresentationButtons.add(lblSections, gbcLblSections);
		
		scrollPaneSectionButtons = new JScrollPane();
		GridBagConstraints gbcScrollPaneSectionButtons = new GridBagConstraints();
		gbcScrollPaneSectionButtons.insets = new Insets(0, 0, 0, 5);
		gbcScrollPaneSectionButtons.fill = GridBagConstraints.BOTH;
		gbcScrollPaneSectionButtons.gridwidth = 4;
		gbcScrollPaneSectionButtons.gridx = 0;
		gbcScrollPaneSectionButtons.gridy = 4;
		panelPresentationButtons.add(scrollPaneSectionButtons, gbcScrollPaneSectionButtons);
		
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
		btnJumpToPresented.addActionListener(safeAction(e -> handleJumpToPresentedSong()));
		panelPresentRight.add(btnJumpToPresented, BorderLayout.SOUTH);
		
		// MARK Export/Import Panel
		JPanel panelImportExportStatistics = new JPanel();
		panelImportExportStatistics.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Import / Export / Statistics", null, panelImportExportStatistics, null);
		GridBagLayout gblPanelImportExportStatistics = new GridBagLayout();
		gblPanelImportExportStatistics.columnWidths = new int[] { 0, 70, 0, 0 };
		gblPanelImportExportStatistics.rowHeights = new int[] { 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30 };
		gblPanelImportExportStatistics.columnWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gblPanelImportExportStatistics.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			Double.MIN_VALUE };
		panelImportExportStatistics.setLayout(gblPanelImportExportStatistics);
		
		JLabel lblExportToPDF = new JLabel("Songs as PDF");
		GridBagConstraints gbc_lblExportToPDF = new GridBagConstraints();
		gbc_lblExportToPDF.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblExportToPDF.gridwidth = 3;
		gbc_lblExportToPDF.anchor = GridBagConstraints.SOUTH;
		gbc_lblExportToPDF.insets = new Insets(0, 0, 5, 0);
		gbc_lblExportToPDF.gridx = 0;
		gbc_lblExportToPDF.gridy = 0;
		panelImportExportStatistics.add(lblExportToPDF, gbc_lblExportToPDF);
		
		chckbxWithTranslation = new JCheckBox("with translation");
		GridBagConstraints gbc_chckbxWithTranslation = new GridBagConstraints();
		gbc_chckbxWithTranslation.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxWithTranslation.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxWithTranslation.gridx = 0;
		gbc_chckbxWithTranslation.gridy = 1;
		panelImportExportStatistics.add(chckbxWithTranslation, gbc_chckbxWithTranslation);
		
		chckbxWithChords = new JCheckBox("with chords");
		GridBagConstraints gbc_chckbxWithChords = new GridBagConstraints();
		gbc_chckbxWithChords.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxWithChords.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxWithChords.gridx = 0;
		gbc_chckbxWithChords.gridy = 2;
		panelImportExportStatistics.add(chckbxWithChords, gbc_chckbxWithChords);
		
		btnExportPdfSelected = new JButton("Export selected song");
		btnExportPdfSelected.addActionListener(safeAction(e -> handleExport(Arrays.asList(selectedSong))));
		
		chckbxOnlyExportSongs = new JCheckBox("only export songs which have chords");
		GridBagConstraints gbc_chckbxOnlyExportSongs = new GridBagConstraints();
		gbc_chckbxOnlyExportSongs.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxOnlyExportSongs.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxOnlyExportSongs.gridx = 0;
		gbc_chckbxOnlyExportSongs.gridy = 3;
		panelImportExportStatistics.add(chckbxOnlyExportSongs, gbc_chckbxOnlyExportSongs);
		GridBagConstraints gbc_btnExportPdfSelected = new GridBagConstraints();
		gbc_btnExportPdfSelected.anchor = GridBagConstraints.NORTH;
		gbc_btnExportPdfSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportPdfSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportPdfSelected.gridx = 0;
		gbc_btnExportPdfSelected.gridy = 4;
		panelImportExportStatistics.add(btnExportPdfSelected, gbc_btnExportPdfSelected);
		
		btnExportPdfAll = new JButton("Export all songs");
		btnExportPdfAll.addActionListener(safeAction(e -> handleExport(songsModel.getSongs())));
		GridBagConstraints gbc_btnExportPdfAll = new GridBagConstraints();
		gbc_btnExportPdfAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportPdfAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportPdfAll.gridx = 0;
		gbc_btnExportPdfAll.gridy = 5;
		panelImportExportStatistics.add(btnExportPdfAll, gbc_btnExportPdfAll);
		
		btnExportStatisticsAll = new JButton("Export statistics");
		btnExportStatisticsAll.addActionListener(safeAction(e -> {
			// select target
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("choose target file for statistics export");
			CustomFileFilter filter = new CustomFileFilter("Excel", ".xls");
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
			chooser.setApproveButtonText("Export");
			chooser.setSelectedFile(new File("song-statistics.xls"));
			int result = chooser.showOpenDialog(MainWindow.this);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				File target = chooser.getSelectedFile();
				// export
				try {
					controller.exportStatisticsAll(target);
				} catch (Throwable ex) {
					handleError(ex);
				}
			}
		}));
		
		labelExportStatisctics = new JLabel("Statistics");
		GridBagConstraints gbc_labelExportStatisctics = new GridBagConstraints();
		gbc_labelExportStatisctics.anchor = GridBagConstraints.SOUTH;
		gbc_labelExportStatisctics.gridwidth = 3;
		gbc_labelExportStatisctics.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelExportStatisctics.insets = new Insets(0, 0, 5, 0);
		gbc_labelExportStatisctics.gridx = 0;
		gbc_labelExportStatisctics.gridy = 7;
		panelImportExportStatistics.add(labelExportStatisctics, gbc_labelExportStatisctics);
		GridBagConstraints gbcBtnExportStatisticsAll = new GridBagConstraints();
		gbcBtnExportStatisticsAll.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnExportStatisticsAll.insets = new Insets(0, 0, 5, 5);
		gbcBtnExportStatisticsAll.gridx = 0;
		gbcBtnExportStatisticsAll.gridy = 8;
		panelImportExportStatistics.add(btnExportStatisticsAll, gbcBtnExportStatisticsAll);
		
		// TODO comment in again when there are importers
		// JLabel lblImportingSongs = new JLabel("Importing Songs");
		// GridBagConstraints gbcLblImportingSongs = new GridBagConstraints();
		// gbcLblImportingSongs.gridwidth = 3;
		// gbcLblImportingSongs.fill = GridBagConstraints.HORIZONTAL;
		// gbcLblImportingSongs.anchor = GridBagConstraints.SOUTH;
		// gbcLblImportingSongs.insets = new Insets(0, 0, 5, 0);
		// gbcLblImportingSongs.gridx = 0;
		// gbcLblImportingSongs.gridy = 8;
		// panelImportExportStatistics.add(lblImportingSongs, gbcLblImportingSongs);
		
		// TODO handle importers dynamically, load every implementation of "Importer" as button (=> ServiceLoader?)
		// btnImportFromEasiSlides = new JButton("Import from EasiSlides 4.0");
		// btnImportFromEasiSlides.addActionListener(safeAction(e -> handleImportFromEasiSlides()));
		// GridBagConstraints gbcBtnImportFromEasiSlides = new GridBagConstraints();
		// gbcBtnImportFromEasiSlides.fill = GridBagConstraints.HORIZONTAL;
		// gbcBtnImportFromEasiSlides.insets = new Insets(0, 0, 5, 5);
		// gbcBtnImportFromEasiSlides.gridx = 0;
		// gbcBtnImportFromEasiSlides.gridy = 9;
		// panelImportExportStatistics.add(btnImportFromEasiSlides, gbcBtnImportFromEasiSlides);
		
		JLabel lblProgramVersionTitle = new JLabel("Program Version");
		lblProgramVersionTitle.setFont(new Font("DejaVu Sans", Font.ITALIC, 12));
		GridBagConstraints gbcLblProgramVersionTitle = new GridBagConstraints();
		gbcLblProgramVersionTitle.gridwidth = 3;
		gbcLblProgramVersionTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblProgramVersionTitle.insets = new Insets(0, 0, 5, 0);
		gbcLblProgramVersionTitle.gridx = 0;
		gbcLblProgramVersionTitle.gridy = 10;
		panelImportExportStatistics.add(lblProgramVersionTitle, gbcLblProgramVersionTitle);
		
		lblProgramVersion = new JLabel("<PROGRAM VERSION>");
		lblProgramVersion.setBorder(new EmptyBorder(0, 0, 0, 0));
		lblProgramVersion.setFont(new Font("DejaVu Sans", Font.ITALIC, 12));
		GridBagConstraints gbcLblProgramVersion = new GridBagConstraints();
		gbcLblProgramVersion.insets = new Insets(0, 0, 5, 0);
		gbcLblProgramVersion.fill = GridBagConstraints.HORIZONTAL;
		gbcLblProgramVersion.gridwidth = 3;
		gbcLblProgramVersion.gridx = 0;
		gbcLblProgramVersion.gridy = 11;
		panelImportExportStatistics.add(lblProgramVersion, gbcLblProgramVersion);
		
		lblGitCommitHash = new JLabel(" ");
		lblGitCommitHash.setBorder(new EmptyBorder(0, 0, 0, 0));
		lblGitCommitHash.setFont(new Font("DejaVu Sans", Font.ITALIC, 12));
		GridBagConstraints gbc_lblGitCommitHash = new GridBagConstraints();
		gbc_lblGitCommitHash.gridwidth = 3;
		gbc_lblGitCommitHash.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblGitCommitHash.insets = new Insets(0, 0, 5, 0);
		gbc_lblGitCommitHash.gridx = 0;
		gbc_lblGitCommitHash.gridy = 12;
		panelImportExportStatistics.add(lblGitCommitHash, gbc_lblGitCommitHash);
		
		// MARK Settings Panel
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
		GridBagLayout gblPanel = new GridBagLayout();
		gblPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gblPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0 };
		gblPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		panel.setLayout(gblPanel);
		
		btnUnlock = new JButton("Unlock");
		btnUnlock.addActionListener(safeAction(e -> handleSettingsUnlock()));
		GridBagConstraints gbcBtnUnlock = new GridBagConstraints();
		gbcBtnUnlock.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnUnlock.gridwidth = 5;
		gbcBtnUnlock.insets = new Insets(0, 0, 5, 5);
		gbcBtnUnlock.gridx = 1;
		gbcBtnUnlock.gridy = 1;
		panel.add(btnUnlock, gbcBtnUnlock);
		
		JLabel lblTitleFont = new JLabel("Title font");
		GridBagConstraints gbcLblTitleFont = new GridBagConstraints();
		gbcLblTitleFont.anchor = GridBagConstraints.EAST;
		gbcLblTitleFont.insets = new Insets(0, 0, 5, 5);
		gbcLblTitleFont.gridx = 1;
		gbcLblTitleFont.gridy = 2;
		panel.add(lblTitleFont, gbcLblTitleFont);
		
		btnSelectTitleFont1 = new JButton("Screen A");
		btnSelectTitleFont1.addActionListener(safeAction(e -> selectFont(SettingKey.TITLE_FONT)));
		GridBagConstraints gbc_btnSelectTitleFont1 = new GridBagConstraints();
		gbc_btnSelectTitleFont1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTitleFont1.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTitleFont1.gridx = 3;
		gbc_btnSelectTitleFont1.gridy = 2;
		panel.add(btnSelectTitleFont1, gbc_btnSelectTitleFont1);
		
		btnSelectTitleFont2 = new JButton("Screen B");
		btnSelectTitleFont2.addActionListener(safeAction(e -> selectFont(SettingKey.TITLE_FONT_2)));
		GridBagConstraints gbc_btnSelectTitleFont2 = new GridBagConstraints();
		gbc_btnSelectTitleFont2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTitleFont2.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTitleFont2.gridx = 4;
		gbc_btnSelectTitleFont2.gridy = 2;
		panel.add(btnSelectTitleFont2, gbc_btnSelectTitleFont2);
		
		btnSelectTitleFontBoth = new JButton("Change for both Screens");
		btnSelectTitleFontBoth.addActionListener(safeAction(e -> selectFont(SettingKey.TITLE_FONT, SettingKey.TITLE_FONT_2)));
		GridBagConstraints gbc_btnSelectTitleFontBoth = new GridBagConstraints();
		gbc_btnSelectTitleFontBoth.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTitleFontBoth.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTitleFontBoth.gridx = 5;
		gbc_btnSelectTitleFontBoth.gridy = 2;
		panel.add(btnSelectTitleFontBoth, gbc_btnSelectTitleFontBoth);
		
		JLabel lblLyricsFont = new JLabel("Lyrics font");
		GridBagConstraints gbcLblLyricsFont = new GridBagConstraints();
		gbcLblLyricsFont.anchor = GridBagConstraints.EAST;
		gbcLblLyricsFont.insets = new Insets(0, 0, 5, 5);
		gbcLblLyricsFont.gridx = 1;
		gbcLblLyricsFont.gridy = 3;
		panel.add(lblLyricsFont, gbcLblLyricsFont);
		
		btnSelectLyricsFont1 = new JButton("Screen A");
		btnSelectLyricsFont1.addActionListener(safeAction(e -> selectFont(SettingKey.LYRICS_FONT)));
		GridBagConstraints gbc_btnSelectLyricsFont1 = new GridBagConstraints();
		gbc_btnSelectLyricsFont1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectLyricsFont1.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectLyricsFont1.gridx = 3;
		gbc_btnSelectLyricsFont1.gridy = 3;
		panel.add(btnSelectLyricsFont1, gbc_btnSelectLyricsFont1);
		
		btnSelectLyricsFont2 = new JButton("Screen B");
		btnSelectLyricsFont2.addActionListener(safeAction(e -> selectFont(SettingKey.LYRICS_FONT_2)));
		GridBagConstraints gbc_btnSelectLyricsFont2 = new GridBagConstraints();
		gbc_btnSelectLyricsFont2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectLyricsFont2.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectLyricsFont2.gridx = 4;
		gbc_btnSelectLyricsFont2.gridy = 3;
		panel.add(btnSelectLyricsFont2, gbc_btnSelectLyricsFont2);
		
		btnSelectLyricsFontBoth = new JButton("Change for both Screens");
		btnSelectLyricsFontBoth.addActionListener(safeAction(e -> selectFont(SettingKey.LYRICS_FONT, SettingKey.LYRICS_FONT_2)));
		GridBagConstraints gbc_btnSelectLyricsFontBoth = new GridBagConstraints();
		gbc_btnSelectLyricsFontBoth.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectLyricsFontBoth.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectLyricsFontBoth.gridx = 5;
		gbc_btnSelectLyricsFontBoth.gridy = 3;
		panel.add(btnSelectLyricsFontBoth, gbc_btnSelectLyricsFontBoth);
		
		JLabel lblChordSequenceFont = new JLabel("Chord sequence font");
		GridBagConstraints gbc_lblChordSequenceFont = new GridBagConstraints();
		gbc_lblChordSequenceFont.anchor = GridBagConstraints.EAST;
		gbc_lblChordSequenceFont.insets = new Insets(0, 0, 5, 5);
		gbc_lblChordSequenceFont.gridx = 1;
		gbc_lblChordSequenceFont.gridy = 4;
		panel.add(lblChordSequenceFont, gbc_lblChordSequenceFont);
		
		btnSelectChordSequenceFont1 = new JButton("Screen A");
		btnSelectChordSequenceFont1.addActionListener(safeAction(e -> selectFont(SettingKey.CHORD_SEQUENCE_FONT)));
		GridBagConstraints gbc_btnChordSequenceFont1 = new GridBagConstraints();
		gbc_btnChordSequenceFont1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnChordSequenceFont1.insets = new Insets(0, 0, 5, 5);
		gbc_btnChordSequenceFont1.gridx = 3;
		gbc_btnChordSequenceFont1.gridy = 4;
		panel.add(btnSelectChordSequenceFont1, gbc_btnChordSequenceFont1);
		
		btnSelectChordSequenceFont2 = new JButton("Screen B");
		btnSelectChordSequenceFont2.addActionListener(safeAction(e -> selectFont(SettingKey.CHORD_SEQUENCE_FONT_2)));
		GridBagConstraints gbc_btnChordSequenceFont2 = new GridBagConstraints();
		gbc_btnChordSequenceFont2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnChordSequenceFont2.insets = new Insets(0, 0, 5, 5);
		gbc_btnChordSequenceFont2.gridx = 4;
		gbc_btnChordSequenceFont2.gridy = 4;
		panel.add(btnSelectChordSequenceFont2, gbc_btnChordSequenceFont2);
		
		btnSelectChordSequenceFontBoth = new JButton("Change for both Screens");
		btnSelectChordSequenceFontBoth.addActionListener(safeAction(e -> selectFont(SettingKey.CHORD_SEQUENCE_FONT,
			SettingKey.CHORD_SEQUENCE_FONT_2)));
		GridBagConstraints gbc_btnChordSequenceFontBoth = new GridBagConstraints();
		gbc_btnChordSequenceFontBoth.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnChordSequenceFontBoth.insets = new Insets(0, 0, 5, 5);
		gbc_btnChordSequenceFontBoth.gridx = 5;
		gbc_btnChordSequenceFontBoth.gridy = 4;
		panel.add(btnSelectChordSequenceFontBoth, gbc_btnChordSequenceFontBoth);
		
		JLabel lblTranslationFont = new JLabel("Translation font");
		GridBagConstraints gbcLblTranslationFont = new GridBagConstraints();
		gbcLblTranslationFont.anchor = GridBagConstraints.EAST;
		gbcLblTranslationFont.insets = new Insets(0, 0, 5, 5);
		gbcLblTranslationFont.gridx = 1;
		gbcLblTranslationFont.gridy = 5;
		panel.add(lblTranslationFont, gbcLblTranslationFont);
		
		btnSelectTranslationFont1 = new JButton("Screen A");
		btnSelectTranslationFont1.addActionListener(safeAction(e -> selectFont(SettingKey.TRANSLATION_FONT)));
		GridBagConstraints gbc_btnSelectTranslationFont1 = new GridBagConstraints();
		gbc_btnSelectTranslationFont1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTranslationFont1.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTranslationFont1.gridx = 3;
		gbc_btnSelectTranslationFont1.gridy = 5;
		panel.add(btnSelectTranslationFont1, gbc_btnSelectTranslationFont1);
		
		btnSelectTranslationFont2 = new JButton("Screen B");
		btnSelectTranslationFont2.addActionListener(safeAction(e -> selectFont(SettingKey.TRANSLATION_FONT_2)));
		GridBagConstraints gbc_btnSelectTranslationFont2 = new GridBagConstraints();
		gbc_btnSelectTranslationFont2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTranslationFont2.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTranslationFont2.gridx = 4;
		gbc_btnSelectTranslationFont2.gridy = 5;
		panel.add(btnSelectTranslationFont2, gbc_btnSelectTranslationFont2);
		
		btnSelectTranslationFontBoth = new JButton("Change for both Screens");
		btnSelectTranslationFontBoth.addActionListener(safeAction(e -> selectFont(SettingKey.TRANSLATION_FONT, SettingKey.TRANSLATION_FONT_2)));
		GridBagConstraints gbc_btnSelectTranslationFontBoth = new GridBagConstraints();
		gbc_btnSelectTranslationFontBoth.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTranslationFontBoth.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTranslationFontBoth.gridx = 5;
		gbc_btnSelectTranslationFontBoth.gridy = 5;
		panel.add(btnSelectTranslationFontBoth, gbc_btnSelectTranslationFontBoth);
		
		JLabel lblCopyrightFont = new JLabel("Copyright font");
		GridBagConstraints gbcLblCopyrightFont = new GridBagConstraints();
		gbcLblCopyrightFont.anchor = GridBagConstraints.EAST;
		gbcLblCopyrightFont.insets = new Insets(0, 0, 5, 5);
		gbcLblCopyrightFont.gridx = 1;
		gbcLblCopyrightFont.gridy = 6;
		panel.add(lblCopyrightFont, gbcLblCopyrightFont);
		
		btnSelectCopyrightFont1 = new JButton("Screen A");
		btnSelectCopyrightFont1.addActionListener(safeAction(e -> selectFont(SettingKey.COPYRIGHT_FONT)));
		GridBagConstraints gbc_btnSelectCopyrightFont1 = new GridBagConstraints();
		gbc_btnSelectCopyrightFont1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectCopyrightFont1.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectCopyrightFont1.gridx = 3;
		gbc_btnSelectCopyrightFont1.gridy = 6;
		panel.add(btnSelectCopyrightFont1, gbc_btnSelectCopyrightFont1);
		
		btnSelectCopyrightFont2 = new JButton("Screen B");
		btnSelectCopyrightFont2.addActionListener(safeAction(e -> selectFont(SettingKey.COPYRIGHT_FONT_2)));
		GridBagConstraints gbc_btnSelectCopyrightFont2 = new GridBagConstraints();
		gbc_btnSelectCopyrightFont2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectCopyrightFont2.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectCopyrightFont2.gridx = 4;
		gbc_btnSelectCopyrightFont2.gridy = 6;
		panel.add(btnSelectCopyrightFont2, gbc_btnSelectCopyrightFont2);
		
		btnSelectCopyrightFontBoth = new JButton("Change for both Screens");
		btnSelectCopyrightFontBoth.addActionListener(safeAction(e -> selectFont(SettingKey.COPYRIGHT_FONT, SettingKey.COPYRIGHT_FONT_2)));
		GridBagConstraints gbc_btnSelectCopyrightFontBoth = new GridBagConstraints();
		gbc_btnSelectCopyrightFontBoth.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectCopyrightFontBoth.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectCopyrightFontBoth.gridx = 5;
		gbc_btnSelectCopyrightFontBoth.gridy = 6;
		panel.add(btnSelectCopyrightFontBoth, gbc_btnSelectCopyrightFontBoth);
		
		JLabel lblTextColor = new JLabel("Text color");
		GridBagConstraints gbcLblTextColor = new GridBagConstraints();
		gbcLblTextColor.anchor = GridBagConstraints.EAST;
		gbcLblTextColor.insets = new Insets(0, 0, 5, 5);
		gbcLblTextColor.gridx = 1;
		gbcLblTextColor.gridy = 7;
		panel.add(lblTextColor, gbcLblTextColor);
		
		btnSelectTextColor1 = new JButton("Screen A");
		btnSelectTextColor1.addActionListener(safeAction(e -> handleSelectTextColor1()));
		GridBagConstraints gbc_btnSelectTextColor1 = new GridBagConstraints();
		gbc_btnSelectTextColor1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTextColor1.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTextColor1.gridx = 3;
		gbc_btnSelectTextColor1.gridy = 7;
		panel.add(btnSelectTextColor1, gbc_btnSelectTextColor1);
		
		btnSelectTextColor2 = new JButton("Screen B");
		btnSelectTextColor2.addActionListener(safeAction(e -> handleSelectTextColor2()));
		GridBagConstraints gbc_btnSelectTextColor2 = new GridBagConstraints();
		gbc_btnSelectTextColor2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTextColor2.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTextColor2.gridx = 4;
		gbc_btnSelectTextColor2.gridy = 7;
		panel.add(btnSelectTextColor2, gbc_btnSelectTextColor2);
		
		btnSelectTextColorBoth = new JButton("Change for both Screens");
		btnSelectTextColorBoth.addActionListener(safeAction(e -> handleSelectTextColorBoth()));
		GridBagConstraints gbc_btnSelectTextColorBoth = new GridBagConstraints();
		gbc_btnSelectTextColorBoth.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectTextColorBoth.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectTextColorBoth.gridx = 5;
		gbc_btnSelectTextColorBoth.gridy = 7;
		panel.add(btnSelectTextColorBoth, gbc_btnSelectTextColorBoth);
		
		JLabel lblBackgroundColor = new JLabel("Background color");
		GridBagConstraints gbcLblBackgroundColor = new GridBagConstraints();
		gbcLblBackgroundColor.anchor = GridBagConstraints.EAST;
		gbcLblBackgroundColor.insets = new Insets(0, 0, 5, 5);
		gbcLblBackgroundColor.gridx = 1;
		gbcLblBackgroundColor.gridy = 8;
		panel.add(lblBackgroundColor, gbcLblBackgroundColor);
		
		btnSelectBackgroundColor1 = new JButton("Screen A");
		btnSelectBackgroundColor1.addActionListener(safeAction(e -> handleSelectBackgroundColor1()));
		GridBagConstraints gbc_btnSelectBackgroundColor1 = new GridBagConstraints();
		gbc_btnSelectBackgroundColor1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectBackgroundColor1.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectBackgroundColor1.gridx = 3;
		gbc_btnSelectBackgroundColor1.gridy = 8;
		panel.add(btnSelectBackgroundColor1, gbc_btnSelectBackgroundColor1);
		
		btnSelectBackgroundColor2 = new JButton("Screen B");
		btnSelectBackgroundColor2.addActionListener(safeAction(e -> handleSelectBackgroundColor2()));
		GridBagConstraints gbc_btnSelectBackgroundColor2 = new GridBagConstraints();
		gbc_btnSelectBackgroundColor2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectBackgroundColor2.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectBackgroundColor2.gridx = 4;
		gbc_btnSelectBackgroundColor2.gridy = 8;
		panel.add(btnSelectBackgroundColor2, gbc_btnSelectBackgroundColor2);
		
		btnSelectBackgroundColorBoth = new JButton("Change for both Screens");
		btnSelectBackgroundColorBoth.addActionListener(safeAction(e -> handleSelectBackgroundColorBoth()));
		GridBagConstraints gbc_btnSelectBackgroundColorBoth = new GridBagConstraints();
		gbc_btnSelectBackgroundColorBoth.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectBackgroundColorBoth.insets = new Insets(0, 0, 5, 5);
		gbc_btnSelectBackgroundColorBoth.gridx = 5;
		gbc_btnSelectBackgroundColorBoth.gridy = 8;
		panel.add(btnSelectBackgroundColorBoth, gbc_btnSelectBackgroundColorBoth);
		
		JLabel lblLogo = new JLabel("Logo");
		GridBagConstraints gbcLblLogo = new GridBagConstraints();
		gbcLblLogo.anchor = GridBagConstraints.EAST;
		gbcLblLogo.insets = new Insets(0, 0, 5, 5);
		gbcLblLogo.gridx = 1;
		gbcLblLogo.gridy = 9;
		panel.add(lblLogo, gbcLblLogo);
		
		btnSelectLogo = new JButton("Select...");
		btnSelectLogo.addActionListener(safeAction(e -> handleSelectLogo()));
		GridBagConstraints gbcBtnSelectLogo = new GridBagConstraints();
		gbcBtnSelectLogo.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectLogo.gridwidth = 3;
		gbcBtnSelectLogo.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectLogo.gridx = 3;
		gbcBtnSelectLogo.gridy = 9;
		panel.add(btnSelectLogo, gbcBtnSelectLogo);
		
		JLabel lblTopMargin = new JLabel("Top margin");
		GridBagConstraints gbcLblTopMargin = new GridBagConstraints();
		gbcLblTopMargin.anchor = GridBagConstraints.EAST;
		gbcLblTopMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblTopMargin.gridx = 1;
		gbcLblTopMargin.gridy = 10;
		panel.add(lblTopMargin, gbcLblTopMargin);
		
		spinnerTopMargin = new JSpinner();
		mustBePositive(spinnerTopMargin, true);
		GridBagConstraints gbcSpinnerTopMargin = new GridBagConstraints();
		gbcSpinnerTopMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerTopMargin.gridwidth = 3;
		gbcSpinnerTopMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerTopMargin.gridx = 3;
		gbcSpinnerTopMargin.gridy = 10;
		panel.add(spinnerTopMargin, gbcSpinnerTopMargin);
		
		JLabel lblLeftMargin = new JLabel("Left margin");
		GridBagConstraints gbcLblLeftMargin = new GridBagConstraints();
		gbcLblLeftMargin.anchor = GridBagConstraints.EAST;
		gbcLblLeftMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblLeftMargin.gridx = 1;
		gbcLblLeftMargin.gridy = 11;
		panel.add(lblLeftMargin, gbcLblLeftMargin);
		
		spinnerLeftMargin = new JSpinner();
		mustBePositive(spinnerLeftMargin, true);
		GridBagConstraints gbcSpinnerLeftMargin = new GridBagConstraints();
		gbcSpinnerLeftMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerLeftMargin.gridwidth = 3;
		gbcSpinnerLeftMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerLeftMargin.gridx = 3;
		gbcSpinnerLeftMargin.gridy = 11;
		panel.add(spinnerLeftMargin, gbcSpinnerLeftMargin);
		
		JLabel lblRightMargin = new JLabel("Right margin");
		GridBagConstraints gbcLblRightMargin = new GridBagConstraints();
		gbcLblRightMargin.anchor = GridBagConstraints.EAST;
		gbcLblRightMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblRightMargin.gridx = 1;
		gbcLblRightMargin.gridy = 12;
		panel.add(lblRightMargin, gbcLblRightMargin);
		
		spinnerRightMargin = new JSpinner();
		mustBePositive(spinnerRightMargin, true);
		GridBagConstraints gbcSpinnerRightMargin = new GridBagConstraints();
		gbcSpinnerRightMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerRightMargin.gridwidth = 3;
		gbcSpinnerRightMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerRightMargin.gridx = 3;
		gbcSpinnerRightMargin.gridy = 12;
		panel.add(spinnerRightMargin, gbcSpinnerRightMargin);
		
		JLabel lblBottomMargin = new JLabel("Bottom margin");
		GridBagConstraints gbcLblBottomMargin = new GridBagConstraints();
		gbcLblBottomMargin.anchor = GridBagConstraints.EAST;
		gbcLblBottomMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblBottomMargin.gridx = 1;
		gbcLblBottomMargin.gridy = 13;
		panel.add(lblBottomMargin, gbcLblBottomMargin);
		
		spinnerBottomMargin = new JSpinner();
		mustBePositive(spinnerBottomMargin, true);
		GridBagConstraints gbcSpinnerBottomMargin = new GridBagConstraints();
		gbcSpinnerBottomMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerBottomMargin.gridwidth = 3;
		gbcSpinnerBottomMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerBottomMargin.gridx = 3;
		gbcSpinnerBottomMargin.gridy = 13;
		panel.add(spinnerBottomMargin, gbcSpinnerBottomMargin);
		
		JLabel lblShowTitle = new JLabel("Show title in presentation");
		lblShowTitle.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbcLblShowTitle = new GridBagConstraints();
		gbcLblShowTitle.anchor = GridBagConstraints.EAST;
		gbcLblShowTitle.insets = new Insets(0, 0, 5, 5);
		gbcLblShowTitle.gridx = 1;
		gbcLblShowTitle.gridy = 14;
		panel.add(lblShowTitle, gbcLblShowTitle);
		
		checkboxShowTitle = new JCheckBox("");
		GridBagConstraints gbcCheckboxShowTitle = new GridBagConstraints();
		gbcCheckboxShowTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcCheckboxShowTitle.gridwidth = 3;
		gbcCheckboxShowTitle.insets = new Insets(0, 0, 5, 5);
		gbcCheckboxShowTitle.gridx = 3;
		gbcCheckboxShowTitle.gridy = 14;
		panel.add(checkboxShowTitle, gbcCheckboxShowTitle);
		
		JLabel lblDistanceBetweenTitle = new JLabel("Distance between title and text");
		GridBagConstraints gbcLblDistanceBetweenTitle = new GridBagConstraints();
		gbcLblDistanceBetweenTitle.anchor = GridBagConstraints.EAST;
		gbcLblDistanceBetweenTitle.insets = new Insets(0, 0, 5, 5);
		gbcLblDistanceBetweenTitle.gridx = 1;
		gbcLblDistanceBetweenTitle.gridy = 15;
		panel.add(lblDistanceBetweenTitle, gbcLblDistanceBetweenTitle);
		
		spinnerDistanceTitleText = new JSpinner();
		mustBePositive(spinnerDistanceTitleText, true);
		GridBagConstraints gbcSpinnerDistanceTitleText = new GridBagConstraints();
		gbcSpinnerDistanceTitleText.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerDistanceTitleText.gridwidth = 3;
		gbcSpinnerDistanceTitleText.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerDistanceTitleText.gridx = 3;
		gbcSpinnerDistanceTitleText.gridy = 15;
		panel.add(spinnerDistanceTitleText, gbcSpinnerDistanceTitleText);
		
		JLabel lblDistanceBetweenText = new JLabel("Distance between text and copyright");
		GridBagConstraints gbcLblDistanceBetweenText = new GridBagConstraints();
		gbcLblDistanceBetweenText.anchor = GridBagConstraints.EAST;
		gbcLblDistanceBetweenText.insets = new Insets(0, 0, 5, 5);
		gbcLblDistanceBetweenText.gridx = 1;
		gbcLblDistanceBetweenText.gridy = 16;
		panel.add(lblDistanceBetweenText, gbcLblDistanceBetweenText);
		
		spinnerDistanceTextCopyright = new JSpinner();
		mustBePositive(spinnerDistanceTextCopyright, true);
		GridBagConstraints gbcSpinnerDistanceTextCopyright = new GridBagConstraints();
		gbcSpinnerDistanceTextCopyright.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerDistanceTextCopyright.gridwidth = 3;
		gbcSpinnerDistanceTextCopyright.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerDistanceTextCopyright.gridx = 3;
		gbcSpinnerDistanceTextCopyright.gridy = 16;
		panel.add(spinnerDistanceTextCopyright, gbcSpinnerDistanceTextCopyright);
		
		JLabel lblSongListFiltering = new JLabel("Song list filter");
		GridBagConstraints gbcLblSongListFiltering = new GridBagConstraints();
		gbcLblSongListFiltering.anchor = GridBagConstraints.EAST;
		gbcLblSongListFiltering.insets = new Insets(0, 0, 5, 5);
		gbcLblSongListFiltering.gridx = 1;
		gbcLblSongListFiltering.gridy = 17;
		panel.add(lblSongListFiltering, gbcLblSongListFiltering);
		
		comboSongListFiltering = new JComboBox<>();
		GridBagConstraints gbcComboSongListFiltering = new GridBagConstraints();
		gbcComboSongListFiltering.fill = GridBagConstraints.HORIZONTAL;
		gbcComboSongListFiltering.gridwidth = 3;
		gbcComboSongListFiltering.insets = new Insets(0, 0, 5, 5);
		gbcComboSongListFiltering.gridx = 3;
		gbcComboSongListFiltering.gridy = 17;
		panel.add(comboSongListFiltering, gbcComboSongListFiltering);
		
		JLabel lblPresentationScreen1Display = new JLabel("Physical Display for Screen A");
		GridBagConstraints gbcLblPresentationScreen1Display = new GridBagConstraints();
		gbcLblPresentationScreen1Display.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen1Display.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen1Display.gridx = 1;
		gbcLblPresentationScreen1Display.gridy = 18;
		panel.add(lblPresentationScreen1Display, gbcLblPresentationScreen1Display);
		
		comboPresentationScreen1Display = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen1Display = new GridBagConstraints();
		gbcComboPresentationScreen1Display.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen1Display.gridwidth = 3;
		gbcComboPresentationScreen1Display.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen1Display.gridx = 3;
		gbcComboPresentationScreen1Display.gridy = 18;
		panel.add(comboPresentationScreen1Display, gbcComboPresentationScreen1Display);
		
		JLabel lblPresentationScreen1Contents = new JLabel("Contents of Screen A");
		GridBagConstraints gbcLblPresentationScreen1Contents = new GridBagConstraints();
		gbcLblPresentationScreen1Contents.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen1Contents.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen1Contents.gridx = 1;
		gbcLblPresentationScreen1Contents.gridy = 19;
		panel.add(lblPresentationScreen1Contents, gbcLblPresentationScreen1Contents);
		
		comboPresentationScreen1Contents = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen1Contents = new GridBagConstraints();
		gbcComboPresentationScreen1Contents.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen1Contents.gridwidth = 3;
		gbcComboPresentationScreen1Contents.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen1Contents.gridx = 3;
		gbcComboPresentationScreen1Contents.gridy = 19;
		panel.add(comboPresentationScreen1Contents, gbcComboPresentationScreen1Contents);
		
		JLabel lblPresentationScreen2Display = new JLabel("Physical Display for Screen B");
		GridBagConstraints gbcLblPresentationScreen2Display = new GridBagConstraints();
		gbcLblPresentationScreen2Display.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen2Display.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen2Display.gridx = 1;
		gbcLblPresentationScreen2Display.gridy = 20;
		panel.add(lblPresentationScreen2Display, gbcLblPresentationScreen2Display);
		
		comboPresentationScreen2Display = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen2Display = new GridBagConstraints();
		gbcComboPresentationScreen2Display.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen2Display.gridwidth = 3;
		gbcComboPresentationScreen2Display.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen2Display.gridx = 3;
		gbcComboPresentationScreen2Display.gridy = 20;
		panel.add(comboPresentationScreen2Display, gbcComboPresentationScreen2Display);
		
		JLabel lblPresentationScreen2Contents = new JLabel("Contents of Screen B");
		GridBagConstraints gbcLblPresentationScreen2Contents = new GridBagConstraints();
		gbcLblPresentationScreen2Contents.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen2Contents.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen2Contents.gridx = 1;
		gbcLblPresentationScreen2Contents.gridy = 21;
		panel.add(lblPresentationScreen2Contents, gbcLblPresentationScreen2Contents);
		
		btnSlideShowDirectory = new JButton("Select...");
		btnSlideShowDirectory.addActionListener(safeAction(e -> handleSelectSlideShowDirectory()));
		
		comboPresentationScreen2Contents = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen2Contents = new GridBagConstraints();
		gbcComboPresentationScreen2Contents.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen2Contents.gridwidth = 3;
		gbcComboPresentationScreen2Contents.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen2Contents.gridx = 3;
		gbcComboPresentationScreen2Contents.gridy = 21;
		panel.add(comboPresentationScreen2Contents, gbcComboPresentationScreen2Contents);
		
		lblMinimizeScrolling = new JLabel("Minimize Scrolling");
		GridBagConstraints gbc_lblMinimizeScrolling = new GridBagConstraints();
		gbc_lblMinimizeScrolling.anchor = GridBagConstraints.EAST;
		gbc_lblMinimizeScrolling.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinimizeScrolling.gridx = 1;
		gbc_lblMinimizeScrolling.gridy = 22;
		panel.add(lblMinimizeScrolling, gbc_lblMinimizeScrolling);
		
		checkboxMinimizeScrolling1 = new JCheckBox("Screen A");
		GridBagConstraints gbc_checkboxMinimizeScrolling1 = new GridBagConstraints();
		gbc_checkboxMinimizeScrolling1.anchor = GridBagConstraints.WEST;
		gbc_checkboxMinimizeScrolling1.insets = new Insets(0, 0, 5, 5);
		gbc_checkboxMinimizeScrolling1.gridx = 3;
		gbc_checkboxMinimizeScrolling1.gridy = 22;
		panel.add(checkboxMinimizeScrolling1, gbc_checkboxMinimizeScrolling1);
		
		checkboxMinimizeScrolling2 = new JCheckBox("Screen B");
		GridBagConstraints gbc_checkboxMinimizeScrolling2 = new GridBagConstraints();
		gbc_checkboxMinimizeScrolling2.anchor = GridBagConstraints.WEST;
		gbc_checkboxMinimizeScrolling2.insets = new Insets(0, 0, 5, 5);
		gbc_checkboxMinimizeScrolling2.gridx = 4;
		gbc_checkboxMinimizeScrolling2.gridy = 22;
		panel.add(checkboxMinimizeScrolling2, gbc_checkboxMinimizeScrolling2);
		
		JLabel lblSecondsToCount = new JLabel("Seconds to count a song as displayed after");
		GridBagConstraints gbcLblSecondsToCount = new GridBagConstraints();
		gbcLblSecondsToCount.anchor = GridBagConstraints.EAST;
		gbcLblSecondsToCount.insets = new Insets(0, 0, 5, 5);
		gbcLblSecondsToCount.gridx = 1;
		gbcLblSecondsToCount.gridy = 23;
		panel.add(lblSecondsToCount, gbcLblSecondsToCount);
		
		spinnerCountAsDisplayedAfter = new JSpinner();
		mustBePositive(spinnerCountAsDisplayedAfter, false);
		GridBagConstraints gbcSpinnerCountAsDisplayedAfter = new GridBagConstraints();
		gbcSpinnerCountAsDisplayedAfter.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerCountAsDisplayedAfter.gridwidth = 3;
		gbcSpinnerCountAsDisplayedAfter.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerCountAsDisplayedAfter.gridx = 3;
		gbcSpinnerCountAsDisplayedAfter.gridy = 23;
		panel.add(spinnerCountAsDisplayedAfter, gbcSpinnerCountAsDisplayedAfter);
		
		lblSlideShowDirectory = new JLabel("Directory for slide show");
		GridBagConstraints gbc_lblSlideShowDirectory = new GridBagConstraints();
		gbc_lblSlideShowDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblSlideShowDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblSlideShowDirectory.gridx = 1;
		gbc_lblSlideShowDirectory.gridy = 24;
		panel.add(lblSlideShowDirectory, gbc_lblSlideShowDirectory);
		GridBagConstraints gbc_btnSlideShowDirectory = new GridBagConstraints();
		gbc_btnSlideShowDirectory.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSlideShowDirectory.gridwidth = 3;
		gbc_btnSlideShowDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_btnSlideShowDirectory.gridx = 3;
		gbc_btnSlideShowDirectory.gridy = 24;
		panel.add(btnSlideShowDirectory, gbc_btnSlideShowDirectory);
		
		lblSlideShowSeconds = new JLabel("Seconds between slide show changes");
		GridBagConstraints gbc_lblSlideShowSeconds = new GridBagConstraints();
		gbc_lblSlideShowSeconds.anchor = GridBagConstraints.EAST;
		gbc_lblSlideShowSeconds.insets = new Insets(0, 0, 5, 5);
		gbc_lblSlideShowSeconds.gridx = 1;
		gbc_lblSlideShowSeconds.gridy = 25;
		panel.add(lblSlideShowSeconds, gbc_lblSlideShowSeconds);
		
		spinnerSlideShowSeconds = new JSpinner();
		mustBePositive(spinnerSlideShowSeconds, false);
		GridBagConstraints gbc_spinnerSlideShowSeconds = new GridBagConstraints();
		gbc_spinnerSlideShowSeconds.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerSlideShowSeconds.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerSlideShowSeconds.gridx = 3;
		gbc_spinnerSlideShowSeconds.gridy = 25;
		panel.add(spinnerSlideShowSeconds, gbc_spinnerSlideShowSeconds);
		
		lblFadeTime = new JLabel("Fade time in milliseconds");
		GridBagConstraints gbc_lblFadeTime = new GridBagConstraints();
		gbc_lblFadeTime.anchor = GridBagConstraints.EAST;
		gbc_lblFadeTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblFadeTime.gridx = 1;
		gbc_lblFadeTime.gridy = 26;
		panel.add(lblFadeTime, gbc_lblFadeTime);
		
		spinnerFadeTime = new JSpinner();
		mustBePositive(spinnerFadeTime, false);
		GridBagConstraints gbc_spinnerFadeTime = new GridBagConstraints();
		gbc_spinnerFadeTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerFadeTime.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerFadeTime.gridx = 3;
		gbc_spinnerFadeTime.gridy = 26;
		panel.add(spinnerFadeTime, gbc_spinnerFadeTime);
		
		lblRemoteEnabled = new JLabel("Remote enabled");
		GridBagConstraints gbc_lblRemoteEnabled = new GridBagConstraints();
		gbc_lblRemoteEnabled.anchor = GridBagConstraints.EAST;
		gbc_lblRemoteEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemoteEnabled.gridx = 1;
		gbc_lblRemoteEnabled.gridy = 27;
		panel.add(lblRemoteEnabled, gbc_lblRemoteEnabled);
		
		checkboxRemoteEnabled = new JCheckBox();
		GridBagConstraints gbc_checkboxRemoteEnabled = new GridBagConstraints();
		gbc_checkboxRemoteEnabled.fill = GridBagConstraints.HORIZONTAL;
		gbc_checkboxRemoteEnabled.insets = new Insets(0, 0, 5, 5);
		gbc_checkboxRemoteEnabled.gridx = 3;
		gbc_checkboxRemoteEnabled.gridy = 27;
		panel.add(checkboxRemoteEnabled, gbc_checkboxRemoteEnabled);
		
		lblRemoteServer = new JLabel("Remote server");
		GridBagConstraints gbc_lblRemoteServer = new GridBagConstraints();
		gbc_lblRemoteServer.anchor = GridBagConstraints.EAST;
		gbc_lblRemoteServer.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemoteServer.gridx = 1;
		gbc_lblRemoteServer.gridy = 28;
		panel.add(lblRemoteServer, gbc_lblRemoteServer);
		
		textFieldRemoteServer = new JTextField();
		GridBagConstraints gbc_textFieldRemoteServer = new GridBagConstraints();
		gbc_textFieldRemoteServer.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldRemoteServer.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldRemoteServer.gridx = 3;
		gbc_textFieldRemoteServer.gridy = 28;
		panel.add(textFieldRemoteServer, gbc_textFieldRemoteServer);
		
		lblRemoteUsername = new JLabel("Remote username");
		GridBagConstraints gbc_lblRemoteUsername = new GridBagConstraints();
		gbc_lblRemoteUsername.anchor = GridBagConstraints.EAST;
		gbc_lblRemoteUsername.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemoteUsername.gridx = 1;
		gbc_lblRemoteUsername.gridy = 29;
		panel.add(lblRemoteUsername, gbc_lblRemoteUsername);
		
		textFieldRemoteUsername = new JTextField();
		GridBagConstraints gbc_textFieldRemoteUsername = new GridBagConstraints();
		gbc_textFieldRemoteUsername.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldRemoteUsername.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldRemoteUsername.gridx = 3;
		gbc_textFieldRemoteUsername.gridy = 29;
		panel.add(textFieldRemoteUsername, gbc_textFieldRemoteUsername);
		
		lblRemotePassword = new JLabel("Remote password");
		GridBagConstraints gbc_lblRemotePassword = new GridBagConstraints();
		gbc_lblRemotePassword.anchor = GridBagConstraints.EAST;
		gbc_lblRemotePassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemotePassword.gridx = 1;
		gbc_lblRemotePassword.gridy = 30;
		panel.add(lblRemotePassword, gbc_lblRemotePassword);
		
		textFieldRemotePassword = new JTextField();
		GridBagConstraints gbc_textFieldRemotePassword = new GridBagConstraints();
		gbc_textFieldRemotePassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldRemotePassword.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldRemotePassword.gridx = 3;
		gbc_textFieldRemotePassword.gridy = 30;
		panel.add(textFieldRemotePassword, gbc_textFieldRemotePassword);
		
		lblRemotePrefix = new JLabel("Remote prefix");
		GridBagConstraints gbc_lblRemotePrefix = new GridBagConstraints();
		gbc_lblRemotePrefix.anchor = GridBagConstraints.EAST;
		gbc_lblRemotePrefix.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemotePrefix.gridx = 1;
		gbc_lblRemotePrefix.gridy = 31;
		panel.add(lblRemotePrefix, gbc_lblRemotePrefix);
		
		textFieldRemotePrefix = new JTextField();
		GridBagConstraints gbc_textFieldRemotePrefix = new GridBagConstraints();
		gbc_textFieldRemotePrefix.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldRemotePrefix.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldRemotePrefix.gridx = 3;
		gbc_textFieldRemotePrefix.gridy = 31;
		panel.add(textFieldRemotePrefix, gbc_textFieldRemotePrefix);
		
		lblRemoteRoom = new JLabel("Remote room");
		GridBagConstraints gbc_lblRemoteRoom = new GridBagConstraints();
		gbc_lblRemoteRoom.anchor = GridBagConstraints.EAST;
		gbc_lblRemoteRoom.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemoteRoom.gridx = 1;
		gbc_lblRemoteRoom.gridy = 32;
		panel.add(lblRemoteRoom, gbc_lblRemoteRoom);
		
		textFieldRemoteRoom = new JTextField();
		GridBagConstraints gbc_textFieldRemoteRoom = new GridBagConstraints();
		gbc_textFieldRemoteRoom.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldRemoteRoom.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldRemoteRoom.gridx = 3;
		gbc_textFieldRemoteRoom.gridy = 32;
		panel.add(textFieldRemoteRoom, gbc_textFieldRemoteRoom);
		
		glassPane = (Container) getGlassPane();
		glassPane.setVisible(true);
		glassPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.00001;
		gbc.weighty = 0.00001;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		buttonPanel.setOpaque(false);
		saveButton = new JButton("Save");
		saveButton.setFocusable(false);
		saveButton.addActionListener(safeAction(e -> {
			handleSave();
			displayNotification("SAVED", 3500);
		}));
		saveButton.setFont(new Font(null, Font.PLAIN, 10));
		lblStatus = new JLabel();
		buttonPanel.add(lblStatus);
		buttonPanel.add(saveButton);
		glassPane.add(buttonPanel, gbc);
		setRemoteStatus(RemoteStatus.OFF);
		
		afterConstruction();
	}
	
	private void mustBePositive(JSpinner spinner, boolean zeroAllowed) {
		spinner.getModel().addChangeListener(e -> {
			if (zeroAllowed && ((Number) spinner.getValue()).intValue() < 0) {
				spinner.setValue(0);
			} else if (!zeroAllowed && ((Number) spinner.getValue()).intValue() < 1) {
				spinner.setValue(1);
			}
		});
	}
	
	public void setRemoteStatus(RemoteStatus status) {
		String path = null;
		String tooltip = null;
		switch (status) {
			case OFF -> {
				path = "/org/zephyrsoft/sdb2/remote-off.png";
				tooltip = "Remote disabled.";
			}
			case CONNECTING -> {
				path = "/org/zephyrsoft/sdb2/remote-orange.png";
				tooltip = "Remote connecting...";
			}
			case CONNECTED -> {
				path = "/org/zephyrsoft/sdb2/remote-green.png";
				tooltip = "Remote connected. Type Strg+R to reconnect.";
			}
			case DISCONNECTING -> {
				path = "/org/zephyrsoft/sdb2/remote-orange.png";
				tooltip = "Remote disconnecting...";
			}
			case DB_DISCONNECTED -> {
				path = "/org/zephyrsoft/sdb2/remote-orange.png";
				tooltip = "Remote db offline! Please notify your admin!";
			}
			case FAILURE -> {
				path = "/org/zephyrsoft/sdb2/remote-red.png";
				tooltip = "Remote connection failure! Type Strg+R to reconnect.";
			}
		}
		lblStatus.setIcon(ResourceTools.getIcon(getClass(), path));
		lblStatus.setToolTipText(tooltip);
	}
	
	private void updateFontButtons() {
		int size = btnSelectTitleFont1.getFont().getSize();
		updateFontButton(SCREEN_A, btnSelectTitleFont1, SCREEN_A.getTitleFont(settingsModel), size);
		updateFontButton(SCREEN_B, btnSelectTitleFont2, SCREEN_B.getTitleFont(settingsModel), size);
		updateFontButton(SCREEN_A, btnSelectLyricsFont1, SCREEN_A.getLyricsFont(settingsModel), size);
		updateFontButton(SCREEN_B, btnSelectLyricsFont2, SCREEN_B.getLyricsFont(settingsModel), size);
		updateFontButton(SCREEN_A, btnSelectChordSequenceFont1, SCREEN_A.getChordSequenceFont(settingsModel), size);
		updateFontButton(SCREEN_B, btnSelectChordSequenceFont2, SCREEN_B.getChordSequenceFont(settingsModel), size);
		updateFontButton(SCREEN_A, btnSelectTranslationFont1, SCREEN_A.getTranslationFont(settingsModel), size);
		updateFontButton(SCREEN_B, btnSelectTranslationFont2, SCREEN_B.getTranslationFont(settingsModel), size);
		updateFontButton(SCREEN_A, btnSelectCopyrightFont1, SCREEN_A.getCopyrightFont(settingsModel), size);
		updateFontButton(SCREEN_B, btnSelectCopyrightFont2, SCREEN_B.getCopyrightFont(settingsModel), size);
	}
	
	private void updateFontButton(VirtualScreen screen, JButton fontButton, Font font, int size) {
		fontButton.setFont(font.deriveFont((float) size));
		fontButton.setText("Screen " + screen.getName() + " (font size: " + font.getSize() + ")");
	}
	
	private void handleExport(Collection<Song> songs) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("choose target file for PDF export");
		CustomFileFilter filter = new CustomFileFilter("PDF", ".pdf");
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);
		chooser.setApproveButtonText("Export");
		chooser.setSelectedFile(new File("songs.pdf"));
		int result = chooser.showOpenDialog(MainWindow.this);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			File target = chooser.getSelectedFile();
			// export
			ExportFormat format = new ExportFormat(chckbxWithTranslation.isSelected(),
				chckbxWithChords.isSelected(), chckbxOnlyExportSongs.isSelected());
			try {
				byte[] exported = exportService.export(format, songs);
				Files.write(exported, target);
			} catch (Exception e) {
				handleError(e);
			}
		}
	}
	
	private ActionListener safeAction(Consumer<ActionEvent> listener) {
		return e -> {
			try {
				listener.accept(e);
			} catch (Throwable ex) {
				handleError(ex);
			}
		};
	}
	
	private void displayNotification(String textToDisplay, long millis) {
		NotificationLabel notificationLabel = new NotificationLabel(textToDisplay, millis, glassPane);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(20, 20, 20, 20);
		gbc.anchor = GridBagConstraints.NORTHEAST;
		glassPane.add(notificationLabel, gbc, 0);
		notificationLabel.setVisible(true);
		glassPane.revalidate();
	}
	
	public static List<Image> getIconsFromResources(Class<?> classToUse) {
		return Arrays
			.asList(ResourceTools.getImage(classToUse, "/org/zephyrsoft/sdb2/icon-128.png"), ResourceTools.getImage(
				classToUse, "/org/zephyrsoft/sdb2/icon-64.png"), ResourceTools.getImage(classToUse,
					"/org/zephyrsoft/sdb2/icon-32.png"), ResourceTools.getImage(classToUse,
						"/org/zephyrsoft/sdb2/icon-16.png"));
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
	
	public JComboBox<String> getComboBoxLanguage() {
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
	
	public JTextField getTextFieldTempo() {
		return textFieldTempo;
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
	
	public SongsModel getPresentModel() {
		return presentModel;
	}
	
}
