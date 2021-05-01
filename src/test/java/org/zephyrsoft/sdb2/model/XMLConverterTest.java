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
package org.zephyrsoft.sdb2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.junit.Test;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.model.statistics.StatisticsModel;

/**
 * Tests the behaviour of {@link XMLConverter}.
 */
public class XMLConverterTest {
	
	private static final String SONG_1_LYRICS = "Liebe Gäste, liebe Geschwister,\n"
		+ "bitte schaltet eure Handys während\n" + "des Gottesdienstes aus oder auf lautlos.\n" + "Danke!\n" + "\n"
		+ "Dear guests, dear brothers and sisters\n" + "please switch off or mute your cellphones\n"
		+ "during the service.\n" + "Thank You!\n" + "\n" + "If you need translation \n"
		+ "please ask one of the ushers.";
	
	private static final String SONG_2_LYRICS = "The sun comes up, it is a new day dawning,\n"
		+ "[Die Sonne geht auf, ein neuer Tag bricht an]\n" + "It is time to sing Your song again.\n"
		+ "[Es ist wieder Zeit Dein Lied zu singen]\n" + "Whatever may pass, and whatever lies before me,\n"
		+ "[Was immer passieren mag, was immer vor mir liegt]\n" + "Let me be singing when the evening comes!\n"
		+ "[Lass mich singen wenn der Abend kommt]\n" + "\n" + "Bless the Lord, o my soul, o my soul,\n"
		+ "[Preis den Herrn, meine Seele, oh meine Seele]\n" + "Worship His holy name!\n"
		+ "[Bete Seinen heiligen Namen an]\n" + "Sing like never before, o my soul!\n"
		+ "[Sing wie nie zuvor, oh meine Seele!]\n" + "I will worship His holy name.\n"
		+ "[Ich werde seinen heiligen Namen anbeten]\n" + "\n" + "...";
	
	private static final String SONGS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		+ "<songs>\n" + "    <song>\n"
		+ "        <additionalCopyrightNotes></additionalCopyrightNotes>\n" + "        <authorText></authorText>\n"
		+ "        <authorTranslation></authorTranslation>\n" + "        <chordSequence></chordSequence>\n"
		+ "        <composer></composer>\n" + "        <lyrics>" + SONG_1_LYRICS + "</lyrics>\n"
		+ "        <publisher></publisher>\n" + "        <songNotes></songNotes>\n"
		+ "        <title>_Liebe Gäste, Liebe Geschwister</title>\n" + "        <tonality></tonality>\n"
		+ "        <uuid>094a9c6b-456c-405c-b8f7-7285c33d3c37</uuid>\n" + "    </song>\n" + "    <song>\n"
		+ "        <additionalCopyrightNotes></additionalCopyrightNotes>\n" + "        <authorText></authorText>\n"
		+ "        <authorTranslation></authorTranslation>\n" + "        <chordSequence></chordSequence>\n"
		+ "        <composer>Matt Redman &amp; Jonas Myrin</composer>\n" + "        <language>english</language>\n"
		+ "        <lyrics>" + SONG_2_LYRICS + "</lyrics>\n" + "        <publisher></publisher>\n"
		+ "        <songNotes></songNotes>\n" + "        <title>10.000 Reasons</title>\n"
		+ "        <tonality></tonality>\n" + "        <uuid>b867931a-d333-4e05-9847-f8ae80cdb5b2</uuid>\n"
		+ "    </song>\n" + "</songs>\n";
	
	private static final String STATISTICS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		+ "<statistics>\n" + "    <songStatistics>\n" + "        <presentedOn>\n"
		+ "            <date>2017-08-22</date>\n" + "            <date>2017-08-24</date>\n"
		+ "        </presentedOn>\n" + "        <songUuid>7d3ee81d-e69b-4d05-a257-2e51c0377ac5</songUuid>\n"
		+ "    </songStatistics>\n" + "    <songStatistics>\n" + "        <presentedOn>\n"
		+ "            <date>2017-11-21</date>\n" + "        </presentedOn>\n"
		+ "        <songUuid>094a9c6b-456c-405c-b8f7-7285c33d3c37</songUuid>\n" + "    </songStatistics>\n"
		+ "</statistics>\n";
	
	private static final String SETTINGS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		+ "<settings>\n" + "    <setting>\n" + "        <key>BACKGROUND_COLOR</key>\n"
		+ "        <value class=\"awt-color\">\n" + "            <red>1</red>\n" + "            <green>2</green>\n"
		+ "            <blue>3</blue>\n" + "            <alpha>255</alpha>\n" + "        </value>\n"
		+ "    </setting>\n" + "    <setting>\n" + "        <key>BOTTOM_MARGIN</key>\n"
		+ "        <value class=\"int\">25</value>\n" + "    </setting>\n" + "    <setting>\n"
		+ "        <key>COPYRIGHT_FONT</key>\n" + "        <value class=\"awt-font\">\n"
		+ "            <name>Dialog.bold</name>\n" + "            <style>1</style>\n"
		+ "            <size>14</size>\n" + "        </value>\n" + "    </setting>\n" + "    <setting>\n"
		+ "        <key>LOGO_FILE</key>\n"
		+ "        <value class=\"string\">/home/mathis/Bilder-Incoming/20170917-1927_img_0155.jpg</value>\n"
		+ "    </setting>\n" + "    <setting>\n" + "        <key>SCREEN_2_CONTENTS</key>\n"
		+ "        <value class=\"org.zephyrsoft.sdb2.model.ScreenContentsEnum\">OnlyLyrics</value>\n"
		+ "    </setting>\n" + "    <setting>\n" + "        <key>SHOW_TITLE</key>\n"
		+ "        <value class=\"boolean\">false</value>\n" + "    </setting>\n" + "    <setting>\n"
		+ "        <key>SONG_LIST_FILTER</key>\n"
		+ "        <value class=\"org.zephyrsoft.sdb2.model.FilterTypeEnum\">TitleAndLyrics</value>\n"
		+ "    </setting>\n" + "</settings>\n";
	
	@Test
	public void xmlFromStatistics() {
		StatisticsModel model = new StatisticsModel();
		Song song1 = new Song("7d3ee81d-e69b-4d05-a257-2e51c0377ac5");
		model.addStatisticsEntry(song1, LocalDate.of(2017, 8, 22));
		model.addStatisticsEntry(song1, LocalDate.of(2017, 8, 24));
		Song song2 = new Song("094a9c6b-456c-405c-b8f7-7285c33d3c37");
		model.addStatisticsEntry(song2, LocalDate.of(2017, 11, 21));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XMLConverter.fromPersistableToXML(model, outputStream);
		String output = outputStream.toString();
		assertEquals(STATISTICS_XML, output);
	}
	
	@Test
	public void statisticsFromXML() {
		StatisticsModel model = XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(STATISTICS_XML.getBytes()));
		
		assertNotNull(model);
		assertNotNull(model.getStatistics("7d3ee81d-e69b-4d05-a257-2e51c0377ac5"));
		assertNotNull(model.getStatistics("094a9c6b-456c-405c-b8f7-7285c33d3c37"));
	}
	
	@Test
	public void xmlFromSettings() {
		SettingsModel settingsModel = new SettingsModel();
		settingsModel.put(SettingKey.BACKGROUND_COLOR, new Color(1, 2, 3, 255));
		settingsModel.put(SettingKey.COPYRIGHT_FONT, new Font("Dialog.bold", 1, 14));
		settingsModel.put(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.ONLY_LYRICS);
		settingsModel.put(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.TITLE_AND_LYRICS);
		settingsModel.put(SettingKey.BOTTOM_MARGIN, 25);
		settingsModel.put(SettingKey.LOGO_FILE, "/home/mathis/Bilder-Incoming/20170917-1927_img_0155.jpg");
		settingsModel.put(SettingKey.SHOW_TITLE, false);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XMLConverter.fromPersistableToXML(settingsModel, outputStream);
		String output = outputStream.toString();
		assertEquals(SETTINGS_XML, output);
	}
	
	@Test
	public void settingsFromXML() {
		SettingsModel model = XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(SETTINGS_XML.getBytes()));
		
		assertNotNull(model);
		assertTrue(model.isSet(SettingKey.BACKGROUND_COLOR));
		assertNotNull(model.get(SettingKey.BACKGROUND_COLOR, Color.class));
		assertTrue(model.isSet(SettingKey.COPYRIGHT_FONT));
		assertNotNull(model.get(SettingKey.COPYRIGHT_FONT, Font.class));
		assertTrue(model.isSet(SettingKey.SCREEN_2_CONTENTS));
		assertNotNull(model.get(SettingKey.SCREEN_2_CONTENTS, ScreenContentsEnum.class));
		assertTrue(model.isSet(SettingKey.SONG_LIST_FILTER));
		assertNotNull(model.get(SettingKey.SONG_LIST_FILTER, FilterTypeEnum.class));
		assertTrue(model.isSet(SettingKey.BOTTOM_MARGIN));
		assertNotNull(model.get(SettingKey.BOTTOM_MARGIN, Integer.class));
		assertTrue(model.isSet(SettingKey.LOGO_FILE));
		assertNotNull(model.get(SettingKey.LOGO_FILE, String.class));
		assertTrue(model.isSet(SettingKey.SHOW_TITLE));
		assertNotNull(model.get(SettingKey.SHOW_TITLE, Boolean.class));
	}
	
	@Test
	public void xmlFromSongs() {
		SongsModel model = new SongsModel();
		
		Song song1 = new Song("094a9c6b-456c-405c-b8f7-7285c33d3c37");
		song1.setTitle("_Liebe Gäste, Liebe Geschwister");
		song1.setComposer("");
		song1.setAuthorText("");
		song1.setAuthorTranslation("");
		song1.setPublisher("");
		song1.setAdditionalCopyrightNotes("");
		song1.setSongNotes("");
		song1.setTonality("");
		song1.setChordSequence("");
		song1.setLyrics(SONG_1_LYRICS);
		model.addSong(song1);
		
		Song song2 = new Song("b867931a-d333-4e05-9847-f8ae80cdb5b2");
		song2.setTitle("10.000 Reasons");
		song2.setComposer("Matt Redman & Jonas Myrin");
		song2.setAuthorText("");
		song2.setAuthorTranslation("");
		song2.setPublisher("");
		song2.setAdditionalCopyrightNotes("");
		song2.setLanguage("English");
		song2.setSongNotes("");
		song2.setTonality("");
		song2.setChordSequence("");
		song2.setLyrics(SONG_2_LYRICS);
		model.addSong(song2);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		XMLConverter.fromPersistableToXML(model, outputStream);
		String output = outputStream.toString();
		assertEquals(SONGS_XML, output);
	}
	
	@Test
	public void songsFromXML() {
		SongsModel model = XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(SONGS_XML.getBytes()));
		
		assertNotNull(model);
		assertNotNull(model.getSongs());
		assertEquals(2, model.getSongs().size());
	}
	
}
