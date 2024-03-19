package org.zephyrsoft.sdb2.remote;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.zephyrsoft.sdb2.FileAndDirectoryLocations;
import org.zephyrsoft.sdb2.model.Song;
import org.zephyrsoft.sdb2.model.SongsModel;
import org.zephyrsoft.sdb2.remote.MqttObject.OnChangeListener;
import org.zephyrsoft.sdb2.util.StringTools;

public class FileController {
	
	private RemoteController remoteController;
	
	public FileController(RemoteController remoteController) {
		this.remoteController = remoteController;
		this.remoteController.getFilesRequestFile().onRemoteChange((data, args) -> {
			try {
				Files.createDirectories(Paths.get(FileAndDirectoryLocations.getDBBlobDir()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Files.write(Paths.get(FileAndDirectoryLocations.getDBBlobDir(), (String) args[0]), data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	private Collection<String> getMissingFiles(Song song){
		HashSet<String> missingFiles = new HashSet<>();
		
		if(!StringTools.isEmpty(song.getImage()) && URI.create(song.getImage()).getScheme().equals("sdb")) {
			Path path = Paths.get(FileAndDirectoryLocations.getDBBlobDir(), song.getImage().replace("sdb://", ""));
			if (!Files.isRegularFile(path)) {
				missingFiles.add(path.getFileName().toString());
			}
		}
		return missingFiles;
	}
	
	public void downloadFiles(Song song, Runnable callback) {
		this.downloadFiles(getMissingFiles(song), callback);
	}
	
	public void downloadFiles(SongsModel songsModel, Runnable callback) {
		HashSet<String> missingFiles = new HashSet<>();
		
		for (Song song: songsModel.getSongs()) {
			missingFiles.addAll(getMissingFiles(song));
		}
		
		this.downloadFiles(missingFiles, callback);
	}
	
	public void downloadFiles(Collection<String> files, Runnable callback) {
		if(files.isEmpty()) {
			callback.run();
			return;
		}
		HashSet<String> missingFiles = new HashSet<>(files);
		
		this.remoteController.getFilesRequestFile().onRemoteChange(new OnChangeListener<byte[]>() {
			@Override
			public void onChange(byte[] object, Object... args) {
				String fileName = (String)args[0];
				if ( missingFiles.contains(fileName)){
					missingFiles.remove(fileName);
					if (missingFiles.isEmpty()) {
						callback.run();
						remoteController.getFilesRequestFile().removeOnRemoteChangeListener(this);
					}
				}
			}
		});
		missingFiles.forEach((fileName) -> remoteController.getFilesRequestGet().set(new FileRequest(fileName)));
	}

	
	public void uploadFiles(SongsModel songsModel, Consumer<SongsModel> callback) {
		HashSet<String> localFiles = new HashSet<>();
		
		for (Song song: songsModel.getSongs()) {
			if(!StringTools.isEmpty(song.getImage()) && URI.create(song.getImage()).getScheme().equals("file")) {
				Path path = Paths.get(URI.create(song.getImage()));
				String oldFilename = path.getFileName().toString();
				Optional<String> fileExtension = Optional.ofNullable(oldFilename)
					      .filter(f -> f.contains("."))
					      .map(f -> f.substring(oldFilename.lastIndexOf(".")));
				String newFilename = StringTools.createUUID() + fileExtension.get();
				localFiles.add(newFilename);
				Path dbPath = Paths.get(FileAndDirectoryLocations.getDBBlobDir(), newFilename);
				try {
					Files.createDirectories(Paths.get(FileAndDirectoryLocations.getDBBlobDir()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					Files.copy(path, dbPath, StandardCopyOption.COPY_ATTRIBUTES);
				} catch (IOException e) {
					e.printStackTrace();
				}
				song.setImage("sdb://"+newFilename);
			}
		}
		if(localFiles.isEmpty()) {
			callback.accept(songsModel);
			return;
		}
		
		this.remoteController.getFilesRequestSetResponse().onRemoteChange(new OnChangeListener<FileSetResponse>() {
			@Override
			public void onChange(FileSetResponse object, Object... args) {
				String fileName = object.getUuid();
				if ( localFiles.contains(fileName) ){
					if(!object.isOk()) {
						System.err.println("Could not upload file " + fileName + " Reason: " + object.getReason());
						remoteController.getFilesRequestSetResponse().removeOnRemoteChangeListener(this);
					}else {
						localFiles.remove(fileName);
						if (localFiles.isEmpty()) {
							callback.accept(songsModel);
							remoteController.getFilesRequestSetResponse().removeOnRemoteChangeListener(this);
						}
					}
				}
			}
		});
		
		localFiles.forEach((fileName) -> {
			Path dbPath = Paths.get(FileAndDirectoryLocations.getDBBlobDir(), fileName);
			try {
				byte[] content = Files.readAllBytes(dbPath);
				remoteController.getFilesRequestSet().set(content, fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
