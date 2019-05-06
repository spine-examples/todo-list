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

package io.spine.examples.todolist.q.projection;

import io.spine.core.Subscribe;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.c.events.LabelCreated;
import io.spine.examples.todolist.c.events.LabelDetailsUpdated;
import io.spine.server.projection.Projection;

/**
 * A projection which mirrors the state of a single label.
 */
@SuppressWarnings("unused") // Methods used reflectively by Spine.
public class LabelViewProjection extends Projection<LabelId, LabelView, LabelViewVBuilder> {

    public LabelViewProjection(LabelId id) {
        super(id);
    }

    @Subscribe
    void labelCreated(LabelCreated event) {
        builder().setId(event.getId())
                 .setTitle(event.getDetails()
                                .getTitle())
                 .setColor(LabelColor.GRAY);
    }

    @Subscribe
    void labelDetailsUpdated(LabelDetailsUpdated event) {
        LabelDetails labelDetails = event.getLabelDetailsChange()
                                         .getNewDetails();
        builder().setId(event.getLabelId())
                 .setTitle(labelDetails.getTitle())
                 .setColor(labelDetails.getColor());
    }
}
