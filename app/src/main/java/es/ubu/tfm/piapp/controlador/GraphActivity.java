package es.ubu.tfm.piapp.controlador;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;


import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Handler;

import es.ubu.tfm.piapp.R;

/**
 * Actividad de Gráficos, pintará los datos recibidos por el dispositivo Bluetooth.
 * @author    Sandra Ajates González
 * @version   1.0
 */
public class GraphActivity extends Activity {

    /**
     * DEfinición del GraphLayout.
     */
    private RelativeLayout graphLayout;

    /**
     * Definición tipo gráfico.
     */
    private LineChart mChart;

    /**
     * Instancia objeto de la clase principal.
     */
    private MainActivity mainPrinc = new MainActivity();

    /**
     * Establece el dataset para múltiples líneas en el gráfico.
     */
    private XYMultipleSeriesDataset dataset;

    /**
     * Definición de la vista del gráfico.
     */
    private GraphicalView graphicalView;

    /**
     * Obtiene de la clase principal el número de datos para la gráfica.
     */
    private double addX = mainPrinc.getPosEjeX();//30;
    /**
     * Definición parámetros necesarios.
     */
    private double plus = 6;
    private double minus = 13;

    /**
     * Obtención de un handler.
     */
    private Handler handler = new Handler();

    /**
     * Instancia el método de ejecución.
     */

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            dataset.getSeriesAt(0).add(addX, plus);
            dataset.getSeriesAt(1).add(addX, minus);
            addX++;
            plus++;
            minus--;
            graphicalView.repaint();
            if (addX < 20) handler.postDelayed(updateRunnable, 1000);
        }
    };

    /**
     * Método onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gr_activity);
        graphLayout = (RelativeLayout) findViewById(R.id.graphLayout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        /**
         * Establece los títulos de las dos líneas de datos.
         */
        String[] titles = new String[] { "Velocidad Real", "Velocidad Deseada" };
        List<double[]> x = new ArrayList<double[]>();


        /**
         * Obtiene los datos recibidos por Bluetooth.
         */
        double [] vecValoresEjeX = mainPrinc.getVelEncoder();
        int valor = mainPrinc.getVelDeseada();
        int logGraph = mainPrinc.getPosEjeX();
        double [] vecValoresEjeY = new double[logGraph];
        double [] vec = new double[logGraph];


        for (int i = 0; i < titles.length; i++) {
            for (int j = 0; j < logGraph; j++) {
                vec[j] = j;
            }
            x.add(vec);
        }

        List<double[]> values = new ArrayList<double[]>();

        //Toast.makeText(getApplicationContext(), "salida" + " " + titles.length, Toast.LENGTH_SHORT).show();

        /**
         * Establece en cada punto del gráfico la velocidad deseada, siempre constante.
         */
        for (int i=0; i < logGraph; i++) {
            vecValoresEjeY[i] = valor;
        }

        for (int i=0; i < titles.length; i++) {
            values.add(vecValoresEjeX);
            values.add(vecValoresEjeY);
        }

        /**
         * Define los colores de las líneas de los gráficos.
         */
        int[] colors = new int[] { Color.BLUE, Color.GREEN };
        /**
         * Define los puntos de definición de los datos imprimidos.
         */
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE,	PointStyle.DIAMOND };
        /**
         * REnderizado de los datos.
         */
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
                    .setFillPoints(true);
        }

        /**
         * Definición de la leyenda del gráfico.
         */
        setChartSettings(renderer, "Velocidad", "Tiempo (ms)",
                "Velocidad Encoder", 0, 12, 0, 260, Color.LTGRAY, Color.LTGRAY);
        /**
         * Establece los parámetros necesarios de renderizado.
         */
        renderer.setXLabels(12);
        renderer.setYLabels(10);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Align.RIGHT);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setZoomButtonsVisible(true);
        renderer.setPanLimits(new double[] { 0, logGraph, 0, 260 });
        renderer.setZoomLimits(new double[] { 0, logGraph, 0, 260 });

        dataset = buildDataset(titles, x, values);
        /**
         * Llamada al método para pintar el gráfico.
         */
        graphicalView = ChartFactory.getLineChartView(
                getApplicationContext(), dataset, renderer);

        setContentView(graphicalView);
        handler.postDelayed(updateRunnable, 1000);
    }

    /**
     * Creal el dataset de datos.
     * @param titles valores a pintar en los títulos.
     * @param xValues valores del eje x.
     * @param yValues valores del eje y.
     * @return dataset datos a pintar.
     */
    private XYMultipleSeriesDataset buildDataset(String[] titles,
                                                 List<double[]> xValues, List<double[]> yValues) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        addXYSeries(dataset, titles, xValues, yValues, 0);
        return dataset;
    }

    /**
     * Establece las series X e Y.
     * @param dataset pull de datos a pintar.
     * @param titles valores a pintar en los títulos.
     * @param xValues valores del eje x.
     * @param yValues valores del eje y.
     * @param scale escala de datos a pintar.
     */
    private void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles,List<double[]> xValues, List<double[]> yValues, int scale) {
        int length = titles.length;
        for (int i = 0; i < length; i++) {
            XYSeries series = new XYSeries(titles[i], scale);
            double[] xV = xValues.get(i);
            double[] yV = yValues.get(i);
            int seriesLength = xV.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(xV[k], yV[k]);
            }
            dataset.addSeries(series);
        }
    }

    /**
     * Render de datos.
     * @param colors colores a pintar.
     * @param styles estilos a establecer.
     * @return buildRenderer datos renderizados.
     */
    private XYMultipleSeriesRenderer buildRenderer(int[] colors,
                                                   PointStyle[] styles) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setRenderer(renderer, colors, styles);
        return renderer;
    }

    /**
     * Establece el renderizado de datos.
     * @param renderer datos a renderizar.
     * @param colors colores a usar.
     * @param styles estilos a establecer.
     */
    private void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors,
                             PointStyle[] styles) {
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[] { 25, 30, 15, 28 });
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
    }

    /**
     * Establece las propiedades del gráfico.
     * @param renderer datos a renderizar.
     * @param title título de la gráfica.
     * @param xTitle título del eje X.
     * @param yTitle título del eje Y.
     * @param xMin valor mínimo del eje X.
     * @param xMax valor máximo del eje X.
     * @param yMin valor mínimo del eje Y.
     * @param yMax valor máximo del eje Y.
     * @param axesColor colores de los ejes a usar.
     * @param labelsColor colores de los labels a usar.
     */
    private void setChartSettings(XYMultipleSeriesRenderer renderer,
                                  String title, String xTitle, String yTitle, double xMin,
                                  double xMax, double yMin, double yMax, int axesColor,
                                  int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
    }
}
