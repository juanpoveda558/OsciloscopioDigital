package org.example;

import javax.sound.sampled.*;

public class MicrofonoSource implements FuenteDeDatos {
    private TargetDataLine line;
    private final byte[] buffer = new byte[32768];
    private boolean activa = false;

    public MicrofonoSource(float sampleRate) {
        try {
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, 4096);
            line.start();
            activa = true;

            // Hilo de captura
            Thread t = new Thread(() -> {
                byte[] temp = new byte[1024];
                while (activa) {
                    int read = line.read(temp, 0, temp.length);
                    System.arraycopy(buffer, read, buffer, 0, buffer.length - read);
                    System.arraycopy(temp, 0, buffer, buffer.length - read, read);
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] obtenerDatos() {
        return buffer;
    }

    @Override
    public boolean estaActiva() {
        return activa;
    }
}