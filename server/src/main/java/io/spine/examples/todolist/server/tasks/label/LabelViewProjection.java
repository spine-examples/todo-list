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

package io.spine.examples.todolist.server.tasks.label;

import io.spine.core.Subscribe;
import io.spine.examples.todolist.tasks.LabelDetails;
import io.spine.examples.todolist.tasks.LabelId;
import io.spine.examples.todolist.tasks.event.LabelCreated;
import io.spine.examples.todolist.tasks.event.LabelDetailsUpdated;
import io.spine.examples.todolist.tasks.view.LabelView;
import io.spine.server.projection.Projection;

import static io.spine.examples.todolist.tasks.LabelColor.DEFAULT;

/**
 * A projection which mirrors the state of a single label.
 */
public class LabelViewProjection extends Projection<LabelId, LabelView, LabelView.Builder> {

    public LabelViewProjection(LabelId id) {
        super(id);
    }

    @Subscribe
    void on(LabelCreated e) {
        builder().setTitle(e.getDetails().getTitle())
                 .setColor(DEFAULT);
    }

    @Subscribe
    void on(LabelDetailsUpdated e) {
        LabelDetails label = e.getLabelDetailsChange()
                              .getNewDetails();
        builder().setTitle(label.getTitle())
                 .setColor(label.getColor());
    }
}
