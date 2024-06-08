package com.artemistechnica.federation.controllers;

import com.artemistechnica.commons.datatypes.EitherE;
import com.artemistechnica.commons.datatypes.Envelope;
import com.artemistechnica.commons.datatypes.Pair;
import com.artemistechnica.commons.utils.HelperFunctions;
import com.artemistechnica.federation.models.SampleModel;
import com.artemistechnica.federation.services.Federation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SimpleController implements Federation {

    @GetMapping("/sample")
    public String getSample() {
        return "Hello, World!";
    }

    @GetMapping("/envelope")
    public @ResponseBody Envelope<SampleModel> getSampleEnvelope() {
        return Envelope.mkSuccess(
                SampleModel.mk(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                ),
                SampleModel.mk(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                ),
                SampleModel.mk(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                )
        );
    }

    @GetMapping("/envelope/async")
    public @ResponseBody Envelope<SampleModel> getSampleEnvelopeAsync() {
        return EitherE.success(Pair.pair(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .mapAsyncE(pair -> SampleModel.mk(pair.left, pair.right))
                // Materialize the result from the [[CompletableFutureE]]
                .materialize()
                // Materialize an [[Envelope]] from an [[EitherE]]
                .materialize(
                        // Handle the error
                        err     -> Envelope.mkFailure(err.error),
                        // Handle the success
                        model   -> Envelope.mkSuccess(model)
                );
    }

    @GetMapping("/envelope/async/error")
    public @ResponseBody Envelope<SampleModel> getSampleErrorEnvelopeAsync() {
        return EitherE.success(Pair.pair(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .mapAsyncE(pair -> SampleModel.mk(pair.left, pair.right))
                // Need to explicitly parameterize with <SampleModel> since we're purposefully throwing an exception
                .<SampleModel>mapAsyncE(model -> { throw new RuntimeException(String.format("Error raised processing model with values: %s and %s", model.valueA, model.valueB)); })
                .materialize()
                .materialize(
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
                .materialize(
                        error -> Envelope.mkFailure(String.format("Materialized error: %s", error.error)),
                        model -> Envelope.mkSuccess(model)
                );
    }

    @GetMapping("/envelope/error")
    public @ResponseBody Envelope<SampleModel> getSampleErrorEnvelope() {
        return Envelope.mkFailure(String.format("Error: %s", UUID.randomUUID()));
    }
}
