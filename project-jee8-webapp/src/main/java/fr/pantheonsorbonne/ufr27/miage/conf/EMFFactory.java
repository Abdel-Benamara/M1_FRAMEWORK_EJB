package fr.pantheonsorbonne.ufr27.miage.conf;

import java.util.function.Supplier;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMFFactory implements Supplier<EntityManagerFactory> {
	private final EntityManagerFactory emf;

	public EMFFactory() {
		emf = Persistence.createEntityManagerFactory("default");
	}

	@Override
	public EntityManagerFactory get() {
		return emf;
	}

}