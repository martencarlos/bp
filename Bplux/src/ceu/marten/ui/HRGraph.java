package ceu.marten.ui;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * Created by martencarlos on 25/07/13.
 */

public class HRGraph {

	private GraphViewSeries serie;
	private GraphViewSeriesStyle style;
	private double xValue;
	private GraphView graphView;

	public HRGraph(android.content.Context context) {
		// STYLE
		style = new GraphViewSeriesStyle(Color.rgb(23, 118, 8), 2); // green and
																	// thickness
																	// 2

		// INIT SERIE DATA
		serie = new GraphViewSeries("BPM", style, new GraphViewData[] {
				new GraphViewData(1.0, 60.0), new GraphViewData(1.5, 100) });

		// INIT GRAPHVIEW
		graphView = new LineGraphView(context, "");// title

		// ADD SERIES TO GRAPHVIEW and SET SCROLLABLE
		graphView.addSeries(serie);
		// graphView.setScrollable(true);
		graphView.setViewPort(2, 60);
		graphView.setScalable(true);

		// Current X value
		xValue = 2d;
	}

	public GraphView getGraphView() {
		return graphView;
	}

	public GraphViewSeries getSerie() {
		return serie;
	}

	public void setSerie(GraphViewSeries serie) {
		this.serie = serie;
	}

	public double getxValue() {
		return xValue;
	}

	public void setxValue(double xValue) {
		this.xValue = xValue;
	}

}
