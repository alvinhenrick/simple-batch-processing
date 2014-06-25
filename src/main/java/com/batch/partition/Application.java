package com.batch.partition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

@ComponentScan
@EnableAutoConfiguration
public class Application {

  public static void main(String[] args) {
    ApplicationContext ctx = SpringApplication.run(Application.class, args);

    ctx.getBean(JdbcTemplate.class).query("SELECT JOB_NAME, JOB_KEY FROM BATCH_JOB_INSTANCE", new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {

        System.out.println("JOB_NAME=" + rs.getString("JOB_NAME"));
        System.out.println("JOB_KEY=" + rs.getString("JOB_KEY"));

      }
    });

  }

}
