package com.batch.scheduler.writers;


import com.batch.simple.model.Report;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class CustomWriter implements ItemWriter<Report> {

	@Override
	public void write(List<? extends Report> items) throws Exception {

		System.out.println("writer..." + items.size());		
		for(Report item : items){
			System.out.println(item);
		}

	}

}