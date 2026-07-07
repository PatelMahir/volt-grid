package com.voltgrid.dto;

import jakarta.validation.constraints.NotNull;

public record CommandRequest(
        @NotNull Command command
) {
    public enum Command { START, STOP }
}
