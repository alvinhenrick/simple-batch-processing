package com.batch.partition;

import com.batch.partition.data.InsertData;
import com.batch.partition.mapper.UserRowMapper;
import com.batch.partition.model.User;
import com.batch.partition.processor.UserProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

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
  DataSource dataSource;

  @Bean
  public TaskExecutor taskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

  @Bean
  public InsertData insertData() {
    return new InsertData();
  }

  @StepScope
  @Bean
  public ItemReader<User> reader(@Value("#{stepExecutionContext[fromId]}") Integer fromId, @Value("#{stepExecutionContext[toId]}") Integer toId) {
    JdbcPagingItemReader pagingItemReader = new JdbcPagingItemReader();
    pagingItemReader.setDataSource(dataSource);
    SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
    pagingQueryProvider.setDataSource(dataSource);
    pagingQueryProvider.setSelectClause("select id, user_login, user_pass, age");
    pagingQueryProvider.setFromClause("from users");
    pagingQueryProvider.setWhereClause("where id >= :fromId and id <= :toId");
    pagingQueryProvider.setSortKey("id");
    try {
      pagingItemReader.setQueryProvider(pagingQueryProvider.getObject());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put("fromId",fromId);
    map.put("toId", toId);
    pagingItemReader.setParameterValues(map);
    pagingItemReader.setPageSize(10);
    pagingItemReader.setRowMapper(new UserRowMapper());
    return pagingItemReader;
  }

  @StepScope
  @Bean
  public ItemProcessor<User, User> processor() {
    return new UserProcessor();
  }

  @StepScope
  @Bean
  public FlatFileItemWriter<User> writer(@Value("#{stepExecutionContext[fromId]}") Integer fromId, @Value("#{stepExecutionContext[toId]}") Integer toId) {
    FlatFileItemWriter<User> writer = new FlatFileItemWriter<User>();
    writer.setResource(new FileSystemResource("csv/outputs/users.processed-"+fromId+"-"+toId+".csv"));
    writer.setAppendAllowed(false);
    writer.setShouldDeleteIfExists(true);
    writer.setLineAggregator(new DelimitedLineAggregator<User>() {{
      setDelimiter(",");
      setFieldExtractor(new BeanWrapperFieldExtractor<User>() {{
        setNames(new String[]{"id", "username", "password", "age"});
      }});
    }});
    return writer;
  }

  @Bean
  public Partitioner partitioner() {
    return new RangePartitioner();
  }

  @Bean
  public Job partitionJob() {
    return jobBuilderFactory.get("partitionJob")
        .start(partitionStep())
        .build();
  }

  @Bean
  public Step partitionStep(){
    return stepBuilderFactory.get("partitionStep")
        .partitioner(slaveStep())
        .partitioner("slaveStep", partitioner())
        .gridSize(10)
        .taskExecutor(taskExecutor())
        .build();
  }

  @Bean
  public Step slaveStep() {
    return stepBuilderFactory.get("slaveStep")
        .<User, User>chunk(1)
        .reader(reader(null,null))
        .processor(processor())
        .writer(writer(null,null))
        .build();
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource);
  }


}
