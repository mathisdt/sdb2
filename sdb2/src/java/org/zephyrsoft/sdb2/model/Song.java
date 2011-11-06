package org.zephyrsoft.sdb2.model;

import java.io.*;
import java.util.*;

/**
 * Representation of a song.
 * @author Mathis Dirksen-Thedens
 */
public class Song implements Serializable {
	
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
	
}
