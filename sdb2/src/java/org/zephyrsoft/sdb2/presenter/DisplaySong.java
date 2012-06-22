package org.zephyrsoft.sdb2.presenter;

import java.awt.Image;
import java.util.List;
import org.zephyrsoft.sdb2.model.Song;

/**
 * The representation of a {@link Song} for presentation.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class DisplaySong implements Presentable, Movable {
	
	private Song song;
	
	public DisplaySong(Song song) {
		this.song = song;
		// TODO split text to parts that can be addressed
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presentable#getText()
	 */
	@Override
	public String getText() {
		// TODO respect display content selection in the settings: maybe cut out the chord lines
		return song.getLyrics();
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presentable#getBackground()
	 */
	@Override
	public Image getBackground() {
		// no image
		return null;
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Movable#getParts()
	 */
	@Override
	public List<String> getParts() {
		// TODO
		return null;
	}
	
}
