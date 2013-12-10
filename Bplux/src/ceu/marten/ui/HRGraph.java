package ceu.marten.ui;

import java.text.DecimalFormat;

import android.graphics.Color;
import android.util.DisplayMetrics;

import ceu.marten.bplux.R;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * Created by martencarlos on 25/07/13.
 */

public class HRGraph{

	private GraphViewSeries serie;
	private GraphViewSeriesStyle style;
	private double xValue;
	private GraphView graphView;
	private double currentValue;
	private DecimalFormat df = new DecimalFormat("#.##");

	public HRGraph(android.content.Context context, String title) {
		// STYLE
		style = new GraphViewSeriesStyle(Color.rgb(23, 118, 8), 2); // green and
																	// thickness
		
		// INIT SERIE DATA
		serie = new GraphViewSeries("BPM", style, new GraphViewData[] {
				new GraphViewData(1.0, 125), new GraphViewData(1.5, 10) });
		// INIT GRAPHVIEW
		graphView = new LineGraphView(context, title);

		// ADD SERIES TO GRAPHVIEW and SET SCROLLABLE
		graphView.addSeries(serie);
		// graphView.setScrollable(true);
		graphView.setViewPort(2, 150);
		graphView.setScalable(true);
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {  
			   @Override  
			   public String formatLabel(double value, boolean isValueX) {  
			      if (isValueX) {  
			         if(value>1000){
			        	 currentValue=value/1000;
			        	 if(currentValue>60)
			        		 return String.valueOf((int)Math.floor(currentValue/60))+","+(int)(currentValue%60);
			        	 else
			        		 return String.valueOf(df.format(currentValue));
			         }
			        	 
			      }  
			      return null; // let graphview generate Y-axis label for us  
			   }  
			}); 
		
		
		GraphViewStyle gvs= new GraphViewStyle();
		gvs.setNumHorizontalLabels(5);
		gvs.setHorizontalLabelsColor(context.getResources().getColor(R.color.grey));
		gvs.setVerticalLabelsColor(context.getResources().getColor(R.color.grey));
		switch (context.getResources().getDisplayMetrics().densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			gvs.setTextSize((float) 10);
		    break;
		case DisplayMetrics.DENSITY_MEDIUM:
			gvs.setTextSize((float) 14);
		    break;
		case DisplayMetrics.DENSITY_HIGH:
			gvs.setTextSize((float) 18);
		    break;
		case DisplayMetrics.DENSITY_XHIGH:
			gvs.setTextSize((float) 30);
		    break;
		}
		graphView.setGraphViewStyle(gvs);

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
