package edu.nmsu.erikness.anomalydetection;

import com.google.common.collect.Lists;
import edu.nmsu.erikness.miningcommon.DataSet;
import edu.nmsu.erikness.miningcommon.Point;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * Created by Erik Ness at 3/28/2015 12:41 AM
 */
public class DataSetWithAnomaly extends DataSet
{
	private Point anomaly;

	public DataSetWithAnomaly(Collection<Point> points)
	{
		super(points);
	}

	public DataSetWithAnomaly(Collection<Point> points, Point anomaly)
	{
		super(points);
		this.anomaly = anomaly;
	}

	public Point getAnomaly()
	{
		return anomaly;
	}

	public void setAnomaly(Point anomaly)
	{
		this.anomaly = anomaly;
	}

	public static DataSetWithAnomaly fromStream(InputStream stream)
	{
		String line = null;
		StringBuilder all = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			while ((line = reader.readLine()) != null) {
				all.append(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return fromDirectContents(all.toString());
	}

	public static DataSetWithAnomaly fromFile(File f)
	{
		return fromFile(f.getPath());
	}

	public static DataSetWithAnomaly fromFile(String path)
	{
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException ex) {
			throw new RuntimeException("Gotta have a valid dataset! " + path, ex);
		}

		String contents = new String(encoded, StandardCharsets.US_ASCII);
		return fromDirectContents(contents);
	}

	public static DataSetWithAnomaly fromDirectContents(String contents)
	{
		contents = contents.replaceAll("\\s+","");  // get rid of whitespace
		String[] sections = contents.split("\\$");
		String metaPart = sections[0];
		String[] anomalyParts = metaPart.substring(1, metaPart.length() - 1).split(",");
		Point anomaly = Point.fromGrid(Double.parseDouble(anomalyParts[0]), Double.parseDouble(anomalyParts[1]));
		String[] tuples = sections[1].split(";");

		List<Point> points = Lists.newArrayList();
		for (String tuple : tuples) {
			String[] numbers = tuple.substring(1, tuple.length() - 1).split(",");
			Point point = Point.fromGrid(Double.parseDouble(numbers[0]), Double.parseDouble(numbers[1]));
			points.add(point);
		}

		return new DataSetWithAnomaly(points, anomaly);
	}
}
