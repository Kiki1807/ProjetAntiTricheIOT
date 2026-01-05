import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class verifServeur {
    public static void main(String[] args) {
        int port = 8080; // Le port sur lequel le serveur écoute

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur de vérification démarré sur le port " + port);
            System.out.println("En attente de données du moniteur...");

            while (true) {
                // Le serveur accepte une connexion entrante
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println("[REÇU] : " + line);

                        // Logique de détection d'anomalie simple
                        if (line.contains("CPU") && extractValue(line) > 50.0) {
                            System.out.println("⚠️ ALERTE : Charge CPU critique détectée sur un poste !");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors de la réception : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Impossible de démarrer le serveur : " + e.getMessage());
        }
    }

    // Petite méthode pour extraire le nombre d'une chaîne comme "CPU Usage: 95,00%"
    private static double extractValue(String line) {
        try {
            return Double.parseDouble(line.replaceAll("[^0-9,.]", "").replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}