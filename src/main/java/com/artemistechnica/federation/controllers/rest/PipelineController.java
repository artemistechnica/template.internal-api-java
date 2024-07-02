package com.artemistechnica.federation.controllers.rest;

import com.artemistechnica.commons.datatypes.Envelope;
import com.artemistechnica.commons.services.Federation;
import com.artemistechnica.commons.utils.HelperFunctions;
import com.artemistechnica.federation.generated.example.api.PipelineApi;
import com.artemistechnica.federation.generated.example.models.SimpleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class PipelineController implements PipelineApi, Federation {

    @Override
    public ResponseEntity<Envelope> getPipeline() {
        return ResponseEntity.ok(
                federate(HelperFunctions::identity)
                        .apply(Federation.Context.mk(UUID.randomUUID().toString()))
                        .flatMapE(mat -> mat.materialize(c -> new SimpleData("SUCCESS", c.value)))
                        .map(Envelope::mkSuccess).right.orElse(Envelope.mkFailure(String.format("Error: %s", UUID.randomUUID())))
        );
    }

    @Override
    public ResponseEntity<Envelope> getPipelineError() {
        return ResponseEntity.internalServerError().body(
                federate(ctx ->  { throw new RuntimeException("Exception raised!"); })
                        .apply(Federation.Context.mk(UUID.randomUUID().toString()))
                        .flatMapE(mat -> mat.materialize(c -> new SimpleData("SUCCESS", c.value)))
                        .resolve(
                                error -> Envelope.mkFailure(String.format("Materialized error: %s", error.error)),
                                model -> Envelope.mkSuccess(model)
                        )
        );
    }

    @Override
    public ResponseEntity<Envelope> setPipeline(SimpleData simpleData) {
        return ResponseEntity.internalServerError().body(
                federate(HelperFunctions::identity)
                        .apply(Federation.Context.mk(UUID.randomUUID().toString()))
                        .flatMapE(mat -> mat.materialize(c -> simpleData))
                        .resolve(
                                error -> Envelope.mkFailure(String.format("Materialized error: %s", error.error)),
                                model -> Envelope.mkSuccess(model)
                        )
        );
    }
}
