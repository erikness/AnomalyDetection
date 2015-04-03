package edu.nmsu.erikness.anomalydetection;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by Erik Ness at 3/22/15 9:51 AM
 *
 * Manages a collection of GraphPanels in a CardLayout.
 *
 * Each GraphPanel is a
 */
public class MetaGridPanel extends JPanel
{
	private CardLayout layout;
	// I don't think I can access the Layout's private list of components, but that's why we have a container class!
	private List<GridPanel> panels;
	private GridPanel currentGraphPanel;

	public MetaGridPanel()
	{
		layout = new CardLayout();
		panels = Lists.newArrayList();
		this.setLayout(layout);
	}

	public void addGraphPanel(GridPanel gp)
	{
		this.add(gp, Integer.toString(gp.hashCode()));
		panels.add(gp);
	}

	public List<GridPanel> getGraphPanels()
	{
		return panels;
	}

	public GridPanel getCurrentGraphPanel()
	{
		// for loop is not a big deal, as we only have a couple cards
		for (Component comp : this.getComponents()) {
			if (comp.isVisible()) {
				return (GridPanel) comp;
			}
		}
		return null;
	}

	public void next()
	{
		layout.next(this);
	}

	public void previous()
	{
		layout.previous(this);
	}

	public void first()
	{
		layout.first(this);
	}

	public void last()
	{
		layout.last(this);
	}
}
