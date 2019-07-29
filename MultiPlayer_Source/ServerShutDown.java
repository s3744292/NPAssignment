import java.util.Scanner;

/**
 * This is for server to quit
 * @author xiaoyu chen s3517183 from RMIT
 */
public class ServerShutDown implements Runnable{
    Scanner scanner;
    final String QuitLabel = "q";

    ServerShutDown(Scanner scanner){
        this.scanner = scanner;
    }

    @Override
    public void run() {
        System.out.println("Welcome to guess game server");
        System.out.println("Remember: enter q to quit server");
        try{
            while (scanner.hasNext()){
                String msg = scanner.nextLine();
                if (msg.equals(QuitLabel)){
                    Server.logger.info("Now server is going to quit");
                    // send q label to all the client to force their quit
                    for (int i = 0; i < Server.clients.size(); i++) {
                        Server.clients.peek().sendMsg(QuitLabel);
                        Server.clients.poll().closeResources();
                    }
                    // close server resource
                    Server.handleResource();
                    System.out.println("All resources have closed.");
                    break;
                }
            }
        }finally {
            if (scanner != null){
                scanner.close();
            }
            System.exit(0);
            Server.communicatelogger.info("Server is shutting down");
        }
    }// end of run

}// end of ServerShutDown
