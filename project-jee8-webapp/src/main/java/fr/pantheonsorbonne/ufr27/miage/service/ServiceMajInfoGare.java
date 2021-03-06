package fr.pantheonsorbonne.ufr27.miage.service;

import fr.pantheonsorbonne.ufr27.miage.jpa.Itineraire;

public interface ServiceMajInfoGare {

	/**
	 * Envoyer un msg aux infogares concernées pour signifier de la mise à jour des
	 * horaires de passage du train associé à l'itinéraire passé en paramètre
	 * 
	 * @param itineraire
	 */
	public void majHoraireItineraire(Itineraire itineraire);

	/**
	 * Envoyer un msg aux infogares concernées pour annoncer pour la première fois
	 * les horaires de passage du train associé à l'itinéraire passé en paramètre
	 * 
	 * @param itineraire
	 */
	void publishItineraire(Itineraire itineraire);
}
