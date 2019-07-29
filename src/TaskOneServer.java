import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Task A: Single player version
 *
 * Once Server is running, it will listening port to accept client socket
 *
 * Server will quit when enter q in Server console
 *
 * @author Xiaoyu chen s3517183 from RMIT
 */
public class TaskOneServer {

    private static final int Port = 10086;
    private static ConcurrentLinkedQueue<ProcessGame> players = new ConcurrentLinkedQueue<>();
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    /**
     * Main method
     * @param args
     */
    public static void main(String [] args) {
        System.out.println("Server is running");
        new TaskOneServer().start();
    } // end of main


    /**
     * Two Threads
     * One for scanning server console to detect q to quit
     * other is for handle client game process
     */
    public void start(){

        int clientCount = 0;

        try {
            serverSocket = new ServerSocket(Port);
            while(true) {
                // listen client to connect
                socket = serverSocket.accept();

                // only allow one player to play
                if (players.size() ==  1){
                    socket.close();
                    continue;
                }
                clientCount = clientCount + 1;

                // new a thread to handle this client
                ProcessGame processGame = new ProcessGame(socket,clientCount);
                new Thread(processGame).start();
                players.offer(processGame);
            }
        } catch (IOException e) {
        }
        finally {
            try {
                socket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Server is shutting down now.");
            System.exit(0);
        }
    }

    /**
     * This is used for handle each client
     */
    class ProcessGame implements Runnable{

        private Socket socket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private int clientCount;
        private int guessTimes = 1;
        private int maxGuessTimes = 5;
        private String receiveString;
        private int targetNumber;
        final String QuitLabel = "q";
        private int guessNumber;

        ProcessGame(Socket socket,int clientCount){
            this.socket = socket;
            this.clientCount = clientCount;
        }

        /**
         * create input and output resources for this client
         */
        @Override
        public void run() {
            try{
                System.out.println("Client " + clientCount + " has been connected.");
                // using dataInputStream to receive msg, dataOutputStream to send
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                targetNumber = getRandom(10);
                System.out.println("\nThe Target number is: " + targetNumber);

                // when receive q, then close this client resource
                while (true) {
                    receiveString = dataInputStream.readUTF();
                    if (receiveString.equals(QuitLabel)) {
                        players.poll();
                        System.out.println(players.size());
                        break;
                    }
                    handleGameProcess();
                }

            } catch (IOException e) {
            } finally {
                try{
                    socket.close();
                    dataInputStream.close();
                    dataOutputStream.close();

                } catch (IOException e) {
                }
                System.out.println("Client " + clientCount + " has disconnected Successful");
            }
        }// end of run


        /**
         * Processing game
         */
        public void handleGameProcess(){
            try{
                // check if reach max guess times
                if (guessTimes == maxGuessTimes){
                    dataOutputStream.writeUTF("The Game is over , Please enter q o quit");
                    return;
                }

                guessNumber = Integer.parseInt(receiveString);

                if ( guessNumber == targetNumber){
                    dataOutputStream.writeUTF("Client " + clientCount + " guess number: " +
                            guessNumber + " in " + guessTimes + " times.\n" +
                            "Hi, you win the game.");
                    System.out.println("Client " + clientCount + " guess number:" +
                            guessNumber + " in " + guessTimes + " times.");
                    guessTimes = maxGuessTimes;
                } else if (targetNumber > guessNumber){
                    dataOutputStream.writeUTF("Client " + clientCount + " guess number: " +
                            guessNumber + " in " + guessTimes + " times\n" +
                            "Tips: Your guess number is smaller than target number");
                    System.out.println("Client " + clientCount + " guess number:" +
                            guessNumber + " in " + guessTimes + " times.");
                    guessTimes ++;
                } else if (targetNumber < guessNumber){
                    dataOutputStream.writeUTF("Client " + clientCount + " guess number: " +
                            guessNumber + " in " + guessTimes + " times.\n" +
                            "Tips: Your guess number is bigger than target number");
                    System.out.println("Client " + clientCount + " guess number:" +
                            guessNumber + " in " + guessTimes + " times.");
                    guessTimes ++;
                }
            }catch (IOException e){
                return;
            }
        } // end of handleGameProcess


        /**
         * Create a random int number from 0 to max-1
         *
         * @return a random number
         */
        public int getRandom(int max){
            return new Random().nextInt(max);
        } // end of getRandom
    }// end of ProcessGame

} // end of Server