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
package vip.justlive.oxygen.jdbc;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.jdbc.handler.ResultSetHandler;
import vip.justlive.oxygen.jdbc.page.Page;
import vip.justlive.oxygen.jdbc.record.Entity;

/**
 * @author wubo
 */
public class JdbcTest {

  @Before
  public void before() {
    Bootstrap.start();
  }

  @Test
  public void test() {
    test(Jdbc.PRIMARY_KEY);
    test("a");
  }

  private void test(String dataSourceName) {

    Jdbc.use(dataSourceName);
    Jdbc.update("drop table if exists option");
    Jdbc.update(
        "create table option (id int auto_increment primary key, st varchar, it varchar, lo varchar, fl decimal, bl boolean, bd decimal, dt timestamp);");
    Jdbc.update("insert into option values (1, 'st', '1', '1222', 3.5, true, 5.891, CURRENT_TIME)");

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

    Entity<Option> entity = Entity.parse(Option.class);

    option = entity.findById(1);
    Assert.assertEquals(Long.valueOf(1222), option.getLl());

    option = new Option().setId(123L).setSt("0.2f");
    entity.insert(option);

    Assert.assertEquals(option, entity.findOne(option));

    option.setSt("22x");
    entity.updateById(option);

    Assert.assertEquals("22x", entity.findById(123).getSt());
    Assert.assertEquals(2, entity.findByIds(Arrays.asList(1, 123)).size());
    Assert.assertEquals(2, entity.count(new Option()));
    Assert.assertEquals(1, entity.find(option).size());
    Assert.assertEquals(2, entity.findAll().size());

    entity.deleteById(123);

    Jdbc.startTx();
    entity.delete(new Option());
    Jdbc.rollbackTx();

    Jdbc.closeTx();

    Assert.assertEquals(1, entity.count(new Option()));

    option = new Option().setId(123L).setSt("0.2f");
    entity.insert(option);

    Assert.assertEquals(2, entity.count(new Option()));
    entity.deleteByIds(Arrays.asList(1, 123L));
    Assert.assertEquals(0, entity.count(new Option()));

    option = new Option().setSt("0.2f");
    entity.insert(option);
    Assert.assertNotNull(option.getId());
  }

  @Test
  public void testPage() {
    Jdbc.update("drop table if exists option");
    Jdbc.update(
        "create table option (id int auto_increment primary key, st varchar, it varchar, lo varchar, fl decimal, bl boolean, bd decimal, dt timestamp);");
    Jdbc.update(
        "insert into option values (1, 'st1', '1', '1222', 3.5, true, 5.891, CURRENT_TIME)");
    Jdbc.update(
        "insert into option values (2, 'st2', '2', '1222', 3.5, true, 5.891, CURRENT_TIME)");

    Page<Option> page = new Page<>(1, 1);
    List<Option> list = Jdbc.queryForList("select * from option", Option.class, page);

    Assert.assertEquals(1, list.size());
    Assert.assertEquals("st1", list.get(0).getSt());
    Assert.assertEquals(new Long(2), page.getTotalNumber());

    list = page.getItems();
    Assert.assertNotNull(list);
    Assert.assertEquals(1, list.size());
    Assert.assertEquals("st1", list.get(0).getSt());

    page = new Page<>(2, 1);
    page.setSearchCount(false);
    Entity.parse(Option.class).page(new Option(), page);
    list = page.getItems();
    Assert.assertNotNull(list);
    Assert.assertEquals(1, list.size());
    Assert.assertEquals("st2", list.get(0).getSt());
    Assert.assertNull(page.getTotalNumber());

  }

}
