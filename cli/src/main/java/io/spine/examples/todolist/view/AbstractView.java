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

package io.spine.examples.todolist.view;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.ActionProducer;
import io.spine.examples.todolist.Screen;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.NoOpAction;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.repeat;
import static io.spine.examples.todolist.action.ActionFormatter.format;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.util.Collections.unmodifiableSet;

/**
 * Abstract base class for views.
 *
 * <p>Contains {@link #actions} and provides
 * {@linkplain #selectAction() selection} mechanism for them.
 *
 * @author Dmytro Grankin
 */
public abstract class AbstractView implements View {

    private static final String BACK_NAME = "Back";
    private static final Shortcut BACK_SHORTCUT = new Shortcut("b");

    private static final String ACTION_SELECTION_HINT = format(new Shortcut("?"));
    private static final String SELECT_ACTION_MSG = "Select an action " + ACTION_SELECTION_HINT;
    private static final String INVALID_SELECTION_MSG = "There is no action with specified shortcut.";

    private final String title;
    private final Set<Action> actions;

    private Screen screen;

    /**
     * Creates a new instance without some {@link #actions}.
     *
     * @param title the view title
     */
    protected AbstractView(String title) {
        checkArgument(!isNullOrEmpty(title));
        this.title = title;
        this.actions = new LinkedHashSet<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Has the following render sequence:
     * <ol>
     *     <li>title</li>
     *     <li>body</li>
     *     <li>actions</li>
     * </ol>
     *
     * @param screen {@inheritDoc}
     */
    @Override
    public void render(Screen screen) {
        setScreen(screen);
        renderTitle();
        renderBody();
        addBackAndRenderActions();
        final Action selectedAction = selectAction();
        executeAction(selectedAction);
    }

    /**
     * Renders a body of the view.
     */
    protected abstract void renderBody();

    /**
     * Executes the specified action.
     *
     * @param action the action to execute
     */
    protected void executeAction(Action action) {
        action.execute();
    }

    /**
     * Adds the {@link TransitionAction} created using the specified {@link ActionProducer}.
     *
     * @param producer the producer of the action
     * @param <S>      the type of the source view
     * @param <D>      the type of the destination view
     * @param <T>      the type of the action
     */
    @SuppressWarnings("unchecked" /* Casts this to generic type to provide type covariance
                                     in the derived classes. */)
    public <S extends AbstractView,
            D extends View,
            T extends Action<S, D>>
    void addAction(ActionProducer<S, D, T> producer) {
        final S source = (S) this;
        final T action = producer.create(source);
        addAction(action);
    }

    /**
     * Removes all elements from {@link #actions}.
     */
    protected void clearActions() {
        actions.clear();
    }

    private void renderTitle() {
        final String titleUnderline = repeat("-", title.length());
        getScreen().println(title);
        getScreen().println(titleUnderline);
    }

    private void addBackAndRenderActions() {
        final Action back = createBackAction(BACK_NAME, BACK_SHORTCUT);
        actions.add(back);
        actions.stream()
               .map(Action::toString)
               .forEach(actionView -> getScreen().println(actionView));
    }

    /**
     * Obtains the action leading to the {@linkplain Screen#getPreviousView(View) previous view}.
     *
     * @param name     the name for the action
     * @param shortcut the shortcut for the action
     * @return the back action
     */
    @VisibleForTesting
    Action createBackAction(String name, Shortcut shortcut) {
        final Optional<View> previousView = screen.getPreviousView(this);
        return previousView
                .<Action>map(view -> new TransitionAction<>(name, shortcut, this, view))
                .orElseGet(() -> new NoOpAction(name, shortcut));
    }

    private Action selectAction() {
        do {
            final Optional<Action> selectedAction = trySelectAction();
            if (!selectedAction.isPresent()) {
                getScreen().println(INVALID_SELECTION_MSG);
            } else {
                return selectedAction.get();
            }
        } while (true);
    }

    private Optional<Action> trySelectAction() {
        final String answer = getScreen().promptUser(SELECT_ACTION_MSG);
        final Predicate<Action> actionMatch = new ShortcutMatchPredicate(answer);
        return actions.stream()
                      .filter(actionMatch)
                      .findFirst();
    }

    private void addAction(Action action) {
        checkNotNull(action);
        checkHasNotReservedShortcut(action);
        checkArgument(!actions.contains(action));
        actions.add(action);
    }

    @Override
    public Screen getScreen() {
        return screen;
    }

    @VisibleForTesting
    public void setScreen(Screen screen) {
        checkNotNull(screen);
        this.screen = screen;
    }

    @VisibleForTesting
    Set<Action> getActions() {
        return unmodifiableSet(actions);
    }

    @VisibleForTesting
    public static Shortcut getBackShortcut() {
        return BACK_SHORTCUT;
    }

    private static void checkHasNotReservedShortcut(Action action) {
        final Predicate<Action> predicate = new ShortcutMatchPredicate(BACK_SHORTCUT.getValue());
        final boolean hasReservedShortcut = predicate.test(action);

        if (hasReservedShortcut) {
            throw newIllegalArgumentException("Action with reserved shortcut `%s` was specified.",
                                              BACK_SHORTCUT);
        }
    }

    private static class ShortcutMatchPredicate implements Predicate<Action> {

        private final String shortcutValue;

        private ShortcutMatchPredicate(String shortcutValue) {
            this.shortcutValue = shortcutValue;
        }

        @Override
        public boolean test(Action action) {
            return action.getShortcut()
                         .getValue()
                         .equals(shortcutValue);
        }
    }
}
