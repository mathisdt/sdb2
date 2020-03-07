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
package org.zephyrsoft.sdb2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for input/output operations.
 */
public class IOController {
	
	private static final Logger LOG = LoggerFactory.getLogger(IOController.class);
	
	private Instant ignoreUntil;
	private Boolean ignoreCompletely;
	
	public void startWatching(String fileName, Runnable callback) throws IOException {
		// only on first run:
		if (ignoreCompletely == null) {
			String fileNameToUse = FileAndDirectoryLocations.getSongsFileName(fileName);
			WatchService watchService = FileSystems.getDefault().newWatchService();
			Path file = Paths.get(fileNameToUse).toAbsolutePath().toRealPath();
			Path path = file.getParent();
			// as we first save the songs to a backup file and then copy it to the real destination,
			// we only need to watch for "create"
			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
			
			Runnable runnable = () -> {
				WatchKey key;
				try {
					ignoreUntil = Instant.now();
					while ((key = watchService.take()) != null) {
						boolean affectsSongsXml = false;
						for (WatchEvent<?> event : key.pollEvents()) {
							BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
							Instant fileTime = attr.lastModifiedTime().toInstant();
							if (ignoreCompletely != null && !ignoreCompletely
								&& event.context() instanceof Path && ((Path) event.context()).endsWith(file.getFileName())
								&& ignoreUntil.isBefore(fileTime)) {
								affectsSongsXml = true;
							}
						}
						key.reset();
						if (affectsSongsXml) {
							postponeIgnoredUntilByOneSecond();
							LOG.debug("detected changes to the songs file");
							callback.run();
						}
					}
				} catch (Exception e) {
					LOG.error("error while watching for changes in the songs file", e);
				}
			};
			
			Thread thread = new Thread(runnable, "file-change-watcher");
			thread.start();
			ignoreCompletely = Boolean.FALSE;
		} else {
			// illegal on any run but the first
			throw new IllegalStateException("startWatching() can only be called once");
		}
	}
	
	private void postponeIgnoredUntilByOneSecond() {
		ignoreUntil = Instant.now().plusSeconds(1);
	}
	
	public void startWatchingAgain() {
		if (ignoreCompletely == null) {
			// illegal on first run
			throw new IllegalStateException("startWatchingAgain() is only valid after an initial call to startWatching()");
		}
		postponeIgnoredUntilByOneSecond();
		ignoreCompletely = Boolean.FALSE;
	}
	
	public void stopWatching() {
		ignoreCompletely = Boolean.TRUE;
	}
	
	public <T> T readSongs(String fileName, Function<InputStream, T> handler) {
		String fileNameToUse = FileAndDirectoryLocations.getSongsFileName(fileName);
		File file = new File(fileNameToUse);
		LOG.debug("reading songs from {}", file.getAbsolutePath());
		T result = null;
		try (InputStream xmlInputStream = new FileInputStream(file)) {
			result = handler.apply(xmlInputStream);
		} catch (IOException e) {
			LOG.error("could not read songs from \"" + file.getAbsolutePath() + "\"", e);
		}
		return result;
	}
	
	public <T> T readSettings(Function<InputStream, T> handler) {
		File file = new File(FileAndDirectoryLocations.getSettingsFileName());
		LOG.debug("reading settings from {}", file.getAbsolutePath());
		T result = null;
		try (InputStream xmlInputStream = new FileInputStream(file)) {
			result = handler.apply(xmlInputStream);
		} catch (IOException e) {
			LOG.error("could not read settings from \"" + file.getAbsolutePath() + "\"", e);
		}
		return result;
	}
	
	public <T> T readStatistics(Function<InputStream, T> handler) {
		File file = new File(FileAndDirectoryLocations.getStatisticsFileName());
		LOG.debug("reading statistics from {}", file.getAbsolutePath());
		T result = null;
		try {
			InputStream xmlInputStream = new FileInputStream(file);
			result = handler.apply(xmlInputStream);
			xmlInputStream.close();
		} catch (IOException e) {
			LOG.error("could not read statistics from \"" + file.getAbsolutePath() + "\"");
		}
		return result;
	}
	
}
