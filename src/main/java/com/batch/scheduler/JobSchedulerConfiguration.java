package com.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

/**
 * Created by ahenrick on 6/17/14.
 */
@Configuration
@EnableScheduling
public class JobSchedulerConfiguration {

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job job;

  @Scheduled(cron = "*/5 * * * * *")
  public void run() {
    try {

      String dateParam = new Date().toString();
      JobParameters param = new JobParametersBuilder().addString("date", dateParam).toJobParameters();

      System.out.println(dateParam);

      JobExecution execution = jobLauncher.run(job, param);
      System.out.println("Exit Status : " + execution.getStatus());

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
