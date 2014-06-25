package com.batch.quartz2;

import com.batch.quartz2.job.CustomJob;
import com.batch.scheduler.writers.CustomWriter;
import com.batch.simple.mapper.ReportFieldSetMapper;
import com.batch.simple.model.Report;
import org.quartz.Trigger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ahenrick on 6/17/14.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

  @Autowired
  private JobBuilderFactory jobBuilderFactory;

  @Autowired
  private StepBuilderFactory stepBuilderFactory;

  @Autowired
  JobLauncher jobLauncher;

  @Bean
  public ItemReader<Report> reader() {
    FlatFileItemReader<Report> reader = new FlatFileItemReader<Report>();
    reader.setResource(new ClassPathResource("report.csv"));
    reader.setLineMapper(new DefaultLineMapper<Report>() {{
      setLineTokenizer(new DelimitedLineTokenizer() {{
        setNames(new String[]{"id", "sales", "qty", "staffName", "date"});
      }});
      setFieldSetMapper(new ReportFieldSetMapper());
    }});
    return reader;
  }

  @Bean
  public ItemWriter<Report> writer() {
    return new CustomWriter();
  }

  @Bean
  public Job customJob() {
    return jobBuilderFactory.get("customJob")
        .flow(step1())
        .end()
        .build();
  }

  @Bean
  public Step step1() {
    return stepBuilderFactory.get("step1")
        .<Report, Report>chunk(10)
        .reader(reader())
        .writer(writer())
        .build();
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public SchedulerFactoryBean quartzScheduler() {
    SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
    Trigger[] triggers = {processTrigger().getObject()};
    quartzScheduler.setTriggers(triggers);
    return quartzScheduler;
  }

  @Bean
  public CronTriggerFactoryBean processTrigger() {
    CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
    cronTriggerFactoryBean.setJobDetail(processJob().getObject());
    cronTriggerFactoryBean.setCronExpression("*/5 * * * * ?");
    return cronTriggerFactoryBean;
  }

  @Bean
  public JobDetailFactoryBean processJob() {
    Map<String, Object> dataAsMap = new HashMap<String, Object>();
    dataAsMap.put("jobLauncher", jobLauncher);
    dataAsMap.put("job", customJob());
    JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    jobDetailFactory.setJobClass(CustomJob.class);
    jobDetailFactory.setDurability(true);
    jobDetailFactory.setJobDataAsMap(dataAsMap);
    return jobDetailFactory;
  }
}
