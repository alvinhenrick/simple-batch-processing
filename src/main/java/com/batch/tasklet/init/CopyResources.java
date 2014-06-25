package com.batch.tasklet.init;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by ahenrick on 6/24/14.
 */
public class CopyResources implements InitializingBean {

  private Resource inputDirectory;
  private Resource backUpDirectory;

  public void setInputDirectory(Resource inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public void setBackUpDirectory(Resource backUpDirectory) {
    this.backUpDirectory = backUpDirectory;
  }

  @Override
  public void afterPropertiesSet() throws Exception {

    File dir = backUpDirectory.getFile();
    Assert.state(dir.isDirectory());
    Assert.state(inputDirectory.getFile().isDirectory());

    File[] backUpFiles = dir.listFiles();
    for (File file : backUpFiles) {
      FileCopyUtils.copy(new FileInputStream(file), new FileOutputStream(new File(inputDirectory.getFile(), file.getName())));
      System.out.println(file.getPath() + " is copied!");
    }
    System.out.println("File Copy Successful");
  }

}
