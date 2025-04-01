/*
 * Copyright (C) 2025 the original author or authors.
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

package vip.justlive.oxygen.core.util.compiler;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.ClassUtils;

class DynamicCompilerTest  {

  @Test
  void compile() {



    String code =
    """
        package com.zfgea.ss;
        
        public class Dax implements vip.justlive.oxygen.core.aop.Callback{
        
          static{
             System.out.println("111");
          }
          
          {System.out.println("12313");}
          
          
          public Object apply(vip.justlive.oxygen.core.aop.Invocation ivt) throws Throwable{
          return null;
          }
          
          public void a(){
           System.out.println("12313");
          }
        }
    """;

    try(DynamicCompiler compiler = new DynamicCompiler();) {

     Class<?> type = compiler.compile(code);
      System.out.println(type);

      ClassUtils.newInstance(type);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }


  }
}