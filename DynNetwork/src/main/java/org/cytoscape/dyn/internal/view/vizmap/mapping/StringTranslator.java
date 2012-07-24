/*
 * DynNetwork plugin for Cytoscape 3.0 (http://www.cytoscape.org/).
 * Copyright (C) 2012 Sabina Sara Pfister
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.cytoscape.dyn.internal.view.vizmap.mapping;

import java.util.List;

import org.cytoscape.view.vizmap.mappings.ValueTranslator;

/**
 * <code> StringTranslator </code> translates values to strings.
 * 
 * @author cytoscape
 *
 */
public class StringTranslator implements ValueTranslator<Object, String>
{

	@Override
	public String translate(final Object inputValue) 
	{
		if (inputValue != null) 
		{
			if (inputValue instanceof List) 
			{
				final List<?> list = (List<?>)inputValue;
				final StringBuffer sb = new StringBuffer();

				if (list != null && !list.isEmpty()) 
				{
					for (Object item : list)
						sb.append(item.toString() + "\n");

					sb.deleteCharAt(sb.length() - 1);
				}

				return sb.toString();
			} 
			else 
			{
				return inputValue.toString();
			}
		} 
		else 
		{
			return null;
		}
	}

	@Override
	public Class<String> getTranslatedValueType() 
	{
		return String.class;
	}
}