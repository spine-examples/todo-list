/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import io.spine.Identifier;
import io.spine.core.Ack;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.type.TypeUrl;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AFTER VIEW";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, TypeUrl.of(Ack.class).value());

        final TodoClient client = TodoClient.instance("10.0.2.2", DEFAULT_CLIENT_SERVICE_PORT);
        findViewById(R.id.btn).setOnClickListener(button -> {
            final CreateDraft command = CreateDraft.newBuilder()
                                                   .setId(TaskId.newBuilder().setValue(Identifier.newUuid()))
                                                   .build();
            client.create(command);
            final DraftTasksView view = client.getDraftTasksView();
            Log.e(TAG, view.getDraftTasks().toString());
        });
    }
}
