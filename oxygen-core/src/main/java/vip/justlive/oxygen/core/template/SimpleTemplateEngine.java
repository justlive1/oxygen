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

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.oxygen.core.util.ExpiringMap.ExpiringPolicy;
import vip.justlive.oxygen.core.util.TokenParser;

/**
 * 简单模板引擎实现
 *
 * @author wubo
 */
public class SimpleTemplateEngine implements TemplateEngine {

  private static final TokenParser SCRIPT_PARSER = new TokenParser("#{", "}");
  private static final TokenParser VARS_PARSER = new TokenParser("${", "}");

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
        return;
      }
      spec = new TemplateSpec(template);
      CACHE.put(template, spec);
      spec.apply(attrs, writer);
    } catch (ScriptException e) {
      throw Exceptions.wrap(e);
    }
  }

  static class TemplateSpec {

    String parsed;
    CompiledScript compiledScript;
    List<String> data = new ArrayList<>();

    TemplateSpec(String template) throws ScriptException {
      parsed = String.format("var func = function(){%s};func();",
          SCRIPT_PARSER.parse(template, text -> VARS_PARSER.parse(text, a -> {
            data.add(a);
            return String.format("$out.write($data[%s]);", data.size() - 1);
          }, b -> String.format("$out.write(''+(%s));", b)), Function.identity()));
      if (COMPILABLE != null) {
        compiledScript = COMPILABLE.compile(parsed);
      } else {
        parsed = null;
      }
    }

    void apply(Map<String, Object> attrs, Writer writer) throws ScriptException {
      Bindings bindings = ENGINE.createBindings();
      ScriptContext ctx = new SimpleScriptContext();
      ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
      bindings.putAll(attrs);
      bindings.put("$out", writer);
      bindings.put("$data", data);
      if (compiledScript != null) {
        compiledScript.eval(bindings);
      } else {
        ENGINE.eval(parsed, bindings);
      }
    }
  }
}
