/*
 * TextDuplex.java
 */

/**
 *
 * @author  abj
 */
public class TextDuplex {

    public static void main (String[] args) {
        //==================================== DatagramSocket ====================================
        TextSenderThread sender = new TextSenderThread();
        TextReceiverThread receiver = new TextReceiverThread();

        //==================================== DatagramSocket2 ====================================
//        TextSenderThread2 sender = new TextSenderThread2();
//        TextReceiverThread2 receiver = new TextReceiverThread2();

        //==================================== DatagramSocket3 ====================================
//        TextSenderThread3 sender = new TextSenderThread3();
//        TextReceiverThread3 receiver = new TextReceiverThread3();

        //==================================== DatagramSocket4 ====================================
//        TextSenderThread4 sender = new TextSenderThread4();
//        TextReceiverThread4 receiver = new TextReceiverThread4();

        receiver.start();
        sender.start();

    }

}