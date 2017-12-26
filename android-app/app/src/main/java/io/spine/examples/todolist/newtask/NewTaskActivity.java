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
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.lifecycle.AbstractActivity;

/**
 * The {@code Activity} allowing the user to create a new task.
 *
 * @see NewTaskViewModel
 */
public final class NewTaskActivity extends AbstractActivity<NewTaskViewModel> {

    private ViewPager wizardView;
    private WizardAdapter wizard;
    private Button nextButton;

    private boolean lastPage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model().startCreatingTask();
    }

    @Override
    protected Class<NewTaskViewModel> getViewModelClass() {
        return NewTaskViewModel.class;
    }

    @Override
    protected int getTitleResource() {
        return R.string.new_task;
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_new_task;
    }

    @Override
    protected void initializeView() {
        wizardView = findViewById(R.id.new_task_pager);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        wizard = new WizardAdapter(fragmentManager);
        wizardView.setAdapter(wizard);
        // Disable scrolling by swipe
        wizardView.beginFakeDrag();

        nextButton = findViewById(R.id.next_btn);
        nextButton.setOnClickListener(button -> nextPage());
    }

    private void nextPage() {
        final int currentPageIndex = wizardView.getCurrentItem();
        final PagerFragment fragment = wizard.getItem(currentPageIndex);
        fragment.complete();
        final int newIndex = currentPageIndex + 1;
        if (lastPage) {
            finish();
            return;
        }
        if (newIndex + 1 == wizard.getCount()) {
            lastPage = true;
            nextButton.setText(R.string.complete);
        }
        wizardView.setCurrentItem(newIndex);
        final PagerFragment newPage = wizard.getItem(newIndex);
        newPage.prepare();
    }
}
