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

package io.spine.examples.todolist.view.newtask;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.R;

import java.util.Collection;
import java.util.List;

import static android.support.v7.widget.RecyclerView.HORIZONTAL;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableCollection;

/**
 * An implementation of {@link RecyclerView.Adapter} for the list of new task labels.
 *
 * @author Dmytro Dashenkov
 */
final class NewLabelsAdapter extends RecyclerView.Adapter<NewLabelsAdapter.ViewBinder> {

    private final List<LabelDetails> data = newArrayList();

    @Override
    public ViewBinder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final View itemView = LayoutInflater.from(context)
                                            .inflate(R.layout.item_new_label, parent, false);
        return new ViewBinder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewBinder holder, int position) {
        final MutableLiveData<LabelDetails> details = new MutableLiveData<>();
        holder.bind(details);
        details.observeForever(newDetails -> data.set(position, newDetails));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    void appendItem() {
        data.add(0, LabelDetails.getDefaultInstance());
        notifyItemInserted(0);
    }

    Collection<LabelDetails> collectData() {
        final Collection<LabelDetails> result = unmodifiableCollection(data);
        return result;
    }

    /**
     * The implementation of the {@link RecyclerView.ViewHolder} holding a single
     * view prompting a new {@link LabelDetails}.
     */
    static final class ViewBinder extends RecyclerView.ViewHolder {

        private final EditText labelNameInput;
        private final RecyclerView colorPickerView;

        private LabelDetails currentDetails = LabelDetails.getDefaultInstance();

        private ViewBinder(View itemView) {
            super(itemView);
            this.labelNameInput = itemView.findViewById(R.id.new_label_name);
            this.colorPickerView = itemView.findViewById(R.id.color_picker);

            final LayoutManager layoutManager = new LinearLayoutManager(itemView.getContext(),
                                                                        HORIZONTAL,
                                                                        false);
            colorPickerView.setLayoutManager(layoutManager);
        }

        private void bind(MutableLiveData<LabelDetails> labelDetails) {
            labelNameInput.addTextChangedListener(new TextObserver(labelDetails));
            final ColorPickerAdapter adapter = new ColorPickerAdapter();
            colorPickerView.setAdapter(adapter);
            adapter.observeColor(color -> {
                currentDetails = currentDetails.toBuilder()
                                               .setColor(color)
                                               .build();
                labelDetails.setValue(currentDetails);
            });
        }

        /**
         * An implementation of {@link TextWatcher} updating the given {@link LabelDetails} with
         * the new title on the text change.
         */
        private class TextObserver implements TextWatcher {

            private final MutableLiveData<LabelDetails> labelDetails;

            private TextObserver(MutableLiveData<LabelDetails> labelDetails) {
                this.labelDetails = labelDetails;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                final String newName = s.toString();
                currentDetails = currentDetails.toBuilder()
                                               .setTitle(newName)
                                               .build();
                labelDetails.setValue(currentDetails);
            }
        }
    }
}
