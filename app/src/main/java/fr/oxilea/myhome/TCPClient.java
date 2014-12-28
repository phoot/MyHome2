package fr.oxilea.myhome;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class TCPClient {

    // used to send messages
    private PrintStream mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    // reference to the created socket
    Socket socket = null;


    /**
     * Constructor of the class.
     * create a Socket
     */
    public TCPClient(String server, int port) {
        try {
            //create a socket to make the connection with the server
            socket = new Socket(server, port);
        }
        catch (Exception e) {
        }
    }
    public String SendOverTCP(byte[] str2send, Boolean expectResponse, Boolean LonhandStatus) {
        String retStr= "";

        try {
            // set a read timeout (ms) only if wait for EOL on socket read
            // socket.setSoTimeout(500);

            mBufferOut = new PrintStream(socket.getOutputStream(), true);

            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            mBufferOut.write(str2send);
            if(expectResponse) {
                char cRead[]={'\0','\0','\0','\0','\0','\0','\0','\0','\0','\0','\0'};
                // wait a little bit before read status
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBufferIn.read(cRead,0,10);

                // if only request lonhand status
                if (LonhandStatus)
                {
                    if (cRead[6]==1)
                        retStr = "on";
                    else if (cRead[6]==0)
                        retStr = "off";
                    else
                        retStr = "unknown";

                }
                else {
                    // keep only valid char read
                    int i = 0;
                    while (cRead[i] != '\0') {
                        i++;
                    } // the last char is always \0 as only 10 chars are read
                    retStr = String.valueOf(cRead, 0, i);
                }
            }
        }
        catch (Exception e) {
            retStr= "";
        }
        return retStr;
    }

    public Boolean CloseSocket() {

        Boolean retStatus= true;

        try {
            // close socket !!!
            socket.close();
        } catch (IOException e) {
            retStatus= false;
        }
        return retStatus;
    }
}
