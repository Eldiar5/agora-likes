package org.example.agoralikes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private String message;
    private LocalDateTime timestamp;

    public ErrorResponseDto(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}
