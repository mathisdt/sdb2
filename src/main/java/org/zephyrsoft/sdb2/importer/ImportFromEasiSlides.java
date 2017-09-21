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
package org.zephyrsoft.sdb2.importer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.importer.easislides.EasiSlides;
import org.zephyrsoft.sdb2.importer.easislides.EasiSlidesSong;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.util.StringTools;

import com.google.common.base.Joiner;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

/**
 * Importer from EasiSlides 4.0
 * 
 * @author Mathis Dirksen-Thedens
 */
public class ImportFromEasiSlides implements Importer {
	
	private static Logger LOG = LoggerFactory.getLogger(ImportFromEasiSlides.class);
	
	private static final String FILE_EXTENSION = ".xml";
	
	@Override
	public String getSourceName() {
		return "EasiSlides 4.0";
	}
	
	@Override
	public String getFileScheme() {
		return "*" + FILE_EXTENSION;
	}
	
	@Override
	public FileFilter getFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname != null && pathname.getName().endsWith(FILE_EXTENSION);
			}
		};
	}
	
	@Override
	public List<Song> loadFromFile(File inputFile) {
		LOG.debug("importing songs from file {}", inputFile.getAbsolutePath());
		
		try {
			XStream xstream = new XStream();
			// clear out existing permissions and set own ones
			xstream.addPermission(NoTypePermission.NONE);
			// allow some classes
			xstream.addPermission(NullPermission.NULL);
			xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
			xstream.allowTypeHierarchy(Collection.class);
			xstream.allowTypesByWildcard(new String[] { "java.lang.**", "java.util.**", "java.awt.**",
				"org.zephyrsoft.sdb2.**" });
			// aliases and omitted fields of model classes are defined via annotations
			xstream.processAnnotations(EasiSlides.class);
			xstream.processAnnotations(EasiSlidesSong.class);
			// unknown XML elements can be ignored (e.g. <linkedSongs> which was removed)
			xstream.ignoreUnknownElements();
			
			InputStream xmlInputStream = new FileInputStream(inputFile);
			EasiSlides easiSlides = (EasiSlides) xstream.fromXML(xmlInputStream);
			xmlInputStream.close();
			
			List<Song> songs = new LinkedList<>();
			
			// map EasiSlides items to SDB2 songs
			for (EasiSlidesSong easiSlidesSong : easiSlides.getEasiSlidesSongs()) {
				Song song = new Song(StringTools.createUUID());
				
				song.setTitle(easiSlidesSong.getTitle1());
				String lyrics = easiSlidesSong.getContents().replaceAll("([^\n])\n\\[", "$1\n\n[");
				song.setLyrics(lyrics);
				if (StringUtils.isBlank(easiSlidesSong.getWriter())) {
					easiSlidesSong.setWriter(null);
				}
				if (StringUtils.isBlank(easiSlidesSong.getCopyright())) {
					easiSlidesSong.setCopyright(null);
				}
				if (StringUtils.isBlank(easiSlidesSong.getLicenceAdmin1())) {
					easiSlidesSong.setLicenceAdmin1(null);
				}
				String copyrightNotes = Joiner.on(", ").skipNulls()
					.join(easiSlidesSong.getWriter(), easiSlidesSong.getCopyright(), easiSlidesSong.getLicenceAdmin1());
				song.setAdditionalCopyrightNotes(copyrightNotes);
				
				songs.add(song);
			}
			
			return songs;
		} catch (IOException e) {
			LOG.error("could not import songs from " + inputFile.getAbsolutePath());
			return null;
		}
	}
	
}
