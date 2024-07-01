package com.artemistechnica.federation.controllers.rest;

import com.artemistechnica.federation.generated.example.api.EnvelopeApi;
import com.artemistechnica.federation.generated.example.models.SimpleData;
import com.artemistechnica.federation.models.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class EnvelopeController implements EnvelopeApi {

    @Override
    public ResponseEntity<ServiceResponse> getErrorEnvelope() {
        return ResponseEntity.internalServerError().body(
                ServiceResponse.mkFailure(
                        String.format("Error: %s", UUID.randomUUID())
                )
        );
    }

    @Override
    public ResponseEntity<ServiceResponse> getSuccessEnvelope() {
        return ResponseEntity.ok(
                ServiceResponse.mkSuccess(
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
                )
        );
    }
}
