package org.example;

public interface FuenteDeDatos {
    byte[] obtenerDatos();
    boolean estaActiva();
    void setSampleRate(float rate);
}