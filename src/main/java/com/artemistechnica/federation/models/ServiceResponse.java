package com.artemistechnica.federation.models;

import com.artemistechnica.commons.datatypes.Envelope;

public class ServiceResponse<A> {

    public final Envelope<A> response;

    private ServiceResponse(A... data) {
        this.response = Envelope.mkSuccess(data);
    }

    private ServiceResponse(String error) {
        this.response = Envelope.mkFailure(error);
    }

    private ServiceResponse(Envelope<A> envelope) {
        this.response = envelope;
    }

    public static <A> ServiceResponse<A> mkSuccess(A... data) {
        return new ServiceResponse<>(data);
    }

    public static <A> ServiceResponse<A> mkFailure(String data) {
        return new ServiceResponse<>(data);
    }

    public static <A> ServiceResponse<A> mk(Envelope<A> envelope) {
        return new ServiceResponse<>(envelope);
    }
}
