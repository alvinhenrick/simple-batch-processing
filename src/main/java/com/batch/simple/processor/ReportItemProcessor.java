package com.batch.simple.processor;


import com.batch.simple.model.Report;
import org.springframework.batch.item.ItemProcessor;

/**
 * Created by ahenrick on 6/17/14.
 */
public class ReportItemProcessor implements ItemProcessor<Report, Report> {

  @Override
  public Report process(Report item) throws Exception {
    System.out.println("Processing..." + item);
    return item;
  }

}