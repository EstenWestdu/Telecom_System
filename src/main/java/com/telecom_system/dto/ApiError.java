package com.telecom_system.dto;

import java.time.Instant;

public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    private ApiError(Builder builder) {
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.error = builder.error;
        this.message = builder.message;
        this.path = builder.path;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public static final class Builder {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        private Builder() {
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public ApiError build() {
            if (timestamp == null) {
                timestamp = Instant.now();
            }
            return new ApiError(this);
        }
    }
}
