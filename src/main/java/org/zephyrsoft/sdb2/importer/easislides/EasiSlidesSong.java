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
package org.zephyrsoft.sdb2.importer.easislides;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Item")
public class EasiSlidesSong {
	
	@XStreamAlias("Title1")
	private String title1;
	
	@XStreamAlias("Contents")
	private String contents;
	
	@XStreamAlias("Writer")
	private String writer;
	
	@XStreamAlias("Copyright")
	private String copyright;
	
	@XStreamAlias("LicenceAdmin1")
	private String licenceAdmin1;
	
	/**
	 * @return the title1
	 */
	public String getTitle1() {
		return title1;
	}
	
	/**
	 * @param title1
	 *            the title1 to set
	 */
	public void setTitle1(String title1) {
		this.title1 = title1;
	}
	
	/**
	 * @return the contents
	 */
	public String getContents() {
		return contents;
	}
	
	/**
	 * @param contents
	 *            the contents to set
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}
	
	/**
	 * @return the writer
	 */
	public String getWriter() {
		return writer;
	}
	
	/**
	 * @param writer
	 *            the writer to set
	 */
	public void setWriter(String writer) {
		this.writer = writer;
	}
	
	/**
	 * @return the copyright
	 */
	public String getCopyright() {
		return copyright;
	}
	
	/**
	 * @param copyright
	 *            the copyright to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	/**
	 * @return the licenceAdmin1
	 */
	public String getLicenceAdmin1() {
		return licenceAdmin1;
	}
	
	/**
	 * @param licenceAdmin1
	 *            the licenceAdmin1 to set
	 */
	public void setLicenceAdmin1(String licenceAdmin1) {
		this.licenceAdmin1 = licenceAdmin1;
	}
	
}
