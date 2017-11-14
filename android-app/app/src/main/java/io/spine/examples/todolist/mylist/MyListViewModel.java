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
import io.spine.examples.todolist.lifecycle.BaseViewModel;
import io.spine.examples.todolist.q.projection.MyListView;

final class MyListViewModel extends BaseViewModel {

    private static final String TAG = MyListViewModel.class.getSimpleName();

    private final MutableLiveData<MyListView> myList = new MutableLiveData<>();

    private Subscription myTasksSubscription;

    // Required by the `ViewModelProviders` utility.
    public MyListViewModel() {
    }

    void subscribeToMyList() {
        execute(() -> myTasksSubscription = client().subscribeToTasks(new MyListStreamObserver()));
    }

    void subscribe(LifecycleOwner owner, Observer<MyListView> observer) {
        myList.observe(owner, observer);
    }

    void unSubscribeFromMyList() {
        if (myTasksSubscription != null) {
            execute(() -> client().unSubscribe(myTasksSubscription));
        }
    }

    private final class MyListStreamObserver implements StreamObserver<MyListView> {

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
