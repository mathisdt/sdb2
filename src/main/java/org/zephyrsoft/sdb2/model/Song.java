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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.zephyrsoft.sdb2.util.StringTools;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a song.
 */
@XmlRootElement(name = "song")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Song implements Serializable, Comparable<Song>, Persistable {
	
	private static final long serialVersionUID = -7133402923581521674L;
	
	@XmlElement(name = "title")
	private String title;
	@XmlElement(name = "composer")
	private String composer;
	@XmlElement(name = "authorText")
	private String authorText;
	@XmlElement(name = "authorTranslation")
	private String authorTranslation;
	@XmlElement(name = "publisher")
	private String publisher;
	@XmlElement(name = "additionalCopyrightNotes")
	private String additionalCopyrightNotes;
	@XmlElement(name = "language")
	private String language;
	@XmlElement(name = "songNotes")
	private String songNotes;
	@XmlElement(name = "tonality")
	private String tonality;
	@XmlElement(name = "uuid")
	private String uuid;
	@XmlElement(name = "chordSequence")
	private String chordSequence;
	@XmlElement(name = "drumNotes")
	private String drumNotes;
	@XmlElement(name = "tempo")
	private String tempo;
	@XmlElement(name = "lyrics")
	private String lyrics;
	@XmlElement(name = "image")
	private String image;
	/**
	 * in degrees to rotate right
	 */
	@XmlElement(name = "imageRotation")
	private String imageRotation;

	/**
	 * Create a song instance. CAUTION: every song has to have a UUID! This constructor is only necessary for
	 * unmarshalling from XML.
	 */
	public Song() {
		initIfNecessary();
		// default constructor
	}
	
	public Song(Map<String, String> map) {
		this();
		this.fromMap(map);
	}
	
	public Song(Song song) {
		this();
		title = song.getTitle();
		composer = song.getComposer();
		authorText = song.getAuthorText();
		authorTranslation = song.getAuthorTranslation();
		publisher = song.getPublisher();
		additionalCopyrightNotes = song.getAdditionalCopyrightNotes();
		language = song.getLanguage();
		songNotes = song.getSongNotes();
		tonality = song.getTonality();
		uuid = song.getUUID();
		chordSequence = song.getChordSequence();
		drumNotes = song.getDrumNotes();
		tempo = song.getTempo();
		lyrics = song.getLyrics();
		image = song.getImage();
		imageRotation = song.getImageRotation();
	}
	
	/**
	 * Create a song instance.
	 *
	 * @param uuid
	 *            a UUID for this song
	 * @see StringTools#createUUID()
	 */
	public Song(String uuid) {
		this();
		this.uuid = uuid;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getComposer() {
		return composer;
	}
	
	public String getAuthorText() {
		return authorText;
	}
	
	public String getAuthorTranslation() {
		return authorTranslation;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public String getAdditionalCopyrightNotes() {
		return additionalCopyrightNotes;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getSongNotes() {
		return songNotes;
	}
	
	public String getLyrics() {
		return lyrics;
	}
	
	public String getTonality() {
		return tonality;
	}
	
	public String getTempo() {
		return tempo;
	}
	
	public String getChordSequence() {
		return chordSequence;
	}
	
	public String getDrumNotes() {
		return drumNotes;
	}
	
	public String getImage() {
		return image;
	}

	public String getImageRotation() {
		return imageRotation;
	}

	public int getImageRotationAsInt() {
		return imageRotation == null ? 0 : Integer.parseInt(imageRotation);
	}

	public String getCleanChordSequence() {
		return chordSequence == null
			? null
			: chordSequence.replaceAll("^\\p{Space}+", "").replaceAll("\\p{Space}+$", "");
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setComposer(String composer) {
		this.composer = composer;
	}
	
	public void setAuthorText(String authorText) {
		this.authorText = authorText;
	}
	
	public void setAuthorTranslation(String authorTranslation) {
		this.authorTranslation = authorTranslation;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	public void setAdditionalCopyrightNotes(String additionalCopyrightNotes) {
		this.additionalCopyrightNotes = additionalCopyrightNotes;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public void setSongNotes(String songNotes) {
		this.songNotes = songNotes;
	}
	
	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}
	
	public void setTonality(String tonality) {
		this.tonality = tonality;
	}
	
	public void setChordSequence(String chordSequence) {
		this.chordSequence = chordSequence;
	}
	
	public void setImage(String image) {
		this.image = image;
	}

	public void setImageRotation(String imageRotation) {
		this.imageRotation = imageRotation;
	}

	public void setImageRotationAsInt(int imageRotation) {
		this.imageRotation = String.valueOf(imageRotation);
	}

	public String getUUID() {
		return uuid;
	}
	
	public void setTempo(String tempo) {
		this.tempo = tempo;
	}
	
	public void setDrumNotes(String drumNotes) {
		this.drumNotes = drumNotes;
	}
	
	private void setUUID(String uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public int compareTo(Song o) {
		int ret = 0;
		
		ret = StringTools.compareLocaleBasedWithNullFirst(getTitle(), o.getTitle());
		if (ret != 0) {
			return ret;
		}
		
		ret = StringTools.compareLocaleBasedWithNullFirst(getLyrics(), o.getLyrics());
		if (ret != 0) {
			return ret;
		}
		
		ret = StringTools.compareLocaleBasedWithNullFirst(getChordSequence(), o.getChordSequence());
		if (ret != 0) {
			return ret;
		}
		
		return ret;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chordSequence == null) ? 0 : chordSequence.hashCode());
		result = prime * result + ((lyrics == null) ? 0 : lyrics.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Song other = (Song) obj;
		return equalsAllowNull(uuid, other.uuid) &&
			equalsAllowNull(title, other.title) &&
			equalsAllowNull(composer, other.composer) &&
			equalsAllowNull(authorText, other.authorText) &&
			equalsAllowNull(authorTranslation, other.authorTranslation) &&
			equalsAllowNull(publisher, other.publisher) &&
			equalsAllowNull(additionalCopyrightNotes, other.additionalCopyrightNotes) &&
			equalsAllowNull(language, other.language) &&
			equalsAllowNull(songNotes, other.songNotes) &&
			equalsAllowNull(tonality, other.tonality) &&
			equalsAllowNull(chordSequence, other.chordSequence) &&
			equalsAllowNull(drumNotes, other.drumNotes) &&
			equalsAllowNull(tempo, other.tempo) &&
			equalsAllowNull(lyrics, other.lyrics) &&
			equalsAllowNull(image, other.image) &&
			equalsAllowNull(imageRotation, other.imageRotation);
	}
	
	@Override
	public String toString() {
		return "SONG[" + title + "|" + uuid + "]";
	}
	
	private static boolean equalsAllowNull(String str, String str2) {
		if (str == null || str.isEmpty()) {
			if (str2 != null && !str2.isEmpty()) {
				return false;
			}
		} else if (!str.equals(str2)) {
			return false;
		}
		return true;
	}
	
	private static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}
	
	public boolean isEmpty() {
		return isEmpty(getTitle())
			&& isEmpty(getComposer())
			&& isEmpty(getAuthorText())
			&& isEmpty(getAuthorTranslation())
			&& isEmpty(getPublisher())
			&& isEmpty(getAdditionalCopyrightNotes())
			&& isEmpty(getLanguage())
			&& isEmpty(getSongNotes())
			&& isEmpty(getLyrics())
			&& isEmpty(getTonality())
			&& isEmpty(getTempo())
			&& isEmpty(getDrumNotes())
			&& isEmpty(getChordSequence())
			&& isEmpty(getImage())
			&& isEmpty(getImageRotation());
	}
	
	public Map<String, String> toMap() {
		return new HashMap<>() {
			private static final long serialVersionUID = -4450093906395824130L;
			{
				put("uuid", getUUID());
				put("title", getTitle());
				put("composer", getComposer());
				put("authorText", getAuthorText());
				put("authorTranslation", getAuthorTranslation());
				put("publisher", getPublisher());
				put("additionalCopyrightNotes", getAdditionalCopyrightNotes());
				put("language", getLanguage()); // .getInternalName()
				put("songNotes", getSongNotes());
				put("lyrics", getLyrics());
				put("tonality", getTonality());
				put("tempo", getTempo());
				put("drumNotes", getDrumNotes());
				put("chordSequence", getChordSequence());
				put("image", getImage());
				put("imageRotation", getImageRotation());
			}
		};
	}
	
	public void fromMap(Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String value = entry.getValue();
			switch (entry.getKey()) {
				case "uuid":
					setUUID(value);
					break;
				case "title":
					setTitle(value);
					break;
				case "composer":
					setComposer(value);
					break;
				case "authorText":
					setAuthorText(value);
					break;
				case "authorTranslation":
					setAuthorTranslation(value);
					break;
				case "publisher":
					setPublisher(value);
					break;
				case "additionalCopyrightNotes":
					setAdditionalCopyrightNotes(value);
					break;
				case "language":
					setLanguage(value);
					break;
				case "songNotes":
					setSongNotes(value);
					break;
				case "lyrics":
					setLyrics(value);
					break;
				case "tonality":
					setTonality(value);
					break;
				case "tempo":
					setTempo(value);
					break;
				case "drumNotes":
					setDrumNotes(value);
					break;
				case "chordSequence":
					setChordSequence(value);
					break;
				case "image":
					setImage(value);
					break;
				case "imageRotation":
					setImageRotation(value);
					break;
			}
		}
	}
	
	@Override
	public void initIfNecessary() {
		// nothing to do
	}
}
