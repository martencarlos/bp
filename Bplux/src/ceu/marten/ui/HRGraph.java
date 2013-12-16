package ceu.marten.ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.DisplayMetrics;
import ceu.marten.bplux.R;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * Created by martencarlos on 25/07/13.
 */

public class HRGraph implements Serializable{

	private static final long serialVersionUID = -5122704369223869018L;

	
	private GraphViewSeries serie;
	private GraphViewSeriesStyle style;
	private double xValue;
	private GraphView graphView;
	private Date currentValue;
	//private DecimalFormat decimalFormat = new DecimalFormat("#.##"); 
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss",Locale.UK);

	@SuppressWarnings("deprecation")
	public HRGraph(android.content.Context context, String title) {
		int legendWidth=0;
		// STYLE
		style = new GraphViewSeriesStyle(randomColor(), 2); // green and
																	// thickness
		
		// INIT SERIE DATA
		serie = new GraphViewSeries(title, style, new GraphViewData[] {
				new GraphViewData(1.0, 125), new GraphViewData(1.5, 10) });
		// INIT GRAPHVIEW
		graphView = new LineGraphView(context, "");

		// ADD SERIES TO GRAPHVIEW and SET SCROLLABLE
		graphView.addSeries(serie);
		// graphView.setScrollable(true);
		graphView.setViewPort(2, 2000);
		
		graphView.setScalable(true);
		
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {  
			   @Override  
			   public String formatLabel(double value, boolean isValueX) {  
			      if (isValueX) {
			    	  if(value<0)
			    		  return "00:00:00";
			    	  currentValue = new Date((long)(value));
			    	  return timeFormat.format(currentValue);
			      }
			      else
			    	  return String.valueOf(((Double)value).intValue());
			     
			   }  
			}); 
		
		GraphViewStyle graphStyle= new GraphViewStyle();
		graphStyle.setNumHorizontalLabels(3);
		
		graphStyle.setVerticalLabelsAlign(Align.LEFT);
		graphStyle.setGridColor(context.getResources().getColor(R.color.light_grey));
		graphStyle.setHorizontalLabelsColor(context.getResources().getColor(R.color.grey));
		graphStyle.setVerticalLabelsColor(context.getResources().getColor(R.color.grey));
		
		switch (context.getResources().getDisplayMetrics().densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			graphStyle.setTextSize((float) 10);
		    break;
		case DisplayMetrics.DENSITY_MEDIUM:
			graphStyle.setTextSize((float) 14);
			graphStyle.setVerticalLabelsWidth(40);
			legendWidth = 100;
			
		    break;
		case DisplayMetrics.DENSITY_HIGH:
			graphStyle.setTextSize((float) 18);
			graphStyle.setVerticalLabelsWidth(50);
			legendWidth = 150;
		    break;
		case DisplayMetrics.DENSITY_XHIGH:
			graphStyle.setTextSize((float) 30);
			graphStyle.setVerticalLabelsWidth(70);
			legendWidth = 200;
			
		    break;
		}
		graphView.setGraphViewStyle(graphStyle);
		graphView.setLegendWidth(legendWidth);
		graphView.setLegendAlign(LegendAlign.BOTTOM);
		graphView.setShowLegend(true);

		// Current X value
		xValue = 2d;
	}
	
	private int randomColor() {
		int min = 100, max = 180;
		int r = 10 + (int)(Math.random()*50);
		int g = min + (int)(Math.random()*max); 
		int b = min + (int)(Math.random()*max); 
		return Color.rgb(r, g, b);
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
