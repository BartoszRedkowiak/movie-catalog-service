package com.example.moviecatalogservice.resources;

import com.example.moviecatalogservice.models.CatalogItem;
import com.example.moviecatalogservice.models.Movie;
import com.example.moviecatalogservice.models.UserRating;
import com.netflix.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

    private final RestTemplate restTemplate; //to be deprecated. Alternative class is WebClient (reactive programming)
    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;


    @Autowired
    public MovieCatalogResource(RestTemplate restTemplate, WebClient.Builder webClientBuilder, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.webClientBuilder = webClientBuilder;
        this.discoveryClient = discoveryClient;
    }

    @RequestMapping("/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {

        // get all rated movie IDs
        //when connected to eureka URI contains microservice application name
        UserRating ratings = restTemplate.getForObject("http://rating-data-service/ratingsdata/users/" + userId, UserRating.class);

        return ratings.getUserRating().stream().map(rating -> {
            // for each movie ID, call movie info service and get details
            Movie movie = restTemplate.getForObject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
            // put the all together
            return new CatalogItem(movie.getName(), "Desc", rating.getRating());

        })
                .collect(Collectors.toList());




    }



}


/*
    // getting movie object using WebClient
    Movie movie = webClientBuilder.build()
            .get() //HTTP request method
            .uri("http://localhost:8082/movies/" + rating.getMovieId())
            .retrieve()
            .bodyToMono(Movie.class)// convert response body to movie class
            .block(); //blocks execution until mono is completed (with data)
 */