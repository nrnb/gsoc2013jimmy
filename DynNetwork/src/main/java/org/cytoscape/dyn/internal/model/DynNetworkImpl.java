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

package org.cytoscape.dyn.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.dyn.internal.model.tree.DynAttribute;
import org.cytoscape.dyn.internal.model.tree.DynInterval;
import org.cytoscape.dyn.internal.model.tree.DynIntervalTreeImpl;
import org.cytoscape.dyn.internal.util.KeyPairs;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * <code> DynNetworkImpl </code> implements the interface {@link DynNetwork}
 * and provides method and data structures to record and retrieve network dynamic 
 * information
 * 
 * @author sabina
 *
 * @param <T>
 */
public final class DynNetworkImpl<T> implements DynNetwork<T>
{	
	private final CyNetwork network;
	private final CyGroupManager groupManager;
	
	private final boolean isDirected;

	private final Map<String, Long> cyNodes;
	private final Map<String, Long> cyEdges;

	private List<DynInterval<T>> currentNodes;
	private List<DynInterval<T>> currentEdges;
	private List<DynInterval<T>> currentGraphsAttr;
	private List<DynInterval<T>> currentNodesAttr;
	private List<DynInterval<T>> currentEdgesAttr;

	private final DynIntervalTreeImpl<T> graphTree;
	private final DynIntervalTreeImpl<T> nodeTree;
	private final DynIntervalTreeImpl<T> edgeTree;
	private final DynIntervalTreeImpl<T> graphTreeAttr;
	private final DynIntervalTreeImpl<T> nodeTreeAttr;
	private final DynIntervalTreeImpl<T> edgeTreeAttr;

	private final Map<KeyPairs,DynAttribute<T>> graphTable;
	private final Map<KeyPairs,DynAttribute<T>> nodeTable;
	private final Map<KeyPairs,DynAttribute<T>> edgeTable;

	private double minStartTime = Double.POSITIVE_INFINITY;
	private double maxStartTime = Double.NEGATIVE_INFINITY;
	private double minEndTime = Double.POSITIVE_INFINITY;
	private double maxEndTime = Double.NEGATIVE_INFINITY;

	public DynNetworkImpl(
			final CyNetwork network,
			final CyGroupManager groupManager,
			final boolean isDirected)
	{
		this.network = network;
		this.groupManager = groupManager;
		this.isDirected = isDirected;

		cyNodes = new HashMap<String, Long>();
		cyEdges = new HashMap<String, Long>();

		this.currentNodes = new ArrayList<DynInterval<T>>();
		this.currentEdges = new ArrayList<DynInterval<T>>();
		this.currentGraphsAttr = new ArrayList<DynInterval<T>>();
		this.currentNodesAttr = new ArrayList<DynInterval<T>>();
		this.currentEdgesAttr = new ArrayList<DynInterval<T>>();

		this.graphTree = new DynIntervalTreeImpl<T>();
		this.nodeTree = new DynIntervalTreeImpl<T>();
		this.edgeTree = new DynIntervalTreeImpl<T>();
		this.graphTreeAttr = new DynIntervalTreeImpl<T>();
		this.nodeTreeAttr = new DynIntervalTreeImpl<T>();
		this.edgeTreeAttr = new DynIntervalTreeImpl<T>();

		this.graphTable = new HashMap<KeyPairs,DynAttribute<T>>();
		this.nodeTable = new HashMap<KeyPairs,DynAttribute<T>>();
		this.edgeTable = new HashMap<KeyPairs,DynAttribute<T>>();
		
	}

	@Override
	public CyNetwork getNetwork() 
	{
		return this.network;
	}
	
	@Override
	public void writeGraphTable(String name, T value) 
	{
		network.getRow(this.network).set(name, value);
	}

	@Override
	public CyNode readNodeTable(long key) 
	{
		return network.getNode(key);
	}

	@Override
	public void writeNodeTable(CyNode node, String name, T value) 
	{
//		System.out.println(name + " set = " + value.toString());
//		System.out.println(name + " " + CyNetwork.SELECTED);
		network.getRow(node).set(name, value);
	}

	@Override
	public CyEdge readEdgeTable(long key) 
	{
		return network.getEdge(key);
	}

	@Override
	public void writeEdgeTable(CyEdge edge, String name, T value) 
	{
		network.getRow(edge).set(name, value);
	}

	@Override
	public synchronized void insertGraph(String column, DynInterval<T> interval)
	{
		setMinMaxTime(interval);
		setDynAttribute(column, interval);
		graphTable.put(interval.getAttribute().getKey(), interval.getAttribute());
		graphTree.insert(interval);
	}

	@Override
	public synchronized void insertNode(CyNode node, String column, DynInterval<T> interval)
	{
		setMinMaxTime(interval);
		setDynAttribute(node, column, interval);
		nodeTable.put(interval.getAttribute().getKey(), interval.getAttribute());
		nodeTree.insert(interval);
	}

	@Override
	public synchronized void insertEdge(CyEdge edge, String column, DynInterval<T> interval)
	{
		setMinMaxTime(interval);
		setDynAttribute(edge, column, interval);
		edgeTable.put(interval.getAttribute().getKey(), interval.getAttribute());
		edgeTree.insert(interval);
	}

	@Override
	public synchronized void insertGraphAttr(String column, DynInterval<T> interval)
	{
		setMinMaxTime(interval);
		setDynAttribute(column, interval);
		graphTable.put(interval.getAttribute().getKey(), interval.getAttribute());
		graphTreeAttr.insert(interval);
	}

	@Override
	public synchronized void insertNodeAttr(CyNode node, String column, DynInterval<T> interval)
	{
		setMinMaxTime(interval);
		setDynAttribute(node, column, interval);
		nodeTable.put(interval.getAttribute().getKey(), interval.getAttribute());
		nodeTreeAttr.insert(interval);
	}

	@Override
	public synchronized void insertEdgeAttr(CyEdge edge, String column, DynInterval<T> interval)
	{
		setMinMaxTime(interval);
		setDynAttribute(edge, column, interval);
		edgeTable.put(interval.getAttribute().getKey(), interval.getAttribute());
		edgeTreeAttr.insert(interval);
	}

	@Override
	public synchronized void removeGraph() 
	{
		this.graphTree.clear();
		this.nodeTree.clear();
		this.edgeTree.clear();
		this.graphTable.clear();
		this.nodeTable.clear();
		this.edgeTable.clear();
	}

	@Override
	public synchronized void removeNode(CyNode node) 
	{
		Iterable<CyEdge> edgeList = this.network.getAdjacentEdgeIterable(node, CyEdge.Type.ANY);
		while (edgeList.iterator().hasNext())
			this.removeEdge(edgeList.iterator().next());

		KeyPairs key = new KeyPairs(CyNetwork.NAME, node.getSUID());
		for (DynInterval<T> interval : nodeTable.get(key).
				getRecursiveIntervalList(new ArrayList<DynInterval<T>>()))
			nodeTree.remove(interval);
		nodeTable.remove(key);
	}

	@Override
	public synchronized void removeEdge(CyEdge edge)
	{
		KeyPairs key = new KeyPairs(CyNetwork.NAME, edge.getSUID());
		for (DynInterval<T> interval : edgeTable.get(key).
				getRecursiveIntervalList(new ArrayList<DynInterval<T>>()))
			edgeTree.remove(interval);
		edgeTable.remove(key);
	}

	@Override
	public void removeGraphAttr() 
	{

	}

	@Override
	public void removeNodeAttr(CyNode node) 
	{

	}

	@Override
	public void removeEdgeAttr(CyEdge edge) 
	{

	}
	
	@Override
	public List<DynInterval<T>> searchNodes(DynInterval<T> interval)
	{
		return nodeTree.search(interval);
	}

	@Override
	public List<DynInterval<T>> searchEdges(DynInterval<T> interval)
	{
		return edgeTree.search(interval);
	}
	
	@Override
	public List<DynInterval<T>> searchNodesNot(DynInterval<T> interval)
	{
		return nodeTree.searchNot(interval);
	}

	@Override
	public List<DynInterval<T>> searchEdgesNot(DynInterval<T> interval)
	{
		return edgeTree.searchNot(interval);
	}
	
	@Override
	public List<DynInterval<T>> searchChangedNodes(DynInterval<T> interval)
	{
		List<DynInterval<T>> tempList = nodeTree.search(interval);
		List<DynInterval<T>> changedList = nonOverlap(currentNodes, tempList);
		currentNodes = tempList;
		return changedList;
	}

	@Override
	public List<DynInterval<T>> searchChangedEdges(DynInterval<T> interval)
	{
		List<DynInterval<T>> tempList = edgeTree.search(interval);
		List<DynInterval<T>> changedList = nonOverlap(currentEdges, tempList);
		currentEdges = tempList;
		return changedList;
	}
	
	@Override
	public List<DynInterval<T>> searchGraphsAttr(DynInterval<T> interval)
	{
		return graphTreeAttr.search(interval);
	}

	@Override
	public List<DynInterval<T>> searchNodesAttr(DynInterval<T> interval)
	{
		return nodeTreeAttr.search(interval);
	}

	@Override
	public List<DynInterval<T>> searchEdgesAttr(DynInterval<T> interval)
	{
		return edgeTreeAttr.search(interval);
	}
	
	@Override
	public List<DynInterval<T>> searchChangedGraphsAttr(DynInterval<T> interval)
	{
		List<DynInterval<T>> tempList = graphTreeAttr.search(interval);
		List<DynInterval<T>> changedList = new ArrayList<DynInterval<T>>(nonOverlap(currentGraphsAttr, tempList));
		currentGraphsAttr = tempList;
		return changedList;
	}

	@Override
	public List<DynInterval<T>> searchChangedNodesAttr(DynInterval<T> interval)
	{
		List<DynInterval<T>> tempList = nodeTreeAttr.search(interval);
		List<DynInterval<T>> changedList = new ArrayList<DynInterval<T>>(nonOverlap(currentNodesAttr, tempList));
		currentNodesAttr = tempList;
		return changedList;
	}

	@Override
	public List<DynInterval<T>> searchChangedEdgesAttr(DynInterval<T> interval)
	{
		List<DynInterval<T>> tempList = edgeTreeAttr.search(interval);
		List<DynInterval<T>> changedList = nonOverlap(currentEdgesAttr, tempList);
		currentEdgesAttr = tempList;
		return changedList;
	}

	@Override
	public DynAttribute<T> getDynAttribute(CyNetwork network, String column)
	{
		return this.graphTable.get(new KeyPairs(column, network.getSUID()));
	}

	@Override
	public DynAttribute<T> getDynAttribute(CyNode node, String column)
	{
		return this.nodeTable.get(new KeyPairs(column, node.getSUID()));
	}

	@Override
	public DynAttribute<T> getDynAttribute(CyEdge edge, String column)
	{
		return this.edgeTable.get(new KeyPairs(column, edge.getSUID()));
	}

	@Override
	public long getCyNode(String id)
	{
		return cyNodes.get(id);
	}

	@Override
	public long getCyEdge(String id) 
	{
		return cyEdges.get(id);
	}

	@Override
	public boolean containsCyNode(String id)
	{
		return cyNodes.containsKey(id);
	}

	@Override
	public boolean containsCyEdge(String id) 
	{
		return cyEdges.containsKey(id);
	}

	@Override
	public void setCyNode(String id, long value) 
	{
		cyNodes.put(id, value);
	}

	@Override
	public void setCyEdge(String id, long value) 
	{
		cyEdges.put(id, value);
	}
	
	@Override
	public void finalizeNetwork() 
	{
//		for (CyGroup group : this.groupManager.getGroupSet(this.network))
//		{
//			group.removeNodes(new ArrayList<CyNode>());
//			Iterable<CyEdge> metaEdgeList = this.network.getAdjacentEdgeIterable(group.getGroupNode(), CyEdge.Type.ANY);
//			while (metaEdgeList.iterator().hasNext())
//				System.out.println("yes!");
//		}
	}

	@Override
	public void print()
	{
		this.nodeTree.print(this.nodeTree.getRoot());
	}

	@Override
	public double getMinTime()
	{
		if (Double.isInfinite(minStartTime))
			if (Double.isInfinite(minEndTime))
				return -1;
			else
				return minEndTime;
		else
			return minStartTime;
	}

	@Override
	public double getMaxTime()
	{
		if (Double.isInfinite(maxEndTime))
			if (Double.isInfinite(maxStartTime))
				return 1;
			else
				return maxStartTime;
		else
			return maxEndTime;
	}
	
	@Override
	public boolean isDirected() 
	{
		return this.isDirected;
	}

	private synchronized void setDynAttribute(String column, DynInterval<T> interval)
	{
		KeyPairs key = new KeyPairs(column, this.network.getSUID());
		if (this.graphTable.containsKey(key))
			this.graphTable.get(key).addInterval(interval);
		else
			this.graphTable.put(key, new DynAttribute<T>(interval, key));

		if (!column.equals(CyNetwork.NAME))
			this.graphTable.get(new KeyPairs(CyNetwork.NAME, this.network.getSUID()))
			.addChildren(this.graphTable.get(key));
	}

	private synchronized void setDynAttribute(CyNode node, String column, DynInterval<T> interval)
	{
		KeyPairs key = new KeyPairs(column, node.getSUID());
		if (this.nodeTable.containsKey(key))
			this.nodeTable.get(key).addInterval(interval);
		else
			this.nodeTable.put(key, new DynAttribute<T>(interval, key));

		if (!column.equals(CyNetwork.NAME))
			this.nodeTable.get(new KeyPairs(CyNetwork.NAME, node.getSUID()))
			.addChildren(this.nodeTable.get(key));
	}

	private synchronized void setDynAttribute(CyEdge edge, String column, DynInterval<T> interval)
	{
		KeyPairs key = new KeyPairs(column, edge.getSUID());
		if (this.edgeTable.containsKey(key))
			this.edgeTable.get(key).addInterval(interval);
		else
			this.edgeTable.put(key, new DynAttribute<T>(interval, key));

		if (!column.equals(CyNetwork.NAME))
			this.edgeTable.get(new KeyPairs(CyNetwork.NAME, edge.getSUID()))
			.addChildren(this.edgeTable.get(key));
	}
	
	private List<DynInterval<T>> nonOverlap(List<DynInterval<T>> list1, List<DynInterval<T>> list2) 
	{
		List<DynInterval<T>> diff = new ArrayList<DynInterval<T>>();
		for (DynInterval<T> i : list1)
			if (!list2.contains(i))
				diff.add(i);
		for (DynInterval<T> i : list2)
			if (!list1.contains(i))
				diff.add(i);
		return diff;
	}

	private synchronized void setMinMaxTime(DynInterval<T> interval)
	{
		double start = interval.getStart();
		double end = interval.getEnd();
		if (!Double.isInfinite(start))
		{
			minStartTime = Math.min(minStartTime, start);
			maxStartTime = Math.max(maxStartTime, start);
		}
		if (!Double.isInfinite(end))
		{
			maxEndTime = Math.max(maxEndTime, end);
			minEndTime = Math.min(minEndTime, end);
		}
	}

}
