// The MIT License (MIT)
//
// Copyright (c) 2019 Timothy D. Jones
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
package io.github.jonestimd.swing;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import io.github.jonestimd.util.Streams;

import static io.github.jonestimd.swing.SwingResource.*;

/**
 * This resource bundle provides default values for custom components in this library.
 */
@SuppressWarnings({"MagicCharacter", "MagicNumber"})
public class ComponentResources extends ListResourceBundle {
    public interface Provider extends Supplier<ResourceBundle> {}

    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle(ComponentResources.class.getName());
    private static List<ResourceBundle> PROVIDERS = getProviders();

    private static List<ResourceBundle> getProviders() {
        ServiceLoader<Provider> providers = ServiceLoader.load(Provider.class);
        return Collections.unmodifiableList(Streams.of(providers).map(Provider::get).collect(Collectors.toList()));
    }

    private static Optional<String> getProviderString(String key) {
        for (ResourceBundle override : PROVIDERS) {
            if (override.containsKey(key)) return Optional.of(override.getString(key));
        }
        return Optional.empty();
    }

    private static <T> Optional<T> getProviderValue(String key, BiFunction<Object, Class<?>, Optional<T>> converter) {
        for (ResourceBundle provider : PROVIDERS) {
            if (provider.containsKey(key)) {
                try {
                    return Optional.of(provider.getObject(key)).flatMap(value -> converter.apply(value, provider.getClass()));
                } catch (Exception ex) {
                    // try next provider
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Look up a string value from Provider bundles with fallback to {@link #BUNDLE}.
     * @param key the resource key
     * @return the value from a {@code provider} if it exists, otherwise the value from {@link #BUNDLE}
     * @throws MissingResourceException if the key is not defined in any of the bundles
     */
    public static String lookupString(String key) {
        return getProviderString(key).orElseGet(() -> BUNDLE.getString(key));
    }

    /**
     * Look up a color value from Provider bundles with fallback to {@link #BUNDLE}.
     * @param key the resource key
     * @return the value from {@code bundle} if it exists, otherwise the value from {@link #BUNDLE}
     * @throws MissingResourceException if the key is not defined in any of the bundles
     */
    public static Color lookupColor(String key) {
        return getProviderValue(key, ComponentResources::asColor).orElseGet(() -> (Color) BUNDLE.getObject(key));
    }

    private static Optional<Color> asColor(Object value, Class<?> providerClass) {
        if (value instanceof Color) return Optional.of((Color) value);
        if (value instanceof String) return Optional.of(ColorFactory.createColor((String) value));
        throw new IllegalArgumentException("Invalid Color: " + value);
    }

    /**
     * Look up an icon value from Provider bundles with fallback to {@link #BUNDLE}.
     * @param key the resource key
     * @return the value from {@code bundle} if it exists, otherwise the value from {@link #BUNDLE}
     * @throws MissingResourceException if the key is not defined in any of the bundles
     */
    public static Icon lookupIcon(String key) {
        return getProviderValue(key, ComponentResources::asIcon).orElseGet(() -> (Icon) BUNDLE.getObject(key));
    }

    private static Optional<Icon> asIcon(Object value, Class<?> providerClass) {
        if (value instanceof Icon) return Optional.of((Icon) value);
        if (value instanceof String) return Optional.of(new ImageIcon(providerClass.getResource((String) value)));
        throw new IllegalArgumentException("Invalid Icon: " + value);
    }

    /**
     * Look up an int value from Provider bundles with fallback to {@link #BUNDLE}.
     * @param key the resource key
     * @return the value from {@code bundle} if it exists, otherwise the value from {@link #BUNDLE}
     * @throws MissingResourceException if the key is not defined in any of the bundles
     */
    public static int lookupInt(String key) {
        return getProviderValue(key, ComponentResources::asNumber).orElseGet(() -> (int) BUNDLE.getObject(key)).intValue();
    }

    private static Optional<Number> asNumber(Object value, Class<?> providerClass) {
        if (value instanceof Number) return Optional.of(((Number) value));
        if (value instanceof String) return Optional.of(Integer.valueOf((String) value));
        throw new IllegalArgumentException("Invalid int: " + value);
    }

    /**
     * Look up a float value from Provider bundles with fallback to {@link #BUNDLE}.
     * @param key the resource key
     * @return the value from {@code bundle} if it exists, otherwise the value from {@link #BUNDLE}
     * @throws MissingResourceException if the key is not defined in any of the bundles
     */
    public static float lookupFloat(String key) {
        return getProviderValue(key, ComponentResources::asNumber).orElseGet(() -> (float) BUNDLE.getObject(key)).floatValue();
    }

    @Override
    protected Object[][] getContents() {
        return new Object[][] {
            { BUTTON_TOOLTIP_ACCELERATOR_FORMAT.key, " (%s)" },
            { "filter.iconImage", new ImageIcon(getClass().getResource("/io/github/jonestimd/swing/component/filter.png")) },
            { "filter.invalid.background", new Color(255, 210, 210) },
            { FILTER_OPERATOR_SYMBOL_AND.key, '&' },
            { FILTER_OPERATOR_SYMBOL_OR.key, '\u2502' },
            { FILTER_OPERATOR_SYMBOL_NOT.key, '!' },
            { FILTER_OPERATOR_SYMBOL_GROUP_START.key, '(' },
            { FILTER_OPERATOR_SYMBOL_GROUP_END.key, ')' },
            { "filter.operator.key.and", '&' },
            { "filter.operator.key.or", '|' },
            { "filter.operator.key.not", '!' },
            { "filter.operator.key.group.start", '(' },
            { "filter.operator.key.group.end", ')' },

            { "exceptionDialog.title", "Unexpected Exception" },
            { "exceptionDialog.noStackTrace", "Stack trace is unavailable" },
            { "exceptionDialog.exception.label", "Unexpected exception:" },
            { "exceptionDialog.exception.columns", 50 },
            { "exceptionDialog.exception.rows", 20 },

            { "action.save.mnemonicAndName", "SSave" },
            { "action.ok.name", "OK" },
            { "action.cancel.name", "Cancel" },

            { "confirm.unsavedChanges.title", "Confirm Discard Changes" },
            { "confirm.unsavedChanges.message", "Your changes have not been saved." },
            { "confirm.unsavedChanges.option.confirm", "Discard Changes" },
            { "confirm.unsavedChanges.option.cancel", "Cancel" },

            { VALIDATION_MESSAGE_BACKGROUND.key, new Color(255,255,200) },

            { "calendar.button.tooltip", "Select a date from the calendar" },
            { "calendar.tooltip", "Select a date" },
            { "calendar.tooltip.month.next", "Next month" },
            { "calendar.tooltip.month.previous", "Previous month" },
            { "calendar.tooltip.year.next", "Next year" },
            { "calendar.tooltip.year.previous", "Previous year" },
            { "calendar.currentDate.font.scale", 1.1f },
            { "calendarPanel.selected.border", Color.red },
            { "calendarPanel.month.background", new Color(255,255,224) },
            { "calendarPanel.month.foreground", Color.black },
            { "calendarPanel.month.adjacent.background", new Color(223,223,192) },
            { "calendarPanel.month.adjacent.foreground", Color.darkGray },

            { "multiSelectItem.background", new Color(215, 241, 255) },
            { "multiSelectItem.selectedBackground", new Color(184, 207, 229) },
            { "multiSelectItem.outline.color", new Color(0, 173, 255) },
            { "multiSelectItem.outline.strokeWidth", 1 },
            { "multiSelectItem.outline.roundness.noButton", 0.5f },
            { "multiSelectItem.outline.roundness.button", 1f },
            { "multiSelectItem.button.color", Color.GRAY },
            { "multiSelectItem.button.hoverColor", new Color(0, 173, 255) },
            { "multiSelectItem.button.size", 14 },
            { "multiSelectField.invalidItem.background", Color.PINK },

            { "popupListField.focusCursor", "\u23f5" },
            { "listField.commitKey", "ctrl pressed ENTER" },
            { "listField.cancelKey", "pressed ESCAPE" },
        };
    }
}
