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
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
import javax.swing.JOptionPane;
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
import org.zephyrsoft.sdb2.gui.renderer.LanguageCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.ScreenContentsCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.ScreenDisplayCellRenderer;
import org.zephyrsoft.sdb2.gui.renderer.SongCellRenderer;
import org.zephyrsoft.sdb2.importer.ImportFromEasiSlides;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.LanguageEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.SelectableScreen;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.ScreenHelper;
import org.zephyrsoft.sdb2.presenter.UIScroller;
import org.zephyrsoft.sdb2.service.FieldName;
import org.zephyrsoft.sdb2.service.IndexType;
import org.zephyrsoft.sdb2.service.IndexerService;
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

import com.l2fprod.common.swing.JFontChooser;

/**
 * Main window of the application.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainWindow extends JFrame implements UIScroller {
	
	private static final String PROBLEM_WHILE_SAVING = "There was a problem while saving the data.\n\nPlease examine the log file at:\n";
	
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
	private JComboBox<LanguageEnum> comboBoxLanguage;
	private JTextField textFieldTonality;
	private JTextField textFieldComposer;
	private JTextField textFieldAuthorText;
	private JTextField textFieldAuthorTranslation;
	private JTextField textFieldPublisher;
	private JTextField textFieldAdditionalCopyrightNotes;
	private JTextField textFieldSongNotes;
	private JEditorPane editorChordSequence;
	
	private KeyboardShortcutManager keyboardShortcutManager;
	private final MainController controller;
	private IndexerService<Song> indexer;
	
	private SettingsModel settingsModel;
	
	private FixedWidthJList<Song> songsList;
	private SongsModel songsModel;
	private TransparentFilterableListModel<Song> songsListModel;
	private Collection<Song> songsListFiltered;
	private Song songsListSelected;
	
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
	private List<PartButtonGroup> listSectionButtons = new LinkedList<>();
	private GridBagConstraints panelSectionButtonsHints;
	private GridBagConstraints panelSectionButtonsLastRowHints;
	private JButton btnJumpToSelected;
	private JButton btnShowLogo;
	private JButton btnShowBlankScreen;
	private JButton btnPresentSelectedSong;
	private JScrollPane scrollPaneSectionButtons;
	private JButton btnJumpToPresented;
	private JButton btnExportLyricsOnlyPdfSelected;
	private JButton btnExportCompletePdfSelected;
	private JButton btnExportStatisticsSelected;
	private JButton btnExportLyricsOnlyPdfAll;
	private JButton btnExportCompletePdfAll;
	private JButton btnExportStatisticsAll;
	private JButton btnImportFromSdb1;
	private JButton btnImportFromEasiSlides;
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
	private JComboBox<SelectableScreen> comboPresentationScreen1Display;
	private JComboBox<ScreenContentsEnum> comboPresentationScreen1Contents;
	private JComboBox<SelectableScreen> comboPresentationScreen2Display;
	private JComboBox<ScreenContentsEnum> comboPresentationScreen2Contents;
	private JSpinner spinnerCountAsDisplayedAfter;
	private JButton btnSlideShowDirectory;
	private JSpinner spinnerSlideShowSeconds;
	
	private JButton saveButton;
	
	private SongCellRenderer songCellRenderer;
	private JLabel lblSlideShowDirectory;
	private JLabel lblSlideShowSeconds;
	private JButton btnSlideshow;
	
	@Override
	public List<PartButtonGroup> getUIParts() {
		return listSectionButtons;
	}
	
	private void afterConstruction() {
		MainController.initAnimationTimer();
		
		// read program version
		lblProgramVersion.setText(VersionTools.getCurrent());
		
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
	}
	
	public void startup() {
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
				final JLabel updateLabel = new JLabel("new version available since "
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
	
	public void setModels(SongsModel songs, SettingsModel settings) {
		this.songsModel = songs;
		this.settingsModel = settings;
		songsListModel = songs.getFilterableListModel();
		songsList.setModel(songsListModel);
		
		songsModel.addSongsModelListener(() -> indexAllSongs());
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
		controller.detectScreens();
		comboPresentationScreen1Display.setModel(new TransparentComboBoxModel<>(controller.getScreens()));
		comboPresentationScreen2Display.setModel(new TransparentComboBoxModel<>(controller.getScreens()));
		
		// load values for instantly displayed settings
		Boolean showTitle = settingsModel.get(SettingKey.SHOW_TITLE, Boolean.class);
		checkboxShowTitle.setSelected(showTitle == null ? false : showTitle.booleanValue());
		setSpinnerValue(spinnerTopMargin, settingsModel.get(SettingKey.TOP_MARGIN, Integer.class));
		setSpinnerValue(spinnerLeftMargin, settingsModel.get(SettingKey.LEFT_MARGIN, Integer.class));
		setSpinnerValue(spinnerRightMargin, settingsModel.get(SettingKey.RIGHT_MARGIN, Integer.class));
		setSpinnerValue(spinnerBottomMargin, settingsModel.get(SettingKey.BOTTOM_MARGIN, Integer.class));
		setSpinnerValue(spinnerDistanceTitleText, settingsModel.get(SettingKey.DISTANCE_TITLE_TEXT, Integer.class));
		setSpinnerValue(spinnerDistanceTextCopyright, settingsModel.get(SettingKey.DISTANCE_TEXT_COPYRIGHT,
			Integer.class));
		comboSongListFiltering.setSelectedItem(settingsModel.get(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.class));
		comboPresentationScreen1Display.setSelectedItem(ScreenHelper.getScreen(controller.getScreens(),
			settingsModel.get(SettingKey.SCREEN_1_DISPLAY, Integer.class)));
		comboPresentationScreen1Contents.setSelectedItem(settingsModel.get(SettingKey.SCREEN_1_CONTENTS, ScreenContentsEnum.class));
		comboPresentationScreen2Display.setSelectedItem(ScreenHelper.getScreen(controller.getScreens(),
			settingsModel.get(SettingKey.SCREEN_2_DISPLAY, Integer.class)));
		comboPresentationScreen2Contents.setSelectedItem(settingsModel.get(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.class));
		setSpinnerValue(spinnerCountAsDisplayedAfter, settingsModel.get(SettingKey.SECONDS_UNTIL_COUNTED, Integer.class));
		setSpinnerValue(spinnerSlideShowSeconds, settingsModel.get(SettingKey.SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE, Integer.class));
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
				spinnerDistanceTitleText, spinnerDistanceTextCopyright, spinnerCountAsDisplayedAfter, spinnerSlideShowSeconds);
			// disable controls
			// copy changed settings to the model
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
				: ((SelectableScreen) comboPresentationScreen1Display.getSelectedItem()).getIndex();
			settingsModel.put(SettingKey.SCREEN_1_DISPLAY, screenOneIndex);
			settingsModel.put(SettingKey.SCREEN_1_CONTENTS, comboPresentationScreen1Contents.getSelectedItem());
			Integer screenTwoIndex = comboPresentationScreen2Display.getSelectedItem() == null
				? null
				: ((SelectableScreen) comboPresentationScreen2Display.getSelectedItem()).getIndex();
			settingsModel.put(SettingKey.SCREEN_2_DISPLAY, screenTwoIndex);
			settingsModel.put(SettingKey.SCREEN_2_CONTENTS, comboPresentationScreen2Contents.getSelectedItem());
			settingsModel.put(SettingKey.SECONDS_UNTIL_COUNTED, spinnerCountAsDisplayedAfter.getValue());
			settingsModel.put(SettingKey.SLIDE_SHOW_SECONDS_UNTIL_NEXT_PICTURE, spinnerSlideShowSeconds.getValue());
			// copying is not necessary for fonts, colors, the logo file and the slide show directory
			// because those settings are only stored directly in the model
			
			// apply settings
			// TODO
		}
		setSettingsEnabled(false);
	}
	
	private void indexAllSongs() {
		indexer.index(IndexType.ALL_SONGS, songsModel.getSongs());
	}
	
	/**
	 * Let the user select a font and save it into the {@link SettingsModel}.
	 * 
	 * @param target
	 *            the target setting for the newly selected font
	 * @return {@code true} if the font was changed, {@code false} else
	 */
	private boolean selectFont(SettingKey target) {
		JFontChooser fontChooser = new JFontChooser();
		fontChooser.setSelectedFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 56));
		Font font = settingsModel.get(target, Font.class);
		if (font != null) {
			fontChooser.setSelectedFont(font);
		}
		Font selectedFont = fontChooser.showFontDialog(this, "Choose Font");
		if (selectedFont != null) {
			settingsModel.put(target, selectedFont);
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
		Color color = settingsModel.get(target, Color.class);
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
		setEnabledIfNotNull(btnSlideShowDirectory, enabled);
		setEnabledIfNotNull(spinnerSlideShowSeconds, enabled);
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
			saveSongWithoutChangingGUI();
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
	
	private void saveSongWithoutChangingGUI() {
		if (songsListSelected != null) {
			saveSongData(songsListSelected);
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
		FieldName[] fieldsToSearch = settingsModel.get(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.class).getFields();
		songsListFiltered = indexer.search(IndexType.ALL_SONGS, textFieldFilter.getText(), fieldsToSearch);
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
		saveSongWithoutChangingGUI();
	}
	
	/**
	 * Stores all data contained in the GUI elements.
	 * 
	 * @param song
	 *            the songsModel object to which the data should be written
	 */
	private synchronized void saveSongData(Song song) {
		LOG.debug("saveSongData: {}", song.getTitle());
		
		boolean dataChanged = false;
		
		if (!StringTools.equalsWithNullAsEmpty(song.getLyrics(), editorLyrics.getText())) {
			song.setLyrics(editorLyrics.getText());
			LOG.debug("changed song attribute: Lyrics");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getTitle(), textFieldTitle.getText())) {
			song.setTitle(textFieldTitle.getText());
			LOG.debug("changed song attribute: Title");
			dataChanged = true;
		}
		if (song.getLanguage() != (LanguageEnum) comboBoxLanguage.getSelectedItem()) {
			song.setLanguage((LanguageEnum) comboBoxLanguage.getSelectedItem());
			LOG.debug("changed song attribute: Language");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getTonality(), textFieldTonality.getText())) {
			song.setTonality(textFieldTonality.getText());
			LOG.debug("changed song attribute: Tonality");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getComposer(), textFieldComposer.getText())) {
			song.setComposer(textFieldComposer.getText());
			LOG.debug("changed song attribute: Composer");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getAuthorText(), textFieldAuthorText.getText())) {
			song.setAuthorText(textFieldAuthorText.getText());
			LOG.debug("changed song attribute: AuthorText");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getAuthorTranslation(), textFieldAuthorTranslation.getText())) {
			song.setAuthorTranslation(textFieldAuthorTranslation.getText());
			LOG.debug("changed song attribute: AuthorTranslation");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getPublisher(), textFieldPublisher.getText())) {
			song.setPublisher(textFieldPublisher.getText());
			LOG.debug("changed song attribute: Publisher");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getAdditionalCopyrightNotes(), textFieldAdditionalCopyrightNotes
			.getText())) {
			song.setAdditionalCopyrightNotes(textFieldAdditionalCopyrightNotes.getText());
			LOG.debug("changed song attribute: AdditionalCopyrightNotes");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getSongNotes(), textFieldSongNotes.getText())) {
			song.setSongNotes(textFieldSongNotes.getText());
			LOG.debug("changed song attribute: SongNotes");
			dataChanged = true;
		}
		if (!StringTools.equalsWithNullAsEmpty(song.getChordSequence(), editorChordSequence.getText())) {
			song.setChordSequence(editorChordSequence.getText());
			LOG.debug("changed song attribute: ChordSequence");
			dataChanged = true;
		}
		
		if (dataChanged) {
			// put the songs in the right order again
			songsModel.sortAndUpdateView();
		}
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
	private synchronized void loadSongData(final Song song) {
		LOG.debug("loadSongData: {}", song.getTitle());
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
	}
	
	private static void setTextAndRewind(JTextComponent textComponent, String textToSet) {
		textComponent.setText(textToSet);
		textComponent.setCaretPosition(0);
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
			ErrorDialog.openDialog(this, PROBLEM_WHILE_SAVING + FileAndDirectoryLocations.getLogDir());
		}
	}
	
	protected void handleSave() {
		saveSongWithoutChangingGUI();
		handleSettingsSaveAndLock();
		controller.saveAll();
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
		controller.stopSlideShow();
		if (success) {
			clearSectionButtons();
			List<AddressablePart> parts = controller.getParts();
			int partIndex = 0;
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
		boolean success;
		try {
			success = controller.present(new Presentable(null, controller.loadLogo()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
	protected void handleImportFromEasiSlides() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("choose file to import");
		CustomFileFilter filter = new CustomFileFilter("EasiSlides 4.0 export files", ".xml");
		chooser.addChoosableFileFilter(filter);
		int iValue = chooser.showOpenDialog(this);
		
		if (iValue == JFileChooser.APPROVE_OPTION) {
			List<Song> imported = null;
			ImportFromEasiSlides importer = new ImportFromEasiSlides();
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
			saveSongWithoutChangingGUI();
			boolean success = controller.saveAll();
			if (!success) {
				ErrorDialog.openDialog(MainWindow.this, PROBLEM_WHILE_SAVING
					+ FileAndDirectoryLocations.getLogDir());
			}
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
		IndexerService<Song> indexer) {
		controller = mainController;
		this.keyboardShortcutManager = keyboardShortcutManager;
		this.indexer = indexer;
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
		btnClearFilter.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/clear.gif"));
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
		btnNewSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/newHighlighter.gif"));
		GridBagConstraints gbcBtnNewSong = new GridBagConstraints();
		gbcBtnNewSong.fill = GridBagConstraints.VERTICAL;
		gbcBtnNewSong.anchor = GridBagConstraints.WEST;
		gbcBtnNewSong.insets = new Insets(0, 0, 5, 5);
		gbcBtnNewSong.gridx = 0;
		gbcBtnNewSong.gridy = 0;
		panelSongListButtons.add(btnNewSong, gbcBtnNewSong);
		
		btnDeleteSong = new JButton("Delete");
		btnDeleteSong.addActionListener(safeAction(e -> handleSongDelete()));
		btnDeleteSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/deleteHighlighter.gif"));
		GridBagConstraints gbcBtnDeleteSong = new GridBagConstraints();
		gbcBtnDeleteSong.fill = GridBagConstraints.VERTICAL;
		gbcBtnDeleteSong.anchor = GridBagConstraints.WEST;
		gbcBtnDeleteSong.insets = new Insets(0, 0, 5, 5);
		gbcBtnDeleteSong.gridx = 1;
		gbcBtnDeleteSong.gridy = 0;
		panelSongListButtons.add(btnDeleteSong, gbcBtnDeleteSong);
		
		btnSelectSong = new JButton("Select");
		btnSelectSong.addActionListener(safeAction(e -> handleSongSelect()));
		btnSelectSong.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/month-up.png"));
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
		GridBagConstraints gbcTextFieldTitle = new GridBagConstraints();
		gbcTextFieldTitle.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldTitle.gridx = 0;
		gbcTextFieldTitle.gridy = 3;
		panelEdit.add(textFieldTitle, gbcTextFieldTitle);
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
		GridBagConstraints gbcTextFieldComposer = new GridBagConstraints();
		gbcTextFieldComposer.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldComposer.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldComposer.gridx = 1;
		gbcTextFieldComposer.gridy = 3;
		panelEdit.add(textFieldComposer, gbcTextFieldComposer);
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
		GridBagConstraints gbcComboBoxLanguage = new GridBagConstraints();
		gbcComboBoxLanguage.insets = new Insets(0, 0, 5, 5);
		gbcComboBoxLanguage.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBoxLanguage.gridx = 0;
		gbcComboBoxLanguage.gridy = 5;
		panelEdit.add(comboBoxLanguage, gbcComboBoxLanguage);
		
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
		GridBagConstraints gbcTextFieldAuthorText = new GridBagConstraints();
		gbcTextFieldAuthorText.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldAuthorText.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldAuthorText.gridx = 1;
		gbcTextFieldAuthorText.gridy = 5;
		panelEdit.add(textFieldAuthorText, gbcTextFieldAuthorText);
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
		
		JLabel lblSongNotes = new JLabel("Song Notes (not shown in presentation)");
		GridBagConstraints gbcLblSongNotes = new GridBagConstraints();
		gbcLblSongNotes.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSongNotes.insets = new Insets(0, 0, 5, 0);
		gbcLblSongNotes.gridx = 2;
		gbcLblSongNotes.gridy = 6;
		panelEdit.add(lblSongNotes, gbcLblSongNotes);
		
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
		GridBagConstraints gbcTextFieldTonality = new GridBagConstraints();
		gbcTextFieldTonality.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldTonality.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldTonality.gridx = 0;
		gbcTextFieldTonality.gridy = 7;
		panelEdit.add(textFieldTonality, gbcTextFieldTonality);
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
		GridBagConstraints gbcTextFieldAuthorTranslation = new GridBagConstraints();
		gbcTextFieldAuthorTranslation.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldAuthorTranslation.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldAuthorTranslation.gridx = 1;
		gbcTextFieldAuthorTranslation.gridy = 7;
		panelEdit.add(textFieldAuthorTranslation, gbcTextFieldAuthorTranslation);
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
		GridBagConstraints gbcTextFieldSongNotes = new GridBagConstraints();
		gbcTextFieldSongNotes.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldSongNotes.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldSongNotes.gridx = 2;
		gbcTextFieldSongNotes.gridy = 7;
		panelEdit.add(textFieldSongNotes, gbcTextFieldSongNotes);
		textFieldSongNotes.setColumns(10);
		
		JLabel lblChordSequence = new JLabel("Chord Sequence");
		GridBagConstraints gbcLblChordSequence = new GridBagConstraints();
		gbcLblChordSequence.fill = GridBagConstraints.HORIZONTAL;
		gbcLblChordSequence.gridwidth = 3;
		gbcLblChordSequence.insets = new Insets(0, 0, 5, 5);
		gbcLblChordSequence.gridx = 0;
		gbcLblChordSequence.gridy = 8;
		panelEdit.add(lblChordSequence, gbcLblChordSequence);
		
		JScrollPane scrollPaneChordSequence = new JScrollPane();
		GridBagConstraints gbcScrollPaneChordSequence = new GridBagConstraints();
		gbcScrollPaneChordSequence.gridheight = 2;
		gbcScrollPaneChordSequence.weighty = 1.0;
		gbcScrollPaneChordSequence.fill = GridBagConstraints.BOTH;
		gbcScrollPaneChordSequence.gridwidth = 3;
		gbcScrollPaneChordSequence.insets = new Insets(0, 0, 0, 5);
		gbcScrollPaneChordSequence.gridx = 0;
		gbcScrollPaneChordSequence.gridy = 9;
		panelEdit.add(scrollPaneChordSequence, gbcScrollPaneChordSequence);
		
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
		btnUp.setIcon(ResourceTools.getIcon(getClass(), "/javax/swing/plaf/metal/icons/sortUp.png"));
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
		btnUnselect.setIcon(ResourceTools.getIcon(getClass(), "/org/jdesktop/swingx/JXErrorPane16.png"));
		GridBagConstraints gbcBtnUnselect = new GridBagConstraints();
		gbcBtnUnselect.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnUnselect.insets = new Insets(0, 0, 5, 0);
		gbcBtnUnselect.anchor = GridBagConstraints.NORTH;
		gbcBtnUnselect.gridx = 0;
		gbcBtnUnselect.gridy = 2;
		selectedSongListButtons.add(btnUnselect, gbcBtnUnselect);
		
		btnDown = new JButton("");
		btnDown.addActionListener(safeAction(e -> handleSongDown()));
		btnDown.setIcon(ResourceTools.getIcon(getClass(), "/javax/swing/plaf/metal/icons/sortDown.png"));
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
		btnPresentSelectedSong.setIcon(ResourceTools.getIcon(getClass(), "/milky/play.png"));
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
		btnShowBlankScreen.setIcon(ResourceTools.getIcon(getClass(), "/milky/stop.png"));
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
		btnShowLogo.setIcon(ResourceTools.getIcon(getClass(), "/milky/picture.png"));
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
		btnSlideshow.setIcon(ResourceTools.getIcon(getClass(), "/milky/movie.png"));
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
		gblPanelImportExportStatistics.rowHeights = new int[] { 30, 0, 0, 30, 30, 0, 0, 0, 30, 0, 30, 30, 0 };
		gblPanelImportExportStatistics.columnWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gblPanelImportExportStatistics.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			0.0, 0.0, Double.MIN_VALUE };
		panelImportExportStatistics.setLayout(gblPanelImportExportStatistics);
		
		JLabel lblSelectedSong2 = new JLabel("Selected Song");
		GridBagConstraints gbcLblSelectedSong2 = new GridBagConstraints();
		gbcLblSelectedSong2.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSelectedSong2.gridwidth = 3;
		gbcLblSelectedSong2.anchor = GridBagConstraints.SOUTH;
		gbcLblSelectedSong2.insets = new Insets(0, 0, 5, 0);
		gbcLblSelectedSong2.gridx = 0;
		gbcLblSelectedSong2.gridy = 0;
		panelImportExportStatistics.add(lblSelectedSong2, gbcLblSelectedSong2);
		
		btnExportLyricsOnlyPdfSelected = new JButton("Export lyrics-only PDF");
		btnExportLyricsOnlyPdfSelected.addActionListener(safeAction(e -> JOptionPane.showMessageDialog(MainWindow.this,
			"This function is not implemented yet!", "Information",
			JOptionPane.INFORMATION_MESSAGE)));
		GridBagConstraints gbcBtnExportLyricsOnlyPdfSelected = new GridBagConstraints();
		gbcBtnExportLyricsOnlyPdfSelected.anchor = GridBagConstraints.NORTH;
		gbcBtnExportLyricsOnlyPdfSelected.insets = new Insets(0, 0, 5, 5);
		gbcBtnExportLyricsOnlyPdfSelected.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnExportLyricsOnlyPdfSelected.gridx = 0;
		gbcBtnExportLyricsOnlyPdfSelected.gridy = 1;
		panelImportExportStatistics.add(btnExportLyricsOnlyPdfSelected, gbcBtnExportLyricsOnlyPdfSelected);
		
		btnExportCompletePdfSelected = new JButton("Export complete PDF");
		btnExportCompletePdfSelected.addActionListener(safeAction(e -> JOptionPane.showMessageDialog(MainWindow.this,
			"This function is not implemented yet!", "Information",
			JOptionPane.INFORMATION_MESSAGE)));
		GridBagConstraints gbcBtnExportCompletePdfSelected = new GridBagConstraints();
		gbcBtnExportCompletePdfSelected.anchor = GridBagConstraints.NORTH;
		gbcBtnExportCompletePdfSelected.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnExportCompletePdfSelected.insets = new Insets(0, 0, 5, 5);
		gbcBtnExportCompletePdfSelected.gridx = 0;
		gbcBtnExportCompletePdfSelected.gridy = 2;
		panelImportExportStatistics.add(btnExportCompletePdfSelected, gbcBtnExportCompletePdfSelected);
		
		btnExportStatisticsSelected = new JButton("Export statistics");
		btnExportStatisticsSelected.addActionListener(safeAction(e -> JOptionPane.showMessageDialog(MainWindow.this,
			"This function is not implemented yet!", "Information",
			JOptionPane.INFORMATION_MESSAGE)));
		GridBagConstraints gbcBtnExportStatisticsSelected = new GridBagConstraints();
		gbcBtnExportStatisticsSelected.anchor = GridBagConstraints.NORTH;
		gbcBtnExportStatisticsSelected.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnExportStatisticsSelected.insets = new Insets(0, 0, 5, 5);
		gbcBtnExportStatisticsSelected.gridx = 0;
		gbcBtnExportStatisticsSelected.gridy = 3;
		panelImportExportStatistics.add(btnExportStatisticsSelected, gbcBtnExportStatisticsSelected);
		
		JLabel lblAllSongs2 = new JLabel("All Songs");
		GridBagConstraints gbcLblAllSongs2 = new GridBagConstraints();
		gbcLblAllSongs2.fill = GridBagConstraints.HORIZONTAL;
		gbcLblAllSongs2.insets = new Insets(0, 0, 5, 0);
		gbcLblAllSongs2.gridwidth = 3;
		gbcLblAllSongs2.anchor = GridBagConstraints.SOUTH;
		gbcLblAllSongs2.gridx = 0;
		gbcLblAllSongs2.gridy = 4;
		panelImportExportStatistics.add(lblAllSongs2, gbcLblAllSongs2);
		
		btnExportLyricsOnlyPdfAll = new JButton("Export lyrics-only PDF");
		btnExportLyricsOnlyPdfAll.addActionListener(safeAction(e -> JOptionPane.showMessageDialog(MainWindow.this,
			"This function is not implemented yet!", "Information",
			JOptionPane.INFORMATION_MESSAGE)));
		GridBagConstraints gbcBtnExportLyricsOnlyPdfAll = new GridBagConstraints();
		gbcBtnExportLyricsOnlyPdfAll.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnExportLyricsOnlyPdfAll.insets = new Insets(0, 0, 5, 5);
		gbcBtnExportLyricsOnlyPdfAll.gridx = 0;
		gbcBtnExportLyricsOnlyPdfAll.gridy = 5;
		panelImportExportStatistics.add(btnExportLyricsOnlyPdfAll, gbcBtnExportLyricsOnlyPdfAll);
		
		btnExportCompletePdfAll = new JButton("Export complete PDF");
		btnExportCompletePdfAll.addActionListener(safeAction(e -> JOptionPane.showMessageDialog(MainWindow.this,
			"This function is not implemented yet!", "Information",
			JOptionPane.INFORMATION_MESSAGE)));
		GridBagConstraints gbcBtnExportCompletePdfAll = new GridBagConstraints();
		gbcBtnExportCompletePdfAll.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnExportCompletePdfAll.insets = new Insets(0, 0, 5, 5);
		gbcBtnExportCompletePdfAll.gridx = 0;
		gbcBtnExportCompletePdfAll.gridy = 6;
		panelImportExportStatistics.add(btnExportCompletePdfAll, gbcBtnExportCompletePdfAll);
		
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
		GridBagConstraints gbcBtnExportStatisticsAll = new GridBagConstraints();
		gbcBtnExportStatisticsAll.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnExportStatisticsAll.insets = new Insets(0, 0, 5, 5);
		gbcBtnExportStatisticsAll.gridx = 0;
		gbcBtnExportStatisticsAll.gridy = 7;
		panelImportExportStatistics.add(btnExportStatisticsAll, gbcBtnExportStatisticsAll);
		
		JLabel lblImportingSongs = new JLabel("Importing Songs");
		GridBagConstraints gbcLblImportingSongs = new GridBagConstraints();
		gbcLblImportingSongs.gridwidth = 3;
		gbcLblImportingSongs.fill = GridBagConstraints.HORIZONTAL;
		gbcLblImportingSongs.anchor = GridBagConstraints.SOUTH;
		gbcLblImportingSongs.insets = new Insets(0, 0, 5, 0);
		gbcLblImportingSongs.gridx = 0;
		gbcLblImportingSongs.gridy = 8;
		panelImportExportStatistics.add(lblImportingSongs, gbcLblImportingSongs);
		
		// TODO handle importers dynamically, load every implementation of "Importer" as button (=> ServiceLoader?)
		btnImportFromEasiSlides = new JButton("Import from EasiSlides 4.0");
		btnImportFromEasiSlides.addActionListener(safeAction(e -> handleImportFromEasiSlides()));
		GridBagConstraints gbcBtnImportFromEasiSlides = new GridBagConstraints();
		gbcBtnImportFromEasiSlides.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnImportFromEasiSlides.insets = new Insets(0, 0, 5, 5);
		gbcBtnImportFromEasiSlides.gridx = 0;
		gbcBtnImportFromEasiSlides.gridy = 9;
		panelImportExportStatistics.add(btnImportFromEasiSlides, gbcBtnImportFromEasiSlides);
		
		JLabel lblProgramVersionTitle = new JLabel("Program Version");
		lblProgramVersionTitle.setFont(new Font("DejaVu Sans", Font.ITALIC, 12));
		GridBagConstraints gbcLblProgramVersionTitle = new GridBagConstraints();
		gbcLblProgramVersionTitle.gridwidth = 3;
		gbcLblProgramVersionTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblProgramVersionTitle.anchor = GridBagConstraints.SOUTH;
		gbcLblProgramVersionTitle.insets = new Insets(0, 0, 5, 5);
		gbcLblProgramVersionTitle.gridx = 0;
		gbcLblProgramVersionTitle.gridy = 10;
		panelImportExportStatistics.add(lblProgramVersionTitle, gbcLblProgramVersionTitle);
		
		lblProgramVersion = new JLabel("<PROGRAM VERSION>");
		lblProgramVersion.setBorder(new EmptyBorder(0, 0, 0, 0));
		lblProgramVersion.setFont(new Font("DejaVu Sans", Font.ITALIC, 12));
		GridBagConstraints gbcLblProgramVersion = new GridBagConstraints();
		gbcLblProgramVersion.fill = GridBagConstraints.HORIZONTAL;
		gbcLblProgramVersion.anchor = GridBagConstraints.NORTH;
		gbcLblProgramVersion.gridwidth = 3;
		gbcLblProgramVersion.gridx = 0;
		gbcLblProgramVersion.gridy = 11;
		panelImportExportStatistics.add(lblProgramVersion, gbcLblProgramVersion);
		
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
		gblPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gblPanel.rowHeights = new int[] { 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0 };
		gblPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0 };
		gblPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, Double.MIN_VALUE };
		panel.setLayout(gblPanel);
		
		btnUnlock = new JButton("Unlock");
		btnUnlock.addActionListener(safeAction(e -> handleSettingsUnlock()));
		GridBagConstraints gbcBtnUnlock = new GridBagConstraints();
		gbcBtnUnlock.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnUnlock.gridwidth = 3;
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
		
		btnSelectTitleFont = new JButton("Select...");
		btnSelectTitleFont.addActionListener(safeAction(e -> handleSelectTitleFont()));
		GridBagConstraints gbcBtnSelectTitleFont = new GridBagConstraints();
		gbcBtnSelectTitleFont.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectTitleFont.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectTitleFont.gridx = 3;
		gbcBtnSelectTitleFont.gridy = 2;
		panel.add(btnSelectTitleFont, gbcBtnSelectTitleFont);
		
		JLabel lblLyricsFont = new JLabel("Lyrics font");
		GridBagConstraints gbcLblLyricsFont = new GridBagConstraints();
		gbcLblLyricsFont.anchor = GridBagConstraints.EAST;
		gbcLblLyricsFont.insets = new Insets(0, 0, 5, 5);
		gbcLblLyricsFont.gridx = 1;
		gbcLblLyricsFont.gridy = 3;
		panel.add(lblLyricsFont, gbcLblLyricsFont);
		
		btnSelectLyricsFont = new JButton("Select...");
		btnSelectLyricsFont.addActionListener(safeAction(e -> handleSelectLyricsFont()));
		GridBagConstraints gbcBtnSelectLyricsFont = new GridBagConstraints();
		gbcBtnSelectLyricsFont.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectLyricsFont.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectLyricsFont.gridx = 3;
		gbcBtnSelectLyricsFont.gridy = 3;
		panel.add(btnSelectLyricsFont, gbcBtnSelectLyricsFont);
		
		JLabel lblTranslationFont = new JLabel("Translation font");
		GridBagConstraints gbcLblTranslationFont = new GridBagConstraints();
		gbcLblTranslationFont.anchor = GridBagConstraints.EAST;
		gbcLblTranslationFont.insets = new Insets(0, 0, 5, 5);
		gbcLblTranslationFont.gridx = 1;
		gbcLblTranslationFont.gridy = 4;
		panel.add(lblTranslationFont, gbcLblTranslationFont);
		
		btnSelectTranslationFont = new JButton("Select...");
		btnSelectTranslationFont.addActionListener(safeAction(e -> handleSelectTranslationFont()));
		GridBagConstraints gbcBtnSelectTranslationFont = new GridBagConstraints();
		gbcBtnSelectTranslationFont.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectTranslationFont.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectTranslationFont.gridx = 3;
		gbcBtnSelectTranslationFont.gridy = 4;
		panel.add(btnSelectTranslationFont, gbcBtnSelectTranslationFont);
		
		JLabel lblCopyrightFont = new JLabel("Copyright font");
		GridBagConstraints gbcLblCopyrightFont = new GridBagConstraints();
		gbcLblCopyrightFont.anchor = GridBagConstraints.EAST;
		gbcLblCopyrightFont.insets = new Insets(0, 0, 5, 5);
		gbcLblCopyrightFont.gridx = 1;
		gbcLblCopyrightFont.gridy = 5;
		panel.add(lblCopyrightFont, gbcLblCopyrightFont);
		
		btnSelectCopyrightFont = new JButton("Select...");
		btnSelectCopyrightFont.addActionListener(safeAction(e -> handleSelectCopyrightFont()));
		GridBagConstraints gbcBtnSelectCopyrightFont = new GridBagConstraints();
		gbcBtnSelectCopyrightFont.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectCopyrightFont.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectCopyrightFont.gridx = 3;
		gbcBtnSelectCopyrightFont.gridy = 5;
		panel.add(btnSelectCopyrightFont, gbcBtnSelectCopyrightFont);
		
		JLabel lblTextColor = new JLabel("Text color");
		GridBagConstraints gbcLblTextColor = new GridBagConstraints();
		gbcLblTextColor.anchor = GridBagConstraints.EAST;
		gbcLblTextColor.insets = new Insets(0, 0, 5, 5);
		gbcLblTextColor.gridx = 1;
		gbcLblTextColor.gridy = 6;
		panel.add(lblTextColor, gbcLblTextColor);
		
		btnSelectTextColor = new JButton("Select...");
		btnSelectTextColor.addActionListener(safeAction(e -> handleSelectTextColor()));
		GridBagConstraints gbcBtnSelectTextColor = new GridBagConstraints();
		gbcBtnSelectTextColor.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectTextColor.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectTextColor.gridx = 3;
		gbcBtnSelectTextColor.gridy = 6;
		panel.add(btnSelectTextColor, gbcBtnSelectTextColor);
		
		JLabel lblBackgroundColor = new JLabel("Background color");
		GridBagConstraints gbcLblBackgroundColor = new GridBagConstraints();
		gbcLblBackgroundColor.anchor = GridBagConstraints.EAST;
		gbcLblBackgroundColor.insets = new Insets(0, 0, 5, 5);
		gbcLblBackgroundColor.gridx = 1;
		gbcLblBackgroundColor.gridy = 7;
		panel.add(lblBackgroundColor, gbcLblBackgroundColor);
		
		btnSelectBackgroundColor = new JButton("Select...");
		btnSelectBackgroundColor.addActionListener(safeAction(e -> handleSelectBackgroundColor()));
		GridBagConstraints gbcBtnSelectBackgroundColor = new GridBagConstraints();
		gbcBtnSelectBackgroundColor.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectBackgroundColor.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectBackgroundColor.gridx = 3;
		gbcBtnSelectBackgroundColor.gridy = 7;
		panel.add(btnSelectBackgroundColor, gbcBtnSelectBackgroundColor);
		
		JLabel lblLogo = new JLabel("Logo");
		GridBagConstraints gbcLblLogo = new GridBagConstraints();
		gbcLblLogo.anchor = GridBagConstraints.EAST;
		gbcLblLogo.insets = new Insets(0, 0, 5, 5);
		gbcLblLogo.gridx = 1;
		gbcLblLogo.gridy = 8;
		panel.add(lblLogo, gbcLblLogo);
		
		btnSelectLogo = new JButton("Select...");
		btnSelectLogo.addActionListener(safeAction(e -> handleSelectLogo()));
		GridBagConstraints gbcBtnSelectLogo = new GridBagConstraints();
		gbcBtnSelectLogo.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectLogo.fill = GridBagConstraints.HORIZONTAL;
		gbcBtnSelectLogo.gridx = 3;
		gbcBtnSelectLogo.gridy = 8;
		panel.add(btnSelectLogo, gbcBtnSelectLogo);
		
		JLabel lblTopMargin = new JLabel("Top margin");
		GridBagConstraints gbcLblTopMargin = new GridBagConstraints();
		gbcLblTopMargin.anchor = GridBagConstraints.EAST;
		gbcLblTopMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblTopMargin.gridx = 1;
		gbcLblTopMargin.gridy = 9;
		panel.add(lblTopMargin, gbcLblTopMargin);
		
		spinnerTopMargin = new JSpinner();
		GridBagConstraints gbcSpinnerTopMargin = new GridBagConstraints();
		gbcSpinnerTopMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerTopMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerTopMargin.gridx = 3;
		gbcSpinnerTopMargin.gridy = 9;
		panel.add(spinnerTopMargin, gbcSpinnerTopMargin);
		
		JLabel lblLeftMargin = new JLabel("Left margin");
		GridBagConstraints gbcLblLeftMargin = new GridBagConstraints();
		gbcLblLeftMargin.anchor = GridBagConstraints.EAST;
		gbcLblLeftMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblLeftMargin.gridx = 1;
		gbcLblLeftMargin.gridy = 10;
		panel.add(lblLeftMargin, gbcLblLeftMargin);
		
		spinnerLeftMargin = new JSpinner();
		GridBagConstraints gbcSpinnerLeftMargin = new GridBagConstraints();
		gbcSpinnerLeftMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerLeftMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerLeftMargin.gridx = 3;
		gbcSpinnerLeftMargin.gridy = 10;
		panel.add(spinnerLeftMargin, gbcSpinnerLeftMargin);
		
		JLabel lblRightMargin = new JLabel("Right margin");
		GridBagConstraints gbcLblRightMargin = new GridBagConstraints();
		gbcLblRightMargin.anchor = GridBagConstraints.EAST;
		gbcLblRightMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblRightMargin.gridx = 1;
		gbcLblRightMargin.gridy = 11;
		panel.add(lblRightMargin, gbcLblRightMargin);
		
		spinnerRightMargin = new JSpinner();
		GridBagConstraints gbcSpinnerRightMargin = new GridBagConstraints();
		gbcSpinnerRightMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerRightMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerRightMargin.gridx = 3;
		gbcSpinnerRightMargin.gridy = 11;
		panel.add(spinnerRightMargin, gbcSpinnerRightMargin);
		
		JLabel lblBottomMargin = new JLabel("Bottom margin");
		GridBagConstraints gbcLblBottomMargin = new GridBagConstraints();
		gbcLblBottomMargin.anchor = GridBagConstraints.EAST;
		gbcLblBottomMargin.insets = new Insets(0, 0, 5, 5);
		gbcLblBottomMargin.gridx = 1;
		gbcLblBottomMargin.gridy = 12;
		panel.add(lblBottomMargin, gbcLblBottomMargin);
		
		spinnerBottomMargin = new JSpinner();
		GridBagConstraints gbcSpinnerBottomMargin = new GridBagConstraints();
		gbcSpinnerBottomMargin.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerBottomMargin.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerBottomMargin.gridx = 3;
		gbcSpinnerBottomMargin.gridy = 12;
		panel.add(spinnerBottomMargin, gbcSpinnerBottomMargin);
		
		JLabel lblShowTitle = new JLabel("Show title in presentation");
		lblShowTitle.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbcLblShowTitle = new GridBagConstraints();
		gbcLblShowTitle.anchor = GridBagConstraints.EAST;
		gbcLblShowTitle.insets = new Insets(0, 0, 5, 5);
		gbcLblShowTitle.gridx = 1;
		gbcLblShowTitle.gridy = 13;
		panel.add(lblShowTitle, gbcLblShowTitle);
		
		checkboxShowTitle = new JCheckBox("");
		GridBagConstraints gbcCheckboxShowTitle = new GridBagConstraints();
		gbcCheckboxShowTitle.insets = new Insets(0, 0, 5, 5);
		gbcCheckboxShowTitle.gridx = 3;
		gbcCheckboxShowTitle.gridy = 13;
		panel.add(checkboxShowTitle, gbcCheckboxShowTitle);
		
		JLabel lblDistanceBetweenTitle = new JLabel("Distance between title and text");
		GridBagConstraints gbcLblDistanceBetweenTitle = new GridBagConstraints();
		gbcLblDistanceBetweenTitle.anchor = GridBagConstraints.EAST;
		gbcLblDistanceBetweenTitle.insets = new Insets(0, 0, 5, 5);
		gbcLblDistanceBetweenTitle.gridx = 1;
		gbcLblDistanceBetweenTitle.gridy = 14;
		panel.add(lblDistanceBetweenTitle, gbcLblDistanceBetweenTitle);
		
		spinnerDistanceTitleText = new JSpinner();
		GridBagConstraints gbcSpinnerDistanceTitleText = new GridBagConstraints();
		gbcSpinnerDistanceTitleText.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerDistanceTitleText.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerDistanceTitleText.gridx = 3;
		gbcSpinnerDistanceTitleText.gridy = 14;
		panel.add(spinnerDistanceTitleText, gbcSpinnerDistanceTitleText);
		
		JLabel lblDistanceBetweenText = new JLabel("Distance between text and copyright");
		GridBagConstraints gbcLblDistanceBetweenText = new GridBagConstraints();
		gbcLblDistanceBetweenText.anchor = GridBagConstraints.EAST;
		gbcLblDistanceBetweenText.insets = new Insets(0, 0, 5, 5);
		gbcLblDistanceBetweenText.gridx = 1;
		gbcLblDistanceBetweenText.gridy = 15;
		panel.add(lblDistanceBetweenText, gbcLblDistanceBetweenText);
		
		spinnerDistanceTextCopyright = new JSpinner();
		GridBagConstraints gbcSpinnerDistanceTextCopyright = new GridBagConstraints();
		gbcSpinnerDistanceTextCopyright.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerDistanceTextCopyright.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerDistanceTextCopyright.gridx = 3;
		gbcSpinnerDistanceTextCopyright.gridy = 15;
		panel.add(spinnerDistanceTextCopyright, gbcSpinnerDistanceTextCopyright);
		
		JLabel lblSongListFiltering = new JLabel("Song list filter");
		GridBagConstraints gbcLblSongListFiltering = new GridBagConstraints();
		gbcLblSongListFiltering.anchor = GridBagConstraints.EAST;
		gbcLblSongListFiltering.insets = new Insets(0, 0, 5, 5);
		gbcLblSongListFiltering.gridx = 1;
		gbcLblSongListFiltering.gridy = 16;
		panel.add(lblSongListFiltering, gbcLblSongListFiltering);
		
		comboSongListFiltering = new JComboBox<>();
		GridBagConstraints gbcComboSongListFiltering = new GridBagConstraints();
		gbcComboSongListFiltering.insets = new Insets(0, 0, 5, 5);
		gbcComboSongListFiltering.fill = GridBagConstraints.HORIZONTAL;
		gbcComboSongListFiltering.gridx = 3;
		gbcComboSongListFiltering.gridy = 16;
		panel.add(comboSongListFiltering, gbcComboSongListFiltering);
		
		JLabel lblPresentationScreen1Display = new JLabel("Presentation screen 1 display");
		GridBagConstraints gbcLblPresentationScreen1Display = new GridBagConstraints();
		gbcLblPresentationScreen1Display.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen1Display.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen1Display.gridx = 1;
		gbcLblPresentationScreen1Display.gridy = 17;
		panel.add(lblPresentationScreen1Display, gbcLblPresentationScreen1Display);
		
		comboPresentationScreen1Display = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen1Display = new GridBagConstraints();
		gbcComboPresentationScreen1Display.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen1Display.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen1Display.gridx = 3;
		gbcComboPresentationScreen1Display.gridy = 17;
		panel.add(comboPresentationScreen1Display, gbcComboPresentationScreen1Display);
		
		JLabel lblPresentationScreen1Contents = new JLabel("Presentation screen 1 contents");
		GridBagConstraints gbcLblPresentationScreen1Contents = new GridBagConstraints();
		gbcLblPresentationScreen1Contents.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen1Contents.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen1Contents.gridx = 1;
		gbcLblPresentationScreen1Contents.gridy = 18;
		panel.add(lblPresentationScreen1Contents, gbcLblPresentationScreen1Contents);
		
		comboPresentationScreen1Contents = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen1Contents = new GridBagConstraints();
		gbcComboPresentationScreen1Contents.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen1Contents.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen1Contents.gridx = 3;
		gbcComboPresentationScreen1Contents.gridy = 18;
		panel.add(comboPresentationScreen1Contents, gbcComboPresentationScreen1Contents);
		
		JLabel lblPresentationScreen2Display = new JLabel("Presentation screen 2 display");
		GridBagConstraints gbcLblPresentationScreen2Display = new GridBagConstraints();
		gbcLblPresentationScreen2Display.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen2Display.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen2Display.gridx = 1;
		gbcLblPresentationScreen2Display.gridy = 19;
		panel.add(lblPresentationScreen2Display, gbcLblPresentationScreen2Display);
		
		comboPresentationScreen2Display = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen2Display = new GridBagConstraints();
		gbcComboPresentationScreen2Display.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen2Display.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen2Display.gridx = 3;
		gbcComboPresentationScreen2Display.gridy = 19;
		panel.add(comboPresentationScreen2Display, gbcComboPresentationScreen2Display);
		
		JLabel lblPresentationScreen2Contents = new JLabel("Presentation screen 2 contents");
		GridBagConstraints gbcLblPresentationScreen2Contents = new GridBagConstraints();
		gbcLblPresentationScreen2Contents.anchor = GridBagConstraints.EAST;
		gbcLblPresentationScreen2Contents.insets = new Insets(0, 0, 5, 5);
		gbcLblPresentationScreen2Contents.gridx = 1;
		gbcLblPresentationScreen2Contents.gridy = 20;
		panel.add(lblPresentationScreen2Contents, gbcLblPresentationScreen2Contents);
		
		comboPresentationScreen2Contents = new JComboBox<>();
		GridBagConstraints gbcComboPresentationScreen2Contents = new GridBagConstraints();
		gbcComboPresentationScreen2Contents.insets = new Insets(0, 0, 5, 5);
		gbcComboPresentationScreen2Contents.fill = GridBagConstraints.HORIZONTAL;
		gbcComboPresentationScreen2Contents.gridx = 3;
		gbcComboPresentationScreen2Contents.gridy = 20;
		panel.add(comboPresentationScreen2Contents, gbcComboPresentationScreen2Contents);
		
		JLabel lblSecondsToCount = new JLabel("Seconds to count a song as displayed after");
		GridBagConstraints gbcLblSecondsToCount = new GridBagConstraints();
		gbcLblSecondsToCount.anchor = GridBagConstraints.EAST;
		gbcLblSecondsToCount.insets = new Insets(0, 0, 5, 5);
		gbcLblSecondsToCount.gridx = 1;
		gbcLblSecondsToCount.gridy = 21;
		panel.add(lblSecondsToCount, gbcLblSecondsToCount);
		
		spinnerCountAsDisplayedAfter = new JSpinner();
		GridBagConstraints gbcSpinnerCountAsDisplayedAfter = new GridBagConstraints();
		gbcSpinnerCountAsDisplayedAfter.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerCountAsDisplayedAfter.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerCountAsDisplayedAfter.gridx = 3;
		gbcSpinnerCountAsDisplayedAfter.gridy = 21;
		panel.add(spinnerCountAsDisplayedAfter, gbcSpinnerCountAsDisplayedAfter);
		
		lblSlideShowDirectory = new JLabel("Directory for slide show");
		GridBagConstraints gbc_lblSlideShowDirectory = new GridBagConstraints();
		gbc_lblSlideShowDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblSlideShowDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblSlideShowDirectory.gridx = 1;
		gbc_lblSlideShowDirectory.gridy = 22;
		panel.add(lblSlideShowDirectory, gbc_lblSlideShowDirectory);
		
		btnSlideShowDirectory = new JButton("Select...");
		btnSlideShowDirectory.addActionListener(safeAction(e -> handleSelectSlideShowDirectory()));
		GridBagConstraints gbc_btnSlideShowDirectory = new GridBagConstraints();
		gbc_btnSlideShowDirectory.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSlideShowDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_btnSlideShowDirectory.gridx = 3;
		gbc_btnSlideShowDirectory.gridy = 22;
		panel.add(btnSlideShowDirectory, gbc_btnSlideShowDirectory);
		
		lblSlideShowSeconds = new JLabel("Seconds between slide show changes");
		GridBagConstraints gbc_lblSlideShowSeconds = new GridBagConstraints();
		gbc_lblSlideShowSeconds.anchor = GridBagConstraints.EAST;
		gbc_lblSlideShowSeconds.insets = new Insets(0, 0, 5, 5);
		gbc_lblSlideShowSeconds.gridx = 1;
		gbc_lblSlideShowSeconds.gridy = 23;
		panel.add(lblSlideShowSeconds, gbc_lblSlideShowSeconds);
		
		spinnerSlideShowSeconds = new JSpinner();
		GridBagConstraints gbc_spinnerSlideShowSeconds = new GridBagConstraints();
		gbc_spinnerSlideShowSeconds.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerSlideShowSeconds.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerSlideShowSeconds.gridx = 3;
		gbc_spinnerSlideShowSeconds.gridy = 23;
		panel.add(spinnerSlideShowSeconds, gbc_spinnerSlideShowSeconds);
		
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
		buttonPanel.add(saveButton);
		glassPane.add(buttonPanel, gbc);
		
		afterConstruction();
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
}
