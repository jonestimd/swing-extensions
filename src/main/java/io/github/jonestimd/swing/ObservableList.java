// Copyright (c) 2016 Timothy D. Jones
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
package io.github.jonestimd.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractListModel;

public class ObservableList<E> extends AbstractListModel implements List<E>
{
	private List<E> delegate;

	public ObservableList()
	{
		this(new ArrayList<E>());
	}
	public ObservableList(List<E> delegate)
	{
		this.delegate = delegate;
	}

	public void setList(List<E> delegate)
	{
		int oldSize = this.delegate.size();
		this.delegate = delegate;
		int newSize = delegate.size();
		if (oldSize > 0)
		{
			if (newSize > 0)
			{
				if (newSize > oldSize)
				{
					fireContentsChanged(this, 0, oldSize - 1);
					fireIntervalAdded(this, oldSize, newSize - 1);
				}
				else
				{
					fireContentsChanged(this, 0, newSize - 1);
					if (newSize < oldSize)
					{
						fireIntervalRemoved(this, newSize, oldSize - 1);
					}
				}
			}
			else
			{
				fireIntervalRemoved(this, 0, oldSize - 1);
			}
		}
		else if (newSize > 0)
		{
			fireIntervalAdded(this, 0, newSize - 1);
		}
	}

	public void add(int index, E element)
	{
		delegate.add(index, element);
		fireIntervalAdded(this, index, index);
	}

	public boolean add(E o)
	{
		boolean added = delegate.add(o);
		if (added)
		{
			int index = delegate.size() - 1;
			fireIntervalAdded(this, index, index);
		}
		return added;
	}

	public boolean addAll(Collection<? extends E> c)
	{
		boolean added = delegate.addAll(c);
		if (added)
		{
			int size = delegate.size();
			int fromIndex = size - c.size();
			fireIntervalAdded(this, fromIndex, size - 1);
		}
		return added;
	}

	public boolean addAll(int index, Collection<? extends E> c)
	{
		boolean added = delegate.addAll(index, c);
		if (added)
		{
			fireIntervalAdded(this, index, index + c.size() - 1);
		}
		return added;
	}

	public void clear()
	{
		int size = delegate.size();
		if (size > 0)
		{
			delegate.clear();
			fireIntervalRemoved(this, 0, size - 1);
		}
	}

	public boolean contains(Object o)
	{
		return delegate.contains(o);
	}

	public boolean containsAll(Collection<?> c)
	{
		return delegate.containsAll(c);
	}

	public E get(int index)
	{
		return delegate.get(index);
	}

	//	public boolean equals(Object o)
//	{
//		return delegate.equals(o);
//	}

//	public int hashCode()
//	{
//		return delegate.hashCode();
//	}

	public int indexOf(Object o)
	{
		return delegate.indexOf(o);
	}

	public boolean isEmpty()
	{
		return delegate.isEmpty();
	}

	public Iterator<E> iterator()
	{
		return Collections.unmodifiableList(delegate).iterator();
	}

	public int lastIndexOf(Object o)
	{
		return delegate.lastIndexOf(o);
	}

	public ListIterator<E> listIterator()
	{
		return Collections.unmodifiableList(delegate).listIterator();
	}

	public ListIterator<E> listIterator(int index)
	{
		return Collections.unmodifiableList(delegate).listIterator(index);
	}

	public E remove(int index)
	{
		E removed = delegate.remove(index);
		fireIntervalRemoved(this, index, index);
		return removed;
	}

	public boolean remove(Object o)
	{
		int index = delegate.indexOf(o);
		if (index >= 0)
		{
			delegate.remove(o);
			fireIntervalRemoved(this, index, index);
			return true;
		}
		return false;
	}

	public boolean removeAll(Collection<?> c)
	{
		boolean changed = false;
		for (Iterator<?> iter = c.iterator(); iter.hasNext();)
		{
			changed |= remove(iter.next());
		}
		return changed;
	}

	public boolean retainAll(Collection<?> c)
	{
		boolean changed = false;
		int index = 0;
		for (Iterator<?> iter = delegate.iterator(); iter.hasNext();)
		{
			Object element = iter.next();
			if (c.contains(element))
			{
				index++;
			}
			else
			{
				changed = true;
				iter.remove();
				fireIntervalRemoved(this, index, index);
			}
		}
		return changed;
	}

	public E set(int index, E element)
	{
		E replaced = delegate.set(index, element);
		fireContentsChanged(this, index, index);
		return replaced;
	}

	public int size()
	{
		return delegate.size();
	}

	public List<E> subList(int fromIndex, int toIndex)
	{
		return Collections.unmodifiableList(delegate).subList(fromIndex, toIndex);
	}

	public Object[] toArray()
	{
		return delegate.toArray();
	}

	public <T> T[] toArray(T[] a)
	{
		return delegate.toArray(a);
	}

	public int getSize()
	{
		return delegate.size();
	}

	public E getElementAt(int index)
	{
		return delegate.get(index);
	}
}
