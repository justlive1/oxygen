/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.jdbc;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.record.Record;

/**
 * @author wubo
 */
public class JdbcTest {

  @Before
  public void before() {
    Bootstrap.start();
    Jdbc.update(
        "create table option (id int primary key, st varchar, it varchar, lo varchar, fl decimal, bl boolean, bd decimal, dt timestamp);");
    Jdbc.update("insert into option values (1, 'st', '1', '1222', 3.5, true, 5.891, CURRENT_TIME)");
  }

  @Test
  public void test() {
    String sql = "select * from option where id = ?";
    Assert.assertEquals(8, Jdbc.queryForMap(sql, 1).size());
    Assert.assertEquals(1, Jdbc.queryForMapList(sql, 1).size());
    Assert.assertEquals(8, Jdbc.query(sql, ResultSetHandler.arrayHandler(), 1).length);
    Assert.assertEquals(1, Jdbc.query(sql, ResultSetHandler.arrayListHandler(), 1).size());

    Option option = Jdbc.query(sql, Option.class, 1);
    Assert.assertEquals(new Integer(1), option.getIt());
    List<Option> list = Jdbc.queryForList("select * from option", Option.class);
    Assert.assertNotNull(list);
    Assert.assertEquals(1, list.size());

    Assert.assertNull(Jdbc.query(sql, Option.class, 2));
    Assert.assertEquals(0, Jdbc.queryForList(sql, Option.class, 2).size());

    option = Record.findById(Option.class, 1);
    Assert.assertEquals(Long.valueOf(1222), option.getLl());

    option = new Option();
    option.setId(123l);
    option.setFl(0.2f);
    Record.insert(option);

    Assert.assertEquals(2, Record.count(new Option()));

    Record.deleteById(Option.class, 123);

    Jdbc.startTx();
    Record.delete(new Option());
    Jdbc.rollbackTx();

    Jdbc.closeTx();

    Assert.assertEquals(1, Record.count(new Option()));

  }

}