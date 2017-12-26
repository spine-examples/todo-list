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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import io.spine.examples.todolist.R;

public class DatePickerDialog extends DialogFragment {

    static final int DUE_DATE_REQUEST = 1;
    static final String DUE_DATE_MILLIS_KEY = "due_date";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.dialog_date_picker, container, false);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        final CalendarView calendarView = root.findViewById(R.id.calendar);
        final Button accept = root.findViewById(R.id.accept_btn);
        final Button decline = root.findViewById(R.id.decline_btn);

        accept.setOnClickListener(btn -> {
            final long dateMillis = calendarView.getDate();
            onResult(dateMillis);
            dismiss();
        });
        decline.setOnClickListener(btn -> dismiss());
    }

    private void onResult(long millis) {
        final Intent intent = new Intent();
        intent.putExtra(DUE_DATE_MILLIS_KEY, millis);
        getTargetFragment().onActivityResult(DUE_DATE_REQUEST, 0, intent);
    }
}
