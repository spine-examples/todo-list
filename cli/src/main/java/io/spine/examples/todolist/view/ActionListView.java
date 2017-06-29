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
import io.spine.examples.todolist.action.Action;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.examples.todolist.action.Action.getShortcutFormat;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;

/**
 * An {@code ActionListView} displays the {@link #actions} and provides
 * {@linkplain #selectAction() selection} mechanism for them.
 *
 * @author Dmytro Grankin
 */
public class ActionListView extends View {

    private static final String BACK_NAME = "Back";
    private static final String BACK_SHORTCUT = "b";
    private static final String ACTION_SELECTION_TIP = format(getShortcutFormat(), "?");
    private static final String SELECT_ACTION_MSG = "Select an action " + ACTION_SELECTION_TIP;
    private static final String INVALID_SELECTION_MSG = "There is no action with specified shortcut.";

    private final Set<Action> actions;

    public ActionListView(boolean rootView, Collection<Action> actions) {
        super(rootView);
        checkActions(actions);
        this.actions = new LinkedHashSet<>(actions);
    }

    @Override
    protected void display() {
        addBackAction();
        displayActions();
        final Action selectedAction = selectAction();
        executeAction(selectedAction);
    }

    @VisibleForTesting
    void addBackAction() {
        final Action action = createBackAction(BACK_NAME, BACK_SHORTCUT);
        actions.add(action);
    }

    private void displayActions() {
        for (Action action : actions) {
            println(action.toString());
        }
    }

    private Action selectAction() {
        do {
            final Optional<Action> selectedAction = trySelectAction();
            if (!selectedAction.isPresent()) {
                println(INVALID_SELECTION_MSG);
            } else {
                return selectedAction.get();
            }
        } while (true);
    }

    private Optional<Action> trySelectAction() {
        final String answer = promptUser(SELECT_ACTION_MSG);
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
        action.execute(this);
    }

    protected void addAction(Action action) {
        checkNotNull(action);
        checkArgument(!actions.contains(action));
        actions.add(action);
    }

    @VisibleForTesting
    Set<Action> getActions() {
        return unmodifiableSet(actions);
    }

    @VisibleForTesting
    public static String getBackShortcut() {
        return BACK_SHORTCUT;
    }

    private static void checkActions(Collection<Action> actions) {
        final Predicate<Action> predicate = new ShortcutMatchPredicate(BACK_SHORTCUT);
        final boolean containsConflictAction = actions.stream()
                                                      .anyMatch(predicate);
        if (containsConflictAction) {
            throw newIllegalArgumentException("Action with reserved shortcut `%s` was specified.",
                                              BACK_SHORTCUT);
        }
    }

    private static class ShortcutMatchPredicate implements Predicate<Action> {

        private final String shortcut;

        private ShortcutMatchPredicate(String shortcut) {
            this.shortcut = shortcut;
        }

        @Override
        public boolean test(Action action) {
            return action.getShortcut()
                         .equals(shortcut);
        }
    }
}
