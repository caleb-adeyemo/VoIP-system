/*
 * TextDuplex.java
 */

/**
 *
 * @author  abj
 */
public class TextDuplex {

    public static void main (String[] args){

        TextReceiverThread receiver = new TextReceiverThread();
        TextSenderThread sender = new TextSenderThread();

        receiver.start();
        sender.start();

    }

}