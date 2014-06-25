package com.batch.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by ahenrick on 6/18/14.
 */
@Component
public class MyJob implements Job {

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private org.springframework.batch.core.Job job;

  @Override
  public void execute(JobExecutionContext jobContext) throws JobExecutionException {

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
