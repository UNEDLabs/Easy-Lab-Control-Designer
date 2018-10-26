/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uned.dia.softwarelinks.labview.protocol.tcp;

import java.io.*;

/**
 *
 * @author hector
 */
public class Sender extends Thread {

    private boolean running = true;
    private CircularByteBuffer cbbs = null;
    private DataOutputStream bufferOutputTCP = null;

    public Sender(CircularByteBuffer cb, DataOutputStream dos) {
        this.cbbs = cb;
        this.bufferOutputTCP = dos;
    }

    public void run() {
        System.out.println("Running Sender...");
        while (running) {
            try {
                while (cbbs.getAvailable() > 0) {
                    byte[] data = new byte[cbbs.getAvailable()];
                    cbbs.getInputStream().read(data);
                    bufferOutputTCP.write(data);
                    bufferOutputTCP.flush();
                }
            } catch (IOException io) {
                System.out.println("sender() thread message: IOException = " + io.getMessage());
                stopSender();
            }
            delay(100);
        }
        cbbs.clear();
        System.out.println("Stopping Sender...");
    }

    public void stopSender() {
        if (this.isAlive()) {
            running = false;
            try {
                this.join(1000,0);
                System.out.println("Sender has been stopped");
            } catch (InterruptedException e) {
            }
        }
    }

    private void delay(int mseconds) {
        try {
            Thread.sleep(mseconds);
        } catch (InterruptedException e) {
            System.out.println("Delay interrupted!");
        }
    }
}
