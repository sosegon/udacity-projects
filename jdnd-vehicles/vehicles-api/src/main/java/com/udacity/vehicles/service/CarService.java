package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final WebClient priceClient;
    private final WebClient mapsClient;
    private final ModelMapper mapper;

    public CarService(CarRepository repository,
                      @Qualifier("pricing") WebClient pricing,
                      @Qualifier("maps") WebClient maps,
                      ModelMapper mapper) {
        this.repository = repository;
        this.priceClient = pricing;
        this.mapsClient = maps;
        this.mapper = mapper;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Optional<Car> opPrice = repository.findById(id);
        Car car = opPrice.orElseThrow(CarNotFoundException::new);

        Price price = priceClient
                .get()
                .uri(
                    uriBuilder -> uriBuilder.path("prices/" + id).build()
                )
                .retrieve().bodyToMono(Price.class).block();
        car.setPrice(price.toString());

        Location location = car.getLocation();
        Address address = mapsClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/maps/")
                                            .queryParam("lat", location.getLat())
                                            .queryParam("lon", location.getLon())
                                            .build()
                )
                .retrieve().bodyToMono(Address.class).block();
        mapper.map(Objects.requireNonNull(address), location);

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> opPrice = repository.findById(id);
        Car car = opPrice.orElseThrow(CarNotFoundException::new);

        repository.delete(car);
    }
}
