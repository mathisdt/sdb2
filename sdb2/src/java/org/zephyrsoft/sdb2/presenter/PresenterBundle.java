package org.zephyrsoft.sdb2.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presenter#showPresenter()
	 */
	@Override
	public void showPresenter() {
		for (Presenter presenter : presenters) {
			presenter.showPresenter();
		}
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presenter#hidePresenter()
	 */
	@Override
	public void hidePresenter() {
		for (Presenter presenter : presenters) {
			presenter.hidePresenter();
		}
	}
	
	/**
	 * @see org.zephyrsoft.sdb2.presenter.Presenter#moveToPart(java.lang.Integer)
	 */
	@Override
	public void moveToPart(Integer part) {
		for (Presenter presenter : presenters) {
			presenter.moveToPart(part);
		}
	}
}
