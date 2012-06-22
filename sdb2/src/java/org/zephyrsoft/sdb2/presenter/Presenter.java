package org.zephyrsoft.sdb2.presenter;

/**
 * Control the presentation.
 * 
 * @author Mathis Dirksen-Thedens
 */
public interface Presenter {
	
	/**
	 * Show the presenter.
	 */
	void showPresenter();
	
	/**
	 * Hide the presenter.
	 */
	void hidePresenter();
	
	/**
	 * Start the transition to a specific part of the {@link Presentable}. This method should return immediately, even
	 * if the transition is not finished yet!
	 */
	void moveToPart(Integer part);
	
}
