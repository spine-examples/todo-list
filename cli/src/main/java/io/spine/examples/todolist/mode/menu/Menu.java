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

package io.spine.examples.todolist.mode.menu;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.todolist.mode.Mode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * A {@code Menu}, that consists of the {@linkplain AbstractMenuItem menu items}.
 *
 * <p>Has the following structure:
 * <ol>
 *     <li>common menu items</li>
 *     <li>menu exit</li>
 * </ol>
 *
 * @author Dmytro Grankin
 */
public class Menu extends Mode {

    static final String BACK_TO_THE_MENU_MSG = "Back to the previous menu.";

    @VisibleForTesting
    static final String DEFAULT_EXIT_MSG = "Exit from the menu.";

    @VisibleForTesting
    static final String ID_NAME_SEPARATOR = ". ";
    private static final String SELECT_MENU_ITEM_MSG = "Select a menu item:";
    private static final String INVALID_SELECTION_MSG = "The selected item is invalid.";

    private final List<AbstractMenuItem> items = newLinkedList();

    protected Menu(Builder builder) {
        for (Map.Entry<String, Mode> mode : builder.modeMap.entrySet()) {
            addMenuItem(mode.getKey(), mode.getValue());
        }

        final MenuExit menuExit = new MenuExit(builder.exitMessage);
        items.add(menuExit);
    }

    @Override
    public void start() {
        display();
        final Optional<AbstractMenuItem> selectedItem = selectItem();

        if (selectedItem.isPresent()) {
            selectedItem.get()
                        .start();
        } else {
            println(INVALID_SELECTION_MSG);
        }
    }

    protected void display() {
        for (AbstractMenuItem item : items) {
            final String itemAsString = stringRepresentationOf(item);
            println(itemAsString);
        }
    }

    private Optional<AbstractMenuItem> selectItem() {
        final String answer = askUser(SELECT_MENU_ITEM_MSG);
        for (AbstractMenuItem item : items) {
            final String itemIdentifier = identifierOf(item);
            if (itemIdentifier.equals(answer)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @VisibleForTesting
    String stringRepresentationOf(AbstractMenuItem item) {
        final String identifier = identifierOf(item);
        final String itemName = item.getName();
        return identifier + ID_NAME_SEPARATOR + itemName;
    }

    /**
     * Obtains an identifier for the specified menu item.
     *
     * <p>The identifier is purposed for an user of application and
     * has no relation to internal structure of a menu.
     *
     * @param item the menu item
     * @return a menu item identifier
     * @see #selectItem()
     */
    @VisibleForTesting
    String identifierOf(AbstractMenuItem item) {
        final int index = items.indexOf(item);

        if (index == -1) {
            throw newIllegalStateException("Specified menu item does not belong to the menu.");
        }

        return String.valueOf(index);
    }

    private void addMenuItem(String name, Mode item) {
        final MenuItem menuItem = new MenuItem(name, item, this);
        items.add(menuItem);
    }

    @VisibleForTesting
    List<AbstractMenuItem> getItems() {
        return unmodifiableList(items);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String exitMessage = DEFAULT_EXIT_MSG;
        private final Map<String, Mode> modeMap = newLinkedHashMap();

        public Builder setExitMessage(String message) {
            checkArgument(!isNullOrEmpty(message));
            this.exitMessage = message;
            return this;
        }

        public Builder addMenuItem(String name, Mode mode) {
            checkArgument(!isNullOrEmpty(name));
            checkNotNull(mode);
            modeMap.put(name, mode);
            return this;
        }

        public String getExitMessage() {
            return exitMessage;
        }

        public Map<String, Mode> getModeMap() {
            return unmodifiableMap(modeMap);
        }
    }
}
