import javafx.util.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 实时绘图
 */
class RealTimeChartListener implements MessageListener {
    public void onMessage(Message message) {
        try {
            Pair<Integer,Double> pair = (Pair<Integer, Double>) ((ObjectMessage)message).getObject();
            int rank = pair.getKey().intValue();
            double data = pair.getValue().doubleValue();
            if(java.util.Objects.nonNull(RealTimeChart.timeSeries[rank]))
            {
                RealTimeChart.timeSeries[rank].add(new Millisecond(), data);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MeanAndVarianceListener implements MessageListener{
    public void onMessage(Message message)
    {
        try{
            double[] tuple = (double[]) ((ObjectMessage)message).getObject();
            int rank = (int)tuple[0];
            double mean = tuple[1];
            double variance = tuple[2];
            double max = tuple[3];
            double min = tuple[4];
            // 把均值和方差赋值给对应的rank显示缓冲区......
            RealTimeChart.meanText[rank].setText(String.valueOf(mean));
            RealTimeChart.varText[rank].setText(String.valueOf(variance));
            RealTimeChart.maxText[rank].setText(String.valueOf(max));
            RealTimeChart.minText[rank].setText(String.valueOf(min));

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}

public class RealTimeChart extends ChartPanel {

    private static final long serialVersionUID = 1L;
    public static TimeSeries[] timeSeries ;
    static RealTimeChart chart;
    private final int devNum;
    static int cnt = 0;
    public static JTextField[] meanText;
    public static JTextField[] varText;
    public static JTextField[] maxText;
    public static JTextField[] minText;

    public RealTimeChart(String chartContent, String title, String yAxisName, int devNum,int rank) {
        super(createChart(chartContent, title, yAxisName,devNum,rank));

        this.devNum = devNum;
    }

    private static JFreeChart createChart(String chartContent, String title, String yAxisName,int devNum,int rank) {
        if(cnt == 0)
        {
            timeSeries = new TimeSeries[devNum];
            meanText = new JTextField[devNum];
            varText = new JTextField[devNum];
            maxText = new JTextField[devNum];
            minText = new JTextField[devNum];

            for(int i = 0; i < devNum; i++)
            {
                timeSeries[i] = new TimeSeries(chartContent, org.jfree.data.time.Millisecond.class);
                meanText[i] = new JTextField(10);
                varText[i] = new JTextField(10);
                maxText[i] = new JTextField(10);
                minText[i] = new JTextField(10);
            }
        }
        cnt++;



        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection(timeSeries[rank]);
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, "时间(秒)", yAxisName, timeseriescollection, true, true, false);
        ValueAxis valueaxis = jfreechart.getXYPlot().getDomainAxis();
        valueaxis.setAutoRange(true);
        valueaxis.setFixedAutoRange(3000D);

        return jfreechart;
    }



    public static void main(int devNum, int[] rank) {
        // 设置显示样式，避免中文乱码
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        standardChartTheme.setExtraLargeFont(new Font("微软雅黑", Font.BOLD, 20));
        standardChartTheme.setRegularFont(new Font("微软雅黑", Font.PLAIN, 15));
        standardChartTheme.setLargeFont(new Font("微软雅黑", Font.PLAIN, 15));
        ChartFactory.setChartTheme(standardChartTheme);




        for (int i : rank)
        {
            JFrame frame = new JFrame("分布式数据采集显示微服务");
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

            RealTimeChart realTimeChart = new RealTimeChart("分布式数据采集动态折线图", "分布式数据采集曲线 设备:"+i, "数值", devNum,i);
            chart = realTimeChart;
            JLabel meanLabel = new JLabel("mean:");
            panel.add(meanLabel);
            panel.add(meanText[i]);

            JLabel varLabel = new JLabel("var:");
            panel.add(varLabel);
            panel.add(varText[i]);

            JLabel maxLabel = new JLabel("max:");
            panel.add(maxLabel);
            panel.add(maxText[i]);

            JLabel minLabel = new JLabel("min:");
            panel.add(minLabel);
            panel.add(minText[i]);

            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(realTimeChart, BorderLayout.CENTER);
            frame.getContentPane().add(panel,BorderLayout.SOUTH);
//            frame.getContentPane().add(panel2,BorderLayout.SOUTH);

            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowevent) {
                    System.exit(0);
                }
            });
        }
//        JFrame frame2 = new JFrame("分布式数据采集显示微服务");
//        RealTimeChart realTimeChart2 = new RealTimeChart("分布式数据采集动态折线图", "分布式数据采集曲线 设备:"+rank2, "数值", devNum,rank2);
//        chart = realTimeChart2;
//        frame2.getContentPane().add(realTimeChart2, new BorderLayout().CENTER);
//        frame2.pack();
//        frame2.setVisible(true);
//        frame2.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent windowevent) {
//                System.exit(0);
//            }
//        });
    }
}