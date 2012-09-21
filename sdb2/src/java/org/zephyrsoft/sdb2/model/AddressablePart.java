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
package org.zephyrsoft.sdb2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A part that can be positioned. Consists of one or more {@link AddressableLine}s.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class AddressablePart implements Addressable {
	
	private List<AddressableLine> lines = new ArrayList<>();
	
	public int size() {
		return lines.size();
	}
	
	public boolean isEmpty() {
		return lines.isEmpty();
	}
	
	public boolean add(AddressableLine e) {
		return lines.add(e);
	}
	
	public AddressableLine get(int index) {
		return lines.get(index);
	}
	
	@Override
	public Integer getPosition() {
		if (isEmpty()) {
			throw new IllegalStateException("an AddressablePart must contain at least one AddressableLine");
		}
		return get(0).getPosition();
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("PART[");
		boolean isFirst = true;
		for (AddressableLine line : lines) {
			if (isFirst) {
				isFirst = false;
			} else {
				ret.append(", ");
			}
			ret.append(line.toString());
		}
		ret.append("]");
		return ret.toString();
	}
	
}
