package fr.pantheonsorbonne.ufr27.miage.n_resource;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.pantheonsorbonne.ufr27.miage.model.jaxb.ArretJAXB;
import fr.pantheonsorbonne.ufr27.miage.model.jaxb.ItineraireJAXB;
import fr.pantheonsorbonne.ufr27.miage.n_jms.ItineraireResponderBean;
import fr.pantheonsorbonne.ufr27.miage.n_jms.MessageGateway;
import fr.pantheonsorbonne.ufr27.miage.n_repository.ItineraireRepository;
import fr.pantheonsorbonne.ufr27.miage.n_repository.TrainRepository;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceItineraire;

@Path("itineraire/")
public class ItineraireEndpoint {

	@Inject
	ServiceItineraire service;

	@Inject
	MessageGateway messageGateway;
	
	@Inject
	ItineraireRepository itineraireRepository;
	
	@Inject
	TrainRepository trainRepository;
	
	@Inject
	ItineraireResponderBean responder;
	
	@Produces(value = { MediaType.APPLICATION_XML })
	@Path("{trainId}")
	@GET
	public Response getItineraire(@PathParam("trainId") int trainId) {
		System.out.println("== Infocentre - getItineraire ==\nidTrain : T" + trainId);
		int idTrain = trainRepository.getTrainByBusinessId(trainId).getId();
		ItineraireJAXB itineraireJAXB = service.getItineraire(idTrain);
		if (itineraireJAXB != null) {
			messageGateway.publishCreation(itineraireRepository.recupItineraireEnCoursOuLeProchain(idTrain));
			return Response.ok(itineraireJAXB).build();
		}
		return Response.noContent().build();
	}

	@Consumes(value = { MediaType.APPLICATION_XML })
	@Path("{trainId}")
	@PUT
	public Response majArret(@PathParam("trainId") int trainId, ArretJAXB arretActuel) {
		System.out.println("== Infocentre - majArret ==\nidTrain : T" + trainId);
		int idTrain = trainRepository.getTrainByBusinessId(trainId).getId();
		if (service.majItineraire(idTrain, arretActuel)) {
			return Response.ok().build();
		}
		return Response.serverError().build();
	}

}
