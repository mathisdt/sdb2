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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
	
	public SongElement current() {
		return handedOut == null || handedOut.size() < 1
			? null
			: handedOut.get(handedOut.size() - 1);
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
	
	public SongElementHistoryQuery queryIncludingCurrent() {
		SongElementHistoryQuery query = new SongElementHistoryQuery();
		query.includeCurrent();
		return query;
	}
	
	public class SongElementHistoryQuery {
		private boolean includeCurrent = false;
		private Set<SongElementEnum> without = Sets.newHashSet();
		private List<List<SongElementMatcher>> lastSeen = Lists.newArrayList();
		
		public void includeCurrent() {
			includeCurrent = true;
		}
		
		/** filter these types from history for this query (aggregated when called multiple times) */
		public SongElementHistoryQuery without(SongElementEnum... withoutTypes) {
			without.addAll(Sets.newHashSet(withoutTypes));
			return this;
		}
		
		/** were these types seen before the current element? (concatenated using OR when called multiple times) */
		public SongElementHistoryQuery lastSeen(SongElementMatcher... lastSeenTypes) {
			lastSeen.add(Lists.newArrayList(lastSeenTypes));
			return this;
		}
		
		/** execute the query */
		public SongElementHistoryQueryResult end() {
			if (handedOut.size() == 0) {
				return SongElementHistoryQueryResult.NO_MATCH;
			}
			List<SongElement> workingCopy = Lists.newArrayList(handedOut);
			if (!includeCurrent) {
				// remove last element as we only want to know what was before it
				workingCopy.remove(workingCopy.size() - 1);
			}
			// filter unwanted types and convert to types only
			List<SongElement> filteredElements = workingCopy.stream()
				.filter(e -> !without.contains(e.getType()))
				.collect(toList());
			// see if one of the type sequence matches
			for (List<SongElementMatcher> sequence : lastSeen) {
				if (lengthOkAndEndOfFirstMatchesSecond(filteredElements, sequence)) {
					return new SongElementHistoryQueryResult(
						filteredElements.subList(filteredElements.size() - sequence.size(), filteredElements.size()));
				}
			}
			return SongElementHistoryQueryResult.NO_MATCH;
		}
		
		private boolean lengthOkAndEndOfFirstMatchesSecond(List<SongElement> filteredElements, List<SongElementMatcher> sequence) {
			return filteredElements.size() >= sequence.size() && endOfFirstMatchesSecond(filteredElements, sequence);
		}
		
		private boolean endOfFirstMatchesSecond(List<SongElement> filteredElements, List<SongElementMatcher> sequence) {
			for (int i = 1; i <= sequence.size(); i++) {
				if (!sequence.get(sequence.size() - i).matches(filteredElements.get(filteredElements.size() - i))) {
					return false;
				}
			}
			return true;
		}
	}
	
	public static class SongElementHistoryQueryResult {
		public static final SongElementHistoryQueryResult NO_MATCH = new SongElementHistoryQueryResult();
		
		private final boolean isMatched;
		private final List<SongElement> matchedElements;
		
		/** not matched */
		private SongElementHistoryQueryResult() {
			this.isMatched = false;
			this.matchedElements = Lists.newArrayList();
		}
		
		/** matched with these elements */
		public SongElementHistoryQueryResult(List<SongElement> matchedElements) {
			this.isMatched = true;
			this.matchedElements = matchedElements;
		}
		
		public boolean isMatched() {
			return isMatched;
		}
		
		public List<SongElement> getMatchedElements() {
			return matchedElements;
		}
		
	}
}
