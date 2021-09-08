package vip.justlive.oxygen.jdbc.job;

import java.util.ArrayList;
import java.util.List;
import vip.justlive.oxygen.core.bean.Singleton;
import vip.justlive.oxygen.core.job.JobTrigger;

/**
 * trigger转换器
 *
 * @author wubo
 */
public class TriggerConverter {

  private final List<Converter> converters = new ArrayList<>();

  public TriggerConverter() {
    converters.add(new FixedTimeJobTriggerConverter());
    converters.add(new DelayOrRateJobTriggerConverter());
    converters.add(new CronJobTriggerConverter());
    converters.addAll(Singleton.getList(Converter.class));
  }

  public JobTriggerEntity convert(JobTrigger trigger) {
    for (Converter converter : converters) {
      if (converter.classType() == trigger.getClass()) {
        return converter.convert(trigger);
      }
    }
    return null;
  }

  public JobTrigger convert(JobTriggerEntity entity) {
    if (entity.getTriggerType() == null) {
      return null;
    }
    for (Converter converter : converters) {
      if (converter.type() == entity.getTriggerType()) {
        return converter.convert(entity);
      }
    }
    return null;
  }
}
