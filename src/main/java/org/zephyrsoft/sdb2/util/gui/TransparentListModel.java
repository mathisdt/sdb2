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
package org.zephyrsoft.sdb2.util.gui;

import java.util.List;

import javax.swing.AbstractListModel;

/**
 * A list model which transparently uses an underlying {@link List}.
 */
public class TransparentListModel<T> extends AbstractListModel<T> {

    private static final long serialVersionUID = -2952298254786461472L;

    private final List<T> underlyingList;

    public TransparentListModel(List<T> underlyingList) {
        this.underlyingList = underlyingList;
    }

    @Override
    public int getSize() {
        return underlyingList.size();
    }

    @Override
    public T getElementAt(int index) {
        return underlyingList.get(index);
    }

    public List<T> getAllElements() {
        return underlyingList;
    }

    public boolean contains(T element) {
        return underlyingList.contains(element);
    }

    public void addSong(final T t) {
        underlyingList.add(t);
        fireIntervalAdded(this, underlyingList.size() - 1, underlyingList.size() - 1);
    }

    public void removeSong(final int index) {
        underlyingList.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void clearSongs() {
        int oldSize = underlyingList.size();
        underlyingList.clear();
        fireIntervalRemoved(this, 0, Math.max(0, oldSize - 1));
    }
}
