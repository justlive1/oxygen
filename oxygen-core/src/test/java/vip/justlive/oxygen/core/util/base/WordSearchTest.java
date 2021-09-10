/*
 * Copyright (C) 2021 the original author or authors.
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
package vip.justlive.oxygen.core.util.base;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.util.base.WordSearch.Result;

class WordSearchTest {


  @Test
  void findFirst() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    Result res = search.findFirst("512231233,123");
    Assertions.assertNotNull(res);
    Assertions.assertEquals(5, res.getIndex());
  }

  @Test
  void testFindFirst() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    Result res = search.findFirst("512231-2-33,123", true);
    Assertions.assertNotNull(res);
    Assertions.assertEquals(5, res.getIndex());

  }

  @Test
  void findAll() {

    WordSearch search = new WordSearch();
    search.addKeyword("123");

    List<Result> res = search.findAll("512231233,123");
    Assertions.assertEquals(2, res.size());

  }

  @Test
  void testFindAll() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    List<Result> res = search.findAll("512231-2&33,123", true);
    Assertions.assertEquals(2, res.size());
  }

  @Test
  void replace() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    String res = search.replace("512231-2&33,123", "$");
    Assertions.assertEquals("512231-2&33,$", res);
  }

  @Test
  void testReplace() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    String res = search.replace("512231-2&33,123", "$", true);
    Assertions.assertEquals("51223$3,$", res);
  }
}