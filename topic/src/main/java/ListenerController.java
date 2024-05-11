import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 消息监听控制器
 */
public class ListenerController {

    public static void main(String[] args) throws JMSException {
        String brokerURL = "tcp://localhost:61616";
        ActiveMQConnectionFactory factory = null;
        Connection connection = null;
        Session session = null;
        Topic topic = null;
        MessageConsumer varianceConsumer = null;
        MessageConsumer averageConsumer = null;
        MessageConsumer outlierConsumer = null;
        MessageConsumer realTimeConsumer = null;
        MessageConsumer statConsumer = null;
        StatCalcultator statCalcultator = null;
        RealTimeChartListener realTimeChartListener = null;

        ActiveMQConnectionFactory factory2 = null;
        Connection connection2 = null;
        Session session2 = null;
        Topic topic2 = null;
        MessageConsumer realTimeConsumer2 = null;
        MeanAndVarianceListener realTimeMeanAndVarListener = null;


        try {
            factory = new ActiveMQConnectionFactory(brokerURL);
            factory.setTrustAllPackages(true);
            connection = factory.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic("DevTopic");

            statConsumer = session.createConsumer(topic);
            realTimeConsumer = session.createConsumer(topic);

            statCalcultator = new StatCalcultator(100,100);
            realTimeChartListener = new RealTimeChartListener();


            factory2 = new ActiveMQConnectionFactory(brokerURL);
            factory2.setTrustAllPackages(true);
            connection2 = factory2.createConnection();

            session2 = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic2 = session2.createTopic("stat_queue");
            realTimeConsumer2 = session2.createConsumer(topic2);

            realTimeMeanAndVarListener = new MeanAndVarianceListener();

            realTimeConsumer2.setMessageListener(realTimeMeanAndVarListener);


            int[] rank = {1,2,3,4,5};
            RealTimeChart.main(100,rank);

            statConsumer.setMessageListener(statCalcultator);
            realTimeConsumer.setMessageListener(realTimeChartListener);

            connection.start();
            connection2.start();

            while (true){
                statCalcultator.sendMeanAndVariance();
//                System.out.println("Mean: ");
//                statCalcultator.sendVariance();
//                System.out.println("Var: " );
                Thread.sleep(1000);
            }
            // Pause
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

}