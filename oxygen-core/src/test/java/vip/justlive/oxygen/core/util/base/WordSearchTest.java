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
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.util.base.WordSearch.Result;

public class WordSearchTest {


  @Test
  public void findFirst() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    Result res = search.findFirst("512231233,123");
    Assert.assertNotNull(res);
    Assert.assertEquals(5, res.getIndex());
  }

  @Test
  public void testFindFirst() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    Result res = search.findFirst("512231-2-33,123", true);
    Assert.assertNotNull(res);
    Assert.assertEquals(5, res.getIndex());

  }

  @Test
  public void findAll() {

    WordSearch search = new WordSearch();
    search.addKeyword("123");

    List<Result> res = search.findAll("512231233,123");
    Assert.assertEquals(2, res.size());

  }

  @Test
  public void testFindAll() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    List<Result> res = search.findAll("512231-2&33,123", true);
    Assert.assertEquals(2, res.size());
  }

  @Test
  public void replace() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    String res = search.replace("512231-2&33,123", "$");
    Assert.assertEquals("512231-2&33,$", res);
  }

  @Test
  public void testReplace() {
    WordSearch search = new WordSearch();
    search.addKeyword("123");

    String res = search.replace("512231-2&33,123", "$", true);
    Assert.assertEquals("51223$3,$", res);
  }
}