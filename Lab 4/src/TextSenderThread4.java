import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class TextSenderThread4 implements Runnable {

    static DatagramSocket4 sending_socket;
    static AudioRecorder recorder;
    int index = 0;
    int key = 10; // Set XOR key
    short authenticationKey = 10; //set header key
    int d = 5;
    int matrixIndex = 0;



    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        //*************************************************** SET UP *****************************
        try {
            recorder = new AudioRecorder();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        //Port to send to
        int PORT = 55555;
        //IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");  //CHANGE localhost to IP or NAME of client machine
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        //Open a socket to send from
        //We don't need to know its port number cuz we never send anything to it.
        //We need the try and catch block to make sure no errors occur.

        //DatagramSocket sending_socket;
        try {
            sending_socket = new DatagramSocket4();
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //*************************************************** SET UP *****************************

        //Main loop.
        boolean running = true;

        while (running) {
            ArrayList<byte[]> stream = new ArrayList<>(d^2);
            for(int i = 0; i < 25; i++){
                stream.add(null);
            }
            // fill up the matrix
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    try {
                        byte[] recording = recorder.getBlock(); // Create Byte Block and add data from recorder

                        ByteBuffer block = ByteBuffer.allocate(518);
                        ByteBuffer unwrapEncrypt = ByteBuffer.allocate(518);
                        block.putShort(authenticationKey);
                        block.putInt(index);
                        index++;
                        block.put(recording);
                        block.rewind(); // Rewind plain text to the start of the buffer for Encryption

                        // Start our Encryption
                        for( int k = 0; k < block.array().length/4; k++) {
                            int fourByte = block.getInt();
                            fourByte = fourByte ^ key; // XOR operation with key
                            unwrapEncrypt.putInt(fourByte);
                        }
                        // Finished Encryption

                        byte[] item = unwrapEncrypt.array();

                        matrixIndex = (j*d) + (d-1-i);

                        stream.set(matrixIndex, item);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (int k = 0; k < stream.size(); k++) {
                byte[] res = stream.get(k);
                DatagramPacket packet = new DatagramPacket(res, res.length, clientIP, PORT); // Create our packet
                try {
                    sending_socket.send(packet); // Send our packet
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Close the socket
        sending_socket.close();
    }
}