import jakarta.jms.*;
import javafx.util.Pair;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.math3.stat.StatUtils;

import java.util.Arrays;

public class StatCalcultator implements MessageListener{

    private final double[][] arrayDouble;
    private final int countOfFloat;
    private final int devNum;
    private int pointer[];
    private double maximum[];
    private double minimum[];

    //既是消费者也是生产者
    private static String brokerURL = "tcp://localhost:61616";
    private static ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Topic topic;

    public StatCalcultator(int CountOfFloat,int devNum) throws JMSException {
        countOfFloat = CountOfFloat; // 统计窗口 N
        this.devNum = devNum; // 设备总数
        arrayDouble = new double[this.devNum][countOfFloat]; // arrayDouble[i][] 表示第i个设备的最近N个数据序列
        pointer = new int[devNum];
        Arrays.fill(pointer,0);
        maximum = new double[this.devNum];
        Arrays.fill(maximum,Double.MIN_VALUE);
        minimum = new double[this.devNum];
        Arrays.fill(minimum,Double.MAX_VALUE);

        // 生产者
        factory = new ActiveMQConnectionFactory(brokerURL);
        connection = factory.createConnection();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        topic = session.createTopic("stat_queue");
        producer = session.createProducer(topic);

        connection.start();
    }

    public void insert(int rank,double a) {
        arrayDouble[rank][pointer[rank]] = a;
        pointer[rank] = (pointer[rank] + 1) % countOfFloat;
        if (a > maximum[rank]) {
            maximum[rank] = a;
        }
        if (a < minimum[rank]) {
            minimum[rank] = a;
        }
    }

    public double mean(int rank) {
        return StatUtils.mean(arrayDouble[rank]);
    }

    public double variance(int rank) {
        return StatUtils.variance(arrayDouble[rank]);
    }
    public double getMaximum(int rank) {
        return maximum[rank];
    }
    public double getMinimum(int rank) {
        return minimum[rank];
    }

    public int getCountOfFloat() {
        return countOfFloat;
    }

    @Override
    public void onMessage(Message message) {
        try {
            Pair<Integer,Double> pair = (Pair<Integer, Double>)((ObjectMessage)message).getObject();
            insert(pair.getKey().intValue(),pair.getValue().doubleValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public double[] getArrayDouble(int rank) {
        return arrayDouble[rank];
    }


    public void sendMeanAndVariance() {
        // 把全部设备平均值发送到stat_queue
        for(int i = 0; i < devNum; i++)
        {
            try{
                double[] tuple = new double[5];
                tuple[0] = i;
                tuple[1] = mean(i);
                tuple[2] = variance(i);
                tuple[3] = getMaximum(i);
                tuple[4] = getMinimum(i);

                Message message = session.createObjectMessage(tuple);
                producer.send(message);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }




}
