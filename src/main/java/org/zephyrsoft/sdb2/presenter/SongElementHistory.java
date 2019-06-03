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
package org.zephyrsoft.sdb2.presenter;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.zephyrsoft.sdb2.model.SongElement;
import org.zephyrsoft.sdb2.model.SongElementEnum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Keeps track of song elements. Not necessarily thread-safe!
 *
 * @author Mathis Dirksen-Thedens
 */
public class SongElementHistory implements Iterable<SongElement> {
	
	private List<SongElement> elements;
	private List<SongElement> handedOut;
	
	public SongElementHistory(List<SongElement> elements) {
		this.elements = elements;
		handedOut = new ArrayList<>(elements.size());
	}
	
	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<SongElement> iterator() {
		Iterator<SongElement> internalIterator = elements.iterator();
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return internalIterator.hasNext();
			}
			
			@Override
			public SongElement next() {
				SongElement nextElement = internalIterator.next();
				handedOut.add(nextElement);
				return nextElement;
			}
		};
	}
	
	public SongElement previous() {
		return handedOut == null || handedOut.size() < 2
			? null
			: handedOut.get(handedOut.size() - 2);
	}
	
	public SongElement beforePrevious() {
		return handedOut == null || handedOut.size() < 3
			? null
			: handedOut.get(handedOut.size() - 3);
	}
	
	public SongElementHistoryQuery query() {
		return new SongElementHistoryQuery();
	}
	
	public class SongElementHistoryQuery {
		private Set<SongElementEnum> without = Sets.newHashSet();
		private List<List<SongElementEnum>> lastSeen = Lists.newArrayList();
		
		/** filter these types from history for this query (aggregated when called multiple times) */
		public SongElementHistoryQuery without(SongElementEnum... withoutTypes) {
			without.addAll(Sets.newHashSet(withoutTypes));
			return this;
		}
		
		/** were these types seen before the current element? (concatenated using OR when called multiple times) */
		public SongElementHistoryQuery lastSeen(SongElementEnum... lastSeenTypes) {
			lastSeen.add(Lists.newArrayList(lastSeenTypes));
			return this;
		}
		
		/** execute the query */
		public boolean end() {
			if (handedOut.size() == 0) {
				return false;
			}
			List<SongElement> workingCopy = Lists.newArrayList(handedOut);
			// remove last element as we want to know what was before it
			workingCopy.remove(workingCopy.size() - 1);
			// filter unwanted types and convert to types only
			List<SongElementEnum> types = workingCopy.stream()
				.filter(e -> !without.contains(e.getType()))
				.map(SongElement::getType)
				.collect(toList());
			// see if one of the type sequence matches
			for (List<SongElementEnum> sequence : lastSeen) {
				if (lengthOkAndEndOfFirstMatchesSecond(types, sequence)) {
					return true;
				}
			}
			return false;
		}
		
		private boolean lengthOkAndEndOfFirstMatchesSecond(List<SongElementEnum> types, List<SongElementEnum> sequence) {
			return types.size() >= sequence.size() && endOfFirstMatchesSecond(types, sequence);
		}
		
		private boolean endOfFirstMatchesSecond(List<SongElementEnum> types, List<SongElementEnum> sequence) {
			for (int i = 1; i <= sequence.size(); i++) {
				if (types.get(types.size() - i) != sequence.get(sequence.size() - i)) {
					return false;
				}
			}
			return true;
		}
	}
}
