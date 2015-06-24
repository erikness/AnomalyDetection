package edu.nmsu.erikness.anomalydetection;

import com.google.common.collect.Lists;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.nmsu.erikness.miningcommon.Point;

import javax.swing.*;

public class Main
{
    public static void main(String[] args)
	{
		int topAreaHeight = 50;
		int gridHeight = 700;
		int appHeight = topAreaHeight + gridHeight;
		int appWidth = 700;

		/* Initialize the static stuff in the Point class. */
		/* Please do not compain about the coupling. I know. */
		Point.pixelWidth = appHeight;
		Point.pixelHeight = appHeight;
		Point.gridXMax = 5;
		Point.gridXMin = -5;
		Point.gridYMax = 5;
		Point.gridYMin = -5;

		ClassLoader cl = Main.class.getClassLoader();
		ImageIcon checkIcon  = loadImageAntwise("circle-check-8x.png");
		ImageIcon xIcon  = loadImageAntwise("circle-x-8x.png");
		URL url = cl.getResource("edu/nmsu/erikness/datasets/");

		String internalDataSetsPath = "edu/nmsu/erikness/datasets/";
		String internalResourcesPath = "edu/nmsu/erikness/resources/";

		List<DataSetWithAnomaly> loadedDataSets = null;
//		if (inJar()) {
//			loadedDataSets = loadDataSetsAsJar(internalDataSetsPath);
//		} else {
//			loadedDataSets = loadDataSetsAsProject(internalDataSetsPath);
//		}
		loadedDataSets = loadDataSetsAntwise();

		JFrame frame = new JFrame();
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		frame.setPreferredSize(new Dimension(appWidth, appHeight));
		frame.pack();
		adjustSize(frame, appWidth, appHeight);
		centerWindow(frame);


		JPanel topArea = new JPanel() {
			public void paintComponent(Graphics g)
			{
				checkIcon.paintIcon(null, g, 0, 0);
				xIcon.paintIcon(null, g, 0, 0);
			}
		};
		topArea.setPreferredSize(new Dimension(appWidth, topAreaHeight));

		MetaGridPanel metaGridPanel = new MetaGridPanel();
		for (DataSetWithAnomaly ds : loadedDataSets) {
			GridPanel gridPanel = new GridPanel();
			gridPanel.setPreferredSize(new Dimension(appWidth, gridHeight));
			gridPanel.setDataSet(ds);

			SelectionListener sl = new SelectionListener();
			sl.setDataSet(ds);
			sl.setParentComponent(gridPanel);
			gridPanel.addMouseListener(sl);
			gridPanel.addMouseMotionListener(sl);
			metaGridPanel.add(gridPanel);
		}

		JButton prevButton = new JButton("\u25C0");
		JButton nextButton = new JButton("\u25B6");

		prevButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				metaGridPanel.previous();
			}
		});

		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				metaGridPanel.next();
			}
		});

		topArea.add(prevButton);
		topArea.add(nextButton);

		frame.add(topArea);
		frame.add(metaGridPanel);

		frame.setVisible(true);
		frame.setTitle("Find the Anomaly");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

	private static void adjustSize(JFrame frame, int intendedWidth, int intendedHeight)
	{
		// We "set" the size initially, but JFrame doesn't entirely agree and includes things like the border
		// in the size. So we let it render invisibly, and resize based on that.
		Dimension actualSize = frame.getContentPane().getSize();

		int extraW = intendedWidth - actualSize.width;
		int extraH = intendedHeight - actualSize.height;

		// Now set the size.
		frame.setSize(intendedWidth + extraW, intendedHeight + extraH);
	}

	private static void centerWindow(JFrame frame)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(
				screenSize.width / 2 - frame.getSize().width / 2,
				screenSize.height / 2 - frame.getSize().height / 2);
	}

	private static boolean inJar()
	{
		URL here = Main.class.getClassLoader().getResource("edu/nmsu/erikness/anomalydetection/Main.class");
		return here.toString().startsWith("jar");
	}

	private static ImageIcon loadImageAntwise(String imageName)
	{
		URL jarLocation = Main.class.getProtectionDomain().getCodeSource().getLocation();
		URI jarParent;
		try {
			jarParent = jarLocation.toURI().resolve(".");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		URI imageURI = jarParent.resolve(imageName);
		try {
			return new ImageIcon(imageURI.toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<DataSetWithAnomaly> loadDataSetsAntwise()
	{
		String location = "datasets";  // relative to the jar file
		URL jarLocation = Main.class.getProtectionDomain().getCodeSource().getLocation();
		URI jarParent;
		try {
			jarParent = jarLocation.toURI().resolve(".");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		URI dirURI = jarParent.resolve(location);
		return Arrays.stream(new File(dirURI).listFiles())
				.map(file -> DataSetWithAnomaly.fromFile(file))
				.collect(Collectors.toList());
	}

	private static List<DataSetWithAnomaly> loadDataSetsAsJar(String internalDataSetsPath)
	{
		List<DataSetWithAnomaly> dataSets = Lists.newArrayList();

		CodeSource src = Main.class.getProtectionDomain().getCodeSource();
		ClassLoader cl = Main.class.getClassLoader();
		URL jar = src.getLocation();

		try {
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			ZipEntry entry = null;
			String name = null;
			while ((entry = zip.getNextEntry()) != null) {
				name = entry.getName();
				if (name.startsWith(internalDataSetsPath) && name.endsWith(".txt")) {
					dataSets.add(DataSetWithAnomaly.fromStream(cl.getResourceAsStream(name)));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return dataSets;
	}

	private static List<DataSetWithAnomaly> loadDataSetsAsProject(String internalDataSetsPath)
	{
		List<DataSetWithAnomaly> dataSets = Lists.newArrayList();

		ClassLoader cl = Main.class.getClassLoader();
		String dataSetDirectory = cl.getResource(internalDataSetsPath).getPath();

		for (File f : new File(dataSetDirectory).listFiles()) {
			dataSets.add(DataSetWithAnomaly.fromFile(f));
		}

		return dataSets;
	}
}
