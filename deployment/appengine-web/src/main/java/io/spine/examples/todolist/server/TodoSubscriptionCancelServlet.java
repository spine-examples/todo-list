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

import io.spine.core.Response;
import io.spine.web.subscription.servlet.SubscriptionCancelServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.spine.examples.todolist.server.Application.application;

/**
 * The {@code /subscription/cancel} endpoint of the TodoList system.
 *
 * <p>Handles {@code POST} requests to cancel the subscription to a topic. See
 * {@link SubscriptionCancelServlet} for more details.
 *
 * <p>Handles {@code OPTIONS} requests for the purposes of CORS.
 */
@WebServlet(name = TodoSubscriptionCancelServlet.NAME, value = "/subscription/cancel")
@SuppressWarnings({"serial",
        "DuplicateStringLiteralInspection" /* Standard Spine endpoint for subscriptions. */})
public final class TodoSubscriptionCancelServlet extends SubscriptionCancelServlet<Response> {

    static final String NAME = "Subscription Cancel Service";

    public TodoSubscriptionCancelServlet() {
        super(application().subscriptionBridge());
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        // NO-OP.
    }
}
