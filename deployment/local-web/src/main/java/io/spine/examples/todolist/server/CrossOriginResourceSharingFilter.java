/*
 * Copyright 2019, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.todolist.server;

import com.google.common.net.HttpHeaders;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * An {@link HttpFilter} which appends the CORS headers to the {@code /command} and {@code /query}
 * HTTP responses.
 *
 * <p>Requests from any origin are allowed. The requests are allowed to contain credentials and
 * the {@code Content-Type} header.
 */
@WebFilter(
        filterName = CrossOriginResourceSharingFilter.NAME,
        servletNames = {
                TodoCommandServlet.NAME,
                TodoSubscribeServlet.NAME,
                TodoSubscriptionCancelServlet.NAME,
                TodoSubscriptionKeepUpServlet.NAME
        }
)
public final class CrossOriginResourceSharingFilter extends HttpFilter {

    private static final long serialVersionUID = 0L;

    static final String NAME = "CORS filter";

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        appendHeaders(response);
        chain.doFilter(request, response);
    }

    private static void appendHeaders(HttpServletResponse response) {
        for (ResponseHeader header : ResponseHeader.values()) {
            header.appendTo(response);
        }
    }

    /**
     * The HTTP headers which configure the cross-origin request handling.
     */
    private enum ResponseHeader {

        ACCESS_CONTROL_ALLOW_ORIGIN(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"),
        ACCESS_CONTROL_ALLOW_CREDENTIALS(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"),
        ACCESS_CONTROL_ALLOW_HEADERS(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, CONTENT_TYPE);

        private final String name;
        private final String value;

        ResponseHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Appends this header to the ginen {@link HttpServletResponse}.
         */
        private void appendTo(HttpServletResponse response) {
            response.addHeader(name, value);
        }
    }
}
