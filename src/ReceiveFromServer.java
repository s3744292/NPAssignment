import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Receive msg from server and print it
 *
 * @author xiaoyu chen s3517183 from RMIT
 */
public class ReceiveFromServer implements Runnable {
    private String receiveString;
    private final String RegLabel = "reg";
    private final String StartLabel = "start";
    private final String EndLabel = "end";
    private final String QuitLabel = "q";

    @Override
    public void run() {
        try {
            // receive msg and print it
            while ((receiveString = Client.dataInputStream.readUTF()) != null) {
                if (receiveString.equals(QuitLabel)){
                    break;
                }
                switch (receiveString){
                    case RegLabel:
                        Client.isRegister = true;
                        break;
                    case StartLabel:
                        Client.isStart = true;
                        break;
                    case EndLabel:
                        Client.isStart = false;
                        break;
                    default:
                        System.out.println(receiveString);
                        break;
                }

            }
        }
        catch (SocketTimeoutException t) {
            System.out.println("Disconnect as socket timeout");
            try {
                // notify server this client is going to quit now
                Client.dataOutputStream.writeUTF("q");
            } catch (IOException e) {
            }
            System.exit(1);
        } catch (IOException e) {
        } finally {
            handleResources();
        }
    }// end of run

    /**
     * close resource
     */
    private void handleResources(){
        try {
            if (Client.dataOutputStream != null){
                Client.dataOutputStream.close();
            }
            if (Client.dataInputStream != null){
                Client.dataInputStream.close();
            }
            if (Client.socket != null){
                Client.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Disconnect Successfully");
        System.exit(0);
    }// end of handleResources

}// end of ReceiveFromServer
