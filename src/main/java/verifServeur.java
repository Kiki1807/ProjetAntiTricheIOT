import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class verifServeur {
    private final static String QUEUE_NAME = "monitor_queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        //"rabbitmq" est le nom du service dans le docker-compose.yml
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        //Connexion au broker RabbitMQ
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //On déclare la file
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Serveur Anti-Triche pret. En attente de messages...");

        //On définit ce qu'on fait quand un message arrive
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [RECU] : " + message);

            //Analyse du message pour la détection de triche/alerte
            analyserMessage(message);
        };

        //On commence à lire la file
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
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
                    System.out.println("[ALERTE] CPS: " + cps + " ! Suspicion de Macro/Autoclick.");
                }
            }
        } catch (Exception e) {
            System.out.println(" [!] Erreur d'analyse du message : " + e.getMessage());
        }
    }
}