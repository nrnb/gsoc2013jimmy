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

package org.cytoscape.dyn.internal.view.layout.algorithm.dynamic;

import java.awt.Dimension;
import java.util.List;
import java.util.Set;

import org.cytoscape.dyn.internal.model.DynNetworkSnapshot;
import org.cytoscape.dyn.internal.model.DynNetworkSnapshotImpl;
import org.cytoscape.dyn.internal.model.tree.DynInterval;
import org.cytoscape.dyn.internal.view.layout.DynLayout;
import org.cytoscape.dyn.internal.view.layout.algorithm.standard.SpringLayout;
import org.cytoscape.dyn.internal.view.model.DynNetworkView;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/**
 * <code> SpringDynLayoutTask </code> is responsible for the generation of spring-based network
 * dynamics by associating to each nodes in the network appropriate intervals of node positions,
 * which are stored in {@link DynLayout}.
 * 
 * @author Sabina Sara Pfister
 *
 * @param <T>
 */
public class SpringDynLayoutTask<T> extends AbstractLayoutTask 
{
    
	private final DynLayout<T> layout;
	private final CyNetworkView view;
	private final DynNetworkView<T> dynView;
	
	private DynNetworkSnapshot<T> snap;
	private SpringLayout<T> springlayout;
	
	private final double currentTime;
	private final double minTime;
	private final double maxTime;
	
	/**
	 * <code> SpringDynLayoutTask </code> constructor.
	 * @param name
	 * @param layout
	 * @param dynView
	 * @param nodesToLayOut
	 * @param layoutAttribute
	 * @param undo
	 * @param currentTime
	 * @param minTime
	 * @param maxTime
	 */
    public SpringDynLayoutTask(
                    final String name,
                    final DynLayout<T> layout,
                    final DynNetworkView<T> dynView,
                    final Set<View<CyNode>> nodesToLayOut, 
                    final String layoutAttribute,
                    final UndoSupport undo,
                    final double currentTime,
                    final double minTime,
                    final double maxTime)
    {
            super(name, layout.getNetworkView(), nodesToLayOut, layoutAttribute, undo);
            this.layout = layout;
            this.view = layout.getNetworkView();
            this.dynView = dynView;
            this.currentTime = currentTime;
            this.minTime = minTime;
            this.maxTime = maxTime;
    }

	@Override
	protected void doLayout(TaskMonitor taskMonitor)
	{	
		if (networkView!=null && dynView!=null)
		{
			taskMonitor.setTitle("Compute Dynamic Kamada Kawai Force Layout");
			taskMonitor.setStatusMessage("Running energy minimization...");
			
			int size = (int) (4*50*Math.sqrt(nodesToLayOut.size()));
			
			snap = new DynNetworkSnapshotImpl<T>(dynView);
			springlayout = new SpringLayout<T>(snap,new Dimension(size,size));
			List<Double> events = dynView.getNetwork().getEventTimeList();
			
			initializePositions(size);

			taskMonitor.setProgress(0);
			for (int t=0;t<events.size()-1;t++)
			{
				snap.setInterval(new DynInterval<T>(events.get(t),events.get(t+1)));
				springlayout.initialize();
				springlayout.run();
				updateGraph();
				taskMonitor.setProgress(0.5);
				taskMonitor.setStatusMessage("Running energy minimization..." + t + "/" + (events.size()-1));
			}
			
			snap.setInterval(new DynInterval<T>(events.get(events.size()-1),maxTime+0.001));
			springlayout.initialize();
			springlayout.run();
			updateGraph();

			taskMonitor.setProgress(1);
			layout.initNodePositions(currentTime);
			view.fitContent();
    		view.updateView();
		}
	}
	
	private void initializePositions(int size)
	{
		for (CyNode node : dynView.getNetworkView().getModel().getNodeList())
		{
			dynView.writeVisualProperty(node, BasicVisualLexicon.NODE_X_LOCATION, size/2);
			dynView.writeVisualProperty(node, BasicVisualLexicon.NODE_Y_LOCATION, size/2);
		}
	}
	
	private void updateGraph()
	{
		for (CyNode node : springlayout.getGraph().getNodes())
		{
			layout.insertNodePositionX(node, new DynInterval<T>(springlayout.getX(node),snap.getInterval().getStart(),snap.getInterval().getEnd()));
			layout.insertNodePositionY(node, new DynInterval<T>(springlayout.getY(node),snap.getInterval().getStart(),snap.getInterval().getEnd()));
		}
	}

}
