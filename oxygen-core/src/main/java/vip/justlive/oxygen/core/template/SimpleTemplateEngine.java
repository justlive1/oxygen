/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.core.template;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.oxygen.core.util.ExpiringMap.ExpiringPolicy;
import vip.justlive.oxygen.core.util.Strings;

/**
 * 简单模板引擎实现
 *
 * @author wubo
 */
public class SimpleTemplateEngine implements TemplateEngine {

  private static final String VALUE_PREFIX = "${";
  private static final String VALUE_SUFFIX = Strings.CLOSE_BRACE;
  private static final String SCRIPT_PREFIX = "#:";

  private static final ScriptEngine ENGINE;
  private static final Compilable COMPILABLE;
  private static final ExpiringMap<String, TemplateSpec> CACHE;

  static {
    ENGINE = new ScriptEngineManager().getEngineByName("js");
    if (ENGINE instanceof Compilable) {
      COMPILABLE = (Compilable) ENGINE;
    } else {
      COMPILABLE = null;
    }
    CACHE = ExpiringMap.<String, TemplateSpec>builder().expiration(10, TimeUnit.MINUTES)
        .expiringPolicy(ExpiringPolicy.ACCESSED).build();
  }

  @Override
  public String render(String template, Map<String, Object> attrs) {
    StringWriter writer = new StringWriter();
    render(template, attrs, writer);
    return writer.toString();
  }

  @Override
  public void render(String template, Map<String, Object> attrs, Writer writer) {
    TemplateSpec spec = CACHE.get(template);
    try {
      if (spec != null) {
        spec.apply(attrs, writer);
      }
      spec = new TemplateSpec(template);
      CACHE.put(template, spec);
      spec.apply(attrs, writer);
    } catch (ScriptException e) {
      throw Exceptions.wrap(e);
    }
  }

  @RequiredArgsConstructor
  private class Text implements Consumer<StringBuilder> {

    final int index;

    @Override
    public void accept(StringBuilder sb) {
      sb.append("$out.write($data[").append(index).append("]);");
    }
  }

  @RequiredArgsConstructor
  private class Script implements Consumer<StringBuilder> {

    final String value;

    @Override
    public void accept(StringBuilder sb) {
      sb.append(value);
    }
  }

  @RequiredArgsConstructor
  private class Statement implements Consumer<StringBuilder> {

    final String value;

    @Override
    public void accept(StringBuilder sb) {
      sb.append("$out.write(''+(").append(value).append("));");
    }
  }

  private class TemplateSpec {

    String parsed;
    CompiledScript compiledScript;
    List<String> data = new ArrayList<>();
    List<Consumer<StringBuilder>> consumers = new ArrayList<>();

    TemplateSpec(String template) throws ScriptException {
      StringBuilder sb = new StringBuilder("var func = function(){");
      BufferedReader reader = new BufferedReader(new StringReader(template));
      reader.lines().forEach(this::parse);
      for (Consumer<StringBuilder> consumer : consumers) {
        consumer.accept(sb);
      }
      sb.append("};func();");
      if (COMPILABLE != null) {
        compiledScript = COMPILABLE.compile(sb.toString());
      } else {
        parsed = sb.toString();
      }
    }

    private void parse(String line) {
      line = line.trim();
      if (line.startsWith(SCRIPT_PREFIX)) {
        consumers.add(new Script(line.substring(SCRIPT_PREFIX.length())));
        return;
      }

      while (true) {
        int start = line.indexOf(VALUE_PREFIX);
        int end = line.indexOf(VALUE_SUFFIX);
        if (start == -1 || end <= start) {
          consumers.add(new Text(data.size()));
          data.add(line);
          break;
        }
        String statement = line.substring(start + 2, end);
        if (statement.contains(VALUE_PREFIX)) {
          start = line.lastIndexOf(VALUE_PREFIX);
          statement = line.substring(start + 2, end);
        }
        consumers.add(new Text(data.size()));
        data.add(line.substring(0, start));
        consumers.add(new Statement(statement));
        line = line.substring(end + 1);
      }
    }

    void apply(Map<String, Object> attrs, Writer writer) throws ScriptException {
      Bindings bindings = ENGINE.createBindings();
      ScriptContext ctx = new SimpleScriptContext();
      ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
      bindings.putAll(attrs);
      bindings.put("$out", writer);
      bindings.put("$data", this.data);
      if (compiledScript != null) {
        compiledScript.eval(bindings);
      } else {
        ENGINE.eval(parsed, bindings);
      }
    }
  }
}
