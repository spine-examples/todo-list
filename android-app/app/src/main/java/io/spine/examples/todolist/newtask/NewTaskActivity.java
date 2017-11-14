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

package io.spine.examples.todolist.newtask;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import io.spine.examples.todolist.lifecycle.BaseActivity;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.TaskDescription;

public class NewTaskActivity extends BaseActivity<NewTaskViewModel> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        initViews();
    }

    @Override
    protected Class<NewTaskViewModel> getViewModelClass() {
        return NewTaskViewModel.class;
    }

    private void initViews() {
        final EditText taskDescription = findViewById(R.id.new_task_description);
        final Button createTask = findViewById(R.id.create_task_btn);
        final Button back = findViewById(R.id.back_btn);

        createTask.setOnClickListener(button -> {
            final String descriptionValue = taskDescription.getText().toString();
            final TaskDescription description = createDescription(descriptionValue);
            model().createTask(description);
            navigator().navigateBack();
        });
        back.setOnClickListener(button -> navigator().navigateBack());
    }

    private static TaskDescription createDescription(String value) {
        final TaskDescription description = TaskDescription.newBuilder()
                                                           .setValue(value)
                                                           .build();
        return description;
    }
}
