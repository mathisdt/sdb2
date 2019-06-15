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
package org.zephyrsoft.sdb2.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.interpolators.AccelerationInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notification with fading effect.
 */
public class NotificationLabel extends JLabel implements TimingTarget {
	
	private static final long serialVersionUID = 7755120521341005608L;
	private static final Logger LOG = LoggerFactory.getLogger(NotificationLabel.class);
	
	float f_alpha = 0.0f; // current opacity
	Animator f_animator;
	BufferedImage f_image = null;
	
	private Container container;
	
	public NotificationLabel(String text, long millis, Container container) {
		super(text);
		this.container = container;
		
		setBackground(Color.WHITE);
		setOpaque(true);
		setFont(new Font("Default", Font.BOLD, 16));
		setBorder(BorderFactory.createLineBorder(Color.WHITE, 15));
		
		f_animator = new Animator.Builder()
			.addTarget(this)
			.setInterpolator(new AccelerationInterpolator(0.5, 0.5))
			.setDuration(millis, TimeUnit.MILLISECONDS)
			.build();
	}
	
	@Override
	public void paint(Graphics g) {
		// Create an image for the button graphics if necessary
		if (f_image == null || f_image.getWidth() != getWidth() || f_image.getHeight() != getHeight()) {
			f_image = getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight());
		}
		Graphics gButton = f_image.getGraphics();
		gButton.setClip(g.getClip());
		
		// Have the superclass render the button for us
		super.paint(gButton);
		
		// Make the graphics object sent to this paint() method translucent
		Graphics2D g2d = (Graphics2D) g;
		try {
			AlphaComposite newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f_alpha);
			g2d.setComposite(newComposite);
		} catch (IllegalArgumentException iae) {
			LOG.debug("illegal alpha value {}", f_alpha);
		}
		
		// Copy the button's image to the destination graphics, translucently
		g2d.drawImage(f_image, 0, 0, null);
	}
	
	@Override
	public void begin(Animator source) {
		// nothing to do
	}
	
	@Override
	public void end(Animator source) {
		container.remove(this);
	}
	
	@Override
	public void repeat(Animator source) {
		// nothing to do
	}
	
	@Override
	public void reverse(Animator source) {
		// nothing to do
	}
	
	/**
	 * This method sets the alpha of our button to be equal to the current elapsed
	 * fraction of the animation
	 */
	@Override
	public void timingEvent(Animator source, double fraction) {
		double fadeIn = 0.2;
		double fadeOut = 0.3;
		
		if (fraction <= fadeIn) {
			f_alpha = (float) (fraction / fadeIn);
			repaint();
			container.repaint();
		} else if (fraction >= (1 - fadeOut)) {
			f_alpha = (float) ((1 - fraction) / fadeOut);
			repaint();
			container.repaint();
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			f_animator.start();
		}
	}
	
}
