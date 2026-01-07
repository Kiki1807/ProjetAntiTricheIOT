Projet pour le cours de L3 Infrastructure IT

Le projet permet de lire les données du CPU, de la RAM  ainsi que le nombre de CPS d'un joueur. Si le % d'utilisation du CPU est trop élevé, un message d'alerte s'affichera dans la console serveur. De même, si le nombre de CPS est supérieur à 10 ( pour que cela sois plus simple à vérifier) un message d'erreur s'affiche.

Le schema de notre infrastructure actuellement. On récupère les données, on les réunis dans un seul message, on les envoie avec RabbitMQ dans un container. Dans ce container on vérifie s'il y a des données anormales, s'il y en a on envoie un message d'alertes et après on envoie les données dans la base de données.

<img width="1229" height="480" alt="image" src="https://github.com/user-attachments/assets/efa1f1e4-2a36-4950-9455-ea3c763fd50c" />


Voici un schéma amélioré de l'infrastructure que nous aurions pu faire.
A la place de récupéré les données, et de les mettres dans un seul message nous aurions pu les mettre dans un objet avec la date et l'heure. Les envoyés avec RabbitMQ dans deux container, un qui s'occupe d'inssérer les données dans la base de données et un autre qui s'occupe de regarder s'il y a une alerte et d'insérer l'alerte dans la table.  
<img width="1468" height="566" alt="image" src="https://github.com/user-attachments/assets/a176ad24-f470-4b5f-b2a7-bf232b5643cd" />
