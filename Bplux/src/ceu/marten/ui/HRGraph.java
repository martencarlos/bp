package ceu.marten.ui;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;
import android.graphics.Paint.Align;
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

	@SuppressWarnings("deprecation")
	public HRGraph(android.content.Context context, String title) {
		// SET THE SERIE STYLE
		style = new GraphViewSeriesStyle(randomColor(), 2); // 2 -> thickness
		
		// INIT SERIE DATA
		serie = new GraphViewSeries(title, style, new GraphViewData[] {
				new GraphViewData(1.0, 125), new GraphViewData(1.5, 10) });
		// INIT GRAPHVIEW
		graphView = new LineGraphView(context, "");

		// ADD SERIES TO GRAPHVIEW and SET SCROLLABLE
		graphView.addSeries(serie);
		graphView.setViewPort(2, Double.parseDouble(context.getResources().getString(R.string.graph_viewport_size)));
		graphView.setScalable(true);
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {  
			   @Override  
			   public String formatLabel(double value, boolean isValueX) {  
			      if (isValueX) {
			    	  if(value<0)
			    		  return "00:00:00";
			    	  String strValue = String.format(Locale.getDefault(),"%02d:%02d:%02d", 
			    			  TimeUnit.MILLISECONDS.toHours((long)value),
			    			  TimeUnit.MILLISECONDS.toMinutes((long)value),
			    			  TimeUnit.MILLISECONDS.toSeconds((long)value) - 
			    			  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)value)));

			    			  return strValue;
			      }
			      else
			    	  return String.valueOf(((Double)value).intValue()); //vertical labels value
			   }  
		}); 
		
		// SET THE GRAPH VIEW STYLE
		GraphViewStyle graphStyle= new GraphViewStyle();
		graphStyle.setNumHorizontalLabels(Integer.parseInt(context.getResources().getString(R.string.graph_numberof_horizontal_labels)));
		graphStyle.setVerticalLabelsAlign(Align.LEFT);
		graphStyle.setGridColor(context.getResources().getColor(R.color.light_grey));
		graphStyle.setHorizontalLabelsColor(context.getResources().getColor(R.color.grey));
		graphStyle.setVerticalLabelsColor(context.getResources().getColor(R.color.grey));
		graphStyle.setTextSize(Float.parseFloat(context.getResources().getString(R.string.graph_labels_text_size)));
		graphStyle.setVerticalLabelsWidth(Integer.parseInt((context.getResources().getString(R.string.graph_vertical_labels_width))));

		graphView.setGraphViewStyle(graphStyle);
		
		// SET THE LEGEND
		graphView.setLegendWidth((Integer.parseInt((context.getResources().getString(R.string.graph_legend_width)))));
		graphView.setLegendAlign(LegendAlign.BOTTOM);
		graphView.setShowLegend(true);
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
