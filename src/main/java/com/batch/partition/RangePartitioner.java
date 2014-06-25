package com.batch.partition;

/**
 * Created by ahenrick on 6/19/14.
 */

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class RangePartitioner implements Partitioner {

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {

    Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();

    int range = 10;
    int fromId = 1;
    int toId = range;

    for (int i = 1; i <= gridSize; i++) {
      ExecutionContext value = new ExecutionContext();

      System.out.println("\nStarting : Thread" + i);
      System.out.println("fromId : " + fromId);
      System.out.println("toId : " + toId);

      value.putInt("fromId", fromId);
      value.putInt("toId", toId);

      // give each thread a name
      value.putString("name", "Thread" + i);

      result.put("partition" + i, value);

      fromId = toId + 1;
      toId += range;

    }

    return result;
  }

}
