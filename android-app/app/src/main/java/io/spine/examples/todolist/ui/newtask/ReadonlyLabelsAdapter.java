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

package io.spine.examples.todolist.ui.newtask;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.R;

import java.util.List;

import static io.spine.examples.todolist.model.Colors.toRgb;

/**
 * An implementation of {@link RecyclerView.Adapter} for the list of {@link LabelDetails}.
 *
 * @author Dmytro Dashenkov
 */
final class ReadonlyLabelsAdapter extends RecyclerView.Adapter<ReadonlyLabelsAdapter.ViewBinder> {

    private final List<LabelDetails> data;

    ReadonlyLabelsAdapter(List<LabelDetails> data) {
        super();
        this.data = data;
    }

    @Override
    public ViewBinder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View root = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.item_label, parent, false);
        return new ViewBinder(root);
    }

    @Override
    public void onBindViewHolder(ViewBinder holder, int position) {
        final LabelDetails element = data.get(position);
        holder.bind(element);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * An implementation of {@link RecyclerView.ViewHolder} holding a single {@link LabelDetails}
     * view.
     */
    static class ViewBinder extends RecyclerView.ViewHolder {

        private final TextView titleLabel;
        private final View colorView;

        private ViewBinder(View itemView) {
            super(itemView);
            this.titleLabel = itemView.findViewById(R.id.label_title);
            this.colorView = itemView.findViewById(R.id.label_color_stripe);
        }

        private void bind(LabelDetails label) {
            final String text = label.getTitle();
            final int colorRgb = toRgb(label.getColor());
            titleLabel.setText(text);
            colorView.setBackgroundColor(colorRgb);
        }
    }
}
