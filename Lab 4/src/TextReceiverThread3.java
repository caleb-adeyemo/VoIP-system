import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket3;

import javax.sound.sampled.LineUnavailableException;

public class TextReceiverThread3 implements Runnable {

    static DatagramSocket3 receiving_socket;
    static AudioPlayer player;
    int key = 10;
    short authenticationKey = 10;
    byte[] savedPacket; //cache for previous packet
    int hashMapKey;
    int previousIndex = 0;
    HashMap<Integer, ByteBuffer> stream = new HashMap<>();


    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        //*************************************************** SET UP *****************************
        try {
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }


        //Port to open socket on
        int PORT = 55555;
        //Open a socket to receive from on port PORT
        //*************************************************** SET UP *********************************

        //DatagramSocket receiving_socket;
        try {
            receiving_socket = new DatagramSocket3(PORT);
        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //Main loop.
        boolean running = true;
        while (running) {

            try {

                byte[] buffer = new byte[518]; // Create our byte array/buffer

                DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length); // Create/Set up our Datagram Packet to receive data

                receiving_socket.receive(packet); // Receive data

                ByteBuffer unwrapDecrypt = ByteBuffer.allocate(buffer.length);   // Create buffer for Final Decrypted data
                ByteBuffer cipherText = ByteBuffer.wrap(buffer); // Add encrypted data to our temp buffer to decrypt

                // Start Decryption
                for(int j = 0; j < buffer.length/4; j++) {
                    int fourByte = cipherText.getInt();
                    fourByte = fourByte ^ key; // XOR decrypt
                    unwrapDecrypt.putInt(fourByte);
                }
                // Finish Decryption
                byte[] decryptedBlock; // Create decrypted block

                if(unwrapDecrypt.getShort(0) == authenticationKey ){  // Check if header matches our key
                    hashMapKey = unwrapDecrypt.getInt(2);
                    stream.put(hashMapKey, unwrapDecrypt);

                    if(stream.size() == 25){
                        for (Map.Entry<Integer, ByteBuffer> entry : stream.entrySet()) {
                            byte[] res = entry.getValue().array();
                            decryptedBlock = Arrays.copyOfRange(res, 6, 518); // Extract Only Voip bytes from our decrypted array

                            player.playBlock(decryptedBlock); // Play Decrypted Block
                            System.out.println("Current packet");
                        }
                        stream.clear();
                    }
                    savedPacket = Arrays.copyOfRange(unwrapDecrypt.array(), 6, 518); // Save/override the packet for retransmission
                    previousIndex = unwrapDecrypt.getInt(2);
                } else{
                    System.out.println("Bad Header");
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        //Close the socket
        receiving_socket.close();
        //***************************************************
    }
}
