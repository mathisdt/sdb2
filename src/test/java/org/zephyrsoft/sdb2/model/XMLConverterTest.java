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
package org.zephyrsoft.sdb2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.zephyrsoft.sdb2.model.settings.SettingKey;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.model.statistics.StatisticsModel;

/**
 * Tests the behaviour of {@link XMLConverter}.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class XMLConverterTest {
	
	private static final String SONGS_XML = "<songs>\n" +
		"  <autoSort>true</autoSort>\n" +
		"  <song>\n" +
		"    <title>_Liebe Gäste, Liebe Geschwister</title>\n" +
		"    <composer></composer>\n" +
		"    <authorText></authorText>\n" +
		"    <authorTranslation></authorTranslation>\n" +
		"    <publisher></publisher>\n" +
		"    <additionalCopyrightNotes></additionalCopyrightNotes>\n" +
		"    <songNotes></songNotes>\n" +
		"    <tonality></tonality>\n" +
		"    <uuid>094a9c6b-456c-405c-b8f7-7285c33d3c37</uuid>\n" +
		"    <chordSequence></chordSequence>\n" +
		"    <lyrics>Liebe Gäste, liebe Geschwister,\n" +
		"bitte schaltet eure Handys während\n" +
		"des Gottesdienstes aus oder auf lautlos.\n" +
		"Danke!\n" +
		"\n" +
		"Dear guests, dear brothers and sisters\n" +
		"please switch off or mute your cellphones\n" +
		"during the service.\n" +
		"Thank You!\n" +
		"\n" +
		"If you need translation \n" +
		"please ask one of the ushers.</lyrics>\n" +
		"  </song>\n" +
		"  <song>\n" +
		"    <title>10.000 Reasons</title>\n" +
		"    <composer>Matt Redman &amp; Jonas Myrin</composer>\n" +
		"    <authorText></authorText>\n" +
		"    <authorTranslation></authorTranslation>\n" +
		"    <publisher></publisher>\n" +
		"    <additionalCopyrightNotes></additionalCopyrightNotes>\n" +
		"    <language>english</language>\n" +
		"    <songNotes></songNotes>\n" +
		"    <tonality></tonality>\n" +
		"    <uuid>b867931a-d333-4e05-9847-f8ae80cdb5b2</uuid>\n" +
		"    <chordSequence></chordSequence>\n" +
		"    <lyrics>The sun comes up, it&apos;s a new day dawning,\n" +
		"[Die Sonne geht auf, ein neuer Tag bricht an]\n" +
		"It&apos;s time to sing Your song again.\n" +
		"[Es ist wieder Zeit Dein Lied zu singen]\n" +
		"Whatever may pass, and whatever lies before me,\n" +
		"[Was immer passieren mag, was immer vor mir liegt]\n" +
		"Let me be singing when the evening comes!\n" +
		"[Lass mich singen wenn der Abend kommt]\n" +
		"\n" +
		"Bless the Lord, o my soul, o my soul,\n" +
		"[Preis den Herrn, meine Seele, oh meine Seele]\n" +
		"Worship His holy name!\n" +
		"[Bete Seinen heiligen Namen an]\n" +
		"Sing like never before, o my soul!\n" +
		"[Sing wie nie zuvor, oh meine Seele!]\n" +
		"I&apos;ll worship His holy name.\n" +
		"[Ich werde seinen heiligen Namen anbeten]\n" +
		"\n" +
		"You&apos;re rich in love, and You&apos;re slow to anger.\n" +
		"[Du bist reich an Liebe und langsam zum Zorn]\n" +
		"Your name is great, and Your heart is kind.\n" +
		"[Dein Name ist groß und Dein Herz freundlich]\n" +
		"For all Your goodness I will keep on singing,\n" +
		"[Für all Deine Güte werde ich weiter singen]\n" +
		"Ten thousand reasons for my heart to find.\n" +
		"[Mein Herz findet 10.000 Gründe]\n" +
		"\n" +
		"And on that day when my strength is failing,\n" +
		"[Und an dem Tag an dem meine Kraft versagt]\n" +
		"The end draws near, and my time has come,\n" +
		"[Das Ende naht und meine Zeit ist gekommen]\n" +
		"Still my soul will sing Your praise unending\n" +
		"[Meine Seele wird noch immer Dein Lob singen]\n" +
		"Ten thousand years and then forevermore.\n" +
		"[10.000 Jahre und bis in Ewigkeit]</lyrics>\n" +
		"  </song>\n" +
		"</songs>";
	
	private static final String STATISTICS_XML = "<statistics>\n" +
		"  <songStatistics>\n" +
		"    <songUuid>7d3ee81d-e69b-4d05-a257-2e51c0377ac5</songUuid>\n" +
		"    <presentedOn>\n" +
		"      <date>\n" +
		"        <year>2017</year>\n" +
		"        <month>8</month>\n" +
		"        <day>22</day>\n" +
		"      </date>\n" +
		"    </presentedOn>\n" +
		"    <presentedOn>\n" +
		"      <date>\n" +
		"        <year>2017</year>\n" +
		"        <month>8</month>\n" +
		"        <day>24</day>\n" +
		"      </date>\n" +
		"    </presentedOn>\n" +
		"  </songStatistics>\n" +
		"  <songStatistics>\n" +
		"    <songUuid>094a9c6b-456c-405c-b8f7-7285c33d3c37</songUuid>\n" +
		"    <presentedOn>\n" +
		"      <date>\n" +
		"        <year>2017</year>\n" +
		"        <month>11</month>\n" +
		"        <day>21</day>\n" +
		"      </date>\n" +
		"    </presentedOn>\n" +
		"  </songStatistics>\n" +
		"</statistics>";
	
	private static final String SETTINGS_XML = "<settings>\n" +
		"  <setting>\n" +
		"    <key>BACKGROUND_COLOR</key>\n" +
		"    <value class=\"awt-color\">\n" +
		"      <red>0</red>\n" +
		"      <green>0</green>\n" +
		"      <blue>0</blue>\n" +
		"      <alpha>255</alpha>\n" +
		"    </value>\n" +
		"  </setting>\n" +
		"  <setting>\n" +
		"    <key>BOTTOM_MARGIN</key>\n" +
		"    <value class=\"int\">25</value>\n" +
		"  </setting>\n" +
		"  <setting>\n" +
		"    <key>COPYRIGHT_FONT</key>\n" +
		"    <value class=\"awt-font\">\n" +
		"      <name>Dialog.bold</name>\n" +
		"      <style>1</style>\n" +
		"      <size>14</size>\n" +
		"    </value>\n" +
		"  </setting>\n" +
		"  <setting>\n" +
		"    <key>DISTANCE_TEXT_COPYRIGHT</key>\n" +
		"    <value class=\"int\">50</value>\n" +
		"  </setting>\n" +
		"  <setting>\n" +
		"    <key>DISTANCE_TITLE_TEXT</key>\n" +
		"    <value class=\"int\">15</value>\n" +
		"  </setting>\n" +
		"  <setting>\n" +
		"    <key>LEFT_MARGIN</key>\n" +
		"    <value class=\"int\">0</value>\n" +
		"  </setting>\n" +
		"  <setting>\n" +
		"    <key>LOGO_FILE</key>\n" +
		"    <value class=\"string\">/home/mathis/Bilder-Incoming/20170917-1927_img_0155.jpg</value>\n" +
		"  </setting>\n" +
		"</settings>";
	
	@Test
	public void xmlFromStatistics() {
		// TODO
		fail();
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
		// TODO
		fail();
	}
	
	@Test
	public void settingsFromXML() {
		SettingsModel model = XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(SETTINGS_XML.getBytes()));
		
		assertNotNull(model);
		assertTrue(model.isSet(SettingKey.BACKGROUND_COLOR));
		assertNotNull(model.get(SettingKey.BACKGROUND_COLOR, Color.class));
		assertTrue(model.isSet(SettingKey.COPYRIGHT_FONT));
		assertNotNull(model.get(SettingKey.COPYRIGHT_FONT, Font.class));
	}
	
	@Test
	public void xmlFromSongs() {
		// TODO
		fail();
	}
	
	@Test
	public void songsFromXML() {
		SongsModel model = XMLConverter.fromXMLToPersistable(new ByteArrayInputStream(SONGS_XML.getBytes()));
		
		assertNotNull(model);
		assertNotNull(model.getSongs());
		assertEquals(2, model.getSongs().size());
	}
	
}
