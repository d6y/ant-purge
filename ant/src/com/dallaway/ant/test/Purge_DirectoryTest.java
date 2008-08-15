package com.dallaway.ant.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import com.dallaway.ant.Purge;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.junit.Before;
import org.junit.Test;

public class Purge_DirectoryTest {

  private File file;

  @Before
  public void execute() throws Exception {
    file = new File("./directoryTest"); //$NON-NLS-1$
    file.mkdir();
    file.deleteOnExit();
    for (int i = 0; i < 10; i++) {
      final File testFolder = new File(file, String.valueOf(i));
      testFolder.mkdir();
      testFolder.deleteOnExit();
    }
    final Project project = new Project();
    project.setBaseDir(file);
    final FileSet set = new FileSet();
    set.setDir(file);
    final Purge purge = new Purge();
    purge.setProject(project);
    purge.addConfiguredFileset(set);
    purge.setDeleteFolders(true);
    purge.setKeep(2);
    purge.execute();
  }

  @Test
  public void leavesParentDirectory() throws Exception {
    assertThat(file.exists(), is(true));
  }

  @Test
  public void leavesConfiguredNumberOfDirectories() throws Exception {
    assertThat(file.listFiles().length, is(2));
  }
}