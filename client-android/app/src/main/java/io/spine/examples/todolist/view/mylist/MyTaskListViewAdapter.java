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

package io.spine.examples.todolist.view.mylist;

import android.arch.lifecycle.Observer;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static com.google.protobuf.util.Timestamps.toMillis;
import static io.spine.examples.todolist.model.Colors.toRgb;
import static io.spine.validate.Validate.isDefault;
import static java.lang.String.valueOf;

/**
 * The {@link RecyclerView.Adapter} implementation listening to the {@link MyListView} data updates
 * and initiating the {@link RecyclerView} updates upon the data updates.
 */
final class MyTaskListViewAdapter
        extends Adapter<MyTaskListViewAdapter.TaskViewHolder> implements Observer<MyListView> {

    /**
     * The cached data currently displayed on the associated view.
     */
    private final List<TaskItem> data = new ArrayList<>();

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View taskView = LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        final TaskItem task = data.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Updates the {@linkplain #data cached data} and initiates the view redraw.
     */
    @Override
    public void onChanged(@Nullable MyListView myListView) {
        data.clear();
        if (myListView != null && !isDefault(myListView)) {
            data.addAll(myListView.getMyList().getItemsList());
        }
        notifyDataSetChanged();
        Log.d("MyTaskListViewAdapter", "Data arrived: " + valueOf(myListView));
    }

    /**
     * Maps the given {@link TaskItem} enum value to the RGB color code.
     *
     * @param item the color as an enum value
     * @return the RGB code of the color
     */
    private static int colorOf(TaskItem item) {
        final LabelColor color = item.getLabelColor();
        return toRgb(color);
    }

    /**
     * The implementation of the {@link RecyclerView.ViewHolder} holding a single
     * {@link TaskItem} view.
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private static final SimpleDateFormat DATE_FORMAT =
                new SimpleDateFormat("dd MMM YYYY", Locale.getDefault());

        private final TextView description;
        private final TextView dueDate;
        private final View dueDateLabel;
        private final View colorView;
        private final View root;

        private final Resources resources;

        private TaskViewHolder(View itemView) {
            super(itemView);
            this.root = itemView;
            this.description = itemView.findViewById(R.id.task_content);
            this.dueDate = itemView.findViewById(R.id.task_due_date);
            this.dueDateLabel = itemView.findViewById(R.id.task_due_date_label);
            this.colorView = itemView.findViewById(R.id.task_label_color_stripe);
            this.resources = itemView.getResources();
        }

        /**
         * Binds the given {@link TaskItem} to the held view.
         *
         * @param data the data to display on the view
         */
        private void bind(TaskItem data) {
            description.setText(data.getDescription().getValue());
            bindDueDate(data.getDueDate());
            colorView.setBackgroundColor(colorOf(data));
            if (data.getCompleted()) {
                root.setBackgroundColor(resources.getColor(R.color.completedTaskColor));
            }
        }

        private void bindDueDate(Timestamp taskDueDate) {
            final boolean isPresent = taskDueDate.getSeconds() > 0;
            if (isPresent) {
                setTime(dueDate, taskDueDate);
            } else {
                dueDate.setVisibility(GONE);
                dueDateLabel.setVisibility(GONE);
            }
        }

        @SuppressWarnings("AccessToNonThreadSafeStaticField")
            // DATE_FORMAT - OK since setTime() is always called from the main thread.
        private static void setTime(TextView view, Timestamp time) {
            final long millis = toMillis(time);
            final Date date = new Date(millis);
            view.setText(DATE_FORMAT.format(date));
        }
    }
}
