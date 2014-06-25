package com.batch.partition.data;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

/**
 * Created by ahenrick on 6/19/14.
 */
public class InsertData implements InitializingBean {

  private JdbcTemplate jdbcTemplate;

  @Autowired
  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }


  @Override
  public void afterPropertiesSet() throws Exception {

    jdbcTemplate.execute("DROP TABLE users IF EXISTS;");

    jdbcTemplate.execute("CREATE TABLE users  (" +
        "    id BIGINT IDENTITY NOT NULL PRIMARY KEY," +
        "    user_login VARCHAR(20)," +
        "    user_pass VARCHAR(20)," +
        "    age BIGINT" +
        ");");

    Random generator = new Random();
    String arraySQl[] = new String[100];

    for (int i = 0; i < 100; i++) {

      String insertTableSQL = "INSERT INTO USERS (ID, USER_LOGIN, USER_PASS, AGE) VALUES (:id,':name',':pass',:age)";

      insertTableSQL = insertTableSQL.replaceAll(":id", String.valueOf(i + 1));
      insertTableSQL = insertTableSQL.replaceAll(":name", "user_" + i + 1);
      insertTableSQL = insertTableSQL.replaceAll(":pass", "pass_" + i + 1);
      insertTableSQL = insertTableSQL.replaceAll(":age", String.valueOf(generator.nextInt(100)));
      arraySQl[i] = insertTableSQL;
      System.out.println(insertTableSQL);
    }

    jdbcTemplate.batchUpdate(arraySQl);

  }
}
