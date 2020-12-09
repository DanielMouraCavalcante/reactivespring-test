package br.com.dnkt.reactivespring.repository;

import br.com.dnkt.reactivespring.document.ItemCapped;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemReactiveCappedRepository extends ReactiveMongoRepository<ItemCapped, String> {


    @Tailable
    Flux<ItemCapped> findItemsBy();

}
