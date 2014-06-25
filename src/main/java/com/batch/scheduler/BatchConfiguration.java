package com.batch.scheduler;

import com.batch.scheduler.writers.CustomWriter;
import com.batch.simple.mapper.ReportFieldSetMapper;
import com.batch.simple.model.Report;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

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
}
