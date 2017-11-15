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

package io.spine.examples.todolist.mylist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.lifecycle.BaseActivity;
import io.spine.examples.todolist.newtask.NewTaskActivity;

public class MyListActivity extends BaseActivity<MyListViewModel> {

    @Override
    protected Class<MyListViewModel> getViewModelClass() {
        return MyListViewModel.class;
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_my_list;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model().subscribeToMyList();
    }

    @Override
    public void initializeView() {
        final RecyclerView myTaskListView = findViewById(R.id.my_task_list_view);
        myTaskListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        final MyTaskListViewAdapter adapter = new MyTaskListViewAdapter();
        myTaskListView.setAdapter(adapter);
        model().subscribe(this, adapter);

        final ImageButton button = findViewById(R.id.fab);
        button.setOnClickListener(view -> navigator().start()
                                                     .revealing(view)
                                                     .into(NewTaskActivity.class));
    }

    @Override
    protected void onDestroy() {
        model().unSubscribeFromMyList();
        super.onDestroy();
    }
}
