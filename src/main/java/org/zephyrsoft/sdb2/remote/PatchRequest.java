package org.zephyrsoft.sdb2.remote;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.zephyrsoft.sdb2.model.Persistable;

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
