package org.zephyrsoft.sdb2.util;

import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.layout.renderer.TextRenderer;

/**
 * Doesn't trim leading spaces.
 */
public class TextRendererNonTrimming extends TextRenderer {
	
	public TextRendererNonTrimming(Text textElement) {
		super(textElement);
	}
	
	@Override
	public IRenderer getNextRenderer() {
		return new TextRendererNonTrimming((Text) getModelElement());
	}
	
	@Override
	public void trimFirst() {
		// don't trim!
	}
}
