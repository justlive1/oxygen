/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.ioc;


import vip.justlive.oxygen.core.config.Value;
import vip.justlive.oxygen.ioc.annotation.Bean;
import vip.justlive.oxygen.ioc.annotation.Inject;

@Bean("xxx")
public class DepBean implements Inter {

  private final NoDepBean noDepBean;
  @Value("${val.a:xxx}")
  private String val;

  @Inject
  public DepBean(NoDepBean noDepBean) {
    this.noDepBean = noDepBean;
  }

  @Override
  public void print() {
    System.out.println("this bean has dependency of NoDepBean and val is " + val);
    noDepBean.print();
  }

}
