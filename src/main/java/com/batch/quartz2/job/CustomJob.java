package com.batch.quartz2.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * Created by ahenrick on 6/18/14.
 */
public class CustomJob extends QuartzJobBean {

  private JobLauncher jobLauncher;
  private Job job;

  public void setJobLauncher(JobLauncher jobLauncher) {
    this.jobLauncher = jobLauncher;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  @Override
  protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
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
