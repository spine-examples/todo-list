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

import io.spine.web.subscription.servlet.SubscriptionKeepUpServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.spine.examples.todolist.server.Application.application;

/**
 * The {@code /subscription/keep-up} endpoint of the TodoList system.
 *
 * <p>Handles {@code POST} requests to keep up a subscription. See
 * {@link SubscriptionKeepUpServlet} for more details.
 *
 * <p>Handles {@code OPTIONS} requests for the purposes of CORS.
 */
@WebServlet(name = TodoSubscriptionKeepUpServlet.NAME, value = "/subscription/keep-up")
@SuppressWarnings({"serial",
        "DuplicateStringLiteralInspection" /* Similar endpoint to `local-firebase`. */})
public class TodoSubscriptionKeepUpServlet extends SubscriptionKeepUpServlet {

    static final String NAME = "Subscription Keep Up Service";

    public TodoSubscriptionKeepUpServlet() {
        super(application().subscriptionBridge());
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        // NO-OP.
    }
}
