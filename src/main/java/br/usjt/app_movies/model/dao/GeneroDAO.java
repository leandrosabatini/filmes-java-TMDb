package br.usjt.app_movies.model.dao;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import br.usjt.app_movies.model.entity.Genero;

@Repository
public class GeneroDAO {
	@PersistenceContext
	EntityManager manager;

	public int inserirGenero(Genero genero) throws IOException {
		manager.persist(genero);
		return genero.getId();
	}
	
	public Genero buscarGenero(int id) throws IOException {
		return manager.find(Genero.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Genero> listarGeneros() throws IOException {
		return manager.createQuery("select g from Genero g").getResultList();
	}
}
