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

import io.spine.examples.todolist.mode.Mode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;

import static io.spine.examples.todolist.mode.menu.Menu.DEFAULT_EXIT_MSG;
import static io.spine.examples.todolist.mode.menu.Menu.ID_NAME_SEPARATOR;
import static io.spine.examples.todolist.mode.menu.MenuTest.FewItemsMenu.EXIT_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Dmytro Grankin
 */
@DisplayName("Menu should")
class MenuTest {

    private final Menu.Builder builder = Menu.newBuilder();

    @Test
    @DisplayName("add menu exit to the end of a menu")
    void addMenuExit() {
        final Menu menu = new FewItemsMenu();
        final int lastItemIndex = menu.getItems()
                                      .size() - 1;
        final AbstractMenuItem lastItem = menu.getItems()
                                              .get(lastItemIndex);
        final MenuExit lastItemAsMenuExit = (MenuExit) lastItem;
        assertEquals(EXIT_MESSAGE, lastItemAsMenuExit.getName());
    }

    @Test
    @DisplayName("return identifier for a menu item")
    void returnItemID() {
        final Menu menu = new FewItemsMenu();
        for (AbstractMenuItem item : menu.getItems()) {
            final int itemIndex = menu.getItems().indexOf(item);
            final String expectedId = String.valueOf(itemIndex);
            assertEquals(expectedId, menu.identifierOf(item));
        }
    }

    @Test
    @DisplayName("return string representation of a menu item")
    void returnStringRepresentationOfItem() {
        final Menu menu = new FewItemsMenu();
        for (AbstractMenuItem item : menu.getItems()) {
            final String id = menu.identifierOf(item);
            final String expectedRepresentation = id + ID_NAME_SEPARATOR + item.getName();
            assertEquals(expectedRepresentation, menu.stringRepresentationOf(item));
        }
    }

    @Nested
    @DisplayName("Builder should")
    private class BuilderTest {

        @Test
        @DisplayName("have default exit message if it was not set")
        void haveDefaultExitMessage() {
            assertEquals(DEFAULT_EXIT_MSG, builder.getExitMessage());
        }

        @Test
        @DisplayName("return updated builder after exit message was set")
        void returnBuilderWithNewExitMessage() {
            final String exitMessage = "Exit";
            final Menu.Builder updatedBuilder = builder.setExitMessage(exitMessage);
            assertEquals(exitMessage, updatedBuilder.getExitMessage());
        }

        @Test
        @DisplayName("not accept empty or null exit message")
        void notAcceptInvalidExitMessage() {
            assertThrows(IllegalArgumentException.class, () -> builder.setExitMessage(""));
            assertThrows(IllegalArgumentException.class, () -> builder.setExitMessage(null));
        }

        @Test
        @DisplayName("save menu items insertion order")
        void saveMenuItemsOrder() {
            final Map<String, Mode> modeMap = builder.getModeMap();
            final String firstName = "first";
            final Mode firstMode = new EmptyMode();
            final String secondName = "longer than first";
            final Mode secondMode = new EmptyMode();

            builder.addMenuItem(firstName, firstMode);
            builder.addMenuItem(secondName, secondMode);

            final Iterator<String> keyIterator = modeMap.keySet()
                                                        .iterator();
            assertEquals(firstMode, modeMap.get(keyIterator.next()));
            assertEquals(secondMode, modeMap.get(keyIterator.next()));
        }

        @Test
        @DisplayName("return updated builder after addition of a menu item")
        void returnBuilderWithAddedMenuItem() {
            final String itemName = "Menu item";
            final Mode expectedMode = new EmptyMode();

            final Menu.Builder updatedBuilder = builder.addMenuItem(itemName, expectedMode);

            final Mode actualMode = updatedBuilder.getModeMap()
                                                  .get(itemName);
            assertThat(actualMode, sameInstance(expectedMode));
        }

        @Test
        @DisplayName("not accept empty or null menu item name")
        void notAcceptInvalidItemName() {
            assertThrows(IllegalArgumentException.class,
                         () -> builder.addMenuItem("", new EmptyMode()));
            assertThrows(IllegalArgumentException.class,
                         () -> builder.addMenuItem(null, new EmptyMode()));
        }
    }

    static class FewItemsMenu extends Menu {

        private static final int MENU_SIZE = 3;
        private static final String MENU_ITEM_PREFIX = "item";
        static final String EXIT_MESSAGE = "Close menu";

        private FewItemsMenu() {
            super(getBuilder());
        }

        private static Menu.Builder getBuilder() {
            final Menu.Builder builder = Menu.newBuilder();
            for (int i = 0; i < MENU_SIZE; i++) {
                final String itemName = MENU_ITEM_PREFIX + i;
                builder.addMenuItem(itemName, new EmptyMode());
            }
            builder.setExitMessage(EXIT_MESSAGE);
            return builder;
        }
    }

    private static class EmptyMode extends Mode {

        @Override
        public void start() {
        }
    }
}
