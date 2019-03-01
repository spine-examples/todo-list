/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.cli.view;

import com.google.common.annotations.VisibleForTesting;
import io.spine.cli.Screen;
import io.spine.cli.action.Action;
import io.spine.cli.action.ActionProducer;
import io.spine.cli.action.Shortcut;
import io.spine.cli.action.TransitionAction;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static io.spine.cli.action.ActionFormatter.format;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.util.Collections.unmodifiableSet;

/**
 * Abstract base class for views.
 *
 * <p>Has the following render sequence:
 * <ol>
 *     <li>{@linkplain #renderTitle(Screen) title}</li>
 *     <li>{@linkplain #renderBody(Screen) body}</li>
 *     <li>{@linkplain #addBackAndRenderActions(Screen) actions}</li>
 * </ol>
 *
 * <p>Automatically adds {@code back} action to the {@link #actions} before each render of actions.
 *
 * <p>In the end of rendering prompts a user to select an action to be executed.
 */
public abstract class AbstractView implements View {

    private static final String BACK_NAME = "Back";
    private static final Shortcut BACK_SHORTCUT = new Shortcut("b");

    private static final String ACTION_SELECTION_HINT = format(new Shortcut("?"));
    private static final String SELECT_ACTION_MSG = "Select an action " + ACTION_SELECTION_HINT;
    private static final String INVALID_SELECTION_MSG =
            "There is no action with specified shortcut.";

    private final String title;
    private final Set<Action> actions;

    /**
     * Creates a new instance without some {@link #actions}.
     *
     * @param title
     *         the view title
     */
    AbstractView(String title) {
        checkArgument(!isNullOrEmpty(title));
        this.title = title;
        this.actions = newLinkedHashSet();
    }

    /**
     * Renders the view, prompts to select an action and then executes the action.
     */
    @Override
    public void render(Screen screen) {
        renderTitle(screen);
        renderBody(screen);
        addBackAndRenderActions(screen);
        Action selectedAction = promptSelectAction(screen);
        executeAction(selectedAction);
    }

    /**
     * Renders a body of the view.
     *
     * @param screen
     *         the screen to use
     */
    protected abstract void renderBody(Screen screen);

    /**
     * Executes the specified action.
     *
     * @param action
     *         the action to execute
     */
    protected void executeAction(Action action) {
        action.execute();
    }

    /**
     * Adds the {@link TransitionAction} created using the specified {@link ActionProducer}.
     *
     * @param producer
     *         the producer of the action
     * @param <S>
     *         the type of the source view
     * @param <D>
     *         the type of the destination view
     * @param <T>
     *         the type of the action
     */
    @SuppressWarnings("unchecked" /* Casts this to generic type to provide type covariance
                                     in the derived classes. */)
    public <S extends AbstractView,
            D extends View,
            T extends Action<S, D>>
    void addAction(ActionProducer<S, D, T> producer) {
        S source = (S) this;
        T action = producer.create(source);
        addAction(action);
    }

    /**
     * Removes all elements from {@link #actions}.
     */
    protected void clearActions() {
        actions.clear();
    }

    private void renderTitle(Screen screen) {
        String titleUnderline = repeat("-", title.length());
        screen.println(title);
        screen.println(titleUnderline);
    }

    private void addBackAndRenderActions(Screen screen) {
        Optional<TransitionAction<View, View>> back = screen.createBackAction(BACK_NAME,
                                                                              BACK_SHORTCUT);
        back.ifPresent(actions::add);
        actions.stream()
               .map(Action::toString)
               .forEach(screen::println);
    }

    private Action promptSelectAction(Screen screen) {
        do {
            String shortcutValue = screen.promptUser(SELECT_ACTION_MSG);
            Optional<Action> selectedAction =
                    actions.stream()
                           .filter(action -> action.getShortcut()
                                                   .getValue()
                                                   .equals(shortcutValue))
                           .findFirst();
            if (!selectedAction.isPresent()) {
                screen.println(INVALID_SELECTION_MSG);
            } else {
                return selectedAction.get();
            }
        } while (true);
    }

    private void addAction(Action action) {
        checkNotNull(action);
        checkHasNotReservedShortcut(action);
        checkArgument(!actions.contains(action));
        actions.add(action);
    }

    @VisibleForTesting
    public Set<Action> getActions() {
        return unmodifiableSet(actions);
    }

    @VisibleForTesting
    public static Shortcut getBackShortcut() {
        return BACK_SHORTCUT;
    }

    private static void checkHasNotReservedShortcut(Action action) {
        boolean hasReservedShortcut = action.getShortcut()
                                            .equals(BACK_SHORTCUT);
        if (hasReservedShortcut) {
            throw newIllegalArgumentException("Action with reserved shortcut `%s` was specified.",
                                              BACK_SHORTCUT);
        }
    }
}
