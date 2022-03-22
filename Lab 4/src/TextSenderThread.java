///*
// * TextSender.java
// */
//
///**
// *
// * @author  abj
// */
//import CMPC3M06.AudioPlayer;
//import CMPC3M06.AudioRecorder;
//
//import javax.sound.sampled.LineUnavailableException;
//import java.net.*;
//import java.io.*;
//import java.nio.ByteBuffer;
//import java.util.Vector;
//
//public class TextSenderThread implements Runnable{
//
//    static DatagramSocket sending_socket;
//    static AudioRecorder recorder = null;
//
//    public void start(){
//        Thread thread = new Thread(this);
//        thread.start();
//    }
//
//    public void run () {
//
//        //***************************************************
//        //Port to send to
//        int PORT = 55555;
//        //IP ADDRESS to send to
//        InetAddress clientIP = null;
//        try {
//            clientIP = InetAddress.getByName("localhost");  //CHANGE localhost to IP or NAME of client machine
//        } catch (UnknownHostException e) {
//            System.out.println("ERROR: TextSender: Could not find client IP");
//            e.printStackTrace();
//            System.exit(0);
//        }
//
//        //***************************************************
//        //Initialise AudioPlayer objects
//
//        //Initialise AudioPlayer and AudioRecorder objects
//
//        try {
//            recorder = new AudioRecorder();
//            sending_socket = new DatagramSocket();
//        } catch (LineUnavailableException | SocketException e) {
//            e.printStackTrace();
//        }
//
//        int key = 15;
//        try{
//            while (true){
//                //Capture audio data and add to voiceVector
//                byte[] block = recorder.getBlock(); // 512 bytes payload
//
//                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(block.length);
//                ByteBuffer plainText = ByteBuffer.wrap(block);
//                for( int j = 0; j < block.length/4; j++) {
//                    int fourByte = plainText.getInt();
//                    fourByte = fourByte ^ key; // XOR operation with key
//                    unwrapEncrypt.putInt(fourByte);
//                }
//                byte[] encryptedBlock = unwrapEncrypt.array();
//
//                //Make a DatagramPacket from it, with client address and port number
//                DatagramPacket packet = new DatagramPacket(encryptedBlock, encryptedBlock.length, clientIP, PORT);
//                sending_socket.send(packet);
//
//            }
//        } catch (IOException e){
//            System.out.println("ERROR: TextSender: Some random IO error occured!");
//            e.printStackTrace();
//        }
//        //Close the socket
//        sending_socket.close();
//        //***************************************************
//    }
//}


import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;


import javax.sound.sampled.LineUnavailableException;


public class TextSenderThread implements Runnable{

    static DatagramSocket sending_socket;
    static AudioRecorder recorder;
    int key = 15; // Set XOR key
    short authenticationKey = 10; //set header key



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
        //We dont need to know its port number as we never send anything to it.
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
//                byte[] block = ("packet" + count).getBytes();

                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(514); // Allocate size of Encryption buffer to length of our Main buffer
                ByteBuffer plainText = ByteBuffer.allocate(514); // Allocate our plaintext buffer to size of full packet

                plainText.putShort(authenticationKey); // Add our Header/Auth key to our plaintext buffer
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
