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
package org.zephyrsoft.sdb2.util.converter;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zephyrsoft.sdb2.model.FilterTypeEnum;
import org.zephyrsoft.sdb2.model.ScreenContentsEnum;
import org.zephyrsoft.sdb2.model.settings.Setting;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * XML adapter for the different values of {@link Setting}s.
 */
public class SettingValueAdapter extends XmlAdapter<Object, Object> {
	
	private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder = null;
	
	private Map<String, Function<Element, Object>> unmarshallers = new HashMap<>();
	private Map<Class<?>, Function<Object, Element>> marshallers = new HashMap<>();
	
	public SettingValueAdapter() {
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			throw new IllegalStateException("could not set up document builder");
		}
		
		unmarshallers.put("awt-color",
			e -> {
				Node nodeRed = find("red", e.getChildNodes());
				Node nodeGreen = find("green", e.getChildNodes());
				Node nodeBlue = find("blue", e.getChildNodes());
				Node nodeAlpha = find("alpha", e.getChildNodes());
				return new Color(intValue(nodeRed), intValue(nodeGreen), intValue(nodeBlue), intValue(nodeAlpha));
			});
		unmarshallers.put("awt-font",
			e -> {
				Node nodeName = find("name", e.getChildNodes());
				Node nodeStyle = find("style", e.getChildNodes());
				Node nodeSize = find("size", e.getChildNodes());
				return new Font(nodeName.getTextContent(), intValue(nodeStyle), intValue(nodeSize));
			});
		unmarshallers.put("org.zephyrsoft.sdb2.model.ScreenContentsEnum",
			e -> ScreenContentsEnum.withInternalName(e.getTextContent()));
		unmarshallers.put("org.zephyrsoft.sdb2.model.FilterTypeEnum",
			e -> FilterTypeEnum.withInternalName(e.getTextContent()));
		unmarshallers.put("int",
			e -> Integer.parseInt(e.getTextContent()));
		unmarshallers.put("string",
			e -> e.getTextContent());
		unmarshallers.put("boolean",
			e -> Boolean.valueOf(e.getTextContent()));
		
		marshallers.put(Color.class,
			e -> {
				Color v = (Color) e;
				Document doc = createDocument();
				Element node = create(doc);
				node.setAttribute("class", "awt-color");
				Node nodeRed = create(doc, "red");
				nodeRed.setTextContent(String.valueOf(v.getRed()));
				node.appendChild(nodeRed);
				Node nodeGreen = create(doc, "green");
				nodeGreen.setTextContent(String.valueOf(v.getGreen()));
				node.appendChild(nodeGreen);
				Node nodeBlue = create(doc, "blue");
				nodeBlue.setTextContent(String.valueOf(v.getBlue()));
				node.appendChild(nodeBlue);
				Node nodeAlpha = create(doc, "alpha");
				nodeAlpha.setTextContent(String.valueOf(v.getAlpha()));
				node.appendChild(nodeAlpha);
				return node;
			});
		marshallers.put(Font.class,
			e -> {
				Font v = (Font) e;
				Document doc = createDocument();
				Element node = create(doc);
				node.setAttribute("class", "awt-font");
				Node nodeName = create(doc, "name");
				nodeName.setTextContent(v.getName());
				node.appendChild(nodeName);
				Node nodeStyle = create(doc, "style");
				nodeStyle.setTextContent(String.valueOf(v.getStyle()));
				node.appendChild(nodeStyle);
				Node nodeSize = create(doc, "size");
				nodeSize.setTextContent(String.valueOf(v.getSize()));
				node.appendChild(nodeSize);
				return node;
			});
		marshallers.put(ScreenContentsEnum.class,
			e -> {
				ScreenContentsEnum v = (ScreenContentsEnum) e;
				Document doc = createDocument();
				Element node = create(doc);
				node.setAttribute("class", "org.zephyrsoft.sdb2.model.ScreenContentsEnum");
				node.setTextContent(v.getInternalName());
				return node;
			});
		marshallers.put(FilterTypeEnum.class,
			e -> {
				FilterTypeEnum v = (FilterTypeEnum) e;
				Document doc = createDocument();
				Element node = create(doc);
				node.setAttribute("class", "org.zephyrsoft.sdb2.model.FilterTypeEnum");
				node.setTextContent(v.getInternalName());
				return node;
			});
		marshallers.put(Integer.class,
			e -> {
				Integer v = (Integer) e;
				Document doc = createDocument();
				Element node = create(doc);
				node.setAttribute("class", "int");
				node.setTextContent(String.valueOf(v));
				return node;
			});
		marshallers.put(String.class,
			e -> {
				String v = (String) e;
				Document doc = createDocument();
				Element node = create(doc);
				node.setAttribute("class", "string");
				node.setTextContent(v);
				return node;
			});
		marshallers.put(Boolean.class,
			e -> {
				Boolean v = (Boolean) e;
				Document doc = createDocument();
				Element node = create(doc);
				node.setAttribute("class", "boolean");
				node.setTextContent(v.toString());
				return node;
			});
	}
	
	private int intValue(Node node) {
		return Integer.parseInt(node.getTextContent());
	}
	
	private Node find(String name, NodeList nodes) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(name)) {
				return node;
			}
		}
		return null;
	}
	
	private Document createDocument() {
		return builder.newDocument();
	}
	
	private Element create(Document doc) {
		return create(doc, "value");
	}
	
	private Element create(Document doc, String tagName) {
		return doc.createElement(tagName);
	}
	
	@Override
	public Object unmarshal(Object v) throws Exception {
		if (v instanceof Element) {
			return fromElement((Element) v);
		} else {
			return v;
		}
	}
	
	private Object fromElement(Element e) {
		String clazz = e.getAttribute("class");
		Function<Element, Object> function = unmarshallers.get(clazz);
		if (function == null) {
			throw new IllegalStateException("found no unmarshaller for setting value type \"" + clazz + "\"");
		}
		return function.apply(e);
	}
	
	@Override
	public Element marshal(Object v) throws Exception {
		if (v == null) {
			return null;
		}
		Class<?> clazz = v.getClass();
		Function<Object, Element> function = marshallers.get(clazz);
		if (function == null) {
			throw new IllegalStateException("found no marshaller for setting value type \"" + clazz + "\"");
		}
		return function.apply(v);
	}
	
}
