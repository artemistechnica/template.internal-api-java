package com.artemistechnica.federation.controllers;

import com.artemistechnica.commons.datatypes.EitherE;
import com.artemistechnica.commons.datatypes.Envelope;
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
                .biFlatMapE(err -> EitherE.success(Envelope.<SampleModel>mkFailure(err.error)), mat -> mat.materialize(c -> Envelope.mkSuccess(SampleModel.mk("SUCCESS", c.value))))
                .right.orElseGet(this::getSampleErrorEnvelope);
    }

    @GetMapping("/envelope/error")
    public @ResponseBody Envelope<SampleModel> getSampleErrorEnvelope() {
        return Envelope.mkFailure(String.format("Error: %s", UUID.randomUUID()));
    }
}
