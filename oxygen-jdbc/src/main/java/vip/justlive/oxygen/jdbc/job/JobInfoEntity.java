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
package vip.justlive.oxygen.jdbc.job;

import lombok.Data;
import lombok.experimental.Accessors;
import vip.justlive.oxygen.jdbc.record.Column;
import vip.justlive.oxygen.jdbc.record.Table;

/**
 * jobInfo实体
 *
 * @author wubo
 */
@Data
@Table("oxy_job_info")
@Accessors(chain = true)
public class JobInfoEntity {

  @Column(pk = true)
  private Long id;

  @Column("job_key")
  private String jobKey;

  @Column
  private String description;

  @Column("handler_class")
  private String handlerClass;

  @Column
  private String param;
}
