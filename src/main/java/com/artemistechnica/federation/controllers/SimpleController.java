package com.artemistechnica.federation.controllers;

import com.artemistechnica.commons.datatypes.Envelope;
import com.artemistechnica.federation.models.SampleModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SimpleController {

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

    @GetMapping("/envelope/error")
    public @ResponseBody Envelope<SampleModel> getSampleErrorEnvelope() {
        return Envelope.mkFailure(String.format("Error: %s", UUID.randomUUID()));
    }
}
