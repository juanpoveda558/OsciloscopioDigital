package org.example;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class ArchivoWAVSource implements FuenteDeDatos {
    // El "tanque" donde guardamos los bytes para el osciloscopio
    private byte[] bufferCircular = new byte[32768];
    private boolean activo = false;

    public ArchivoWAVSource(String rutaArchivo) {
        // Abrimos un hilo (Thread) para que la lectura del disco no congele la pantalla
        new Thread(() -> {
            try {
                File file = new File(rutaArchivo);
                // El "pick-up" o cabezal que lee el archivo
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                activo = true;

                byte[] bloqueTemporal = new byte[1024];
                int bytesLeidos;

                // Bucle infinito para que la onda se repita (Loop)
                while (activo) {
                    // Reiniciamos el flujo si llegamos al final del archivo
                    ais = AudioSystem.getAudioInputStream(file);

                    while (activo && (bytesLeidos = ais.read(bloqueTemporal)) != -1) {
                        // "Desplazamos" la señal antigua para dar espacio a la nueva
                        // Es como mover la cinta magnética
                        System.arraycopy(bufferCircular, bytesLeidos, bufferCircular, 0, bufferCircular.length - bytesLeidos);
                        System.arraycopy(bloqueTemporal, 0, bufferCircular, bufferCircular.length - bytesLeidos, bytesLeidos);

                        // Una pequeña pausa para que la velocidad de lectura
                        // coincida con lo que el ojo puede ver (ajustable)
                        Thread.sleep(10);
                    }
                }
            } catch (UnsupportedAudioFileException | IOException | InterruptedException e) {
                System.out.println("Error al cargar el archivo: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public byte[] obtenerDatos() {
        return bufferCircular;
    }

    @Override
    public boolean estaActiva() {
        return activo;
    }

    @Override
    public void setSampleRate(float rate) {
        // En un archivo, el sample rate ya viene definido internamente
    }
}