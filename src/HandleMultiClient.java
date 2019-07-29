import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  This method mainly handle register, guess number processing
 *
 *  exit from game, quit from game and play game again
 *
 * @author xiaoyu chen s3517183 from RMIT
 */
public class HandleMultiClient implements Runnable{

    Socket socket;
    String receiveMsg;
    Object object = "A";
    DataInputStream dataInputStream=null;
    DataOutputStream dataOutputStream=null;
    HandleMultiClient handleMultiClient=null;
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    boolean isReg = false;
    boolean isComplete = false;
    int count = 1;
    int guess = 0;
    final String quitLabel = "q";
    final String exitLabel = "e";
    final String playAgainLabel = "p";
    String clientName="";
    int clientCount;

    HandleMultiClient(Socket socket,int clientCount) {
        this.socket = socket;
        this.clientCount = clientCount;
    }

    /**
     * override runnable method
     */
    @Override
    public void run() {
        try {
            // keep client alive
            while (true) {
                dataInputStream = new DataInputStream(socket.getInputStream());
                receiveMsg = dataInputStream.readUTF();
                handleMultiClient = getCurrentThread();

                // check if quit in unregister
                if (!isReg){
                    if (receiveMsg.equals(quitLabel)){ handleMultiClient.sendMsg(quitLabel);break; }
                }

                // check if register
                if (!isRegister()){ continue; }

                // handle game processing
                synchronized (object){
                    // handle game if start
                    if (Server.isStart){
                        handleExit(receiveMsg);

                        // check if have finished this game
                        if (isComplete){
                            if (handleNoneGameResponse(receiveMsg)){ break; }
                        }else {
                            processGame(receiveMsg);
                        }
                    }else {
                        if (handleNoneGameResponse(receiveMsg)){ break; }
                    }
                }// end of synchronized
            }// end of while
        } catch (IOException e) {
            Server.logger.info("Client has exited");
        } finally {
            closeResources();
        }
    }// end of run


    /**
     * playernames that will hold thread and player's name, use to check if register name
     * pendingqueue is use to hold players thread who want to play game
     *
     * @return
     */
    private boolean isRegister(){
        synchronized (object){
            if (!Server.playerNames.containsKey(handleMultiClient)){
                if (Server.playerNames.containsValue(receiveMsg)){
                    handleMultiClient.sendMsg("This name has been used by other player, register again");
                    Server.logger.info("Register name has exist");
                    return false;
                }

                Server.playerNames.put(handleMultiClient, receiveMsg);

                // Server.playerQueue is to save who is waiting for game
                Server.pendingQueue.add(handleMultiClient);
                String send = receiveMsg + " has registered successfully. " +
                        "Please wait notification to play game";
                handleMultiClient.sendMsg(send);
                handleMultiClient.sendMsg("reg");
                clientName = receiveMsg;
                System.out.println("Client " + clientCount + " register name is: " + clientName);
                Server.logger.info("Register name: " + receiveMsg);
                isReg = true;
                return false;
            }
        }
        return true;
    }// end of isRegister


    /**
     * if not on playing game, handle this
     * @param msg
     * @return
     */
    private boolean handleNoneGameResponse(String msg){
        synchronized (object){
            if (msg.equals(quitLabel)){
                handleMultiClient.sendMsg(quitLabel);
                Server.logger.info("Quit Game: " + Server.playerNames.get(handleMultiClient));
                return true;
            }
            switch (msg){
                case playAgainLabel:
                    if (!Server.pendingQueue.contains(handleMultiClient)){
                        Server.pendingQueue.add(handleMultiClient);
                        Server.logger.info("Join Game: " + Server.playerNames.get(handleMultiClient));
                    }
                    handleMultiClient.sendMsg("You has already in play queue");
                    break;
                default:
                    handleMultiClient.sendMsg("After release ranking, q to quit or p to play again" );
                    Server.logger.info("Receive msg :" + msg);
                    break;
            }
            count = 1;
        }
        return false;
    }// end of handleNoneGameResponse


    /**
     * Processing game
     * @param msg
     */
    private void processGame(String msg){
        synchronized (object){
            guess = Integer.parseInt(msg);
            if (guess == Server.random){
                Server.ranking.add(count+"-"+format.format(new Date())+"-"+Server.playerNames.get(handleMultiClient));
                handleMultiClient.sendMsg("Congratulation!!!  You guess correct number in " + count + " times.");
                System.out.println("Client " + clientName + " guess number is: " + guess + ", times: " + count);
                Server.countCompleted ++;
                count ++;
                isComplete = true;
                Server.logger.info("Guess Number: " + msg);
                Server.logger.info("Complete Game: " +
                        Server.playerNames.get(handleMultiClient) + " times: " + count);
                return;
            }else if (guess > Server.random){
                handleMultiClient.sendMsg("You guess number " + guess + " " +
                        "is bigger than the target number"+ ", times: " + count);
                System.out.println("Client " + clientName + " guess number is: " + guess + ", times: " + count);
                count ++;
                Server.logger.info("Guess Number: " + msg + " is bigger");
            }else if (guess < Server.random){
                handleMultiClient.sendMsg("You guess number " + guess + " " +
                        "is smaller than the target number"+ ", times: " + count);
                System.out.println("Client " + clientName + " guess number is: " + guess + ", times: " + count);
                count ++;
                Server.logger.info("Guess Number: " + msg + " is smaller");
            }
            if (count == 5){
                isComplete = true;
                Server.countCompleted ++;
                handleMultiClient.sendMsg("Game over, please wait others player to complete.");
                Server.logger.info("Game over: " + Server.playerNames.get(handleMultiClient));
            }
        }
    }// end of processGame


    /**
     * get current thread
     * @return
     */
    private HandleMultiClient getCurrentThread(){
        for (HandleMultiClient item : Server.clients){
            if (item == this){
                handleMultiClient = item;
            }
        }
        return handleMultiClient;
    }// end of HandleMultiClient


    /**
     * check if exit from game
     * @param msg
     */
    private void handleExit(String msg){
        if (!isComplete){
            if (msg.equals(exitLabel)){
                Server.countCompleted ++;
                isComplete = true;
                handleMultiClient.sendMsg("You have exit from the game which means you discard ur ranking");
                System.out.println("Client " + clientName + " has exited from game.");
            }
        }
    }// end of handleExit


    /**
     * close related resources
     */
    public void closeResources(){
        try{
            Server.communicatelogger.info(this.socket.getRemoteSocketAddress() + " disconnect");
            System.out.println("Client " + clientName + " disconnect");
            synchronized (object){
                if (Server.playerNames.containsKey(handleMultiClient)){
                    Server.playerNames.remove(handleMultiClient);
                }
                if (Server.clients.contains(handleMultiClient)){
                    handleMultiClient.sendMsg("q");
                    Server.clients.remove(handleMultiClient);
                }
                if (Server.pendingQueue.contains(handleMultiClient)){
                    Server.pendingQueue.remove(handleMultiClient);
                }
            }

            socket.close();
            dataOutputStream.close();
            dataInputStream.close();
            Server.communicatelogger.info("Resources has been closed");

        }catch (IOException e ){
            Server.logger.warning(e.toString());
        }
    }// end of closeResources


    /**
     * send msg to client
     * @param msg
     */
    public void sendMsg(String msg) {
        try {
            dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
            dataOutputStream.writeUTF(msg);
        } catch (IOException e) {
            Server.logger.warning(e.toString());
        }
    } // end of sendMsg

} // end of HandleMultiClient
