import java.net.Socket;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * This is client that based on TCP socket
 *
 * After server start, client can connect to server
 *
 * Firstly, register to join play queue
 *
 * e means exit from current game
 *
 * p means play again
 *
 * q means quit the client
 *
 * @author xiaoyu chen s3517183 from RMIT
 */
public class Client {

    static Socket socket;
    static Scanner scanner;
    static DataInputStream dataInputStream;
    static DataOutputStream dataOutputStream;
    static volatile boolean isRegister = false;
    static volatile boolean isStart = false;
    static final int socketTimeout = 1000*60*5;
    static final String targetIP = "localhost";


    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args){

        System.out.println("Welcome to Number Guess");
        gameStart();

    }// end of main


    /**
     * Start to run client
     */
    private static void gameStart() {

        try {
            socket = new Socket(targetIP, 10086);
            socket.setSoTimeout(socketTimeout);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            scanner = new Scanner(System.in);

            //create two thread, one handle receive msg, one handle send msg
            new Thread(new SendToServer()).start();
            new Thread(new ReceiveFromServer()).start();

            System.out.println("Register name");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }// end of gameStart

}// end of Client