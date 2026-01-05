
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import java.io.PrintWriter;
import java.net.Socket;

public class verifJoueur {
    public static void main(String[] args) {
        // Initialisation des outils OSHI
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        String serverIp = "localhost"; // Mettre l'IP du serveur si c'est un autre PC
        int serverPort = 8080;

        System.out.println("=== Client de Monitoring E-sport ===");
        System.out.println("Tentative de connexion au serveur " + serverIp + ":" + serverPort + "...");

        // On utilise un try-with-resources pour gérer la connexion Socket
        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("✅ Connecté au serveur ! Envoi des données en cours...");

            // Boucle de monitoring infinie
            while (true) {
                // 1. Calcul de la charge CPU (moyenne sur 1 seconde)
                long[] prevTicks = processor.getSystemCpuLoadTicks();
                Thread.sleep(1000);
                double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

                // 2. Calcul de la mémoire
                long totalMem = memory.getTotal() / 1024 / 1024;
                long availableMem = memory.getAvailable() / 1024 / 1024;

                // 3. Préparation du message
                // On formate une ligne claire que le serveur pourra lire facilement
                String message = String.format("OS: %s | CPU: %.2f%% | RAM: %d/%d MB",
                        os.getFamily(), cpuLoad, availableMem, totalMem);

                // 4. Envoi au serveur
                out.println(message);

                // 5. Affichage local (pour debug)
                System.out.println("Données envoyées : " + message);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            System.err.println("Assurez-vous que la classe 'verifServeur' est bien lancée avant le client.");
        }
    }
}