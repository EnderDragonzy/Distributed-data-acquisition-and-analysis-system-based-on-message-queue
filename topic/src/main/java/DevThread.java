import jakarta.jms.*;
import javafx.util.Pair;
import org.apache.activemq.ActiveMQConnectionFactory;

public class DevThread extends Thread{
    int rank;


    private static String brokerURL = "tcp://localhost:61616";
    private static ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Topic topic;


    public DevThread(int rank,String topicName) throws JMSException {
        this.rank = rank;

        factory = new ActiveMQConnectionFactory(brokerURL);
        connection = factory.createConnection();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        topic = session.createTopic(topicName);
        producer = session.createProducer(topic);

        connection.start();
    }

    public void close() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }

    public void run()
    {
        while(true)
        {
            double data = Math.random() * 100; // 生成[0.0,100.0)范围内的随机数
            Pair<Integer,Double> pair = new Pair<Integer, Double>(rank,data);
            try{
                Message message = session.createObjectMessage(pair);
                producer.send(message);
                System.out.println(String.format("Dev[%d] send a message!",rank));
            }catch (Exception e){
                e.printStackTrace();
            }

            try{
                Thread.sleep(100);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
