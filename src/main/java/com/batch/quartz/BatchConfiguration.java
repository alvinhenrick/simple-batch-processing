package com.batch.quartz;

import com.batch.quartz.job.MyJob;
import com.batch.scheduler.writers.CustomWriter;
import com.batch.simple.mapper.ReportFieldSetMapper;
import com.batch.simple.model.Report;
import org.quartz.Trigger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

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
  private ApplicationContext applicationContext;

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
  public Job reportJob() {
    return jobBuilderFactory.get("reportJob")
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
    // Custom job factory of spring with DI support for @Autowired
    AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
    jobFactory.setApplicationContext(applicationContext);

    SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
    Trigger[] triggers = { processTrigger().getObject() };
    quartzScheduler.setTriggers(triggers);
    quartzScheduler.setJobFactory(jobFactory);

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
  public JobDetailFactoryBean processJob(){
    JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    jobDetailFactory.setJobClass(MyJob.class);
    jobDetailFactory.setDurability(true);
    return jobDetailFactory;
  }
}
