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

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.jdbc.record.Column;
import vip.justlive.oxygen.jdbc.record.Table;

/**
 * @author wubo
 */
@Data
@Accessors(chain = true)
@Table
public class Option {

  @Column(pk = true)
  private Long id;

  @Column
  private String st;

  private Integer it;

  private Long lo;

  private float fl;

  private boolean bl;

  private BigDecimal bd;

  private Date dt;

  @Column("lo")
  private Long ll;

}
