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

package io.spine.examples.todolist.lifecycle;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Button;
import io.spine.examples.todolist.R;

import static android.support.v4.app.ActivityOptionsCompat.makeBasic;
import static android.support.v4.app.ActivityOptionsCompat.makeClipRevealAnimation;
import static android.support.v4.app.ActivityOptionsCompat.makeCustomAnimation;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Navigator {

    private final Activity activity;

    private Navigator(Activity activity) {
        this.activity = activity;
    }

    public static Navigator from(Activity activity) {
        checkNotNull(activity);
        return new Navigator(activity);
    }

    public NavigationBuilder start() {
        return new NavigationBuilder();
    }

    public void navigateBack() {
        activity.finish();
    }

    public class NavigationBuilder {

        private ActivityOptionsCompat inAnimation = makeBasic();

        private NavigationBuilder() {
            // Prevent direct instantiation.
        }

        public NavigationBuilder revealing(View view) {
            inAnimation = makeClipRevealAnimation(view, 0, 0, 0, 0);
            return this;
        }

        public void into(Class<? extends Activity> activityClass) {
            final Intent intent = new Intent(activity.getApplicationContext(), activityClass);
            final Bundle inAnimationBundle = inAnimation.toBundle();
            activity.startActivity(intent, inAnimationBundle);
        }
    }
}
