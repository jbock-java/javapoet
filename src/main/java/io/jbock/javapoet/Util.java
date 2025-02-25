/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jbock.javapoet;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Character.isISOControl;

/**
 * Like Guava, but worse and standalone. This makes it easier to mix JavaPoet with libraries that
 * bring their own version of Guava.
 */
final class Util {
  private Util() {
  }

  static void checkArgument(boolean condition, String format, Object... args) {
    if (!condition) throw new IllegalArgumentException(String.format(format, args));
  }

  static <T> T checkNotNull(T reference, String format, Object... args) {
    if (reference == null) throw new NullPointerException(String.format(format, args));
    return reference;
  }

  static void checkState(boolean condition, String format, Object... args) {
    if (!condition) throw new IllegalStateException(String.format(format, args));
  }

  static <T> List<T> immutableList(Collection<T> collection) {
    if (collection instanceof List) {
      return (List<T>) collection;
    }
    return new ArrayList<>(collection);
  }

  static <T> Set<T> immutableSet(Collection<T> set) {
    if (set instanceof Set) {
      return (Set<T>) set;
    }
    return new LinkedHashSet<>(set);
  }

  static <T> Set<T> union(Set<T> a, Set<T> b) {
    if (b.isEmpty()) {
      return a;
    }
    Set<T> result = new LinkedHashSet<>((int) Math.max(8, 1.4 * (a.size() + b.size())));
    result.addAll(a);
    result.addAll(b);
    return result;
  }

  static void requireExactlyOneOf(Set<Modifier> modifiers, Modifier... mutuallyExclusive) {
    int count = 0;
    for (Modifier modifier : mutuallyExclusive) {
      if (modifiers.contains(modifier)) count++;
    }
    checkArgument(count == 1, "modifiers %s must contain one of %s",
        modifiers, Arrays.toString(mutuallyExclusive));
  }

  static String characterLiteralWithoutSingleQuotes(char c) {
    // see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
    switch (c) {
      case '\b':
        return "\\b"; /* \u0008: backspace (BS) */
      case '\t':
        return "\\t"; /* \u0009: horizontal tab (HT) */
      case '\n':
        return "\\n"; /* \u000a: linefeed (LF) */
      case '\f':
        return "\\f"; /* \u000c: form feed (FF) */
      case '\r':
        return "\\r"; /* \u000d: carriage return (CR) */
      case '\"':
        return "\"";  /* \u0022: double quote (") */
      case '\'':
        return "\\'"; /* \u0027: single quote (') */
      case '\\':
        return "\\\\";  /* \u005c: backslash (\) */
      default:
        return isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
    }
  }

  /** Returns the string literal representing {@code value}, including wrapping double quotes. */
  static String stringLiteralWithDoubleQuotes(String value, String indent) {
    StringBuilder result = new StringBuilder(value.length() + 2);
    result.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      // trivial case: single quote must not be escaped
      if (c == '\'') {
        result.append("'");
        continue;
      }
      // trivial case: double quotes must be escaped
      if (c == '\"') {
        result.append("\\\"");
        continue;
      }
      // default case: just let character literal do its work
      result.append(characterLiteralWithoutSingleQuotes(c));
      // need to append indent after linefeed?
      if (c == '\n' && i + 1 < value.length()) {
        result.append("\"\n").append(indent).append(indent).append("+ \"");
      }
    }
    result.append('"');
    return result.toString();
  }
}
