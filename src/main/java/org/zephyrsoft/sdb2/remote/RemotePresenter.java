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
package org.zephyrsoft.sdb2.remote;

import java.util.List;
import java.util.Timer;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.model.settings.SettingsModel;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.Presenter;

/**
 * The presentation display for the lyrics.
 */
public class RemotePresenter implements Presenter {
	private static final long serialVersionUID = -2390462756699128439L;
	
	private static final Logger LOG = LoggerFactory.getLogger(RemotePresenter.class);
	
	private final SettingsModel settings;
	private final Presentable presentable;
	private final RemoteController remoteController;
	
	// If multiple Instances are used, only the last one can hide content.
	private static RemotePresenter activePresenter;
	
	// private ArrayList parts;
	
	public RemotePresenter(Presentable presentable, SettingsModel settings, RemoteController remoteController) {
		this.presentable = presentable;
		this.settings = settings;
		this.remoteController = remoteController;
		
		activePresenter = this;
		prepareContent();
	}
	
	private void prepareContent() {
		if (presentable.getSong() != null) {
			this.remoteController.getSong().set(presentable.getSong());
			// TODO FAILS if directly called and presenter views does not exist. we should remove the observers
			// temporary instead.
			new Timer().schedule(
				new java.util.TimerTask() {
					@Override
					public void run() {
						RemotePresenter.this.remoteController.getSongPosition().set(new SongPosition(0, 0));
					}
				}, 100);
		} else if (presentable.getImage() != null) {
			// Not supported jet
		} else {
			// display a blank screen: only set the background color (already done)
		}
		// TODO if necessary: hide cursor above every child of the content pane
		
	}
	
	@Override
	public void showPresenter() {
		this.remoteController.getVisible().set(Boolean.TRUE);
	}
	
	@Override
	public void hidePresenter() {
		if (this == activePresenter) {
			this.remoteController.getVisible().set(Boolean.FALSE);
		}
	}
	
	@Override
	public void disposePresenter() {
		LOG.debug("disposing presenter for {}", presentable);
	}
	
	@Override
	public void moveToPart(Integer part) {
		this.moveToLine(part, 0);
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		// TODO Select song first: RemoteController.getInstance().getSong().set(presentable.getSong());
		this.remoteController.getSongPosition().set(new SongPosition(part, line));
	}
	
	@Override
	public List<AddressablePart> getParts() {
		throw new NotImplementedException("RemotePresenter can't be used solo yet. Add a screen first.");
		// if (songView != null) {
		// return songView.getParts();
		// } else {
		// throw new IllegalStateException("it seems there is no song to display");
		// }
	}
}
