package com.artemistechnica.federation.controllers.rest;

import com.artemistechnica.commons.datatypes.CompletableFutureE;
import com.artemistechnica.commons.datatypes.EitherE;
import com.artemistechnica.commons.datatypes.Envelope;
import com.artemistechnica.commons.datatypes.Pair;
import com.artemistechnica.commons.utils.HelperFunctions;
import com.artemistechnica.federation.example.models.SimpleData;
import com.artemistechnica.federation.models.SampleModel;
import com.artemistechnica.federation.services.Federation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
public class SimpleController implements Federation {

    @GetMapping("/sample")
    public String getSample() {
        return "Hello, World!";
    }

    @GetMapping("/envelope")
    public @ResponseBody Envelope<SimpleData> getSampleEnvelope() {
        return Envelope.mkSuccess(
                new SimpleData(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                ),
                new SimpleData(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                ),
                new SimpleData(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                )
        );
    }

    @GetMapping("/envelope/async")
    public @ResponseBody Envelope<SampleModel> getSampleEnvelopeAsync() {
        log.debug("Entering: /envelope/async");
        return EitherE.success(Pair.pair(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .mapAsyncE(pair -> SampleModel.mk(pair.left, pair.right))
                // Materialize the result from the [[CompletableFutureE]]
                .materialize()
                // Materialize an [[Envelope]] from an [[EitherE]]
                .resolve(
                        // Handle the error
                        err     -> { log.error(err.error); return Envelope.mkFailure(err.error); },
                        // Handle the success
                        model   -> Envelope.mkSuccess(model)
                );
    }

    @GetMapping("/envelope/async/advanced")
    public @ResponseBody Envelope<SampleModel> getSampleEnvelopeAsyncMany() throws InterruptedException {

        // TODO Major refactor. Look at condensing and adding to commons-java (i.e. map reduce)

        List<Integer>                                   workerCount         = IntStream.rangeClosed(1, 10).boxed().toList();
        AtomicReference<List<SampleModel>>              results             = new AtomicReference<>(new ArrayList<>());
        AtomicReference<Boolean>                        masterComplete      = new AtomicReference<>(false);
        List<CompletableFutureE<EitherE<SampleModel>>>  threads             = workerCount
                .stream()
                // Need EitherE#flatMapEAsync
                .map(i -> EitherE.success(i).mapAsyncE(id -> EitherE.success(id).mapAsyncE(workId -> {
                    long sleepLength = ThreadLocalRandom.current().nextLong(2500, 8000 + 1);
                    try {
                        Thread.sleep(sleepLength);
                        SampleModel model = SampleModel.mk("meta", String.format("Work ID: %d, Thread ID: %s Thread sleep: %d", id, Thread.currentThread().threadId(), sleepLength));
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
            Thread.sleep(250);
            masterComplete.updateAndGet(c -> threads.stream().filter(CompletableFutureE::isDone).toList().size() == threads.size());
        }

        List<SampleModel> models = threads
                .stream()
                .map(t -> t.materialize() // A 'materializeFlatMap' of sorts might help here
                        .flatMapE(HelperFunctions::identity)
                        .resolve(
                                err -> { log.error(err.exception.map(e -> e.getClass().getName()).orElseGet(() -> "UNKNOWN ERROR MESSAGE!")); return SampleModel.mk("error", err.exception.get().getClass().getName()); },
                                HelperFunctions::identity
                        )
                ).toList();

        return Envelope.mkSuccess(models.toArray(new SampleModel[0]));
    }

    @GetMapping("/envelope/async/error")
    public @ResponseBody Envelope<SampleModel> getSampleErrorEnvelopeAsync() {
        return EitherE.success(Pair.pair(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .mapAsyncE(pair -> SampleModel.mk(pair.left, pair.right))
                // Need to explicitly parameterize with <SampleModel> since we're purposefully throwing an exception
                .<SampleModel>mapAsyncE(model -> { throw new RuntimeException(String.format("Error raised processing model with values: %s and %s", model.valueA, model.valueB)); })
                .materialize()
                .resolve(
                        // Handle the error
                        err     -> Envelope.mkFailure(err.error),
                        // Handle the success
                        model   -> Envelope.mkSuccess(model)
                );
    }

    @GetMapping("/pipeline")
    public @ResponseBody Envelope<SampleModel> getSamplePipeline() {
        return federate(HelperFunctions::identity)
                .apply(Context.mk(UUID.randomUUID().toString()))
                .flatMapE(mat -> mat.materialize(c -> SampleModel.mk("SUCCESS", c.value)))
                .map(Envelope::mkSuccess).right.orElse(getSampleErrorEnvelope());
    }

    @GetMapping("/pipeline/error")
    public @ResponseBody Envelope<SampleModel> getSampleErrorPipeline() {
        return federate(ctx ->  { throw new RuntimeException("Exception raised!"); })
                .apply(Context.mk(UUID.randomUUID().toString()))
                .flatMapE(mat -> mat.materialize(c -> SampleModel.mk("SUCCESS", c.value)))
                .resolve(
                        error -> Envelope.mkFailure(String.format("Materialized error: %s", error.error)),
                        model -> Envelope.mkSuccess(model)
                );
    }

    @GetMapping("/envelope/error")
    public @ResponseBody Envelope<SampleModel> getSampleErrorEnvelope() {
        return Envelope.mkFailure(String.format("Error: %s", UUID.randomUUID()));
    }
}
