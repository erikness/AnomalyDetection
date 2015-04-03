package edu.nmsu.erikness.anomalydetection;

import edu.nmsu.erikness.miningcommon.Point;

import java.util.HashSet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by Erik Ness at 3/28/2015 1:05 AM
 */
public class SelectionListener implements MouseListener, MouseMotionListener
{
	private DataSetWithAnomaly currentDataSet;
	private Component parentComponent;

	private Color highlightColor;
	private Cursor handCursor;
	private Cursor defaultCursor;

	public SelectionListener()
	{
		super();
		handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		defaultCursor = Cursor.getDefaultCursor();
	}

	public void mousePressed(MouseEvent e)
	{
		Point.Pair clickedPosition = new Point.Pair(e.getX(), e.getY());
		for (Point p : currentDataSet) {
			if (withinBounds(clickedPosition, p.asPixels())) {
				select(p);
			}
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		// We REALLY need a more efficient solution to looping on every mouse move
		// (though it seems to be fast enough so far).
		// Maybe some sort of set membership data structure?
		Point.Pair clickedPosition = new Point.Pair(e.getX(), e.getY());

		boolean cursorChanged = false;

		for (Point p : currentDataSet) {
			if (withinBounds(clickedPosition, p.asPixels())) {
				parentComponent.setCursor(handCursor);
				cursorChanged = true;
			}
		}

		if (!cursorChanged) {
			parentComponent.setCursor(defaultCursor);
		}
	}

	private void select(Point p)
	{
		Set<Color> highlightColors = new HashSet<Color>(Arrays.asList(Color.RED, Color.GREEN));
		Color defaultColor = Point.DEFAULT_COLOR;
		Point anomaly = ((GridPanel) parentComponent).getDataSet().getAnomaly();

		if (p.getColor().equals(defaultColor)) {
			if (p.equals(anomaly)) {
				p.setColor(Color.GREEN);
			} else {
				p.setColor(Color.RED);
			}
		} else {
			p.setColor(defaultColor);
		}
		parentComponent.repaint();
	}

	private boolean withinBounds(Point.Pair clickedPixel, Point.Pair candidatePair)
	{
		boolean xInBounds = candidatePair.x - Point.RADIUS <= clickedPixel.x &&
				clickedPixel.x <= candidatePair.x + Point.RADIUS;
		boolean yInBounds = candidatePair.y - Point.RADIUS <= clickedPixel.y &&
				clickedPixel.y <= candidatePair.y + Point.RADIUS;

		return xInBounds && yInBounds;
	}

	public void setDataSet(DataSetWithAnomaly set)
	{
		currentDataSet = set;
	}

	public void setParentComponent(Component comp)
	{
		parentComponent = comp;
	}

	public void colorUpdate(Color c)
	{
		highlightColor = c;
	}

	public void mouseDragged(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
}

