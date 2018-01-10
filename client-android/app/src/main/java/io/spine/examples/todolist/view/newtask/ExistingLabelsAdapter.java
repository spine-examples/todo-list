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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.TaskLabel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static io.spine.examples.todolist.model.Colors.toRgb;
import static java.util.Collections.unmodifiableCollection;

/**
 * An implementation of {@link RecyclerView.Adapter} for the selectable list of predefined task
 * labels.
 *
 * @author Dmytro Dashenkov
 */
final class ExistingLabelsAdapter extends RecyclerView.Adapter<ExistingLabelsAdapter.ViewBinder> {

    private final List<TaskLabel> data;
    private final Set<TaskLabel> selectedLabels = newHashSet();

    ExistingLabelsAdapter() {
        super();
        this.data = newArrayList();
    }

    @Override
    public ViewBinder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View root = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.item_label, parent, false);
        return new ViewBinder(root, selectedLabels);
    }

    @Override
    public void onBindViewHolder(ViewBinder holder, int position) {
        final TaskLabel element = data.get(position);
        holder.bind(element);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Binds the given data to the list.
     *
     * @param data new data to bind
     */
    void update(List<TaskLabel> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * Retrieves the selected task labels.
     */
    Collection<TaskLabel> getSelected() {
        return unmodifiableCollection(selectedLabels);
    }

    /**
     * The implementation of the {@link RecyclerView.ViewHolder} holding a single
     * {@link TaskLabel} view.
     */
    static final class ViewBinder extends RecyclerView.ViewHolder {

        private final Set<TaskLabel> selectedLabels;

        private final TextView titleLabel;
        private final View colorView;

        private final View root;

        private TaskLabel label;

        private ViewBinder(View itemView, Set<TaskLabel> selectedLabels) {
            super(itemView);
            this.selectedLabels = selectedLabels;

            this.titleLabel = itemView.findViewById(R.id.label_title);
            this.colorView = itemView.findViewById(R.id.label_color_stripe);
            this.root = itemView;
        }

        private void bind(TaskLabel label) {
            this.label = label;

            final String text = label.getTitle();
            final int colorRgb = toRgb(label.getColor());
            titleLabel.setText(text);
            colorView.setBackgroundColor(colorRgb);

            root.setOnClickListener(unselectedListener());
        }

        private OnClickListener unselectedListener() {
            return view -> {
                view.setOnClickListener(selectedListener());
                view.setSelected(true);
                selectedLabels.add(label);
            };
        }

        private OnClickListener selectedListener() {
            return view -> {
                view.setOnClickListener(unselectedListener());
                view.setSelected(false);
                selectedLabels.remove(label);
            };
        }
    }
}
