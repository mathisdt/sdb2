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
package org.zephyrsoft.sdb2.remote;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.presenter.Presentable;
import org.zephyrsoft.sdb2.presenter.Presenter;

/**
 * The presentation display for the lyrics.
 */
public class RemotePresenter implements Presenter {
	private static final long serialVersionUID = -2390462756699128439L;
	
	private static final Logger LOG = LoggerFactory.getLogger(RemotePresenter.class);
	
	private Presentable presentable;
	private final RemoteController remoteController;
	
	// private ArrayList parts;
	
	public RemotePresenter(RemoteController remoteController) {
		this.remoteController = remoteController;
	}
	
	@Override
	public void showPresenter() {
		// call setContent with a song instead
	}
	
	@Override
	public void hidePresenter() {
		// call setContent with null song instead
	}
	
	@Override
	public void disposePresenter() {
		LOG.debug("disposing presenter for {}", presentable);
	}
	
	@Override
	public void moveToPart(Integer part) {
		moveToLine(part, 0);
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		if (presentable.getSong() != null) {
			remoteController.getPosition().set(new Position(presentable.getSong().getUUID(), part, line));
		}
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
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presenter#setContent(org.zephyrsoft.sdb2.presenter.Presentable)
	 */
	@Override
	public void setContent(Presentable presentable) {
		this.presentable = presentable;
		
		if (presentable.getSong() != null) {
			remoteController.getSong().set(presentable.getSong());
			remoteController.getPosition().set(new Position(presentable.getSong().getUUID(), 0, 0));
		} else if (presentable.getImage() != null) {
			LOG.warn("Images are not presented over remote");
		} else {
			// display a blank screen: // hidePresenter();
			remoteController.getPosition().set(new Position(remoteController.getPosition().get(), Position.Visibility.BLANK));
		}
	}
	
	/**
	 * @return
	 */
	public RemoteController getRemoteController() {
		return this.remoteController;
	}
}
