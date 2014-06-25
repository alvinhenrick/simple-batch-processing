package com.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;

/**
 * Created by ahenrick on 6/17/14.
 */
public class FileDeletingTasklet implements Tasklet {

  private Resource directory;

  @Override
  public RepeatStatus execute(StepContribution contribution,
                              ChunkContext chunkContext) throws Exception {

    File dir = directory.getFile();
    Assert.state(dir.isDirectory());

    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      boolean deleted = files[i].delete();
      if (!deleted) {
        throw new UnexpectedJobExecutionException(
            "Could not delete file " + files[i].getPath());
      } else {
        System.out.println(files[i].getPath() + " is deleted!");
      }
    }
    return RepeatStatus.FINISHED;
  }

  public Resource getDirectory() {
    return directory;
  }

  public void setDirectory(Resource directory) {
    this.directory = directory;
  }

}