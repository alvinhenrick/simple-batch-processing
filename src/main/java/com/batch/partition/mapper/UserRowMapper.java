package com.batch.partition.mapper;

import com.batch.partition.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ahenrick on 6/19/14.
 */
public class UserRowMapper implements RowMapper<User> {

  @Override
  public User mapRow(ResultSet rs, int rowNum) throws SQLException {

    User user = new User();

    user.setId(rs.getInt("id"));
    user.setUsername(rs.getString("user_login"));
    user.setPassword(rs.getString("user_pass"));
    user.setAge(rs.getInt("age"));

    return user;
  }

}