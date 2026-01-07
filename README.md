Projet pour le cours de L3 Infrastructure IT

Le projet permet de lire les données du CPU, de la RAM  ainsi que le nombre de CPS d'un joueur. Si le % d'utilisation du CPU est trop élevé, un message d'alerte s'affichera dans la console serveur. De même, si le nombre de CPS est supérieur à 10 ( pour que cela sois plus simple à vérifier) un message d'erreur s'affiche.

Le schema de notre infrastructure actuellement. On récupère les données, on les réunis dans un seul message, on les envoie avec RabbitMQ dans un container. Dans ce container on vérifie s'il y a des données anormales, s'il y en a on envoie un message d'alerte et après on envoie les données dans la base de données.

<img width="1396" height="558" alt="image" src="https://github.com/user-attachments/assets/7fff2c3c-2d42-4be5-aac8-a4d428222a62" />


Voici un schéma amélioré de l'infrastructure que nous aurions pu faire.
A la place de récupérer les données, et de les mettre dans un seul message nous aurions pu les mettre dans un objet avec la date et l'heure. Puis on les envoies avec RabbitMQ dans deux container, un qui s'occupe d'insérer les données dans la base de données et un autre qui s'occupe de regarder s'il y a une alerte et d'insérer l'alerte dans la table.  
<img width="1554" height="573" alt="image" src="https://github.com/user-attachments/assets/b8971383-1077-47c1-9565-76b35d0d5d9b" />
