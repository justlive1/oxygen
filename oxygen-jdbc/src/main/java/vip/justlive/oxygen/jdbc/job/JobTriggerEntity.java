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
 * job trigger实体
 *
 * @author wubo
 */
@Data
@Table("oxy_job_trigger")
@Accessors(chain = true)
public class JobTriggerEntity {

  @Column(pk = true)
  private Long id;

  @Column("job_key")
  private String jobKey;

  @Column("trigger_key")
  private String triggerKey;

  @Column("trigger_type")
  private Integer triggerType;

  @Column("trigger_value")
  private String triggerValue;

  @Column
  private Integer state;

  @Column
  private Long rounds;

  @Column("start_time")
  private Long startTime;

  @Column("end_time")
  private Long endTime;

  @Column("previous_fire_time")
  private Long previousFireTime;

  @Column("next_fire_time")
  private Long nextFireTime;

  @Column("last_completed_time")
  private Long lastCompletedTime;

}
