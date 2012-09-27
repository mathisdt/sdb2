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
package org.zephyrsoft.sdb2.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.zephyrsoft.sdb2.model.AddressablePart;

/**
 * Provide control over any number of running presenter windows.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class PresenterBundle implements Presenter {
	
	private final List<Presenter> presenters;
	
	public PresenterBundle() {
		presenters = new ArrayList<>();
	}
	
	public PresenterBundle(Presenter... presenters) {
		this.presenters = Arrays.asList(presenters);
	}
	
	public void addPresenter(Presenter presenter) {
		presenters.add(presenter);
	}
	
	@Override
	public void showPresenter() {
		for (Presenter presenter : presenters) {
			presenter.showPresenter();
		}
	}
	
	@Override
	public void hidePresenter() {
		for (Presenter presenter : presenters) {
			presenter.hidePresenter();
		}
	}
	
	@Override
	public List<AddressablePart> getParts() {
		if (presenters.isEmpty()) {
			throw new IllegalStateException("there has to be at least one real presenter in a presenter bundle");
		}
		return presenters.get(0).getParts();
	}
	
	@Override
	public void moveToPart(Integer part) {
		for (Presenter presenter : presenters) {
			presenter.moveToPart(part);
		}
	}
	
	@Override
	public void moveToLine(Integer part, Integer line) {
		for (Presenter presenter : presenters) {
			presenter.moveToLine(part, line);
		}
	}
	
	public int size() {
		return presenters.size();
	}
}
