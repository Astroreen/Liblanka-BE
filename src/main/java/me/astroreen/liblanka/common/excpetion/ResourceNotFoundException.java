package me.astroreen.liblanka.common.excpetion;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException implements ApiException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

