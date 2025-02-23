package me.astroreen.liblanka.common.excpetion;

import org.springframework.http.HttpStatus;

public interface ApiException {
    default HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
