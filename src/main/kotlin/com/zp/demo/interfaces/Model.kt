package com.zp.demo.interfaces

import javax.validation.constraints.NotBlank

data class ConfirmPayerClientRequest(
    @NotBlank val phoneNumber: String, @NotBlank val rqUID: String
)