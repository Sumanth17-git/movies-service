package com.movies.api.service;

import com.movies.api.model.Movie;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private static final Logger LOG = LoggerFactory.getLogger(MovieService.class);
    private final Gson gson = new Gson();

    // Cached list for fast APIs
    private final List<Movie> cachedMovies;

    public MovieService() {
        this.cachedMovies = loadMovies();
    }

    private List<Movie> loadMovies() {
        try (var is = getClass().getClassLoader().getResourceAsStream("movies.json");
             var reader = new InputStreamReader(is)) {
            LOG.info("Loading movies.json into memory...");
            return gson.fromJson(reader, new TypeToken<List<Movie>>(){}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load movies.json", e);
        }
    }

    // ✅ Fast APIs
    public List<Movie> getAllMovies() {
        LOG.debug("Returning {} movies", cachedMovies.size());
        return cachedMovies;
    }

    public long getStats() {
        return cachedMovies.size();
    }

    // ❌ Slow API: reload file + scan every request
    public Movie getMovieDetailsSlow(String name) {
        long start = System.currentTimeMillis();
        try (var is = getClass().getClassLoader().getResourceAsStream("movies.json");
             var reader = new InputStreamReader(is)) {

            List<Movie> freshList = gson.fromJson(reader, new TypeToken<List<Movie>>(){}.getType());
            Movie result = freshList.stream()
                    .filter(m -> m.getTitle() != null && m.getTitle().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);

            LOG.warn("getMovieDetailsSlow: query={} scanned {} records in {} ms",
                    name, freshList.size(), (System.currentTimeMillis() - start));
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to search movies.json", e);
        }
    }

    // ❌ Slow API: reload + filter every request
    public List<Movie> getOldMoviesSlow(String year) {
        long start = System.currentTimeMillis();
        try (var is = getClass().getClassLoader().getResourceAsStream("movies.json");
             var reader = new InputStreamReader(is)) {

            List<Movie> freshList = gson.fromJson(reader, new TypeToken<List<Movie>>(){}.getType());
            List<Movie> result = freshList.stream()
                    .filter(m -> m.getReleaseDate() != null && m.getReleaseDate().compareTo(year) < 0)
                    .collect(Collectors.toList());

            LOG.warn("getOldMoviesSlow: year={} scanned {} records, matched={} in {} ms",
                    year, freshList.size(), result.size(), (System.currentTimeMillis() - start));
            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to filter old movies", e);
        }
    }
}
