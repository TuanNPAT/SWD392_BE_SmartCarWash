package com.swd392.smartcarwash.dto.request;

import lombok.Data;

@Data
public class VerifyTokenRequest {
    private String token; // token FE gửi lên (cf-turnstile-response)
}
