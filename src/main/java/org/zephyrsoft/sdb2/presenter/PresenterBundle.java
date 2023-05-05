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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.remote.RemotePresenter;

/**
 * Provide control over any number of running presenter windows.
 */
public class PresenterBundle implements Presenter {
	
	private final List<Presenter> presenters = new ArrayList<>();
	
	public void addPresenter(Presenter presenter) {
		presenters.add(presenter);
	}
	
	public List<Presenter> getPresenters() {
		return Collections.unmodifiableList(presenters);
	}
	
	public List<Presenter> getScreenPresenters() {
		return Collections.unmodifiableList(presenters.stream()
			.filter(p -> !(p instanceof RemotePresenter))
			.toList());
	}
	
	@Override
	public void setContent(Presentable presentable, PresentationPosition presentationPosition) {
		for (Presenter presenter : presenters) {
			presenter.setContent(presentable, presentationPosition);
		}
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
	public void disposePresenter() {
		for (Presenter presenter : presenters) {
			presenter.disposePresenter();
		}
	}
	
	@Override
	public boolean hasParts() {
		return presenters.stream().anyMatch(Presenter::hasParts);
	}
	
	@Override
	public List<AddressablePart> getParts() {
		if (presenters.isEmpty()) {
			throw new IllegalStateException("there has to be at least one real presenter in a presenter bundle");
		} else if (presenters.stream().noneMatch(Presenter::hasParts)) {
			throw new IllegalStateException("no presenter did contain any parts");
		}
		return presenters.stream()
			.filter(Scroller::hasParts)
			.findAny()
			.map(Scroller::getParts)
			.orElseThrow(() -> new IllegalStateException("no presenter did contain any parts (although at least one did moments ago)"));
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
	
	@Override
	public void moveTo(SongPresentationPosition position) {
		for (Presenter presenter : presenters) {
			presenter.moveTo(position);
		}
	}
	
	public boolean isEmpty() {
		return presenters.isEmpty();
	}
	
	public void removeIf(Predicate<Presenter> filter) {
		presenters.removeIf(filter);
	}
}
