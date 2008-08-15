package com.dallaway.ant.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import com.dallaway.ant.Purge;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.junit.Before;
import org.junit.Test;

public class Purge_MultiFilesetTest {

  private File file;
  private File file2;

  @Before
  public void execute() throws Exception {
    file2 = new File("./multiTest1"); //$NON-NLS-1$
    createDirectory(file2);
    file = new File("./multiTest1"); //$NON-NLS-1$
    createDirectory(file);
    for (int i = 0; i < 10; i++) {
      createDirectory(new File(file, String.valueOf(i)));
    }
    final Project project = new Project();
    project.setBaseDir(new File("."));
    final FileSet set = new FileSet();
    set.setDir(file);
    final FileSet set2 = new FileSet();
    set2.setDir(file2);
    final Purge purge = new Purge();
    purge.setProject(project);
    purge.addConfiguredFileset(set);
    purge.addConfiguredFileset(set2);
    purge.setDeleteFolders(true);
    purge.setKeep(2);
    purge.execute();
  }

  private void createDirectory(final File file) {
    file.mkdir();
    file.deleteOnExit();
  }

  @Test
  public void leavesParentDirectory() throws Exception {
    assertThat(file.exists(), is(true));
  }

  @Test
  public void leavesSecondParentDirectory() throws Exception {
    assertThat(file2.exists(), is(true));
  }

  @Test
  public void leavesConfiguredNumberOfDirectories() throws Exception {
    assertThat(file.listFiles().length, is(2));
  }
}