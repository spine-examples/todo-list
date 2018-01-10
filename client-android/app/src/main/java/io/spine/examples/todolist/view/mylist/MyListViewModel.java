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

package io.spine.examples.todolist.view.mylist;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import io.spine.examples.todolist.client.FirebaseSubscriber;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.view.AbstractViewModel;

/**
 * The {@link android.arch.lifecycle.ViewModel ViewModel} of the {@link MyListActivity}.
 */
final class MyListViewModel extends AbstractViewModel {

    // Required by the `ViewModelProviders` utility.
    public MyListViewModel() {}

    /**
     * Subscribes the given {@link Observer} to the updates of the {@link MyListView}.
     *
     * <p>The {@link LifecycleOwner} defines the time bounds of the observation, i.e.
     * the {@code observer} will receive the updates if the {@code owner} is active. See
     * {@link android.arch.lifecycle.LiveData LiveData} for more details.
     *
     * @param owner    the {@link LifecycleOwner} controlling the {@code observer}
     * @param observer the {@link Observer} to subscribe to the updates
     */
    void subscribe(LifecycleOwner owner, Observer<MyListView> observer) {
        execute(() -> {
            final MyListView initialState = client().getMyListView();
            inMainThread(() -> observer.onChanged(initialState));
        });
        final FirebaseSubscriber subscriber = FirebaseSubscriber.instance();
        final LiveData<MyListView> subscription = subscriber.subscribeToSingle(MyListView.class);
        subscription.observe(owner, observer);
    }
}
