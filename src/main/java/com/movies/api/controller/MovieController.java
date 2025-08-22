package com.movies.api.controller;

import com.movies.api.model.Movie;
import com.movies.api.service.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class MovieController {
    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping("movies")
    public List<Movie> getMovies() {
        return service.getAllMovies();
    }

    @GetMapping("stats")
    public String getStats() {
        return "Total movies: " + service.getStats();
    }

    @GetMapping("old-movies")
    public List<Movie> getOldMovies(@RequestParam(name = "q", defaultValue = "2010") String year) {
        return service.getOldMoviesSlow(year); // intentionally slow
    }

    @GetMapping("getmoviedetails")
    public Movie getMovieDetails(@RequestParam String name) {
        return service.getMovieDetailsSlow(name); // intentionally slow
    }
}
