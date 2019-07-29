import java.util.*;
import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;


/**
 * This is server for play guess game
 *
 * @author xiaoyu chen s3517183 from RMIT
 */
public class Server {

    private static Socket socket;
    static Logger logger;
    static Logger communicatelogger;
    static ServerSocket serverSocket;
    static ArrayList<String> ranking = new ArrayList<>();
    static ConcurrentLinkedQueue<HandleMultiClient> clients = new ConcurrentLinkedQueue<>();
    static ArrayList<HandleMultiClient> onPlaying = new ArrayList<>();
    static ArrayList<HandleMultiClient> pendingQueue = new ArrayList<>();
    static HashMap<HandleMultiClient, String> playerNames= new HashMap<>();

    static volatile int onPlayingNum = -1;
    static volatile boolean isStart = false;
    static volatile boolean isPrepare = false;
    static int random;
    static int countCompleted = 0;
    static int roundNo = 0;
    static int port = 10086;
    static int clientCount = 1;
    static final int preparingTime = 1000*60*2;
    static final int playingTime = 1000*60*3;

    /**
     * This is main method
     *
     * @param args
     */
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        new Thread(new ServerShutDown(scanner)).start();
        serverGame();

    } // end of main

    /**
     * This method is used to create server socket to waiting client connect.
     * Create a thread for each client.
     * Create a thread for manage game resources.
     */
    public static void serverGame() {

        try {
            gameLogger("GamingLog");
            comLogger("CommunicateLog");
            System.out.println("Server is running");

            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            communicatelogger.info("Server is running now");
            HandleGameProcess handleGameProcess = new HandleGameProcess();
            Thread thread = new Thread(handleGameProcess);
            thread.start();
            // keep alive
            while (true) {
                socket = serverSocket.accept();
                HandleMultiClient currentClient = new HandleMultiClient(socket, clientCount);
                clients.offer(currentClient);
                new Thread(currentClient).start();

                System.out.println("Client " + clientCount + " connect");
                communicatelogger.info("Client " + clientCount + " connect, " +
                        "remote socket address is " + socket.getRemoteSocketAddress());
                clientCount ++;
            }
        } catch (IOException e) {
            logger.warning(e.toString());
        } finally {
            handleResource();
        }
    }// end of serverGame

    /**
     * Create Logger for server
     * @throws IOException
     */
    private static void comLogger(String logType) throws IOException{
        communicatelogger = Logger.getLogger(logType);
        communicatelogger.setLevel(Level.INFO);
        String yyyyMMdd = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        FileHandler fileHandler;
        fileHandler = new FileHandler(yyyyMMdd+"_"+logType+".log", true);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LoggerFormatter());
        communicatelogger.addHandler(fileHandler);
        communicatelogger.setUseParentHandlers(false);
    }// end of comLogger

    /**
     * Close resources
     */
    public static void handleResource(){
        try {
            if (socket != null){
                socket.close();
            }
            if (serverSocket != null){
                serverSocket.close();
            }
            communicatelogger.info("Serve is shutting down");
        } catch (IOException e) {
            communicatelogger.warning(e.toString());
        }
    }// end of handleResource


    /**
     * Create Logger for server
     * @throws IOException
     */
    private static void gameLogger(String logType) throws IOException{
        logger = Logger.getLogger(logType);
        logger.setLevel(Level.INFO);
        Date date = new Date();
        String yyyyMMdd = new SimpleDateFormat("dd-MM-yyyy").format(date);
        FileHandler fileHandler = new FileHandler(yyyyMMdd+"_"+logType+".log", true);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LoggerFormatter());
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
    }// end of gameLogger


}// end of server
