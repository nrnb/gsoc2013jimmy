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

package org.cytoscape.dyn.internal.layout.standard.force;

import java.util.Iterator;


/**
 * <code> RungeKuttaIntegrator </code> updates velocity and position data using the 4th-Order Runge-Kutta method.
 * It is slower but more accurate than other techniques such as Euler's Method.
 * The technique requires re-evaluating forces 4 times for a given timestep.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class RungeKuttaIntegrator implements Integrator 
{
    
    /**
     * @see prefuse.util.force.Integrator#integrate(prefuse.util.force.ForceSimulator, long)
     */
    public void integrate(ForceSimulator sim, long timestep) 
    {
        float speedLimit = sim.getSpeedLimit();
        float vx, vy, v, coeff;
        float[][] k, l;
        
        Iterator<ForceItem> iter = sim.getItems();
        while ( iter.hasNext() ) {
            ForceItem item = (ForceItem)iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            item.plocation[0] = item.location[0];
            item.plocation[1] = item.location[1];
            k[0][0] = timestep*item.velocity[0];
            k[0][1] = timestep*item.velocity[1];
            l[0][0] = coeff*item.force[0];
            l[0][1] = coeff*item.force[1];
        
            item.location[0] += 0.5f*k[0][0];
            item.location[1] += 0.5f*k[0][1];
        }
        
        sim.accumulate();
        
        iter = sim.getItems();
        while ( iter.hasNext() ) 
        {
            ForceItem item = (ForceItem)iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            vx = item.velocity[0] + .5f*l[0][0];
            vy = item.velocity[1] + .5f*l[0][1];
            v = (float)Math.sqrt(vx*vx+vy*vy);
            if ( v > speedLimit ) {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            k[1][0] = timestep*vx;
            k[1][1] = timestep*vy;
            l[1][0] = coeff*item.force[0];
            l[1][1] = coeff*item.force[1];
        
            item.location[0] = item.plocation[0] + 0.5f*k[1][0];
            item.location[1] = item.plocation[1] + 0.5f*k[1][1];
        }
        
        sim.accumulate();
        
        iter = sim.getItems();
        while ( iter.hasNext() ) 
        {
            ForceItem item = (ForceItem)iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            vx = item.velocity[0] + .5f*l[1][0];
            vy = item.velocity[1] + .5f*l[1][1];
            v = (float)Math.sqrt(vx*vx+vy*vy);
            if ( v > speedLimit ) 
            {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            k[2][0] = timestep*vx;
            k[2][1] = timestep*vy;
            l[2][0] = coeff*item.force[0];
            l[2][1] = coeff*item.force[1];
        
            item.location[0] = item.plocation[0] + 0.5f*k[2][0];
            item.location[1] = item.plocation[1] + 0.5f*k[2][1];
        }
        
        sim.accumulate();
        
        iter = sim.getItems();
        while ( iter.hasNext() ) 
        {
            ForceItem item = (ForceItem)iter.next();
            coeff = timestep / item.mass;
            k = item.k;
            l = item.l;
            float[] p = item.plocation;
            vx = item.velocity[0] + l[2][0];
            vy = item.velocity[1] + l[2][1];
            v = (float)Math.sqrt(vx*vx+vy*vy);
            if ( v > speedLimit ) {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            k[3][0] = timestep*vx;
            k[3][1] = timestep*vy;
            l[3][0] = coeff*item.force[0];
            l[3][1] = coeff*item.force[1];
            item.location[0] = p[0] + (k[0][0]+k[3][0])/6.0f + (k[1][0]+k[2][0])/3.0f;
            item.location[1] = p[1] + (k[0][1]+k[3][1])/6.0f + (k[1][1]+k[2][1])/3.0f;
            
            vx = (l[0][0]+l[3][0])/6.0f + (l[1][0]+l[2][0])/3.0f;
            vy = (l[0][1]+l[3][1])/6.0f + (l[1][1]+l[2][1])/3.0f;
            v = (float)Math.sqrt(vx*vx+vy*vy);
            if ( v > speedLimit ) {
                vx = speedLimit * vx / v;
                vy = speedLimit * vy / v;
            }
            item.velocity[0] += vx;
            item.velocity[1] += vy;
        }
    }
} 
