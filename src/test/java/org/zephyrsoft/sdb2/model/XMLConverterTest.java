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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.model.statistics.StatisticsModel;

/**
 * Tests the behaviour of {@link XMLConverter}.
 */
public class XMLConverterTest {
	
	private static final String SONG_1_LYRICS = """
		Liebe Gäste, liebe Geschwister,
		bitte schaltet eure Handys während
		des Gottesdienstes aus oder auf lautlos.
		Danke!
		
		Dear guests, dear brothers and sisters
		please switch off or mute your cellphones
		during the service.
		Thank You!
		
		If you need translation
		please ask one of the ushers.
		""";
	
	private static final String SONG_2_LYRICS = """
		The sun comes up, it is a new day dawning,
		[Die Sonne geht auf, ein neuer Tag bricht an]
		It is time to sing Your song again.
		[Es ist wieder Zeit Dein Lied zu singen]
		Whatever may pass, and whatever lies before me,
		[Was immer passieren mag, was immer vor mir liegt]
		Let me be singing when the evening comes!
		[Lass mich singen wenn der Abend kommt]
		
		Bless the Lord, o my soul, o my soul,
		[Preis den Herrn, meine Seele, oh meine Seele]
		Worship His holy name!
		[Bete Seinen heiligen Namen an]
		Sing like never before, o my soul!
		[Sing wie nie zuvor, oh meine Seele!]
		I will worship His holy name.
		[Ich werde seinen heiligen Namen anbeten]
		
		...
		""";
	
	private static final String SONGS_XML = """
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<songs>
		    <song>
		        <additionalCopyrightNotes></additionalCopyrightNotes>
		        <authorText></authorText>
		        <authorTranslation></authorTranslation>
		        <chordSequence></chordSequence>
		        <composer></composer>
		        <lyrics>""" + SONG_1_LYRICS + """
		</lyrics>
		        <publisher></publisher>
		        <songNotes></songNotes>
		        <title>_Liebe Gäste, Liebe Geschwister</title>
		        <tonality></tonality>
		        <uuid>094a9c6b-456c-405c-b8f7-7285c33d3c37</uuid>
		    </song>
		    <song>
		        <additionalCopyrightNotes></additionalCopyrightNotes>
		        <authorText></authorText>
		        <authorTranslation></authorTranslation>
		        <chordSequence></chordSequence>
		        <composer>Matt Redman &amp; Jonas Myrin</composer>
		        <language>english</language>
		        <lyrics>""" + SONG_2_LYRICS + """
		</lyrics>
		        <publisher></publisher>
		        <songNotes></songNotes>
		        <title>10.000 Reasons</title>
		        <tonality></tonality>
		        <uuid>b867931a-d333-4e05-9847-f8ae80cdb5b2</uuid>
		    </song>
		</songs>
		""";
	
	private static final String STATISTICS_XML = """
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<statistics>
		    <songStatistics>
		        <presentedOn>
		            <date>2017-08-22</date>
		            <date>2017-08-24</date>
		        </presentedOn>
		        <songUuid>7d3ee81d-e69b-4d05-a257-2e51c0377ac5</songUuid>
		    </songStatistics>
		    <songStatistics>
		        <presentedOn>
		            <date>2017-11-21</date>
		        </presentedOn>
		        <songUuid>094a9c6b-456c-405c-b8f7-7285c33d3c37</songUuid>
		    </songStatistics>
		</statistics>
		""";
	
	private static final String SETTINGS_XML = """
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<settings>
		    <setting>
		        <key>BACKGROUND_COLOR</key>
		        <value class="awt-color">
		            <red>1</red>
		            <green>2</green>
		            <blue>3</blue>
		            <alpha>255</alpha>
		        </value>
		    </setting>
		    <setting>
		        <key>BOTTOM_MARGIN</key>
		        <value class="int">25</value>
		    </setting>
		    <setting>
		        <key>COPYRIGHT_FONT</key>
		        <value class="awt-font">
		            <name>Dialog.bold</name>
		            <style>1</style>
		            <size>14</size>
		        </value>
		    </setting>
		    <setting>
		        <key>LOGO_FILE</key>
		        <value class="string">/home/mathis/Bilder-Incoming/20170917-1927_img_0155.jpg</value>
		    </setting>
		    <setting>
		        <key>SCREEN_2_CONTENTS</key>
		        <value class="org.zephyrsoft.sdb2.model.ScreenContentsEnum">OnlyLyrics</value>
		    </setting>
		    <setting>
		        <key>SHOW_TITLE</key>
		        <value class="boolean">false</value>
		    </setting>
		    <setting>
		        <key>SONG_LIST_FILTER</key>
		        <value class="org.zephyrsoft.sdb2.model.FilterTypeEnum">TitleAndLyrics</value>
		    </setting>
		</settings>
		""";
	
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
		song2.setLanguage("english");
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
