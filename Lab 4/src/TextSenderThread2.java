//import CMPC3M06.AudioRecorder;
//import uk.ac.uea.cmp.voip.DatagramSocket4;
//
//import javax.sound.sampled.LineUnavailableException;
//import java.io.IOException;
//import java.net.*;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//
//
//public class TextSenderThread2 implements Runnable {
//
//    static DatagramSocket sending_socket;
//    static AudioRecorder recorder;
//    int key = 15; // Set XOR key
//    short authenticationKey = 10; //set header key
//
//
//    public void start() {
//        Thread thread = new Thread(this);
//        thread.start();
//    }
//
//    public void run() {
//        try {
//            recorder = new AudioRecorder();
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        }
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
//        //***************************************************
//
//        //***************************************************
//        //Open a socket to send from
//        //We dont need to know its port number as we never send anything to it.
//        //We need the try and catch block to make sure no errors occur.
//
//        //DatagramSocket sending_socket;
//        try {
//            sending_socket = new DatagramSocket();
//        } catch (SocketException e) {
//            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
//            e.printStackTrace();
//            System.exit(0);
//        }
//        //***************************************************
//
//        //***************************************************
//        //Main loop.
//
//        boolean running = true;
//        int count = 0;
//        while (running) {
//
//            byte[][] interleaver = new byte[25][];
//            byte[][] ScrambledBlock = new byte[25][];
//            int counter = 0;
//            while (interleaver[24] == null) {
//                try {
//                    byte[] recording = recorder.getBlock(); // Create Byte Block and add data from recorder
//
//                    ByteBuffer header = ByteBuffer.allocate(1000);
//                    header.putInt(count);
//                    header.put(recording);
//                    byte[] block = header.array();
//                    interleaver[counter] = block;
//
//
//                    counter++;
//                    count++;
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            int counter2 = 0;
//
//            for (int i = 0; i < 21; i += 5) {
//                ScrambledBlock[counter2] = interleaver[i];
//                counter2++;
//            }
//            for (int i = 0; i < 22; i += 5) {
//                ScrambledBlock[counter2] = interleaver[i];
//                counter2++;
//            }
//            for (int i = 0; i < 23; i += 5) {
//                ScrambledBlock[counter2] = interleaver[i];
//                counter2++;
//            }
//            for (int i = 0; i < 24; i += 5) {
//                ScrambledBlock[counter2] = interleaver[i];
//                counter2++;
//            }
//            for (int i = 0; i < 25; i += 5) {
//                ScrambledBlock[counter2] = interleaver[i];
//                counter2++;
//            }
//
//            for (int i = 0; i < ScrambledBlock.length; i++) {
//                System.out.println(Arrays.toString(ScrambledBlock[i]));
//                DatagramPacket packet = new DatagramPacket(ScrambledBlock[i], ScrambledBlock[i].length, clientIP, PORT); // Create our packet
//                try {
//                    sending_socket.send(packet); // Send our packet
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//                ByteBuffer unwrapEncrypt = ByteBuffer.allocate(514); // Allocate size of Encryption buffer to length of our Main buffer
//                ByteBuffer plainText = ByteBuffer.allocate(514); // Allocate our plaintext buffer to size of full packet
//
//                plainText.putShort(authenticationKey); // Add our Header/Auth key to our plaintext buffer
//                plainText.put(block); // Add our recorded bytes (Block) to our plaintext buffer
//                plainText.rewind(); // Rewind plain text to the start of the buffer for Encryption
//
//                // Start our Encryption
//                for( int j = 0; j < plainText.array().length/4; j++) {
//                    int fourByte = plainText.getInt();
//                    fourByte = fourByte ^ key; // XOR operation with key
//                    unwrapEncrypt.putInt(fourByte);
//                }
//                // Finished Encryption
//
//                byte[] encryptedBlock = unwrapEncrypt.array(); // Create our Encrypted block and add our Encrypted array
//
//                DatagramPacket packet = new DatagramPacket(encryptedBlock, encryptedBlock.length, clientIP, PORT); // Create our packet
//                sending_socket.send(packet); // Send our packet
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        //Close the socket
//        sending_socket.close();
//    }
//}