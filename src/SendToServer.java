import java.io.IOException;

/**
 * This is a runnable that handle raw input and send to server
 *
 * @author xiaoyu chen s3517183 from RMIT
 */
public class SendToServer implements Runnable {
    private String rawString;
    private final int firstNameLength = 3;
    private final int inputLength = 1;
    private final String quitLabel = "q";
    private final String exitLabel = "e";
    private final String playAgainLabel = "p";

    @Override
    public void run() {
        try {
            while (Client.scanner.hasNext()) {
                rawString = Client.scanner.nextLine();

                if (!handleRegister(rawString)){continue;}
                if (!handleGameProcess(rawString)){continue;}
                if (rawString.equals(quitLabel)){
                    Client.dataOutputStream.writeUTF(rawString);
                    break;
                }
                if (Client.isRegister && !Client.isStart){
                    if (!rawString.equals(playAgainLabel)){
                        System.out.println("Please wait game to start");
                        continue;
                    }
                }

                Client.dataOutputStream.writeUTF(rawString);
            }
        } catch (IOException e) {
        } finally {
            if (Client.scanner != null){
                Client.scanner.close();
            }
            System.out.println("Disconnect Successfully");
        }
    }// end of run


    /**
     * Force the user to register to join game
     * @param rawString user input
     * @return if less than 3 letter, ask input again
     */
    private boolean handleRegister(String rawString){
        if (!Client.isRegister){
            if (rawString.equals(quitLabel)){
                return true;
            }
            if (rawString.trim().length() < firstNameLength){
                System.out.println("Deny! Serious, is it your true name?");
                return false;
            }
        }
        return true;
    } // end of handleRegister


    /**
     * Check if in playing, if in playing status, only allow to exit by e
     * Check if in playing, only allow number from 0 to 9
     * @param rawString user input
     * @return if in playing, not allow q
     */
    private boolean handleGameProcess(String rawString){
        if (Client.isStart){
            if (rawString.equals(quitLabel)){
                System.out.println("Deny! e to exit from game then q to quit");
                return false;
            }
            if (rawString.equals(exitLabel)){
                return true;
            }
            if (rawString.trim().length() == inputLength){
                try {
                    Integer.parseInt(rawString);
                    return true;
                }
                catch( Exception e ) {
                    System.out.println("Deny! Only accept: number 0 to 9, letter e");
                    return false;
                }
            }else {
                System.out.println("Deny! Only accept: number 0 to 9, letter e");
                return false;
            }
        }
        return true;
    }// end of handleGameProcess

}// end of SendToServer
