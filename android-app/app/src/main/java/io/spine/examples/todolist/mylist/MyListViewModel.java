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

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.util.Log;
import io.grpc.stub.StreamObserver;
import io.spine.client.Subscription;
import io.spine.examples.todolist.lifecycle.AbstractViewModel;
import io.spine.examples.todolist.q.projection.MyListView;

/**
 * The {@link android.arch.lifecycle.ViewModel ViewModel} of the {@link MyListActivity}.
 */
final class MyListViewModel extends AbstractViewModel {

    private static final String TAG = MyListViewModel.class.getSimpleName();

    private final MutableLiveData<MyListView> myList = new MutableLiveData<>();

    private Subscription myTasksSubscription;

    // Required by the `ViewModelProviders` utility.
    public MyListViewModel() {}

    /**
     * Subscribes to the updates of the {@link MyListView} projection.
     *
     * <p>After calling this method, the {@linkplain #subscribe subscribed} observers will start to
     * receive the updates for the data.
     *
     * <p>The call initiates a data pull, so the observers will receive the first payload soon
     * after the {@code subscribeToMyList()} call.
     */
    void subscribeToMyList() {
        execute(() -> {
            myList.postValue(client().getMyListView());
            myTasksSubscription = client().subscribeToTasks(new MyListStreamObserver(myList));
        });
    }

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
        myList.observe(owner, observer);
    }

    /**
     * Cancels current subscription to the {@link MyListView}.
     *
     * <p>Performs no action if {@link #subscribeToMyList()} has never been called.
     */
    void unSubscribeFromMyList() {
        if (myTasksSubscription != null) {
            execute(() -> client().unSubscribe(myTasksSubscription));
        }
    }

    /**
     * The {@link StreamObserver} posting the received values to an instance of
     * {@link MutableLiveData}.
     *
     * <p>Both {@link StreamObserver#onError(Throwable)} and {@link StreamObserver#onCompleted()}
     * methods log a message and perform to other visible action.
     */
    private static final class MyListStreamObserver implements StreamObserver<MyListView> {

        private final MutableLiveData<MyListView> myList;

        private MyListStreamObserver(MutableLiveData<MyListView> myList) {
            this.myList = myList;
        }

        @Override
        public void onNext(MyListView value) {
            myList.postValue(value);
        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, "MyListStreamObserver received an error.", t);
        }

        @Override
        public void onCompleted() {
            Log.d(TAG, "MyListStreamObserver.onCompleted()");
        }
    }
}
