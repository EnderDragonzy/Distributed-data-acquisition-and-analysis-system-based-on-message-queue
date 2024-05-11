import jakarta.jms.JMSException;

public class Client {
    public static void main(String[] args) throws JMSException{
        int devNum = 100;
        for(int i = 0; i < devNum; i++)
        {
            int rank = i; // rank 代表模拟设备id
            DevThread devThread = new DevThread(rank,"DevTopic");
            devThread.start();
        }
    }

}
