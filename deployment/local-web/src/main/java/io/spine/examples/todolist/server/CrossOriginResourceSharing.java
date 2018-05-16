/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Dmytro Dashenkov
 */
@WebFilter(
        filterName = CrossOriginResourceSharing.NAME,
        servletNames = {TodoCommandServlet.NAME, TodoQueryServlet.NAME}
)
public final class CrossOriginResourceSharing extends HttpFilter {

    private static final long serialVersionUID = 0L;

    static final String NAME = "CORS filter";

    public CrossOriginResourceSharing() {
        super();
    }

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

    private enum ResponseHeader {

        ACCESS_CONTROL_ALLOW_ORIGIN("access-control-allow-origin", "*"),
        ACCESS_CONTROL_ALLOW_CREDENTIALS("access-control-allow-credentials", "true"),
        ACCESS_CONTROL_ALLOW_HEADERS("access-control-allow-headers", "Content-Type");

        private final String name;
        private final String value;

        ResponseHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        void appendTo(HttpServletResponse response) {
            response.addHeader(name, value);
        }
    }
}
