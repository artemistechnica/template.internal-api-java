package com.artemistechnica.federation.controllers.rest;

import com.artemistechnica.commons.datatypes.CompletableFutureE;
import com.artemistechnica.commons.datatypes.EitherE;
import com.artemistechnica.commons.datatypes.Envelope;
import com.artemistechnica.commons.datatypes.Pair;
import com.artemistechnica.commons.utils.HelperFunctions;
import com.artemistechnica.commons.utils.Threads;
import com.artemistechnica.federation.generated.example.api.AsyncApi;
import com.artemistechnica.federation.generated.example.models.SimpleData;
import com.artemistechnica.federation.models.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Slf4j
@RestController
public class AsyncController implements AsyncApi {

    @Override
    public ResponseEntity<ServiceResponse> getAsyncError() {
        return ResponseEntity.internalServerError().body(
                ServiceResponse.mk(
                        EitherE.success(Pair.pair(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                                .mapAsyncE(pair -> new SimpleData(pair.left, pair.right))
                                // Need to explicitly parameterize with <SampleModel> since we're purposefully throwing an exception
                                .<SimpleData>mapAsyncE(model -> { throw new RuntimeException(String.format("Error raised processing model with values: %s and %s", model.getValueA(), model.getValueB())); })
                                .materialize()
                                .resolve(
                                        // Handle the error
                                        err     -> Envelope.mkFailure(err.error),
                                        // Handle the success
                                        model   -> Envelope.mkSuccess(model)
                                )
                )
        );
    }

    @Override
    public ResponseEntity<ServiceResponse> getAsync() {
        return ResponseEntity.ok(
                ServiceResponse.mk(
                        EitherE.success(Pair.pair(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                                .mapAsyncE(pair -> new SimpleData(pair.left, pair.right))
                                // Materialize the result from the [[CompletableFutureE]]
                                .materialize()
                                // Materialize an [[Envelope]] from an [[EitherE]]
                                .resolve(
                                        // Handle the error
                                        err     -> { log.error(err.error); return Envelope.mkFailure(err.error); },
                                        // Handle the success
                                        model   -> Envelope.mkSuccess(model)
                                )
                )
        );
    }

    @Override
    public ResponseEntity<ServiceResponse> getAsyncLong() {
        // TODO Major refactor. Look at condensing and adding to commons-java (i.e. map reduce)

        List<Integer> workerCount         = IntStream.rangeClosed(1, 10).boxed().toList();
        AtomicReference<List<SimpleData>> results             = new AtomicReference<>(new ArrayList<>());
        AtomicReference<Boolean>                        masterComplete      = new AtomicReference<>(false);
        List<CompletableFutureE<EitherE<SimpleData>>>  threads             = workerCount
                .stream()
                // Need EitherE#flatMapEAsync
                .map(i -> EitherE.success(i).mapAsyncE(id -> EitherE.success(id).mapAsyncE(workId -> {
                    long sleepLength = ThreadLocalRandom.current().nextLong(2500, 8000 + 1);
                    try {
                        Thread.sleep(sleepLength);
                        SimpleData model = new SimpleData("meta", String.format("Work ID: %d, Thread ID: %s Thread sleep: %d", id, Thread.currentThread().threadId(), sleepLength));
                        results.updateAndGet(l -> {
                            l.add(model);
                            return l;
                        });
                        return model;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).materialize(5000, TimeUnit.MILLISECONDS))).toList(); // It is expected some will fail with time-boxed materialize call.

        // Blocking
        while (!masterComplete.get()) {
            Threads.sleep(200);
            masterComplete.updateAndGet(c -> threads.stream().filter(CompletableFutureE::isDone).toList().size() == threads.size());
        }

        List<SimpleData> models = threads
                .stream()
                .map(t -> t.materialize() // A 'materializeFlatMap' of sorts might help here
                        .flatMapE(HelperFunctions::identity)
                        .resolve(
                                err -> { log.error(err.exception.map(e -> e.getClass().getName()).orElseGet(() -> "UNKNOWN ERROR MESSAGE!")); return new SimpleData("error", err.exception.get().getClass().getName()); },
                                HelperFunctions::identity
                        )
                ).toList();

        return ResponseEntity.ok(
                ServiceResponse.mk(
                        Envelope.mkSuccess(models.toArray(new SimpleData[0]))
                )
        );
    }
}
