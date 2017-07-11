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
import io.spine.examples.todolist.action.AbstractAction;
import io.spine.examples.todolist.action.AbstractActionProducer;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.Shortcut;
import io.spine.examples.todolist.action.TransitionAction;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.action.ActionFormatter.format;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;

/**
 * An {@code ActionListView} renders the {@link #actions} and provides
 * {@linkplain #selectAction() selection} mechanism for them.
 *
 * @author Dmytro Grankin
 */
public class ActionListView extends AbstractView {

    private static final String BACK_NAME = "Back";
    private static final Shortcut BACK_SHORTCUT = new Shortcut("b");

    private static final String ACTION_SELECTION_HINT = format(new Shortcut("?"));
    private static final String SELECT_ACTION_MSG = "Select an action " + ACTION_SELECTION_HINT;
    private static final String INVALID_SELECTION_MSG = "There is no action with specified shortcut.";

    private final Set<Action> actions;

    /**
     * Creates a new instance without some {@link #actions}.
     *
     * @param title the view title
     */
    public ActionListView(String title) {
        super(title);
        this.actions = new LinkedHashSet<>();
    }

    /**
     * Renders the string representation of the view and ask to select an action to execute.
     *
     * <p>Override this method only to change the behavior.
     * To change the visual representation, override {@code toString()}.
     */
    @Override
    protected void render() {
        addBackAction();
        renderStringRepresentation();
        final Action selectedAction = selectAction();
        executeAction(selectedAction);
    }

    @VisibleForTesting
    void addBackAction() {
        final Action action = createBackAction(BACK_NAME, BACK_SHORTCUT);
        actions.add(action);
    }

    private void renderStringRepresentation() {
        final String stringRepresentation = toString();
        getScreen().println(stringRepresentation);
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

    /**
     * Executes the specified action.
     *
     * @param action the action to execute
     */
    protected void executeAction(Action action) {
        action.execute();
    }

    /**
     * Adds the {@link TransitionAction} created using
     * the specified {@link AbstractActionProducer}.
     *
     * @param producer the producer of the action
     * @param <S>      the type of the source view
     * @param <D>      the type of the destination view
     * @param <T>      the type of the action
     */
    @SuppressWarnings("unchecked" /* Casts this to generic type to provide type covariance
                                     in the derived classes. */)
    public <S extends ActionListView,
            D extends View,
            T extends AbstractAction<S, D>>
    void addAction(AbstractActionProducer<S, D, T> producer) {
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

    private void addAction(Action action) {
        checkNotNull(action);
        checkHasNotReservedShortcut(action);
        checkArgument(!actions.contains(action));
        actions.add(action);
    }

    @VisibleForTesting
    Set<Action> getActions() {
        return unmodifiableSet(actions);
    }

    @VisibleForTesting
    public static Shortcut getBackShortcut() {
        return BACK_SHORTCUT;
    }

    @VisibleForTesting
    static String getSelectActionMsg() {
        return SELECT_ACTION_MSG;
    }

    @Override
    public String toString() {
        return actions.stream()
                      .map(Action::toString)
                      .collect(joining(lineSeparator()));
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
