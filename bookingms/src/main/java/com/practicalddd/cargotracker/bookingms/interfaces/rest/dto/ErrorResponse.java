package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para padronizar respostas de erro da API.
 * Segue o padrão RFC 7807 (Problem Details for HTTP APIs) de forma simplificada.
 */
public class ErrorResponse {
    private String type;        // URI identificando o tipo de erro
    private String title;       // Título breve do problema
    private String detail;      // Descrição detalhada
    private int status;         // Código HTTP
    private String instance;    // URI que identifica a ocorrência específica
    private LocalDateTime timestamp; // Quando o erro ocorreu
    private Object violations;  // Validações específicas (opcional)

    // Construtores
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String title, String detail, int status) {
        this();
        this.title = title;
        this.detail = detail;
        this.status = status;
        this.type = "about:blank"; // Valor padrão conforme RFC 7807
    }

    public ErrorResponse(String title, String detail, int status, String type) {
        this(title, detail, status);
        this.type = type;
    }

    public ErrorResponse(String title, String detail, int status, String type, String instance) {
        this(title, detail, status, type);
        this.instance = instance;
    }

    // Factory methods para erros comuns
    public static ErrorResponse badRequest(String detail) {
        return new ErrorResponse(
            "Bad Request",
            detail,
            400,
            "/errors/bad-request"
        );
    }

    public static ErrorResponse validationError(String detail, Object violations) {
        ErrorResponse response = new ErrorResponse(
            "Validation Error",
            detail,
            422,
            "/errors/validation"
        );
        response.setViolations(violations);
        return response;
    }

    public static ErrorResponse notFound(String detail) {
        return new ErrorResponse(
            "Not Found",
            detail,
            404,
            "/errors/not-found"
        );
    }

    public static ErrorResponse conflict(String detail) {
        return new ErrorResponse(
            "Conflict",
            detail,
            409,
            "/errors/conflict"
        );
    }

    public static ErrorResponse internalError(String detail) {
        return new ErrorResponse(
            "Internal Server Error",
            detail,
            500,
            "/errors/internal"
        );
    }

    // Builder pattern para construção fluente
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String title;
        private String detail;
        private int status;
        private String instance;
        private Object violations;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder violations(Object violations) {
            this.violations = violations;
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse response = new ErrorResponse();
            response.type = this.type != null ? this.type : "about:blank";
            response.title = this.title;
            response.detail = this.detail;
            response.status = this.status;
            response.instance = this.instance;
            response.violations = this.violations;
            response.timestamp = LocalDateTime.now();
            return response;
        }
    }

    // Getters e Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getViolations() {
        return violations;
    }

    public void setViolations(Object violations) {
        this.violations = violations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorResponse)) return false;
        ErrorResponse that = (ErrorResponse) o;
        return status == that.status &&
               Objects.equals(type, that.type) &&
               Objects.equals(title, that.title) &&
               Objects.equals(detail, that.detail) &&
               Objects.equals(instance, that.instance) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(violations, that.violations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, detail, status, instance, timestamp, violations);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
               "type='" + type + '\'' +
               ", title='" + title + '\'' +
               ", detail='" + detail + '\'' +
               ", status=" + status +
               ", instance='" + instance + '\'' +
               ", timestamp=" + timestamp +
               ", violations=" + violations +
               '}';
    }
}
