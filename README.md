- Coté backend
	Le serveur acceptes toute les requête demandé. Chacun des test est implémenté et est validé
	Il implémente chacun des algorithme de traitement demandé, et permet de renvoyer les bonnes images

- Coté frontend
	La page est une page simple composé de 3 composant :
	- Un champ permettant d'envoyer ses propres images au format jpeg, jpg et png.
	- Une galerie d'image avec toutes les images contenue dans le backend affiché avec une taille fixe, sur lesquelles on peut cliquer pour séléctionner une image.
	- Un champ premettant de choisir et d'apliquer un algorithme de traitement et qui affiche l'image choisi ou le résultat du traitement avec sa taille d'origine
	
Pour une liste complète des modifications, se référer au tableau des besoins fournit (besoins.pdf)

L'application a été testée sur Linux et Windows et sur les navigateur Google Chrome et Mozilla Firefox.

Pour déployer l'application : Taper la commande "mvn clean install" puis "mvn --projects backend spring-boot:run" dans un terminal ouvert à la source du projet (le dossier l1a)