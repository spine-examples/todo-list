/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import io.spine.web.firebase.query.FirebaseQueryResponse;
import io.spine.web.query.QueryServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.spine.examples.todolist.server.Application.application;

/**
 * The {@code /query} endpoint of the TodoList system.
 *
 * <p>Handles query {@code POST} requests. See {@link QueryServlet} for more details.
 *
 * <p>Handles {@code OPTIONS} requests for the purposes of CORS.
 */
@WebServlet(name = TodoQueryServlet.NAME, value = "/query")
@SuppressWarnings({"serial",
        "DuplicateStringLiteralInspection" /* Standard Spine endpoint for queries. */})
public final class TodoQueryServlet extends QueryServlet<FirebaseQueryResponse> {

    static final String NAME = "Query Service";

    public TodoQueryServlet() {
        super(application().queryBridge());
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        // NO-OP.
    }
}
