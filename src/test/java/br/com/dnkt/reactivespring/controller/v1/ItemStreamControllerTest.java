package br.com.dnkt.reactivespring.controller.v1;

import br.com.dnkt.reactivespring.constants.ItemConstants;
import br.com.dnkt.reactivespring.document.ItemCapped;
import br.com.dnkt.reactivespring.repository.ItemReactiveCappedRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ItemStreamControllerTest {

    @Autowired
    ItemReactiveCappedRepository itemReactiveCappedRepository;

    @Autowired
    ReactiveMongoOperations mongoOperations;

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    public void setUp() {

        mongoOperations.dropCollection(ItemCapped.class);
        mongoOperations.createCollection(ItemCapped.class, CollectionOptions.empty().maxDocuments(20).size(50000).capped())
            .block();

        Flux<ItemCapped> itemCappedFlux = Flux.interval(Duration.ofMillis(100))
            .map(i -> new ItemCapped(null, "Random Item " + i, (100.00 + i)))
            .take(5);

        itemReactiveCappedRepository
            .insert(itemCappedFlux)
            .doOnNext((itemCapped -> {
                System.out.println("Inserted Item in setUp " + itemCapped);
            }))
            .blockLast();
    }

    @Test
    public void testStreamAllItems() {

        Flux<ItemCapped> itemCappedFlux = webTestClient.get().uri(ItemConstants.ITEM_STREAM_END_POINT_V1)
            .exchange()
            .expectStatus().isOk()
            .returnResult((ItemCapped.class))
            .getResponseBody()
            .take(5);

        StepVerifier.create(itemCappedFlux)
            .expectNextCount(5)
            .thenCancel()
            .verify();
    }

}
