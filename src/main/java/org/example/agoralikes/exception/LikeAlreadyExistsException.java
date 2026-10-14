package org.example.agoralikes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LikeAlreadyExistsException extends RuntimeException {

    public LikeAlreadyExistsException(String message) {
        super(message);
    }

}
