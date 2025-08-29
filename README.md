- Coté backend
	Le serveur accepte toutes les requêtes demandées. Chaque test est implémenté et est validé
	Il implémente des algorithmes de traitement d'images : changement de teinte, pixellisation, etc...

- Coté frontend
	La page est une page simple composé de 3 composant :
	- Un champ permettant d'envoyer ses propres images au format jpeg, jpg et png.
	- Une galerie d'image avec toutes les images contenue dans le backend affiché avec une taille fixe, sur lesquelles on peut cliquer pour séléctionner une image.
	- Un champ permettant de choisir et d'appliquer un algorithme de traitement et qui affiche l'image choisi ou le résultat du traitement avec sa taille d'origine


L'application a été testée sur Linux et Windows et sur les navigateur Google Chrome et Mozilla Firefox.

Pour déployer l'application : Taper la commande "mvn clean install" puis "mvn --projects backend spring-boot:run" dans un terminal ouvert à la source du projet.
