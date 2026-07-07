package com.voltgrid.dto;

import com.voltgrid.model.ConnectorType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateStationRequest(
        @NotBlank @Size(max = 128) String externalId,
        @NotBlank @Size(max = 200) String name,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        ConnectorType connectorType,
        @Positive Double powerKw,
        @PositiveOrZero BigDecimal pricePerKwh
) {}
