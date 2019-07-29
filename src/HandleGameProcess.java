import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;

/**
 * This is a thread that use to handle each round.
 *
 * Generate target number, control each round timeout, release ranking
 *
 * Reset start condition etc
 *
 * @author xiaoyu chen s3517183 from RMIT
 */
public class HandleGameProcess implements Runnable{

    long startTime;
    long endTime;
    Object object = "A";
    final static int allowPlayerNum = 3;

    /**
     * override runnable method
     */
    @Override
    public void run() {
        try{
            // keep alive to handle each round
            while (true){
                endTime = System.currentTimeMillis();

                if (Server.isStart){
                    // check if came over
                    ifGameOver();
                } else {
                    synchronized (object){ if (Server.pendingQueue.isEmpty()){ continue; } }

                    // only for prepare stage, prepare time is determined on the first player join
                    if (!Server.isPrepare){
                        Server.logger.info("The first round will begin in " +
                                Server.preparingTime + " milliseconds");
                        System.out.println("The first round will begin in " +
                                Server.preparingTime + " milliseconds");
                        Thread.sleep(Server.preparingTime);
                        Server.isPrepare = true;
                    }

                    // reset key flag
                    Server.countCompleted = 0;
                    Server.ranking.clear();
                    Server.isStart = true;
                    Server.random = getRandom(10);

                    Server.roundNo ++;
                    System.out.println("============ New Round  ==========");
                    System.out.println("The round " + Server.roundNo + " target is " + Server.random);
                    Server.logger.info("\nThe round " + Server.roundNo + " target is " + Server.random);

                    preparing();
                    startTime = System.currentTimeMillis();
                }// end of if
            }// end of while
        } catch (InterruptedException e) {
            Server.logger.warning(e.toString());
        }
    } // end of run


    /**
     * Create a random int number from 0 to max-1
     *
     * @return a Server.random number
     */
    private int getRandom(int max){
        return new Random().nextInt(max);
    } // end of getServer.random


    /**
     * When meet start new game requirement
     * call this method to adjust pending queue and player
     */
    private void preparing(){
        StringBuilder notifyInfo = new StringBuilder();
        notifyInfo.append("\n++++++++++++++++++++++++\n");
        notifyInfo.append("Guess Game is starting, Please guess ASAP! Only allow 0 to 9 or e\n");
        notifyInfo.append("The following players is attending this round\n");

        synchronized (object){
            try{
                if (Server.pendingQueue.size() >= allowPlayerNum){
                    Server.onPlayingNum = allowPlayerNum;
                }else {
                    Server.onPlayingNum = Server.pendingQueue.size();
                }

                for (int i = 0; i < Server.onPlayingNum; i++) {
                    notifyInfo.append(Server.playerNames.get(Server.pendingQueue.get(i)) + "  ");
                    Server.pendingQueue.get(i).isComplete = false;
                    Server.onPlaying.add(Server.pendingQueue.get(i));
                    Server.logger.info("Attend player: " + Server.playerNames.get(Server.pendingQueue.get(i)));
                }
                notifyInfo.append("\n++++++++++++++++++++++++\n");
                for (int i = 0; i < Server.onPlayingNum; i++) {
                    new DataOutputStream(Server.pendingQueue.get(i).socket.getOutputStream()).writeUTF(notifyInfo.toString());
                    new DataOutputStream(Server.pendingQueue.get(i).socket.getOutputStream()).writeUTF("start");
                }

                for (int i = 0; i < Server.onPlayingNum; i++) {
                    Server.pendingQueue.remove(0);
                }
            } catch (IOException e) {
                Server.logger.warning(e.toString());
            }
        }
    }// end of preparing


    /**
     * rank players by date time and guess times
     *
     * @return ranking string
     */
    private String handleRanking(){
        StringBuilder send = new StringBuilder();
        send.append("\n++++++++++++++++++++++++\n");
        String previous = "";
        int rank = 0;
        synchronized (object){
            for (int i = 0; i < Server.ranking.size(); i++) {
                String[] stringList = Server.ranking.get(i).split("-");
                if (i != 0){
                    if (!stringList[0].equals(previous)){
                        rank = rank + 1;
                    }
                }else {
                    rank = rank+1;
                }
                previous = stringList[0];
                send.append(stringList[2]+ " is ranking No. " + rank + ", times: " + stringList[0] + "\n");
                Server.logger.info(send.toString());
            }
            send.append("q to quit or p to play again");
            send.append("\n++++++++++++++++++++++++\n");
            return send.toString();
        }
    }// end of handleRanking


    /**
     * check if game over
     * one is time out that will force to handle result
     * other is all players have completed the guess game
     */
    private void ifGameOver(){
        synchronized (object){
            if ((endTime-startTime) > Server.playingTime){
                System.out.println("Time out, now head to handle result");
                gameResult();
            }else if (Server.countCompleted == Server.onPlayingNum){
                System.out.println("Game over, now head to handle result");
                gameResult();
            }
        }
    }// end of ifGameOver


    /**
     * Game over, let us to deal with result
     */
    private void gameResult(){
        synchronized (object){
            StringBuilder send = new StringBuilder();

            try{
                Collections.sort(Server.ranking);
                if (Server.ranking.isEmpty()){
                    send.append("\nThere is no one complete game, q to quit, p to play again\n");
                }else {
                    send.append(handleRanking());
                }

                // notify related players that the result
                for (int i = 0; i < Server.onPlaying.size(); i++) {
                    Server.onPlaying.get(i).isComplete = true;
                    new DataOutputStream(Server.onPlaying.get(i).socket.getOutputStream()).writeUTF(send.toString());
                    new DataOutputStream(Server.onPlaying.get(i).socket.getOutputStream()).writeUTF("end");
                }
                Server.logger.info("Notify related players");

                // reset
                Server.onPlaying.clear();
                Server.isStart = false;
                Server.countCompleted = 0;
                Server.onPlayingNum = -1;
                Server.logger.info("The game is over!!!\n");
            } catch (IOException e) {
                Server.logger.warning(e.toString());
            }
        }
    }// end of gameResult

} // end of HandleGameProcess
