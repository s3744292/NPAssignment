import java.io.*;
import java.net.Socket;
import java.util.Scanner;


/**
 * Single player version
 *
 * @author: Xiaoyu chen s3517183 from RMIT
 */
public class TaskOneClient {
    static final int Port = 10086;
    Socket socket = null;
    Scanner scanner = null;
    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream = null;
    String quitLabel = "q";
    String scanMsg = "";
    String receMsg = "";

    /**
     * The main method
     * @param args
     */
    public static void main(String[] args){
        new TaskOneClient().start();
    } // end of main

    /**
     * process game
     * mainly handle receive msg from server
     * and handle send msg to server
     */
    public void start(){
        try {
            // connect to server
            socket = new Socket("localhost", Port);
            System.out.println("Welcome to guess number game  single version\n" +
                    "Only allow number from 0 to 9 or q\n" +
                    "q to quit from game\n" +
                    "+++++++++++  Game is Running Now ++++++++");

            scanner = new Scanner(System.in);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            while (scanner.hasNextLine()){
                scanMsg = scanner.nextLine();
                if (scanMsg.trim().length() != 1){
                    System.out.println("Only allow number from 0 to 9 or q");
                    continue;
                }

                // quit the game
                if (scanMsg.equals(quitLabel)) {
                    dataOutputStream.writeUTF(quitLabel);
                    break;
                }

                // check if number
                if (!checkIfNumber(scanMsg)){
                    continue;
                }

                // send the msg to server
                dataOutputStream.writeUTF(scanMsg);

                //receive msg from server
                receMsg = dataInputStream.readUTF();
                System.out.println(receMsg);
            }
        } catch (IOException e) {
        } finally {
            closeResources();
        }
    }// end of start


    /**
     * check if number
     * @param string
     * @return if is number return true, else return false
     */
    public boolean checkIfNumber(String string){
        try{
            Integer.parseInt(string);
            return true;
        }catch (Exception e){
            System.out.println("Only number from 0 to 9");
            return false;
        }
    }// end of checkIfNumber


    /**
     * close all related resources
     */
    public void closeResources(){
        try {
            // close all resources
            scanner.close();
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (NullPointerException | IOException msg) {
        }
        System.exit(0);
        System.out.println("Disconnect Successful");
    }// end of closeResources4

} // end of Client

