package testSansIncidents;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.Locale;

import javax.inject.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import fr.pantheonsorbonne.ufr27.miage.conf.EMFFactory;
import fr.pantheonsorbonne.ufr27.miage.conf.EMFactory;
import fr.pantheonsorbonne.ufr27.miage.conf.PersistenceConf;
import fr.pantheonsorbonne.ufr27.miage.dao.InvoiceDAO;
import fr.pantheonsorbonne.ufr27.miage.dao.PaymentDAO;
import fr.pantheonsorbonne.ufr27.miage.exception.ExceptionMapper;
import fr.pantheonsorbonne.ufr27.miage.jms.PaymentValidationAckownledgerBean;
import fr.pantheonsorbonne.ufr27.miage.jms.conf.ConnectionFactorySupplier;
import fr.pantheonsorbonne.ufr27.miage.jms.conf.JMSProducer;
import fr.pantheonsorbonne.ufr27.miage.jms.conf.PaymentAckQueueSupplier;
import fr.pantheonsorbonne.ufr27.miage.jms.conf.PaymentQueueSupplier;
import fr.pantheonsorbonne.ufr27.miage.jms.utils.BrokerUtils;
import fr.pantheonsorbonne.ufr27.miage.n_dao.ArretDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.GareDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.IncidentDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.ItineraireDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.TrainDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.TrajetDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.VoyageDAO;
import fr.pantheonsorbonne.ufr27.miage.n_dao.VoyageurDAO;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceIncident;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceItineraire;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceMajDecideur;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceMajExecuteur;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceMajInfoGare;
import fr.pantheonsorbonne.ufr27.miage.n_service.ServiceUtilisateur;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.BDDFillerServiceImpl;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceIncidentImp;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceItineraireImp;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceMajDecideurImp;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceMajExecuteurImp;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceMajInfoGareImp;
import fr.pantheonsorbonne.ufr27.miage.n_service.impl.ServiceUtilisateurImp;
import fr.pantheonsorbonne.ufr27.miage.service.GymService;
import fr.pantheonsorbonne.ufr27.miage.service.InvoicingService;
import fr.pantheonsorbonne.ufr27.miage.service.MailingService;
import fr.pantheonsorbonne.ufr27.miage.service.PaymentService;
import fr.pantheonsorbonne.ufr27.miage.service.UserService;
import fr.pantheonsorbonne.ufr27.miage.service.impl.GymServiceImpl;
import fr.pantheonsorbonne.ufr27.miage.service.impl.InvoicingServiceImpl;
import fr.pantheonsorbonne.ufr27.miage.service.impl.MailingServiceImpl;
import fr.pantheonsorbonne.ufr27.miage.service.impl.PaymentServiceImpl;
import fr.pantheonsorbonne.ufr27.miage.service.impl.UserServiceImpl;

class TestItineraire {
	
	public static final String BASE_URI = "http://localhost:8080/";

	public static HttpServer startServer() {

		final ResourceConfig rc = new ResourceConfig()//
				.packages(true, "fr.pantheonsorbonne.ufr27.miage")//
				.register(DeclarativeLinkingFeature.class)//
				.register(JMSProducer.class).register(ExceptionMapper.class).register(PersistenceConf.class)
				.register(new AbstractBinder() {

					@Override
					protected void configure() {

						bind(GymServiceImpl.class).to(GymService.class);
						bind(PaymentServiceImpl.class).to(PaymentService.class);
						bind(InvoicingServiceImpl.class).to(InvoicingService.class);
						bind(InvoiceDAO.class).to(InvoiceDAO.class);
						bind(UserServiceImpl.class).to(UserService.class);
						bind(MailingServiceImpl.class).to(MailingService.class);
						bind(PaymentDAO.class).to(PaymentDAO.class);

						bindFactory(PaymentAckQueueSupplier.class).to(Queue.class).named("PaymentAckQueue")
								.in(Singleton.class);
						bindFactory(PaymentQueueSupplier.class).to(Queue.class).named("PaymentQueue")
								.in(Singleton.class);

						bind(PaymentValidationAckownledgerBean.class).to(PaymentValidationAckownledgerBean.class)
								.in(Singleton.class);

						// ---
						
						bindFactory(EMFFactory.class).to(EntityManagerFactory.class).in(Singleton.class);
						bindFactory(EMFactory.class).to(EntityManager.class).in(RequestScoped.class);
						bindFactory(ConnectionFactorySupplier.class).to(ConnectionFactory.class).in(Singleton.class);
						
						bind(ServiceIncidentImp.class).to(ServiceIncident.class);
						bind(ServiceItineraireImp.class).to(ServiceItineraire.class);
						bind(ServiceMajDecideurImp.class).to(ServiceMajDecideur.class);
						bind(ServiceMajExecuteurImp.class).to(ServiceMajExecuteur.class);
						bind(ServiceMajInfoGareImp.class).to(ServiceMajInfoGare.class);
						bind(ServiceUtilisateurImp.class).to(ServiceUtilisateur.class);

						bind(ArretDAO.class).to(ArretDAO.class);
						bind(GareDAO.class).to(GareDAO.class);
						bind(IncidentDAO.class).to(IncidentDAO.class);
						bind(ItineraireDAO.class).to(ItineraireDAO.class);
						bind(TrainDAO.class).to(TrainDAO.class);
						bind(TrajetDAO.class).to(TrajetDAO.class);
						bind(VoyageDAO.class).to(VoyageDAO.class);
						bind(VoyageurDAO.class).to(VoyageurDAO.class);
					}

				});

		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
	}

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		final HttpServer server = startServer();

		BrokerUtils.startBroker();

		PersistenceConf pc = new PersistenceConf();
		pc.getEM();
		pc.launchH2WS();

		BDDFillerServiceImpl filler = new BDDFillerServiceImpl(pc.getEM());
		filler.fill();

	}

	@Test
	void test() {
		assertTrue(true);
	}

}