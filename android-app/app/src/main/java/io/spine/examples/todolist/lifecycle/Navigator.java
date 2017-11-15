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

/**
 * Performs the in-app navigation between activities.
 */
public final class Navigator {

    /**
     * The current {@code Activity} instance.
     */
    private final Activity activity;

    private Navigator(Activity activity) {
        this.activity = activity;
    }

    /**
     * Creates an instance of {@code Navigator} starting in the given {@code activity}.
     *
     * @param activity the {@code activity} to start the navigation from
     * @return new instance of {@code Navigator}
     */
    public static Navigator from(Activity activity) {
        checkNotNull(activity);
        return new Navigator(activity);
    }

    /**
     * Starts the navigation from current {@code activity}.
     *
     * <p>This operation is non-terminal and has no visible side effect.
     *
     * @see NavigationBuilder for the terminal operations
     */
    public NavigationBuilder start() {
        return new NavigationBuilder();
    }

    /**
     * Performs the back navigation.
     *
     * Acts if {@code activity.finish();}.
     */
    public void navigateBack() {
        activity.finish();
    }

    /**
     * The navigation action builder.
     *
     * <p>Provides non-terminal operations for the navigation parameters set up and terminal
     * operations performing the navigation.
     */
    public class NavigationBuilder {

        /**
         * New {@code Activity} appearing animation.
         *
         * <p>By default, the system default animation.
         */
        private ActivityOptionsCompat inAnimation = makeBasic();

        private NavigationBuilder() {
            // Prevent direct instantiation.
        }

        /**
         * Overrides the appearing animation for the pending {@code Activity} with
         * a {@linkplain ActivityOptionsCompat#makeClipRevealAnimation reveal} from the given
         * {@code view} animation.
         *
         * <p>This operation is non-terminal and has no visible side effect.
         *
         * @param view the view for the {@code Activity} to reveal from
         * @return {@code this} reference for method chaining
         */
        public NavigationBuilder revealing(View view) {
            inAnimation = makeClipRevealAnimation(view, 0, 0, 0, 0);
            return this;
        }

        /**
         * Specifies the navigation target and starts the {@code Activity} opening.
         *
         * <p>This is a terminal operation which causes an {@code Activity} of the given class to
         * appear.
         *
         * @param activityClass the class of the {@code Activity} to start
         */
        public void into(Class<? extends Activity> activityClass) {
            final Intent intent = new Intent(activity.getApplicationContext(), activityClass);
            final Bundle inAnimationBundle = inAnimation.toBundle();
            activity.startActivity(intent, inAnimationBundle);
        }
    }
}
