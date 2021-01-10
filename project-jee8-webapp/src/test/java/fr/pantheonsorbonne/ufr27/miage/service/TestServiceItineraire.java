package fr.pantheonsorbonne.ufr27.miage.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import fr.pantheonsorbonne.ufr27.miage.model.jaxb.ArretJAXB;
import fr.pantheonsorbonne.ufr27.miage.model.jaxb.ItineraireJAXB;
import fr.pantheonsorbonne.ufr27.miage.n_dao.ArretDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.ItineraireDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.TrainDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.TrajetDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.VoyageDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.VoyageurDAO;
import fr.pantheonsorbonne.ufr27.miage.n_jpa.Arret;
import fr.pantheonsorbonne.ufr27.miage.n_jpa.Gare;
import fr.pantheonsorbonne.ufr27.miage.n_jpa.Itineraire;
import fr.pantheonsorbonne.ufr27.miage.n_jpa.Itineraire.CodeEtatItinieraire;
import fr.pantheonsorbonne.ufr27.miage.n_mapper.ArretMapper;
import fr.pantheonsorbonne.ufr27.miage.n_jpa.Train;
import fr.pantheonsorbonne.ufr27.miage.n_jpa.TrainAvecResa;
import fr.pantheonsorbonne.ufr27.miage.n_repository.ArretRepository;
import fr.pantheonsorbonne.ufr27.miage.n_repository.ItineraireRepository;
import fr.pantheonsorbonne.ufr27.miage.n_repository.TrainRepository;
import fr.pantheonsorbonne.ufr27.miage.n_repository.TrajetRepository;
import fr.pantheonsorbonne.ufr27.miage.n_repository.VoyageRepository;
import fr.pantheonsorbonne.ufr27.miage.n_repository.VoyageurRepository;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceItineraire;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceUtilisateur;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceItineraireImp;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceUtilisateurImp;
import fr.pantheonsorbonne.ufr27.miage.tests.utils.TestPersistenceProducer;

@TestInstance(Lifecycle.PER_CLASS)
@EnableWeld
public class TestServiceItineraire {

	@WeldSetup
	private WeldInitiator weld = WeldInitiator.from(ServiceItineraire.class, ServiceItineraireImp.class,
			ServiceUtilisateur.class, ServiceUtilisateurImp.class, ItineraireRepository.class, ItineraireDAO.class,
			ArretRepository.class, ArretDAO.class, TrajetRepository.class, TrajetDAO.class, TrainRepository.class,
			TrainDAO.class, VoyageurRepository.class, VoyageurDAO.class, VoyageRepository.class, VoyageDAO.class,
			TestPersistenceProducer.class).activate(RequestScoped.class).build();

	@Inject
	EntityManager em;
	@Inject
	ServiceItineraire serviceItineraire;
	@Inject
	TrainRepository trainRepository;
	@Inject
	ItineraireRepository itineraireRepository;
	@Inject
	ArretRepository arretRepository;

	@BeforeAll
	void initVarInDB() {

		LocalDateTime now = LocalDateTime.now();
		Train train1 = new TrainAvecResa("Marque");

		Gare g1 = new Gare("Gare1");
		Gare g2 = new Gare("Gare2");
		Gare g3 = new Gare("Gare3");
		Gare g4 = new Gare("Gare4");

		Arret arret1 = new Arret(g1, null, now.plusMinutes(1));
		Arret arret2 = new Arret(g2, now.plusMinutes(2), now.plusMinutes(3));
		Arret arret3 = new Arret(g3, now.plusMinutes(4), now.plusMinutes(5));
		Arret arret4 = new Arret(g4, now.plusMinutes(6), null);
		List<Arret> arretsItineraire1 = new ArrayList<Arret>();
		arretsItineraire1.add(arret1);
		arretsItineraire1.add(arret2);
		arretsItineraire1.add(arret3);
		arretsItineraire1.add(arret4);

		Itineraire itineraire1 = new Itineraire();
		itineraire1.setTrain(train1);
		itineraire1.setEtat(CodeEtatItinieraire.EN_COURS.getCode());
		itineraire1.setArretsDesservis(arretsItineraire1);
		itineraire1.setArretActuel(arret1);

		em.getTransaction().begin();
		em.persist(train1);
		em.persist(g1);
		em.persist(g2);
		em.persist(g3);
		em.persist(g4);
		em.persist(arret1);
		em.persist(arret2);
		em.persist(arret3);
		em.persist(arret4);
		em.persist(itineraire1);
		em.getTransaction().commit();
	}

	@Test
	void testGetItineraireJaxbByIdTrain() {
		Train t = trainRepository.getTrainByBusinessId(1);
		ItineraireJAXB itineraireJAXB = this.serviceItineraire.getItineraire(t.getId());
		assertEquals(CodeEtatItinieraire.EN_COURS.getCode(), itineraireJAXB.getEtatItineraire());
		assertEquals("Gare1", itineraireJAXB.getArrets().get(0).getGare());
		assertEquals(4, itineraireJAXB.getArrets().size());
	}

	@Test
	void testMajItineraire() {
		Train t = trainRepository.getTrainByBusinessId(1);
		Itineraire it = itineraireRepository.getItineraireByTrainEtEtat(t.getId(), CodeEtatItinieraire.EN_COURS);
		Arret newArret = arretRepository.getArretParItineraireEtNomGare(it, "Gare3");
		ArretJAXB arretJAXB = ArretMapper.mapArretToArretJAXB(newArret);
		serviceItineraire.majItineraire(t.getId(), arretJAXB);
		assertEquals("Gare3", it.getArretActuel().getGare().getNom());
		assertNotEquals("Gare1", it.getArretActuel().getGare().getNom());
		newArret = arretRepository.getArretParItineraireEtNomGare(it, "Gare2");
		arretJAXB = ArretMapper.mapArretToArretJAXB(newArret);
		serviceItineraire.majItineraire(t.getId(), arretJAXB);
		assertEquals("Gare2", it.getArretActuel().getGare().getNom());
	}

}