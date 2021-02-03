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

package vip.justlive.oxygen.core.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author wubo
 */
public class LocalKeyLockTest {

  @Test
  public void test() {
    final Bank bank = new Bank();
    bank.money = 500;
    new Thread(() -> {
      ThreadUtils.sleep(200);
      bank.login();
      ThreadUtils.sleep(100);
      bank.withdraw(500);
      bank.logout();
    }).start();

    new Thread(() -> {
      ThreadUtils.sleep(100);
      bank.login();
      ThreadUtils.sleep(100);
      bank.withdraw(300);
      bank.logout();
    }).start();

    ThreadUtils.sleep(400);

    Assert.assertEquals(200, bank.money);
  }

  static class Bank {

    int money;
    KeyLock lock = new LocalKeyLock();

    void login() {
      lock.lock(Thread.currentThread().getName());
      System.out.printf("login: %s, balance: %s %n", Thread.currentThread(), money);
    }

    boolean withdraw(int money) {
      if (this.money < money) {
        System.out
            .printf("user: %s, withdraw: %s, insufficient balance %s %n", Thread.currentThread(),
                money, this.money);
        return false;
      }
      this.money -= money;
      System.out
          .printf("user: %s, withdraw: %s, left: %s %n", Thread.currentThread(), money, this.money);
      return true;
    }

    void logout() {
      System.out.printf("logout: %s, balance: %s %n", Thread.currentThread(), money);
      lock.unlock(Thread.currentThread().getName());
    }

  }
}