package com.batch.tasklet;

import com.batch.tasklet.init.CopyResources;
import com.batch.tasklet.model.Domain;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

  @Value("file:csv/inputs/domain-*.csv")
  private Resource[] resources;

  @Value("file:csv/inputs/")
  private Resource inputDirectory;

  @Value("file:csv/bk/")
  private Resource backUpDirectory;

  @Bean
  public CopyResources init() {
    CopyResources copyResources = new CopyResources();
    copyResources.setInputDirectory(inputDirectory);
    copyResources.setBackUpDirectory(backUpDirectory);
    return copyResources;

  }

  @Bean
  public ItemReader<Domain> reader() {
    FlatFileItemReader<Domain> reader = new FlatFileItemReader<Domain>();
    reader.setLineMapper(new DefaultLineMapper<Domain>() {{
      setLineTokenizer(new DelimitedLineTokenizer() {{
        setNames(new String[]{"id", "domain"});
      }});
      setFieldSetMapper(new BeanWrapperFieldSetMapper<Domain>() {{
        setTargetType(Domain.class);
      }});
    }});
    MultiResourceItemReader<Domain> multiResourceItemReader = new MultiResourceItemReader<Domain>();
    multiResourceItemReader.setDelegate(reader);
    multiResourceItemReader.setResources(resources);
    return multiResourceItemReader;
  }

  public ItemWriter<Domain> writer() {
    FlatFileItemWriter<Domain> writer = new FlatFileItemWriter<Domain>();
    writer.setResource(new FileSystemResource("csv/outputs/domain.all.csv"));
    writer.setAppendAllowed(true);
    writer.setLineAggregator(new DelimitedLineAggregator<Domain>() {{
      setDelimiter(",");
      setFieldExtractor(new BeanWrapperFieldExtractor<Domain>() {{
        setNames(new String[]{"id", "domain"});
      }});
    }});
    return writer;
  }


  @Bean
  public Job readMultiFileJob() {
    return jobBuilderFactory.get("readMultiFileJob")
        .flow(step1()).next(fileDeletingTasklet())
        .end()
        .build();
  }

  @Bean
  public Step step1() {
    return stepBuilderFactory.get("step1")
        .<Domain, Domain>chunk(1)
        .reader(reader())
        .writer(writer())
        .build();
  }

  @Bean
  public Step fileDeletingTasklet() {
    FileDeletingTasklet fileDeletingTasklet = new FileDeletingTasklet();
    fileDeletingTasklet.setDirectory(new FileSystemResource("csv/inputs/"));
    return stepBuilderFactory.get("fileDeletingTasklet")
        .tasklet(fileDeletingTasklet)
        .build();
  }

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

}
