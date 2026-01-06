import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

public class verifJoueurCPUetRAM {

    private final static String QUEUE_NAME = "monitor_queue";
    private static volatile int clickCount = 0;

    //Listener souris pour CPS
    static class MouseListener implements NativeMouseListener {
        @Override
        public void nativeMousePressed(NativeMouseEvent e) {
            clickCount++;
        }
    }

    public static void main(String[] args) {
        //Désactiver les logs de JNativeHook
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        //OSHI : Infos système
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        //Configuration RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ tourne sur ton PC via Docker

        try {
            //Activation du trackeur de la souris
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeMouseListener(new MouseListener());

            System.out.println("=== Client Monitoring E-sport (RabbitMQ) ===");

            //Connexion à RabbitMQ
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                //Déclaration de la file
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                System.out.println("Connection à RabbitMQ réussi. Envoi des données...");

                while (true) {
                    //Mesure du CPU
                    Random obj = new Random();
                    long[] prevTicks = processor.getSystemCpuLoadTicks();
                    Thread.sleep(1000);
                    double cpuLoad = obj.nextDouble(101);

                    //Mesure de la RAM
                    long totalMem = memory.getTotal() / 1024 / 1024;
                    long usedMem = (memory.getTotal() - memory.getAvailable()) / 1024 / 1024;

                    //Mesure des CPS
                    int cps = clickCount;
                    clickCount = 0;

                    // Préparation du message
                    String message = String.format(
                            "OS: %s | CPU: %.2f%% | RAM: %d/%d MB | CPS: %d",
                            os.getFamily(), cpuLoad, usedMem, totalMem, cps
                    );

                    //Envoie À RABBITMQ
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));

                    System.out.println(" [x] Envoyé : " + message);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}