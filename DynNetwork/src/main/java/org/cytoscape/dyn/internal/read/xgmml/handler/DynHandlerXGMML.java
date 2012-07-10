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

package org.cytoscape.dyn.internal.read.xgmml.handler;

import java.util.Stack;

import org.cytoscape.dyn.internal.model.DynNetwork;
import org.cytoscape.dyn.internal.model.DynNetworkFactory;
import org.cytoscape.dyn.internal.read.xgmml.ParseDynState;
import org.cytoscape.dyn.internal.read.xgmml.XGMMLDynParser;
import org.cytoscape.dyn.internal.view.model.DynNetworkView;
import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;

/**
 * <code> DynHandlerXGMML </code> implements the interface {@link DynHandler} and
 * handles events generated by the XGMML parser {@link XGMMLDynParser} and generates
 * graph event accordingly.
 * 
 * @author sabina
 *
 * @param <T>
 */
public final class DynHandlerXGMML<T> extends AbstractXGMMLSource<T> implements DynHandler 
{
	private final Stack<CyGroup> groupStack;
	private final Stack<OrphanEdge<T>> orphanEdgeList;
	
	private DynNetwork<T> currentNetwork;
	private CyGroup currentGroup;
	private CyNode currentNode;
	private CyEdge currentEdge;
	
	private String directed;
	private String id;
	private String label;
	private String name;
	private String source;
	private String target;
	private String type;
	private String value;
	private String start;
	private String end;
	
	private int ID = 0;
	
	public DynHandlerXGMML(DynNetworkFactory<T> sink)
	{
		super();
		groupStack = new Stack<CyGroup>();
		orphanEdgeList = new Stack<OrphanEdge<T>>();
		this.sink = sink;
	}

	@Override
	public void handleStart(Attributes atts, ParseDynState current)
	{
		switch(current)
		{
		case NONE:
			break;
			
		case GRAPH:
			id = atts.getValue("id");
			label = atts.getValue("label");
			start = atts.getValue("start");
			end = atts.getValue("end");
			directed = atts.getValue("directed");
			label = label==null?"dynamic network ("+(ID++)+")":label;
			id = id==null?label:id;
			directed = directed==null?"1":directed;
			currentNetwork = this.addGraph(id, label, start, end, directed);
			groupStack.push(null);
			break;
			
		case NODE_GRAPH:
			id = atts.getValue("id");
			label = atts.getValue("label");
			start = atts.getValue("start");
			end = atts.getValue("end");
			currentGroup = this.addGroup(currentNetwork, currentNode);
			groupStack.push(currentGroup);
			break;
			
		case NODE:
			id = atts.getValue("id");
			label = atts.getValue("label");
			start = atts.getValue("start");
			end = atts.getValue("end");
			id = id==null?label:id;
			currentNode = this.addNode(currentNetwork, currentGroup, id, label, start, end);
			break;
			
		case EDGE:
			id = atts.getValue("id");
			label = atts.getValue("label");
			source = atts.getValue("source");
			target = atts.getValue("target");
			start = atts.getValue("start");
			end = atts.getValue("end");
			label = label==null?source+"-"+target:label;
			id = id==null?label:id;
			currentEdge = this.addEdge(currentNetwork, id, label, source, target, start, end);
			if (currentEdge==null)
				orphanEdgeList.push(new OrphanEdge<T>(currentNetwork, id, label, source, target, start, end));
			break;
			
		case NET_ATT:
			name = atts.getValue("name");
			value = atts.getValue("value");
			type = atts.getValue("type");
			start = atts.getValue("start");
			end = atts.getValue("end");
			if (currentNetwork!= null && name!=null && value!=null && type!=null)
				this.addGraphAttribute(currentNetwork, name, value, type, start, end);
			break;
			
		case NODE_ATT:
			name = atts.getValue("name");
			value = atts.getValue("value");
			type = atts.getValue("type");
			start = atts.getValue("start");
			end = atts.getValue("end");
			if (currentNode!= null && name!=null && value!=null && type!=null)
				this.addNodeAttribute(currentNetwork, currentNode, name, value, type, start, end);
			break;
			
		case EDGE_ATT:
			name = atts.getValue("name");
			value = atts.getValue("value");
			type = atts.getValue("type");
			start = atts.getValue("start");
			end = atts.getValue("end");
			if (currentEdge!= null && name!=null && value!=null && type!=null)
				this.addEdgeAttribute(currentNetwork, currentEdge, name, value, type, start, end);
			else
				orphanEdgeList.peek().addAttribute(currentNetwork, name, value, type, start, end);
			break;
		}
	}

	@Override
	public void handleEnd(Attributes atts, ParseDynState current)
	{
		switch(current)
		{
		case GRAPH:
			while (!orphanEdgeList.isEmpty())
				orphanEdgeList.pop().add(this);
			finalize(currentNetwork);
			break;
			
		case NODE_GRAPH:
			currentNode = groupStack.pop().getGroupNode();
            currentGroup = groupStack.peek();
			break;
		}
	}

	@Override
	protected CyEdge addEdge(DynNetwork<T> currentNetwork, String id, String label,
			String source, String target, String start, String end)
	{
		return sink.addedEdge(currentNetwork, id, label, source, target, start, end);
	}

	@Override
	protected void addEdgeAttribute(DynNetwork<T> network, CyEdge currentEdge,
			String name, String value, String Type, String start, String end)
	{
		sink.addedEdgeAttribute(network, currentEdge, name, value, Type, start, end);
	}
	
	@Override
	protected DynNetworkView<T> createView(DynNetwork<T> dynNetwork)
	{
		// Do nothing.
		return null;
	}
	

}
