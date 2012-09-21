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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.zephyrsoft.util.StringTools;

/**
 * Representation of a song.
 * 
 * @author Mathis Dirksen-Thedens
 */
@XStreamAlias("song")
public class Song implements Serializable, Cloneable, Comparable<Song> {
	
	private static final long serialVersionUID = -7133402923581521674L;
	
	private String title;
	private String composer;
	private String authorText;
	private String authorTranslation;
	private String publisher;
	private String additionalCopyrightNotes;
	private LanguageEnum language;
	private String songNotes;
	private String lyrics;
	
	private String tonality;
	private String chordSequence;
	
	private List<Song> linkedSongs = new ArrayList<Song>();
	
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
	
	public LanguageEnum getLanguage() {
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
	
	public String getChordSequence() {
		return chordSequence;
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
	
	public void setLanguage(LanguageEnum language) {
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
	
	public Iterator<Song> linkedSongsIterator() {
		return linkedSongs.iterator();
	}
	
	public boolean addLinkedSong(Song e) {
		return linkedSongs.add(e);
	}
	
	public boolean removeLinkedSong(Song o) {
		return linkedSongs.remove(o);
	}
	
	public int linkedSongsSize() {
		return linkedSongs.size();
	}
	
	public Song getLinkedSong(int index) {
		return linkedSongs.get(index);
	}
	
	public List<Song> getLinkedSongs() {
		return linkedSongs;
	}
	
	public void setLinkedSongs(List<Song> linkedSongs) {
		this.linkedSongs.clear();
		if (linkedSongs != null) {
			this.linkedSongs.addAll(linkedSongs);
		}
	}
	
	@Override
	protected Song clone() throws CloneNotSupportedException {
		Song clone = new Song();
		
		// TODO
		
		return clone;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Song o) {
		int ret = 0;
		
		ret = StringTools.compareWithNullFirst(getTitle(), o.getTitle());
		if (ret != 0) {
			return ret;
		}
		
		ret = StringTools.compareWithNullFirst(getLyrics(), o.getLyrics());
		if (ret != 0) {
			return ret;
		}
		
		ret = StringTools.compareWithNullFirst(getChordSequence(), o.getChordSequence());
		if (ret != 0) {
			return ret;
		}
		
		return ret;
	}
	
	@Override
	public String toString() {
		return "SONG[" + title + "]";
	}
	
}
