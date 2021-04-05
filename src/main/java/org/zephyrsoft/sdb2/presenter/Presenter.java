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
package org.zephyrsoft.sdb2.presenter;

/**
 * Control the presentation.
 */
public interface Presenter extends Scroller {
	
	/**
	 * Set the content. For re-using an existing presenter.
	 */
	void setContent(Presentable presentable);
	
	/**
	 * Show the presenter.
	 */
	void showPresenter();
	
	/**
	 * Hide the presenter.
	 */
	void hidePresenter();
	
	/**
	 * Permanently dispose of the presenter. It cannot be shown again after calling this method.
	 */
	void disposePresenter();
	
}
