package com.batch.partition.processor;

/**
 * Created by ahenrick on 6/19/14.
 */

import com.batch.partition.model.User;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

@StepScope
public class UserProcessor implements ItemProcessor<User, User> {

  @Value("#{stepExecutionContext[name]}")
  private String threadName;

  @Override
  public User process(User item) throws Exception {

    System.out.println(threadName + " processing : " + item.getId() + " : " + item.getUsername());

    return item;
  }

  public String getThreadName() {
    return threadName;
  }

  public void setThreadName(String threadName) {
    this.threadName = threadName;
  }

}
