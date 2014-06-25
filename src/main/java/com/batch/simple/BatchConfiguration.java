package com.batch.simple;

import com.batch.simple.mapper.ReportFieldSetMapper;
import com.batch.simple.model.Report;
import com.batch.simple.processor.ReportItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

/**
 * Created by ahenrick on 6/17/14.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

  @Bean
  public ItemReader<Report> reader() {
    FlatFileItemReader<Report> reader = new FlatFileItemReader<Report>();
    reader.setResource(new ClassPathResource("report.csv"));
    reader.setLineMapper(new DefaultLineMapper<Report>() {{
      setLineTokenizer(new DelimitedLineTokenizer() {{
        setNames(new String[] { "id","sales","qty","staffName","date"});
      }});
      setFieldSetMapper(new ReportFieldSetMapper());
    }});
    return reader;
  }

  @Bean
  public ItemProcessor<Report, Report> processor() {
    return new ReportItemProcessor();
  }

  //http://stackoverflow.com/questions/23089159/why-does-destroy-method-close-fail-for-jpapagingitemreader-configured-with-jav
  @Bean(destroyMethod="")
  public ItemWriter<Report> writer() {
    StaxEventItemWriter<Report> writer = new StaxEventItemWriter<Report>();
    writer.setResource(new FileSystemResource("xml/outputs/report.xml"));
    writer.setMarshaller(jaxb2Marshaller());
    writer.setRootTagName("report");
    return writer;
  }

  @Bean
  public Job csvToXmlImportJob(JobBuilderFactory jobs, Step s1) {
    return jobs.get("csvToXmlImportJob")
        .incrementer(new RunIdIncrementer())
        .flow(s1)
        .end()
        .build();
  }

  @Bean
  public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<Report> reader,
                    ItemWriter<Report> writer, ItemProcessor<Report, Report> processor) {
    return stepBuilderFactory.get("step1")
        .<Report, Report> chunk(10)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public Jaxb2Marshaller  jaxb2Marshaller() {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setClassesToBeBound(Report.class);
    return jaxb2Marshaller;
  }

}
