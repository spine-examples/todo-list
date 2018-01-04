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

package io.spine.examples.todolist.view.newtask;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.view.ViewModelFactory;

import java.util.Collection;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;

/**
 * The the fragment with allows the user to specify the labels to assign to the task.
 *
 * @author Dmytro Dashenkov
 */
public final class NewTaskLabelsFragment extends PagerFragment {

    static final int POSITION_IN_WIZARD = 1;

    private NewTaskViewModel model;
    private NewLabelsAdapter newLabelsAdapter;
    private ExistingLabelsAdapter existingLabelsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = ViewModelProviders.of(this, ViewModelFactory.CACHING)
                                  .get(NewTaskViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_new_task_labels, container, false);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        final Context context = getContext();

        final RecyclerView existingLabels = root.findViewById(R.id.task_labels);
        existingLabels.setLayoutManager(new LinearLayoutManager(context, HORIZONTAL, false));
        this.existingLabelsAdapter = new ExistingLabelsAdapter();
        existingLabels.setAdapter(existingLabelsAdapter);
        model.fetchLabels(existingLabelsAdapter::update);
        existingLabels.setAdapter(existingLabelsAdapter);

        final RecyclerView newLabels = root.findViewById(R.id.new_labels_list);
        newLabels.setLayoutManager(new LinearLayoutManager(context));
        newLabelsAdapter = new NewLabelsAdapter();
        newLabels.setAdapter(newLabelsAdapter);

        final Button addNewLabelButton = root.findViewById(R.id.add_new_label);
        addNewLabelButton.setOnClickListener(btn -> newLabelsAdapter.appendItem());
    }

    @Override
    void complete() {
        final Collection<TaskLabel> existingLabels = existingLabelsAdapter.getSelected();
        final Collection<LabelDetails> newLabels = newLabelsAdapter.collectData();
        model.assignLabels(existingLabels, newLabels);
    }
}
