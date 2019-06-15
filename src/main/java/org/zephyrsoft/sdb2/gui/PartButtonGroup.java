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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.zephyrsoft.sdb2.model.AddressableLine;
import org.zephyrsoft.sdb2.model.AddressablePart;
import org.zephyrsoft.sdb2.presenter.Scroller;
import org.zephyrsoft.sdb2.presenter.UIScroller;

/**
 * Button group for an {@link AddressablePart}.
 */
public class PartButtonGroup extends JPanel {
	
	private static final long serialVersionUID = -3707530126058854873L;
	
	private static final int MARGIN_LEFT_RIGHT = 10;
	private static final int MARGIN_TOP_BOTTOM = 4;
	
	private Scroller scroller;
	private UIScroller uiScroller;
	private Integer partIndex;
	private AddressablePart part;
	private Color defaultForeground;
	private PartMarker partMarker;
	private JPanel lineButtons;
	
	private boolean active = false;
	private int activeLine = 0;
	
	public PartButtonGroup(AddressablePart part, Integer partIndex, Scroller scroller, UIScroller uiScroller) {
		this.part = part;
		this.partIndex = partIndex;
		this.scroller = scroller;
		this.uiScroller = uiScroller;
		
		setLayout(new BorderLayout());
		
		lineButtons = new JPanel();
		add(lineButtons, BorderLayout.CENTER);
		lineButtons.setLayout(new BoxLayout(lineButtons, BoxLayout.Y_AXIS));
		
		partMarker = new PartMarker(Color.WHITE, MARGIN_TOP_BOTTOM, this);
		defaultForeground = partMarker.getForeground();
		Dimension preferredSize = new Dimension(180, 10);
		partMarker.setPreferredSize(preferredSize);
		add(partMarker, BorderLayout.WEST);
		
		// add buttons for every line
		int lineIndex = 0;
		for (AddressableLine line : part) {
			addLineButton(lineIndex, line.getText());
			lineIndex++;
		}
		
		setBorder(createInactiveBorder());
		
		MouseAdapter mouseClickListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PartButtonGroup.this.scroller.moveToPart(PartButtonGroup.this.partIndex);
				setActiveLine(0);
			}
		};
		MouseAdapter mouseOverListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				partMarker.setActive(true);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				partMarker.setActive(false);
			}
		};
		partMarker.addMouseListener(mouseOverListener);
		partMarker.addMouseListener(mouseClickListener);
		addMouseListener(mouseOverListener);
		addMouseListener(mouseClickListener);
	}
	
	private void addLineButton(final Integer lineIndex, final String lineText) {
		final JLabel lineElement = new JLabel();
		lineElement.setBorder(createInactiveBorder());
		lineElement.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PartButtonGroup.this.scroller.moveToLine(PartButtonGroup.this.partIndex, lineIndex);
				setActiveLine(lineIndex);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				setHighlighted(lineElement, true);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				setHighlighted(lineElement, false);
			}
		});
		lineElement.setText(lineText);
		lineButtons.add(lineElement);
	}
	
	private void setHighlighted(JComponent component, boolean highlighted) {
		if (highlighted) {
			component.setBorder(createActiveBorder());
		} else {
			component.setBorder(createInactiveBorder());
		}
	}
	
	private Border createInactiveBorder() {
		return BorderFactory.createEmptyBorder(MARGIN_TOP_BOTTOM, MARGIN_LEFT_RIGHT, MARGIN_TOP_BOTTOM, MARGIN_LEFT_RIGHT);
	}
	
	private Border createActiveBorder() {
		return BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(MARGIN_TOP_BOTTOM / 2, MARGIN_LEFT_RIGHT / 2, MARGIN_TOP_BOTTOM / 2, MARGIN_LEFT_RIGHT / 2, Color.WHITE),
			BorderFactory.createEmptyBorder(MARGIN_TOP_BOTTOM / 2, MARGIN_LEFT_RIGHT / 2, MARGIN_TOP_BOTTOM / 2, MARGIN_LEFT_RIGHT / 2));
	}
	
	protected void setActiveLine(int lineIndex) {
		for (PartButtonGroup group : uiScroller.getUIParts()) {
			group.setInactive();
		}
		active = true;
		this.activeLine = lineIndex;
		repaint();
	}
	
	public void setInactive() {
		if (active) {
			active = false;
			repaint();
		}
	}
	
	protected int getActiveLine() {
		return activeLine;
	}
	
	protected boolean isActive() {
		return active;
	}
}
