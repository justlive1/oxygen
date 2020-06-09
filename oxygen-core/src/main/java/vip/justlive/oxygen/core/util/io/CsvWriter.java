/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package vip.justlive.oxygen.core.util.io;

import java.io.IOException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * csv writer
 *
 * @author wubo
 */
@Getter
@RequiredArgsConstructor
public class CsvWriter {

  public static final char NO_QUOTE_OR_ESCAPE = '\u0000';
  public static final char DEFAULT_SEPARATOR = ',';
  public static final char DEFAULT_ESCAPE = '"';
  public static final char DEFAULT_QUOTE = '"';
  public static final String DEFAULT_END = "\r\n";
  private final char separator;
  private final char quote;
  private final char escape;
  private final String end;

  public CsvWriter() {
    this(DEFAULT_SEPARATOR, DEFAULT_QUOTE, DEFAULT_ESCAPE);
  }

  public CsvWriter(char separator, char quote, char escape) {
    this(separator, quote, escape, DEFAULT_END);
  }

  public void writeAll(Appendable appendable, Iterable<String[]> lines) {
    writeAll(appendable, lines, true);
  }

  public void writeAll(Appendable appendable, Iterable<String[]> lines, boolean applyQuotes) {
    for (String[] line : lines) {
      write(appendable, line, applyQuotes);
    }
  }

  public void write(Appendable appendable, Iterable<String> line) {
    write(appendable, line, true);
  }

  public void write(Appendable appendable, Iterable<String> line, boolean applyQuotes) {
    if (line == null) {
      return;
    }
    int i = 0;
    for (String element : line) {
      writeElement(i++, element, appendable, applyQuotes);
    }
    append(appendable, end);
  }

  public void write(Appendable appendable, String[] line) {
    write(appendable, line, true);
  }

  public void write(Appendable appendable, String[] line, boolean applyQuotes) {
    if (line == null) {
      return;
    }
    for (int i = 0; i < line.length; i++) {
      writeElement(i, line[i], appendable, applyQuotes);
    }
    append(appendable, end);
  }

  private void writeElement(int index, String element, Appendable appendable, boolean applyQuotes) {
    if (index != 0) {
      append(appendable, separator);
    }
    if (element == null) {
      return;
    }

    boolean hasSpecialChar = hasSpecialChar(element);
    appendQuote(applyQuotes, hasSpecialChar, appendable);
    if (hasSpecialChar) {
      processLine(element, appendable);
    } else {
      append(appendable, element);
    }
    appendQuote(applyQuotes, hasSpecialChar, appendable);
  }

  private void processLine(String element, Appendable appendable) {
    for (int i = 0; i < element.length(); i++) {
      char next = element.charAt(i);
      if (escape != NO_QUOTE_OR_ESCAPE && checkCharactersToEscape(next)) {
        append(appendable, escape);
      }
      append(appendable, next);
    }
  }

  private boolean checkCharactersToEscape(char next) {
    if (quote == NO_QUOTE_OR_ESCAPE) {
      return next == quote || next == escape || next == separator || next == '\n';
    }
    return next == quote || next == escape;
  }

  private boolean hasSpecialChar(String line) {
    return line.indexOf(quote) != -1 || line.indexOf(escape) != -1 || line.indexOf(separator) != -1
        || line.contains("\r") || line.contains("\n");
  }

  private void appendQuote(boolean applyQuotes, boolean hasSpecialChar, Appendable appendable) {
    boolean needQuote = (applyQuotes || hasSpecialChar) && quote != NO_QUOTE_OR_ESCAPE;
    if (needQuote) {
      append(appendable, quote);
    }
  }

  private void append(Appendable appendable, CharSequence sequence) {
    try {
      appendable.append(sequence);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void append(Appendable appendable, char sequence) {
    try {
      appendable.append(sequence);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}

