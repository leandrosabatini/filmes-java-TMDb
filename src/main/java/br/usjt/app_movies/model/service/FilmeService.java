package br.usjt.app_movies.model.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import br.usjt.app_movies.model.dao.FilmeDAO;
import br.usjt.app_movies.model.dao.GeneroDAO;
import br.usjt.app_movies.model.entity.Filme;
import br.usjt.app_movies.model.entity.Genero;
import br.usjt.app_movies.model.javabeans.Lancamentos;
import br.usjt.app_movies.model.javabeans.Movie;
import br.usjt.app_movies.model.javabeans.Populares;

@Service
public class FilmeService {
	public static final String BASE_URL = "https://api.themoviedb.org/3";
	public static final String POPULAR = "/movie/popular";
	public static final String LANCAMENTOS = "/movie/now_playing";
	public static final String LINGUA = "&language=pt-BR";
	public static final String API_KEY = "api_key=9b05335557a0bcac3c7756e7a0f85ce6";
	public static final String POSTER_URL = "https://image.tmdb.org/t/p/w300";

	@Autowired
	private FilmeDAO dao;

	@Autowired
	private GeneroDAO generoDao;
	
	public Filme buscarFilme(int id) throws IOException {
		return dao.buscarFilme(id);
	}

	@Transactional
	public Filme inserirFilme(Filme filme) throws IOException {
		int id = dao.inserirFilme(filme);
		filme.setId(id);
		return filme;
	}

	@Transactional
	public void atualizarFilme(Filme filme) throws IOException {
		dao.atualizarFilme(filme);
	}

	@Transactional
	public void excluirFilme(int id) throws IOException {
		dao.excluirFilme(id);
	}

	public List<Filme> listarFilmes(String chave) throws IOException {
		return dao.listarFilmes(chave);
	}

	public List<Filme> listarFilmes() throws IOException {
		return dao.listarFilmes();
	}

	public List<Filme> listarNovosFilmes() throws IOException {
		return dao.listarNovosFilmes();
	}

	@Transactional
	public List<Filme> baixarFilmesMaisPopulares() throws IOException {
		RestTemplate rest = new RestTemplate();
		String url = BASE_URL + POPULAR + "?" + API_KEY + LINGUA;
		Populares resultado = rest.getForObject(url, Populares.class);
		List<Filme> filmes = new ArrayList<>();

		for (Movie movie : resultado.getResults()) {
			Filme filme = dao.buscarFilmePorIdExterno(movie.getId());

			if (filme == null) {
				filme = new Filme();
			}

			filme.setTitulo(movie.getTitle());
			filme.setIdExterno(movie.getId());
			filme.setDataLancamento(movie.getRelease_date());
			filme.setPopularidade(movie.getPopularity());
			filme.setPosterPath(POSTER_URL + movie.getPoster_path());
			filme.setDescricao(movie.getOverview());

			Genero genero = generoDao.buscarGenero(movie.getGenre_ids()[0]);

			filme.setGenero(genero);
			dao.inserirFilme(filme);
			filmes.add(filme);
		}

		return filmes;
	}

	@Transactional
	public List<Filme> baixarFilmesLancamentos() throws IOException {
		RestTemplate rest = new RestTemplate();
		String url = BASE_URL + LANCAMENTOS + "?" + API_KEY + LINGUA;
		Lancamentos resultado = rest.getForObject(url, Lancamentos.class);
		List<Filme> filmes = new ArrayList<>();

		for (Movie movie : resultado.getResults()) {
			Filme filme = dao.buscarFilmePorIdExterno(movie.getId());

			if (filme == null) {
				filme = new Filme();
			}

			filme.setTitulo(movie.getTitle());
			filme.setDataLancamento(movie.getRelease_date());
			filme.setPopularidade(movie.getPopularity());
			filme.setPosterPath(POSTER_URL + movie.getPoster_path());
			filme.setDescricao(movie.getOverview());

			Genero genero = generoDao.buscarGenero(movie.getGenre_ids()[0]);

			filme.setGenero(genero);
			dao.inserirFilme(filme);
			filmes.add(filme);
		}

		return filmes;
	}

	@Transactional
	public void gravarImagem(String path, Filme filme, MultipartFile file) throws IOException {
		if (!file.isEmpty()) {
			BufferedImage src = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

			path = path.substring(0, path.lastIndexOf(File.separatorChar));
			String nomeArquivo = "img" + filme.getId() + ".jpg";

			filme.setPosterPath(File.separatorChar + "img" + File.separatorChar + nomeArquivo);
			dao.atualizarFilme(filme);
			File destination = new File(path + File.separatorChar + "img" + File.separatorChar + nomeArquivo);
			if (destination.exists()) {
				destination.delete();
			}

			ImageIO.write(src, "jpg", destination);
		}

	}
}
