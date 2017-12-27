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
import android.arch.lifecycle.Observer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.common.collect.ImmutableList;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.R;

import java.util.List;

import static io.spine.examples.todolist.model.Colors.toRgb;

/**
 * An implementation of the {@link RecyclerView.Adapter} for the color picker view.
 *
 * The data for this adapter is the list of all values of {@link LabelColor} (except for the default
 * values).
 *
 * @author Dmytro Dashenkov
 */
final class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewBinder> {

    private static final List<LabelColor> COLORS;

    static {
        final ImmutableList.Builder<LabelColor> colorsBuilder = ImmutableList.builder();
        final LabelColor[] declaredColors = LabelColor.values();
        for (LabelColor color : declaredColors) {
            if (color != LabelColor.UNRECOGNIZED && color.getNumber() > 0) {
                colorsBuilder.add(color);
            }
        }
        COLORS = colorsBuilder.build();
    }

    private final MutableLiveData<LabelColor> selected = new MutableLiveData<>();

    ColorPickerAdapter() {
        super();
        selected.observeForever(color -> notifyDataSetChanged());
    }

    @Override
    public ViewBinder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View element = LayoutInflater.from(parent.getContext())
                                           .inflate(R.layout.item_color_option, parent, false);
        return new ViewBinder(element, selected);
    }

    @Override
    public void onBindViewHolder(ViewBinder holder, int position) {
        final LabelColor color = COLORS.get(position);
        holder.bind(color);
    }

    @Override
    public int getItemCount() {
        return COLORS.size();
    }

    /**
     * Subscribes the given observer to the updates of the selected color.
     *
     * @param observer the observer of the color changes
     */
    void observeColor(Observer<LabelColor> observer) {
        selected.observeForever(observer);
    }

    /**
     * The implementation of the {@link RecyclerView.ViewHolder} holding a single
     * {@link LabelColor} view.
     */
    static final class ViewBinder extends RecyclerView.ViewHolder {

        private final MutableLiveData<LabelColor> selector;
        private final ImageView colorView;
        private final ImageView selectorView;

        private ViewBinder(View itemView, MutableLiveData<LabelColor> selector) {
            super(itemView);
            this.selector = selector;
            this.colorView = itemView.findViewById(R.id.color);
            this.selectorView = itemView.findViewById(R.id.border);
        }

        private void bind(LabelColor color) {
            final int rgb = toRgb(color);
            colorView.setColorFilter(rgb);
            if (selector.getValue() == color) {
                final int colorFilter = selectorView.getContext()
                                                    .getResources()
                                                    .getColor(R.color.colorAccentLight);
                selectorView.setColorFilter(colorFilter);
            } else {
                selectorView.clearColorFilter();
            }
            colorView.setOnClickListener(view -> selector.setValue(color));
        }
    }
}
