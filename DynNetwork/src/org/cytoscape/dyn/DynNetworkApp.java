package org.cytoscape.dyn;

import org.cytoscape.app.swing.AbstractCySwingApp;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.dyn.internal.events.MenuAction;

/**
 * Dynamical Network App
 * @author sabina
 *
 * @param <T>
 */
public class DynNetworkApp<T> extends AbstractCySwingApp
{
	public DynNetworkApp(CySwingAppAdapter adapter)
	{
		super(adapter);
		adapter.getCySwingApplication().addAction(new MenuAction<T>(adapter));
	}
}
