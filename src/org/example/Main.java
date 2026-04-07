package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import javax.sound.sampled.*;

public class Main extends JFrame {
    private PanelOsciloscopio areaDibujo;
    private boolean congelado = false;
    private FuenteDeDatos miFuente;

    public Main() {
        setTitle("Osciloscopio Profesional - Calibrado");
        setSize(1250, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Instanciamos el area de dibujo
        areaDibujo = new PanelOsciloscopio();

        configurarEntrada("WAV");

        add(areaDibujo, BorderLayout.CENTER);

        // Panel de Controles
        JPanel panelControles = new JPanel();
        panelControles.setPreferredSize(new Dimension(260, 0));
        panelControles.setBackground(new Color(45, 45, 45));
        panelControles.setBorder(new TitledBorder(null, "MEDICION", TitledBorder.CENTER, TitledBorder.TOP, null, Color.WHITE));
        panelControles.setLayout(new BoxLayout(panelControles, BoxLayout.Y_AXIS));

        panelControles.add(Box.createRigidArea(new Dimension(0, 20)));

        // BOTON HOLD
        JButton btnHold = new JButton("CONGELAR");
        btnHold.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHold.setMaximumSize(new Dimension(220, 40));
        btnHold.addActionListener(e -> {
            congelado = !congelado;
            areaDibujo.setCongelado(congelado);
            btnHold.setText(congelado ? "REANUDAR" : "CONGELAR");
            btnHold.setBackground(congelado ? new Color(200, 50, 50) : Color.DARK_GRAY);
        });
        panelControles.add(btnHold);
        panelControles.add(Box.createRigidArea(new Dimension(0, 25)));

        // SELECTOR X (TIEMPO)
        panelControles.add(crearEtiquetaCentrada("Base de Tiempo (X):"));
        String[] tiempos = {"1 ms/div","5 ms/div", "10 ms/div", "20 ms/div", "50 ms/div"};
        JComboBox<String> comboTiempo = new JComboBox<>(tiempos);
        comboTiempo.setMaximumSize(new Dimension(220, 30));
        comboTiempo.setSelectedIndex(2);
        comboTiempo.addActionListener(e -> areaDibujo.setMsPerDiv(comboTiempo.getSelectedIndex()));
        panelControles.add(comboTiempo);

        panelControles.add(Box.createRigidArea(new Dimension(0, 20)));

        // SELECTOR SAMPLE RATE
        panelControles.add(crearEtiquetaCentrada("Sample Rate (Hz):"));
        String[] rates = {"8000", "16000", "44100", "48000", "96000"};
        JComboBox<String> comboRate = new JComboBox<>(rates);
        comboRate.setMaximumSize(new Dimension(220, 30));
        comboRate.setSelectedItem("44100");
        comboRate.addActionListener(e -> {
            float nuevoRate = Float.parseFloat((String) comboRate.getSelectedItem());
            miFuente.setSampleRate(nuevoRate);
        });
        panelControles.add(comboRate);

        panelControles.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- SELECTOR DE ENTRADA (BOTONES) ---
        panelControles.add(crearEtiquetaCentrada("ENTRADA DE SEÑAL:"));

        // Botón para el Micrófono
        JButton btnMic = new JButton("🎙️ MICRÓFONO");
        btnMic.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMic.addActionListener(e -> configurarEntrada("MIC"));
        panelControles.add(btnMic);

        panelControles.add(Box.createRigidArea(new Dimension(0, 10))); // Espacio

        // Botón para el Archivo WAV
        JButton btnWav = new JButton("📁 ARCHIVO WAV");
        btnWav.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnWav.addActionListener(e -> configurarEntrada("WAV"));
        panelControles.add(btnWav);

        panelControles.add(Box.createRigidArea(new Dimension(0, 20))); // Espacio


        // SELECTOR Y (VOLTAJE)
        panelControles.add(crearEtiquetaCentrada("Sensibilidad (Y):"));
        String[] volts = {"0.1 V/div", "0.5 V/div", "1.0 V/div"};
        JComboBox<String> comboVolts = new JComboBox<>(volts);
        comboVolts.setMaximumSize(new Dimension(220, 30));
        comboVolts.setSelectedIndex(2);
        comboVolts.addActionListener(e -> areaDibujo.setEscalaY(comboVolts.getSelectedIndex()));
        panelControles.add(comboVolts);

        // SLIDERS OFFSET
        panelControles.add(Box.createRigidArea(new Dimension(0, 30)));
        panelControles.add(crearEtiquetaCentrada("Offset Vertical (Y)"));
        JSlider sliderY = new JSlider(JSlider.HORIZONTAL, -400, 400, 0);
        sliderY.setBackground(new Color(45, 45, 45));
        sliderY.addChangeListener(e -> areaDibujo.setOffsetY(sliderY.getValue()));
        panelControles.add(sliderY);

        panelControles.add(crearEtiquetaCentrada("Offset Horizontal (X)"));
        JSlider sliderX = new JSlider(JSlider.HORIZONTAL, -2000, 2000, 0);
        sliderX.setBackground(new Color(45, 45, 45));
        sliderX.addChangeListener(e -> areaDibujo.setOffsetX(sliderX.getValue()));
        panelControles.add(sliderX);

        add(panelControles, BorderLayout.EAST);

    }

    public void configurarEntrada(String tipo) {
        if (tipo.equals("MIC")) {
            miFuente = new MicrofonoSource(44100f);
        } else if (tipo.equals("WAV")) {
            miFuente = new ArchivoWAVSource("test.wav");
        }

        if (miFuente != null) {
            areaDibujo.conectarFuente(miFuente);
        }
    }


    private JLabel crearEtiquetaCentrada(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(Color.CYAN);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

class PanelOsciloscopio extends JPanel {
    // 1. El "Puerto": Ahora dependemos de la Interfaz, no del Micrófono directamente
    private FuenteDeDatos fuente;

    private int escalaYIndex = 2;
    private int msPerDivIndex = 2;
    private boolean isFrozen = false;
    private int offsetX = 0;
    private int offsetY = 0;
    private byte[] bufferCongelado;

    public PanelOsciloscopio() {
        setBackground(Color.BLACK);
        // 2. Refresco Automático: Como ya no hay un hilo interno leyendo audio,
        // usamos un Timer de Swing para redibujar la pantalla cada 20ms (50 FPS)
        Timer timer = new Timer(20, e -> repaint());
        timer.start();
    }

    // 3. El "Enchufe": Método para conectar cualquier fuente (Mic, Clima, SQL...)
    public void conectarFuente(FuenteDeDatos nuevaFuente) {
        this.fuente = nuevaFuente;
    }

    // Setters (Se mantienen igual para los controles del panel)
    public void setEscalaY(int index) { this.escalaYIndex = index; }
    public void setMsPerDiv(int index) { this.msPerDivIndex = index; }
    public void setCongelado(boolean estado) {
        this.isFrozen = estado;
        if (isFrozen && fuente != null) {
            // Tomamos una "foto" de los datos actuales
            this.bufferCongelado = fuente.obtenerDatos().clone();
        }
    }

    public void setOffsetX(int val) { this.offsetX = val; }
    public void setOffsetY(int val) { this.offsetY = val; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Si no hay nada enchufado, no dibujamos nada
        if (fuente == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double w = getWidth();
        double h = getHeight();
        double midY = h / 2.0;

        // Dibujo de la Grilla (Se mantiene igual)
        g2.setColor(new Color(0, 70, 0));
        for (int i = 0; i <= w; i += 50) g2.drawLine(i, 0, i, (int) h);
        for (int i = 0; i <= h; i += 50) g2.drawLine(0, i, (int) w, i);

        g2.setColor(isFrozen ? Color.YELLOW : Color.GREEN);
        g2.setStroke(new BasicStroke(2f));

        // 4. LA CLAVE: Pedimos los datos a la fuente, sea cual sea
        byte[] buffer;
        if (isFrozen && bufferCongelado != null) {
            buffer = bufferCongelado;
        } else {
            buffer = fuente.obtenerDatos();
        }

        double multY = switch (escalaYIndex) {
            case 0 -> 4.0; case 1 -> 2.0; default -> 1.0;
        };

        // Ajuste de escala temporal (Valores fijos para 44100Hz por ahora)
        double samplesPerPixel = switch (msPerDivIndex) {
            case 0 -> 0.882; case 1 -> 4.41; case 3 -> 8.82; case 4 -> 44.1; default -> 17.64;
        };

        // El bucle de dibujo usa el buffer que nos entregó la "fuente"
        for (int i = 0; i < buffer.length - 4; i += 2) {
            short s1 = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
            short s2 = (short) ((buffer[i + 3] << 8) | (buffer[i + 2] & 0xff));

            int x1 = (int) (((double) i / 2.0) / samplesPerPixel) + offsetX;
            int x2 = (int) (((double) (i + 2) / 2.0) / samplesPerPixel) + offsetX;
            int y1 = (int) (midY - (s1 * h / 65536.0 * multY) - offsetY);
            int y2 = (int) (midY - (s2 * h / 65536.0 * multY) - offsetY);

            if (x1 >= 0 && x1 < w && x2 >= 0 && x2 < w) {
                g2.drawLine(x1, y1, x2, y2);
            }
        }
    }


}