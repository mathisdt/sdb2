package org.zephyrsoft.sdb2.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.MainController;
import org.zephyrsoft.sdb2.importer.ImportFromSDBv1;
import org.zephyrsoft.sdb2.model.LanguageEnum;
import org.zephyrsoft.sdb2.model.MainModel;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.util.CustomFileFilter;
import org.zephyrsoft.util.gui.FocusTraversalOnArray;

/**
 * Main window of the application.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = -6874196690375696416L;
	
	private static Logger LOG = LoggerFactory.getLogger(MainWindow.class);
	
	private JPanel contentPane;
	
	private JEditorPane editorLyrics;
	private JTextField textFieldTitle;
	private JComboBox<?> comboBoxLanguage;
	private JTextField textFieldTonality;
	private JTextField textFieldComposer;
	private JTextField textFieldAuthorText;
	private JTextField textFieldAuthorTranslation;
	private JTextField textFieldPublisher;
	private JTextField textFieldAdditionalCopyrightNotes;
	private JTextField textFieldSongNotes;
	private JEditorPane editorChordSequence;
	private JList<Song> linkedSongsList;
	
	private KeyboardShortcutManager shortcutManager;
	private final MainController controller;
	private MainModel model;
	private TransparentListModel<Song> listModel;
	private TransparentListModel<Song> linkedSongsListModel;
	
	private JList<Song> songList;
	private JList<Song> presentSongList;
	private JTextField textFieldFilter;
	private JPanel panelSectionButtons;
	private JLabel lblSelectedSongMetadata;
	private JLabel lblPresentedSongMetadata;
	private JLabel lblStatistics;
	private Song songListSelected;
	
	public void setModel(MainModel model) {
		this.model = model;
		this.model.initIfNecessary();
		bindModel();
	}
	
	private void bindModel() {
		listModel = model.getListModel();
		songList.setModel(listModel);
	}
	
	protected void handleSongListSelectionChanged(ListSelectionEvent e) {
		// only the last event in a row should fire these actions (check valueIsAdjusting)
		if (!e.getValueIsAdjusting()) {
			if (songListSelected != null) {
				saveSongData(songListSelected);
				clearSongData();
			}
			songListSelected = songList.getSelectedValue();
			if (songListSelected != null) {
				loadSongData(songListSelected);
			}
		}
	}
	
	/**
	 * Stores all data contained in the GUI elements.
	 * 
	 * @param song the model object to which the data should be written
	 */
	private void saveSongData(Song song) {
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
	 * Reads song data and puts the values into the GUI elements.
	 * 
	 * @param song the model object which should be read
	 */
	private void loadSongData(final Song song) {
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
		if (songListSelected != null) {
			saveSongData(songListSelected);
		}
		boolean mayClose = controller.prepareClose();
		if (mayClose) {
			setVisible(false);
			dispose();
			controller.shutdown();
		}
	}
	
	protected void handleSongNew() {
		Song song = new Song();
		model.addSong(song);
		songList.setSelectedValue(song, true);
	}
	
	protected void handleSongDelete() {
		if (songListSelected != null) {
			Song songToDelete = songListSelected;
			songList.removeSelectionInterval(0, model.getSize() - 1);
			model.removeSong(songToDelete);
		}
	}
	
	protected void handleSongSelect() {
		// TODO
		
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
			try {
				imported = importer.loadFromFile(chooser.getSelectedFile());
			} catch (Exception e) {
				// TODO
			}
			if (imported != null) {
				for (Song song : imported) {
					model.addSong(song);
				}
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
				LOG.debug("Escape-Action");
				if (textFieldFilter.isFocusOwner()) {
					// empty the search field
					textFieldFilter.setText("");
				} else {
					// focus the search field
					textFieldFilter.requestFocus();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 * 
	 * @param controller
	 */
	public MainWindow(MainController mainController) {
		setTitle("Song Database");
		controller = mainController;
		defineShortcuts();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				handleWindowClosing();
			}
		});
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 965, 659);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder(null);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		JPanel panelSongList = new JPanel();
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
		
		JButton btnClearFilter = new JButton("");
		btnClearFilter.setMargin(new Insets(0, 0, 0, 0));
		btnClearFilter.setRolloverIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/plaf/basic/resources/clear_rollover.gif")));
		btnClearFilter.setPressedIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/plaf/basic/resources/clear_pressed.gif")));
		btnClearFilter.setIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/plaf/basic/resources/clear.gif")));
		GridBagConstraints gbc_btnClearFilter = new GridBagConstraints();
		gbc_btnClearFilter.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearFilter.gridx = 2;
		gbc_btnClearFilter.gridy = 0;
		panelFilter.add(btnClearFilter, gbc_btnClearFilter);
		
		JScrollPane scrollPaneSongList = new JScrollPane();
		panelSongList.add(scrollPaneSongList, BorderLayout.CENTER);
		
		songList = new JList<Song>();
		songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		((DefaultListSelectionModel) songList.getSelectionModel())
			.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					handleSongListSelectionChanged(e);
				}
			});
		scrollPaneSongList.setViewportView(songList);
		songList.setCellRenderer(new SongCellRenderer());
		
		JPanel panelSongListButtons = new JPanel();
		panelSongList.add(panelSongListButtons, BorderLayout.SOUTH);
		GridBagLayout gbl_panelSongListButtons = new GridBagLayout();
		gbl_panelSongListButtons.columnWidths = new int[] {0, 0, 0};
		gbl_panelSongListButtons.rowHeights = new int[] {26};
		gbl_panelSongListButtons.columnWeights = new double[] {0.0, 0.0, 0.0};
		gbl_panelSongListButtons.rowWeights = new double[] {0.0};
		panelSongListButtons.setLayout(gbl_panelSongListButtons);
		
		JButton btnNewSong = new JButton("New");
		btnNewSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSongNew();
			}
		});
		btnNewSong.setIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/editors/newHighlighter.gif")));
		GridBagConstraints gbc_btnNewSong = new GridBagConstraints();
		gbc_btnNewSong.fill = GridBagConstraints.VERTICAL;
		gbc_btnNewSong.anchor = GridBagConstraints.WEST;
		gbc_btnNewSong.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewSong.gridx = 0;
		gbc_btnNewSong.gridy = 0;
		panelSongListButtons.add(btnNewSong, gbc_btnNewSong);
		
		JButton btnDeleteSong = new JButton("Delete");
		btnDeleteSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSongDelete();
			}
		});
		btnDeleteSong.setIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/editors/deleteHighlighter.gif")));
		GridBagConstraints gbc_btnDeleteSong = new GridBagConstraints();
		gbc_btnDeleteSong.fill = GridBagConstraints.VERTICAL;
		gbc_btnDeleteSong.anchor = GridBagConstraints.WEST;
		gbc_btnDeleteSong.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSong.gridx = 1;
		gbc_btnDeleteSong.gridy = 0;
		panelSongListButtons.add(btnDeleteSong, gbc_btnDeleteSong);
		
		JButton btnSelectSong = new JButton("Select");
		btnSelectSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSongSelect();
			}
		});
		btnSelectSong.setIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/plaf/basic/resources/month-up.png")));
		GridBagConstraints gbc_btnSelectSong = new GridBagConstraints();
		gbc_btnSelectSong.fill = GridBagConstraints.VERTICAL;
		gbc_btnSelectSong.anchor = GridBagConstraints.EAST;
		gbc_btnSelectSong.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectSong.gridx = 2;
		gbc_btnSelectSong.gridy = 0;
		panelSongListButtons.add(btnSelectSong, gbc_btnSelectSong);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
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
		editorLyrics.setFont(new Font("Courier New", editorLyrics.getFont().getStyle(), editorLyrics.getFont()
			.getSize()));
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
		GridBagConstraints gbc_textFieldTitle = new GridBagConstraints();
		gbc_textFieldTitle.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTitle.gridx = 0;
		gbc_textFieldTitle.gridy = 3;
		panelEdit.add(textFieldTitle, gbc_textFieldTitle);
		textFieldTitle.setColumns(10);
		
		textFieldComposer = new JTextField();
		GridBagConstraints gbc_textFieldComposer = new GridBagConstraints();
		gbc_textFieldComposer.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldComposer.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldComposer.gridx = 1;
		gbc_textFieldComposer.gridy = 3;
		panelEdit.add(textFieldComposer, gbc_textFieldComposer);
		textFieldComposer.setColumns(10);
		
		textFieldPublisher = new JTextField();
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
		
		comboBoxLanguage = new JComboBox<Object>();
		GridBagConstraints gbc_comboBoxLanguage = new GridBagConstraints();
		gbc_comboBoxLanguage.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxLanguage.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxLanguage.gridx = 0;
		gbc_comboBoxLanguage.gridy = 5;
		panelEdit.add(comboBoxLanguage, gbc_comboBoxLanguage);
		
		textFieldAuthorText = new JTextField();
		GridBagConstraints gbc_textFieldAuthorText = new GridBagConstraints();
		gbc_textFieldAuthorText.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldAuthorText.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldAuthorText.gridx = 1;
		gbc_textFieldAuthorText.gridy = 5;
		panelEdit.add(textFieldAuthorText, gbc_textFieldAuthorText);
		textFieldAuthorText.setColumns(10);
		
		textFieldAdditionalCopyrightNotes = new JTextField();
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
		GridBagConstraints gbc_textFieldTonality = new GridBagConstraints();
		gbc_textFieldTonality.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldTonality.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldTonality.gridx = 0;
		gbc_textFieldTonality.gridy = 7;
		panelEdit.add(textFieldTonality, gbc_textFieldTonality);
		textFieldTonality.setColumns(10);
		
		textFieldAuthorTranslation = new JTextField();
		GridBagConstraints gbc_textFieldAuthorTranslation = new GridBagConstraints();
		gbc_textFieldAuthorTranslation.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldAuthorTranslation.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldAuthorTranslation.gridx = 1;
		gbc_textFieldAuthorTranslation.gridy = 7;
		panelEdit.add(textFieldAuthorTranslation, gbc_textFieldAuthorTranslation);
		textFieldAuthorTranslation.setColumns(10);
		
		textFieldSongNotes = new JTextField();
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
		editorChordSequence.setFont(new Font("Courier New", editorChordSequence.getFont().getStyle(),
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
		
		linkedSongsList = new JList<Song>();
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
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setIcon(new ImageIcon(MainWindow.class.getResource("/org/jdesktop/swingx/editors/newHighlighter.gif")));
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.BOTH;
		gbc_btnAdd.insets = new Insets(0, 0, 0, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		panelLinkedSongs.add(btnAdd, gbc_btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/editors/deleteHighlighter.gif")));
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.fill = GridBagConstraints.BOTH;
		gbc_btnRemove.gridx = 1;
		gbc_btnRemove.gridy = 0;
		panelLinkedSongs.add(btnRemove, gbc_btnRemove);
		
		JPanel panelPresent = new JPanel();
		tabbedPane.addTab("Present Songs", null, panelPresent, null);
		panelPresent.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPanePresent = new JSplitPane();
		splitPanePresent.setBorder(null);
		panelPresent.add(splitPanePresent, BorderLayout.CENTER);
		
		JPanel panelPresentLeft = new JPanel();
		panelPresentLeft.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPanePresent.setLeftComponent(panelPresentLeft);
		panelPresentLeft.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPanePresentSongList = new JScrollPane();
		panelPresentLeft.add(scrollPanePresentSongList, BorderLayout.CENTER);
		
		presentSongList = new JList<Song>();
		scrollPanePresentSongList.setViewportView(presentSongList);
		
		JPanel panelSelectedMetadata = new JPanel();
		panelPresentLeft.add(panelSelectedMetadata, BorderLayout.SOUTH);
		GridBagLayout gbl_panelSelectedMetadata = new GridBagLayout();
		gbl_panelSelectedMetadata.columnWidths = new int[] {258, 0};
		gbl_panelSelectedMetadata.rowHeights = new int[] {14, 14, 0};
		gbl_panelSelectedMetadata.columnWeights = new double[] {0.0, Double.MIN_VALUE};
		gbl_panelSelectedMetadata.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
		panelSelectedMetadata.setLayout(gbl_panelSelectedMetadata);
		
		JLabel lblSelectedSong = new JLabel("Selected song:");
		GridBagConstraints gbc_lblSelectedSong = new GridBagConstraints();
		gbc_lblSelectedSong.anchor = GridBagConstraints.NORTH;
		gbc_lblSelectedSong.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSelectedSong.insets = new Insets(0, 0, 5, 0);
		gbc_lblSelectedSong.gridx = 0;
		gbc_lblSelectedSong.gridy = 0;
		panelSelectedMetadata.add(lblSelectedSong, gbc_lblSelectedSong);
		
		lblSelectedSongMetadata = new JLabel("<SONG METADATA>");
		GridBagConstraints gbc_lblSelectedSongMetadata = new GridBagConstraints();
		gbc_lblSelectedSongMetadata.anchor = GridBagConstraints.NORTH;
		gbc_lblSelectedSongMetadata.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSelectedSongMetadata.gridx = 0;
		gbc_lblSelectedSongMetadata.gridy = 1;
		panelSelectedMetadata.add(lblSelectedSongMetadata, gbc_lblSelectedSongMetadata);
		
		JPanel panelSelectedSongListButtons = new JPanel();
		panelPresentLeft.add(panelSelectedSongListButtons, BorderLayout.EAST);
		GridBagLayout gbl_panelSelectedSongListButtons = new GridBagLayout();
		gbl_panelSelectedSongListButtons.columnWidths = new int[] {0};
		gbl_panelSelectedSongListButtons.rowHeights = new int[] {0, 0, 0, 0};
		gbl_panelSelectedSongListButtons.columnWeights = new double[] {0.0};
		gbl_panelSelectedSongListButtons.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0};
		panelSelectedSongListButtons.setLayout(gbl_panelSelectedSongListButtons);
		
		JButton btnUp = new JButton("Up");
		GridBagConstraints gbc_btnUp = new GridBagConstraints();
		gbc_btnUp.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUp.anchor = GridBagConstraints.SOUTH;
		gbc_btnUp.insets = new Insets(0, 0, 5, 0);
		gbc_btnUp.gridx = 0;
		gbc_btnUp.gridy = 1;
		panelSelectedSongListButtons.add(btnUp, gbc_btnUp);
		
		JButton btnUnselect = new JButton("Unselect");
		btnUnselect.setIcon(new ImageIcon(MainWindow.class
			.getResource("/org/jdesktop/swingx/plaf/basic/resources/month-down.png")));
		GridBagConstraints gbc_btnUnselect = new GridBagConstraints();
		gbc_btnUnselect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUnselect.insets = new Insets(0, 0, 5, 0);
		gbc_btnUnselect.anchor = GridBagConstraints.NORTH;
		gbc_btnUnselect.gridx = 0;
		gbc_btnUnselect.gridy = 2;
		panelSelectedSongListButtons.add(btnUnselect, gbc_btnUnselect);
		
		JButton btnDown = new JButton("Down");
		GridBagConstraints gbc_btnDown = new GridBagConstraints();
		gbc_btnDown.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDown.anchor = GridBagConstraints.NORTH;
		gbc_btnDown.gridx = 0;
		gbc_btnDown.gridy = 3;
		panelSelectedSongListButtons.add(btnDown, gbc_btnDown);
		
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
		
		JButton btnShowLogo = new JButton("Show logo");
		GridBagConstraints gbc_btnShowLogo = new GridBagConstraints();
		gbc_btnShowLogo.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnShowLogo.insets = new Insets(0, 0, 5, 0);
		gbc_btnShowLogo.gridx = 0;
		gbc_btnShowLogo.gridy = 0;
		panelPresentationButtons.add(btnShowLogo, gbc_btnShowLogo);
		
		JButton btnShowBlankScreen = new JButton("Blank screen");
		GridBagConstraints gbc_btnShowBlankScreen = new GridBagConstraints();
		gbc_btnShowBlankScreen.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnShowBlankScreen.insets = new Insets(0, 0, 5, 0);
		gbc_btnShowBlankScreen.gridx = 0;
		gbc_btnShowBlankScreen.gridy = 1;
		panelPresentationButtons.add(btnShowBlankScreen, gbc_btnShowBlankScreen);
		
		JButton btnPresentSelectedSong = new JButton("Present selected song");
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
		
		JScrollPane scrollPaneSectionButtons = new JScrollPane();
		GridBagConstraints gbc_scrollPaneSectionButtons = new GridBagConstraints();
		gbc_scrollPaneSectionButtons.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneSectionButtons.gridx = 0;
		gbc_scrollPaneSectionButtons.gridy = 4;
		panelPresentationButtons.add(scrollPaneSectionButtons, gbc_scrollPaneSectionButtons);
		
		panelSectionButtons = new JPanel();
		scrollPaneSectionButtons.setViewportView(panelSectionButtons);
		GridBagLayout gbl_panelSectionButtons = new GridBagLayout();
		gbl_panelSectionButtons.columnWidths = new int[] {0};
		gbl_panelSectionButtons.rowHeights = new int[] {0};
		gbl_panelSectionButtons.columnWeights = new double[] {Double.MIN_VALUE};
		gbl_panelSectionButtons.rowWeights = new double[] {Double.MIN_VALUE};
		panelSectionButtons.setLayout(gbl_panelSectionButtons);
		
		JPanel panelPresentedMetadata = new JPanel();
		panelPresentRight.add(panelPresentedMetadata, BorderLayout.SOUTH);
		GridBagLayout gbl_panelPresentedMetadata = new GridBagLayout();
		gbl_panelPresentedMetadata.columnWidths = new int[] {258, 0};
		gbl_panelPresentedMetadata.rowHeights = new int[] {14, 14, 0};
		gbl_panelPresentedMetadata.columnWeights = new double[] {0.0, Double.MIN_VALUE};
		gbl_panelPresentedMetadata.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
		panelPresentedMetadata.setLayout(gbl_panelPresentedMetadata);
		
		JLabel lblPresentedSong = new JLabel("Presented song:");
		GridBagConstraints gbc_lblPresentedSong = new GridBagConstraints();
		gbc_lblPresentedSong.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPresentedSong.anchor = GridBagConstraints.NORTH;
		gbc_lblPresentedSong.insets = new Insets(0, 0, 5, 0);
		gbc_lblPresentedSong.gridx = 0;
		gbc_lblPresentedSong.gridy = 0;
		panelPresentedMetadata.add(lblPresentedSong, gbc_lblPresentedSong);
		
		lblPresentedSongMetadata = new JLabel("<SONG METADATA>");
		GridBagConstraints gbc_lblPresentedSongMetadata = new GridBagConstraints();
		gbc_lblPresentedSongMetadata.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPresentedSongMetadata.anchor = GridBagConstraints.NORTH;
		gbc_lblPresentedSongMetadata.gridx = 0;
		gbc_lblPresentedSongMetadata.gridy = 1;
		panelPresentedMetadata.add(lblPresentedSongMetadata, gbc_lblPresentedSongMetadata);
		
		JPanel panelImportExportStatistics = new JPanel();
		panelImportExportStatistics.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Import / Export / Statistics", null, panelImportExportStatistics, null);
		GridBagLayout gbl_panelImportExportStatistics = new GridBagLayout();
		gbl_panelImportExportStatistics.columnWidths = new int[] {0, 70, 0, 0};
		gbl_panelImportExportStatistics.rowHeights = new int[] {30, 0, 0, 30, 30, 0, 0, 0, 30, 0, 0};
		gbl_panelImportExportStatistics.columnWeights = new double[] {1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panelImportExportStatistics.rowWeights =
			new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		
		JButton btnExportLyricsOnlyPdfSelected = new JButton("Export lyrics-only PDF");
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
		
		JButton btnExportCompletePdfSelected = new JButton("Export complete PDF");
		GridBagConstraints gbc_btnExportCompletePdfSelected = new GridBagConstraints();
		gbc_btnExportCompletePdfSelected.anchor = GridBagConstraints.NORTH;
		gbc_btnExportCompletePdfSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportCompletePdfSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportCompletePdfSelected.gridx = 0;
		gbc_btnExportCompletePdfSelected.gridy = 2;
		panelImportExportStatistics.add(btnExportCompletePdfSelected, gbc_btnExportCompletePdfSelected);
		
		JButton btnExportStatisticsSelected = new JButton("Export statistics");
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
		
		JButton btnExportLyricsOnlyPdfAll = new JButton("Export lyrics-only PDF");
		GridBagConstraints gbc_btnExportLyricsOnlyPdfAll = new GridBagConstraints();
		gbc_btnExportLyricsOnlyPdfAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportLyricsOnlyPdfAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportLyricsOnlyPdfAll.gridx = 0;
		gbc_btnExportLyricsOnlyPdfAll.gridy = 5;
		panelImportExportStatistics.add(btnExportLyricsOnlyPdfAll, gbc_btnExportLyricsOnlyPdfAll);
		
		JButton btnExportCompletePdfAll = new JButton("Export complete PDF");
		GridBagConstraints gbc_btnExportCompletePdfAll = new GridBagConstraints();
		gbc_btnExportCompletePdfAll.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExportCompletePdfAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportCompletePdfAll.gridx = 0;
		gbc_btnExportCompletePdfAll.gridy = 6;
		panelImportExportStatistics.add(btnExportCompletePdfAll, gbc_btnExportCompletePdfAll);
		
		JButton btnExportStatisticsAll = new JButton("Export statistics");
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
		
		JButton btnImportFromSdb1 = new JButton("Import from SDB 1.x");
		btnImportFromSdb1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleImportFromSDBv1();
			}
		});
		GridBagConstraints gbc_btnImportFromSdb1 = new GridBagConstraints();
		gbc_btnImportFromSdb1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnImportFromSdb1.insets = new Insets(0, 0, 0, 5);
		gbc_btnImportFromSdb1.gridx = 0;
		gbc_btnImportFromSdb1.gridy = 9;
		panelImportExportStatistics.add(btnImportFromSdb1, gbc_btnImportFromSdb1);
		
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
		gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0};
		gbl_panel.rowWeights =
			new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnUnlock = new JButton("Unlock");
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
		
		JButton btnSelectTitleFont = new JButton("Select...");
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
		
		JButton btnSelectLyricsFont = new JButton("Select...");
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
		
		JButton btnSelectTranslationFont = new JButton("Select...");
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
		
		JButton btnSelectCopyrightFont = new JButton("Select...");
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
		
		JButton btnSelectTextColor = new JButton("Select...");
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
		
		JButton btnSelectBackgroundColor = new JButton("Select...");
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
		
		JButton btnSelectLogo = new JButton("Select...");
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
		
		JSpinner spinnerTopMargin = new JSpinner();
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
		
		JSpinner spinnerLeftMargin = new JSpinner();
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
		
		JSpinner spinnerRightMargin = new JSpinner();
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
		
		JSpinner spinnerBottomMargin = new JSpinner();
		GridBagConstraints gbc_spinnerBottomMargin = new GridBagConstraints();
		gbc_spinnerBottomMargin.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerBottomMargin.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerBottomMargin.gridx = 3;
		gbc_spinnerBottomMargin.gridy = 12;
		panel.add(spinnerBottomMargin, gbc_spinnerBottomMargin);
		
		JLabel lblDistanceBetweenTitle = new JLabel("Distance between title and text");
		GridBagConstraints gbc_lblDistanceBetweenTitle = new GridBagConstraints();
		gbc_lblDistanceBetweenTitle.anchor = GridBagConstraints.EAST;
		gbc_lblDistanceBetweenTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblDistanceBetweenTitle.gridx = 1;
		gbc_lblDistanceBetweenTitle.gridy = 13;
		panel.add(lblDistanceBetweenTitle, gbc_lblDistanceBetweenTitle);
		
		JSpinner spinnerDistanceTitleText = new JSpinner();
		GridBagConstraints gbc_spinnerDistanceTitleText = new GridBagConstraints();
		gbc_spinnerDistanceTitleText.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerDistanceTitleText.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerDistanceTitleText.gridx = 3;
		gbc_spinnerDistanceTitleText.gridy = 13;
		panel.add(spinnerDistanceTitleText, gbc_spinnerDistanceTitleText);
		
		JLabel lblDistanceBetweenText = new JLabel("Distance between text and copyright");
		GridBagConstraints gbc_lblDistanceBetweenText = new GridBagConstraints();
		gbc_lblDistanceBetweenText.anchor = GridBagConstraints.EAST;
		gbc_lblDistanceBetweenText.insets = new Insets(0, 0, 5, 5);
		gbc_lblDistanceBetweenText.gridx = 1;
		gbc_lblDistanceBetweenText.gridy = 14;
		panel.add(lblDistanceBetweenText, gbc_lblDistanceBetweenText);
		
		JSpinner spinnerDistanceTextCopyright = new JSpinner();
		GridBagConstraints gbc_spinnerDistanceTextCopyright = new GridBagConstraints();
		gbc_spinnerDistanceTextCopyright.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerDistanceTextCopyright.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerDistanceTextCopyright.gridx = 3;
		gbc_spinnerDistanceTextCopyright.gridy = 14;
		panel.add(spinnerDistanceTextCopyright, gbc_spinnerDistanceTextCopyright);
		
		JLabel lblSongListContents = new JLabel("Song list contents");
		GridBagConstraints gbc_lblSongListContents = new GridBagConstraints();
		gbc_lblSongListContents.anchor = GridBagConstraints.EAST;
		gbc_lblSongListContents.insets = new Insets(0, 0, 5, 5);
		gbc_lblSongListContents.gridx = 1;
		gbc_lblSongListContents.gridy = 15;
		panel.add(lblSongListContents, gbc_lblSongListContents);
		
		JComboBox<?> comboSongListContents = new JComboBox<Object>();
		GridBagConstraints gbc_comboSongListContents = new GridBagConstraints();
		gbc_comboSongListContents.insets = new Insets(0, 0, 5, 5);
		gbc_comboSongListContents.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboSongListContents.gridx = 3;
		gbc_comboSongListContents.gridy = 15;
		panel.add(comboSongListContents, gbc_comboSongListContents);
		
		JLabel lblPresentationScreenContents = new JLabel("Presentation screen contents");
		GridBagConstraints gbc_lblPresentationScreenContents = new GridBagConstraints();
		gbc_lblPresentationScreenContents.anchor = GridBagConstraints.EAST;
		gbc_lblPresentationScreenContents.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresentationScreenContents.gridx = 1;
		gbc_lblPresentationScreenContents.gridy = 16;
		panel.add(lblPresentationScreenContents, gbc_lblPresentationScreenContents);
		
		JComboBox<?> comboPresentationScreenContents = new JComboBox<Object>();
		GridBagConstraints gbc_comboPresentationScreenContents = new GridBagConstraints();
		gbc_comboPresentationScreenContents.insets = new Insets(0, 0, 5, 5);
		gbc_comboPresentationScreenContents.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboPresentationScreenContents.gridx = 3;
		gbc_comboPresentationScreenContents.gridy = 16;
		panel.add(comboPresentationScreenContents, gbc_comboPresentationScreenContents);
		
		JLabel lblSecondsToCount = new JLabel("Seconds to count a song as displayed after");
		GridBagConstraints gbc_lblSecondsToCount = new GridBagConstraints();
		gbc_lblSecondsToCount.anchor = GridBagConstraints.EAST;
		gbc_lblSecondsToCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblSecondsToCount.gridx = 1;
		gbc_lblSecondsToCount.gridy = 17;
		panel.add(lblSecondsToCount, gbc_lblSecondsToCount);
		
		JSpinner spinnerCountAsDisplayedAfter = new JSpinner();
		GridBagConstraints gbc_spinnerCountAsDisplayedAfter = new GridBagConstraints();
		gbc_spinnerCountAsDisplayedAfter.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerCountAsDisplayedAfter.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinnerCountAsDisplayedAfter.gridx = 3;
		gbc_spinnerCountAsDisplayedAfter.gridy = 17;
		panel.add(spinnerCountAsDisplayedAfter, gbc_spinnerCountAsDisplayedAfter);
		
		JPanel panelShortcuts = new JPanel();
		panelShortcuts.setBorder(new EmptyBorder(0, 5, 5, 5));
		tabbedPane.addTab("Keyboard Shortcuts", null, panelShortcuts, null);
		GridBagLayout gbl_panelShortcuts = new GridBagLayout();
		gbl_panelShortcuts.columnWidths = new int[] {0};
		gbl_panelShortcuts.rowHeights = new int[] {0};
		gbl_panelShortcuts.columnWeights = new double[] {Double.MIN_VALUE};
		gbl_panelShortcuts.rowWeights = new double[] {Double.MIN_VALUE};
		panelShortcuts.setLayout(gbl_panelShortcuts);
		tabbedPane.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] {panelEdit, panelPresent,
			panelImportExportStatistics, panelSettings}));
	}
	
	public JTextField getTextFieldTitle() {
		return textFieldTitle;
	}
	
	public JComboBox<?> getComboBoxLanguage() {
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
		return songList;
	}
	
	public JTextField getTextFieldFilter() {
		return textFieldFilter;
	}
	
	public JList<?> getPresentSongList() {
		return presentSongList;
	}
	
	public JPanel getPanelSectionButtons() {
		return panelSectionButtons;
	}
	
	public JLabel getLblSelectedSongMetadata() {
		return lblSelectedSongMetadata;
	}
	
	public JLabel getLblPresentedSongMetadata() {
		return lblPresentedSongMetadata;
	}
	
	public JLabel getLblStatistics() {
		return lblStatistics;
	}
}
