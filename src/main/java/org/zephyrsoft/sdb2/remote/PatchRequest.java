package org.zephyrsoft.sdb2.remote;

import org.zephyrsoft.sdb2.model.Persistable;

import jakarta.xml.bind.annotation.XmlAccessOrder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorOrder;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "patchRequest")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class PatchRequest implements Persistable {
	private static final long serialVersionUID = 8867566661007188776L;
	
	@XmlElement(name = "id")
	private long id;
	@XmlElement(name = "toID")
	private Long toID = null;
	
	public PatchRequest() {
		initIfNecessary();
	}
	
	public PatchRequest(long id) {
		this();
		this.id = id;
		this.toID = null;
	}
	
	public PatchRequest(long id, long toID) {
		this();
		this.id = id;
		this.toID = toID;
	}
	
	@Override
	public void initIfNecessary() {
		id = 1;
		toID = null;
	}
}
