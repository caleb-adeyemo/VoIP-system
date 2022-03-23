import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;


public class TextSenderThread implements Runnable{

    static DatagramSocket sending_socket;
    static AudioRecorder recorder;
    int key = 15;
    long time;
    short authenticationKey = 10; // Set XOR key



    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run (){
        try {
            recorder = new AudioRecorder();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        //***************************************************
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
        //***************************************************

        //***************************************************
        //Open a socket to send from
        //We don't need to know its port number cuz we never send anything to it.
        //We need the try and catch block to make sure no errors occur.

        //DatagramSocket sending_socket;
        try{
            sending_socket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Main loop.

        boolean running = true;
        int count = 0;
        while (running){
            try{
                byte[] block = recorder.getBlock(); // Create Byte Block and add data from recorder

                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(522); // Allocate size of Encryption buffer to length of our Main buffer
                ByteBuffer plainText = ByteBuffer.allocate(522); // Allocate our plaintext buffer to size of full packet

                plainText.putShort(authenticationKey);// Add our Header/Auth key to our plaintext buffer
                time = System.currentTimeMillis();
                plainText.putLong(time); // time stamp
                plainText.put(block); // Add our recorded bytes (Block) to our plaintext buffer
                plainText.rewind(); // Rewind plain text to the start of the buffer for Encryption

                // Start our Encryption
                for( int j = 0; j < plainText.array().length/4; j++) {
                    int fourByte = plainText.getInt();
                    fourByte = fourByte ^ key; // XOR operation with key
                    unwrapEncrypt.putInt(fourByte);
                }
                // Finished Encryption

                byte[] encryptedBlock = unwrapEncrypt.array(); // Create our Encrypted block and add our Encrypted array

                DatagramPacket packet = new DatagramPacket(encryptedBlock, encryptedBlock.length, clientIP, PORT); // Create our packet
                sending_socket.send(packet); // Send our packet


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Close the socket
        sending_socket.close();
    }
}
