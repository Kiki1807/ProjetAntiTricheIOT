import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.Statement;

public class verifServeur {
    private final static String QUEUE_NAME = "monitor_queue";

    //Ajout de la base de donnée avec l'URL utilisant le nom de la base défini dans docker-compose
    private static final String DB_URL = "jdbc:mysql://db:3306/databaseJoueur";
    private static final String DB_USER = "utilisateur_esport";
    private static final String DB_PASS = "motdepasse123";

    public static void main(String[] args) throws Exception {
        //On laisse MySQL démarrer dans Docker
        System.out.println("Démarrage du serveur... Attente de MySQL (10s)");
        Thread.sleep(10000);

        //Connexion à la base de donnee MySQL
        java.sql.Connection sqlConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement stmt = sqlConn.createStatement();

        //Création de la table pour le stockage des donnee
        stmt.execute("CREATE TABLE IF NOT EXISTS stats_joueurs (id INT AUTO_INCREMENT PRIMARY KEY, log_brut VARCHAR(255))");
        System.out.println("La base de données est prête.");


        //Configuration de "rabbitmq"
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        //Connexion à RabbitMQ
        Connection rabbitConn = factory.newConnection();
        Channel channel = rabbitConn.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        System.out.println(" [*] Serveur en attente de données...");

        //On définie la réception des messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [RECU] : " + message);

            //On regarde s'il y a une anomolie sur l'ordinateur du joueur (CPU et CPS)
            analyserMessage(message);

            //On sauvegarde dans la base de données
            try {
               String requete = "INSERT INTO stats_joueurs (log_brut) VALUES ('" + message + "')";
                stmt.executeUpdate(requete);
            } catch (Exception e) {
                System.err.println("Erreur SQL : " + e.getMessage());
            }
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    private static void analyserMessage(String message) {
        try {
            //Vérification de l'utilisation du CPU
            if (message.contains("CPU: ")) {
                //On découpe la chaîne pour récupérer juste le chiffre avant le %
                String cpuPart = message.split("CPU: ")[1].split("%")[0];
                double cpuUsage = Double.parseDouble(cpuPart.replace(",", "."));

                if (cpuUsage > 80.0) {
                    System.out.println("[CRITIQUE] CPU à " + cpuUsage + "% ! Le joueur utilise un logiciel lourd ou subit des lags.");
                }
            }
            //Vérification des CPS
            if (message.contains("CPS: ")) {
                int cps = Integer.parseInt(message.split("CPS: ")[1].trim());
                if (cps > 20) {
                    System.out.println("[ALERTE] CPS: " + cps + " ! Suspicion de macro ou d'autoclick.");
                }
            }
        } catch (Exception e) {
            System.out.println(" [!] Erreur d'analyse du message : " + e.getMessage());
        }
    }
}