package dat3.apicommunication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat3.apicommunication.dtos.Age;
import dat3.apicommunication.dtos.Country;
import dat3.apicommunication.dtos.Gender;
import dat3.apicommunication.entity.NameInfo;
import dat3.apicommunication.repository.NameInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class NameInfoService {
    NameInfoRepository nameInfoRepository;

    public NameInfoService(NameInfoRepository nameInfoRepository) {
        this.nameInfoRepository = nameInfoRepository;
    }
    public NameInfo getNameInfo(String name) {

        NameInfo repoNameInfo = nameInfoRepository.findByName(name);
        if (repoNameInfo != null) {
            return repoNameInfo;
        }

        Mono<Country> country = getCountryForName(name);
        Mono<Age> age = getAgeForName(name);
        Mono<Gender> gender = getGenderForName(name);

        var rs = Mono.zip(country,age,gender).map(t->{
            var nameInfo = new NameInfo(name,
                    t.getT1().getCountry_id(),
                    Integer.parseInt(t.getT2().getAge()),
                    t.getT3().getGender());
            return nameInfo;
        }).block();
        if (rs != null) {
            nameInfoRepository.save(rs);
        }
        return rs;
    }
    private Mono<Country> getCountryForName(String name) {
        WebClient client = WebClient.create();
        return client.get()
                .uri("https://api.nationalize.io?name=" + name)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = null;
                    try {
                        root = mapper.readTree(response);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    JsonNode countriesNode = root.get("country");
                    if (countriesNode == null || !countriesNode.isArray() || countriesNode.size() == 0) {
                        return Mono.empty();
                    } else {
                        List<Country> countries = new ArrayList<>();
                        for (JsonNode countryNode : countriesNode) {
                            Country country = mapper.convertValue(countryNode, Country.class);
                            countries.add(country);
                        }
                        return Mono.just(Collections.max(countries, Comparator.comparing(Country::getProbability)));
                    }
                });
    }
    private Mono<Age> getAgeForName(String name) {
        WebClient client = WebClient.create();
        Mono<Age> age = client.get()
                .uri("https://api.agify.io?name="+name)
                .retrieve()
                .bodyToMono(Age.class);
        return age;
    }
    private Mono<Gender> getGenderForName(String name) {
        WebClient client = WebClient.create();
        Mono<Gender> gender = client.get()
                .uri("https://api.genderize.io?name="+name)
                .retrieve()
                .bodyToMono(Gender.class);
        return gender;
    }
}
