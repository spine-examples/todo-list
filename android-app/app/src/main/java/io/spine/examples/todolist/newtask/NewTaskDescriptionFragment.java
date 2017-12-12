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

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.lifecycle.ViewModelFactory;

import java.util.List;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static io.spine.examples.todolist.TaskPriority.HIGH;
import static io.spine.examples.todolist.TaskPriority.LOW;
import static io.spine.examples.todolist.TaskPriority.NORMAL;

public final class NewTaskDescriptionFragment extends PagerFragment {

    static final int POSITION_IN_WIZARD = 0;

    private NewTaskViewModel model;

    private EditText taskDescription;
    private Spinner spinner;
    private Button setDueDate;

    private Timestamp dueDate = Timestamp.getDefaultInstance();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = ViewModelProviders.of(this, ViewModelFactory.CACHING).get(NewTaskViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_new_task_details, container, false);
        taskDescription = root.findViewById(R.id.new_task_description);
        spinner = root.findViewById(R.id.task_priority_spinner);
        initPrioritySpinner();
        setDueDate = root.findViewById(R.id.select_due_date_btn);
        final CalendarView calendar = root.findViewById(R.id.select_due_date_calendar);
        setDueDate.setOnClickListener((btn) -> {
            if (calendar.getVisibility() == View.GONE) {
                btn.getParent().requestLayout();
                calendar.setVisibility(View.VISIBLE);
                setDueDate.setText(R.string.cancel);
            } else {
                calendar.setVisibility(View.GONE);
                final long millis = calendar.getDate();
                dueDate = fromMillis(millis);
                setDueDate.setText(R.string.set_due_date);
            }
        });
        return root;
    }

    private void initPrioritySpinner() {
        final List<TaskPriority> priorities = ImmutableList.of(HIGH, NORMAL, LOW);
        final SpinnerAdapter adapter = new ArrayAdapter<>(getContext(),
                                                          R.layout.item_priopity,
                                                          R.id.priority,
                                                          priorities);
        spinner.setAdapter(adapter);
        spinner.setSelection(priorities.indexOf(NORMAL));
    }

    private void describeTask() {
        final String description = taskDescription.getText().toString();
        final TaskPriority priority = (TaskPriority) spinner.getSelectedItem();
        model.createTask(description, priority, dueDate);
    }

    @Override
    void complete() {
        describeTask();
    }
}
