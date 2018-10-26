package com.hector;
/*
* VideoClass.java
*
* Created on January 28, 2006, 21:13 PM
*/

import java.awt.Image;

/**
*
* @author Hector Vargas, UNED - 2006.
*/
public class Contenedor {

  private Image img;
  private boolean hayDato = false;

  public synchronized Image get() {
                while (hayDato == false) {
                        try {
                                // espera a que el productor coloque un valor
                                wait(); }
                        catch (InterruptedException e) { }
                }
                hayDato = false;
                // notificar que el valor ha sido consumido
                notifyAll();
                return img;
  }
  public synchronized void put(Image valor) {
                while (hayDato == true) {
                        try {
                                // espera a que se consuma el dato
                                wait();
                        } catch (InterruptedException e) { }
                }
                img = valor;
                hayDato = true;
                // notificar que ya hay dato.
                notifyAll();
  }

}
