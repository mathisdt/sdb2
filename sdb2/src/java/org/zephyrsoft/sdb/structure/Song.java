package org.zephyrsoft.sdb.structure;

import org.zephyrsoft.util.StringTools;

/**
 * Structure which holds one song.
 * 
 * @deprecated This is a LEGACY class from SDB v1 which was stripped down to the absolutely necessary bits!
 * @author Mathis Dirksen-Thedens
 */
@Deprecated
public class Song implements java.io.Serializable {
	
	private static final long serialVersionUID = 5931839542614942316L;
	
	public static final String SEPARATOR = "###";
	
	private int id = 0;
	private String titel = "";
	private String text = ""; // including guitar chords
	private String tonart = "";
	private String sprache = "";
	private String copyright = "";
	private String bemerkungen = "";
	
	public Song(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return titel;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public String getTonart() {
		return tonart;
	}
	
	public void setTonart(String text) {
		this.tonart = text;
	}
	
	public String getTitel() {
		return titel;
	}
	
	public void setTitel(String text) {
		this.titel = text;
	}
	
	public String getSprache() {
		return sprache;
	}
	
	public void setSprache(String text) {
		this.sprache = text;
	}
	
	public String getBemerkungen() {
		return bemerkungen;
	}
	
	public void setBemerkungen(String text) {
		this.bemerkungen = text;
	}
	
	public String getCopyright() {
		return StringTools.replace(StringTools.replace(copyright, "(c)", "\u00A9"), "(C)", "\u00A9");
	}
	
	public void setCopyright(String text) {
		this.copyright = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
