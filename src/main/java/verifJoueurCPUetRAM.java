import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class verifJoueurCPUetRAM {

    //Compteur de CPS
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

        String serverIp = "localhost";
        int serverPort = 8080;
        String playerId = "Joueur-01"; // identifiant du joueur

        try {
            //Activation du trackeur de la souris
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeMouseListener(new MouseListener());

            System.out.println("=== Client Monitoring E-sport ===");
            System.out.println("Connexion au serveur " + serverIp + ":" + serverPort + "...");

            try (Socket socket = new Socket(serverIp, serverPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                System.out.println("Connection au serveur réussi ! Envoi des données en cours...");

                //Boucle permettant l'envoie continue des données
                while (true) {
                    //CPU
                    long[] prevTicks = processor.getSystemCpuLoadTicks();
                    Thread.sleep(1000); //On mesure les données sur 1 seconde
                    double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

                    //RAM
                    long totalMem = memory.getTotal() / 1024 / 1024;
                    long availableMem = memory.getAvailable() / 1024 / 1024;
                    long usedMem = totalMem - availableMem;

                    //CPS
                    int cps = clickCount;
                    clickCount = 0;

                    //Préparation du message
                    String message = String.format(
                            "OS: %s | CPU: %.2f%% | RAM: %d/%d MB | CPS: %d",
                            os.getFamily(), cpuLoad, usedMem, totalMem, cps
                    );

                    //Envoi au serveur
                    out.println(message);

                    //Affichage dans le terminal
                    System.out.println(message);
                }

            }

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
