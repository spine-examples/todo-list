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

package io.spine.examples.todolist.mylist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.Toast;
import io.spine.Identifier;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.c.commands.CreateBasicTask;

import static android.arch.lifecycle.ViewModelProviders.of;

public class MyListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);
        initToolbar();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final MyListViewModel model = of(this).get(MyListViewModel.class);
        model.subscribe(this, data -> {
            Toast.makeText(getApplicationContext(), String.valueOf(data.getMyList().getItemsCount()), Toast.LENGTH_SHORT).show();
        });
        model.fetchMyTasks();
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initViews() {
        final RecyclerView myTaskListView = findViewById(R.id.my_task_list_view);
        myTaskListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        final MyListViewModel model = of(this).get(MyListViewModel.class);

        final ImageButton button = findViewById(R.id.fab);
        button.setOnClickListener(btn -> model.createTask(collectStubData()));
    }

    private static CreateBasicTask collectStubData() {
        final TaskId id = TaskId.newBuilder()
                                .setValue(Identifier.newUuid())
                                .build();
        final TaskDescription description = TaskDescription.newBuilder()
                                                           .setValue("Description-" + id.getValue())
                                                           .build();
        return CreateBasicTask.newBuilder()
                              .setId(id)
                              .setDescription(description)
                              .build();
    }
}
