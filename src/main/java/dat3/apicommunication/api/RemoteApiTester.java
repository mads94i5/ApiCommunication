package dat3.apicommunication.api;

import dat3.apicommunication.dtos.Gender;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RemoteApiTester implements CommandLineRunner {
    private Mono<String> callSlowEndpoint(){
        Mono<String> slowResponse = WebClient.create()
                .get()
                .uri("http://localhost:8080/random-string-slow")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e-> System.out.println("UUUPS : "+e.getMessage()));
        return slowResponse;
    }
    public void callSlowEndpointBlocking(){
        long start = System.currentTimeMillis();
        List<String> ramdomStrings = new ArrayList<>();

        Mono<String> slowResponse = callSlowEndpoint();
        ramdomStrings.add(slowResponse.block()); //Three seconds spent

        slowResponse = callSlowEndpoint();
        ramdomStrings.add(slowResponse.block());//Three seconds spent

        slowResponse = callSlowEndpoint();
        ramdomStrings.add(slowResponse.block());//Three seconds spent
        long end = System.currentTimeMillis();
        ramdomStrings.add(0,"Time spent BLOCKING (ms): "+(end-start));

        System.out.println(ramdomStrings.stream().collect(Collectors.joining(",")));
    }

    public void callSlowEndpointNonBlocking(){
        long start = System.currentTimeMillis();
        Mono<String> sr1 = callSlowEndpoint();
        Mono<String> sr2 = callSlowEndpoint();
        Mono<String> sr3 = callSlowEndpoint();

        var rs = Mono.zip(sr1,sr2,sr3).map(t-> {
            List<String> randomStrings = new ArrayList<>();
            randomStrings.add(t.getT1());
            randomStrings.add(t.getT2());
            randomStrings.add(t.getT3());
            long end = System.currentTimeMillis();
            randomStrings.add(0,"Time spent NON-BLOCKING (ms): "+(end-start));
            return randomStrings;
        });
        List<String> randoms = rs.block(); //We only block when all the three Mono's has fulfilled
        System.out.println(randoms.stream().collect(Collectors.joining(",")));
    }

    private Mono<Gender> getGenderForName(String name) {
        WebClient client = WebClient.create();
        Mono<Gender> gender = client.get()
                .uri("https://api.genderize.io?name="+name)
                .retrieve()
                .bodyToMono(Gender.class);
        return gender;
    }

    List<String> names = Arrays.asList("lars", "peter", "sanne", "kim", "david", "maja");


    public void getGendersBlocking() {
        long start = System.currentTimeMillis();
        List<Gender> genders = names.stream().map(name -> getGenderForName(name).block()).toList();
        long end = System.currentTimeMillis();
        System.out.println("Time for six external requests, BLOCKING: "+ (end-start));
        String gendersString = "";
        for (Gender gender : genders) {
            gendersString += gender + ", ";
        }
        System.out.println("Genders: " + gendersString);
    }

    public void getGendersNonBlocking() {
        long start = System.currentTimeMillis();
        var genders = names.stream().map(name -> getGenderForName(name)).toList();
        Flux<Gender> flux = Flux.merge(Flux.concat(genders));
        List<Gender> res = flux.collectList().block();
        long end = System.currentTimeMillis();
        System.out.println("Time for six external requests, NON-BLOCKING: "+ (end-start));
        String gendersString = "";
        for (Gender gender : res) {
            gendersString += gender.getGender() + ", ";
        }
        System.out.println("Genders: " + gendersString);
    }


    @Override
    public void run(String... args) throws Exception {
        /*

        // make system out print to console with the current time and date
        System.out.println(java.time.LocalDateTime.now() + " - Calling slow endpoint");
        String randomStr = callSlowEndpoint().block();
        System.out.println(java.time.LocalDateTime.now() + " - " + randomStr);

        System.out.println(java.time.LocalDateTime.now() + " - Calling slow endpoint blocking");
        callSlowEndpointBlocking();

        System.out.println(java.time.LocalDateTime.now() + " - Calling slow endpoint non-blocking");
        callSlowEndpointNonBlocking();

        System.out.println(java.time.LocalDateTime.now() + " - Calling gender name endpoint");
        String gender = getGenderForName("jens").block().getGender();
        System.out.println(java.time.LocalDateTime.now() + " - " + gender);

        System.out.println(java.time.LocalDateTime.now() + " - Calling many external endpoints, blocking");
        getGendersBlocking();

        System.out.println(java.time.LocalDateTime.now() + " - Calling many external endpoints, non-blocking");
        getGendersNonBlocking();

         */
    }
}
