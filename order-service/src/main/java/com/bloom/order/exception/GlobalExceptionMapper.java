package com.bloom.order.exception;

import org.jboss.logging.Logger;

import com.bloom.common.dto.ApiResponse;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.errorf(exception, "Unhandled error: %s", exception.getMessage());

        if (exception instanceof WebApplicationException webApplicationException) {
            return Response.status(webApplicationException.getResponse().getStatus())
                    .entity(ApiResponse.error(webApplicationException.getMessage()))
                    .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse
                        .error(exception.getMessage() != null ? exception.getMessage() : "Internal server error"))
                .build();

    }

}
