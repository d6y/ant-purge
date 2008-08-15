/*
 * Copyright 2002-2007 Richard Dallaway <richard@dallaway.com> Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package com.dallaway.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Delete all but the most recent few files from a fileset. <p>For example: <code>&lt;purge
 * keep="5"&gt;&lt;fileset dir="logs"&gt;&lt;/purge&gt;</code> ...would remove all but the five most
 * recent files from the logs folder. </p> <p>If the attribute <code>test="true"</code> is supplied,
 * then the Purge action will report on what it would do, without removing any files.</p> <p>Note
 * that if multiple filesets are specified, then each fileset will be purged individually. For
 * example, if the directors "logs" and "output" are specified to be purged keeping just the most
 * recent 5 files, then the logs folder will be purged (keeping just 5 files) and then the output
 * folder will be purged (also keeping just 5 files). In total 10 files will be left.</p>
 * @author Richard Dallaway <a href="mailto:richard@dallaway.com">richard@dallaway.com</a>
 * @author Urs Reupke <a href="mailto:ursreupke@gmx.net">ursreupke@gmx.net</a>
 * @version $Revision$ $Date$
 */
public class Purge extends Task {

  // List of all the filesets to process.
  private final List<FileSet> filesets = new ArrayList<FileSet>();

  // The number of files to keep (per fileset); default is 10. 
  private int numToKeep = 10;

  /* 
   * Flag to allow sanity checking: when true actions are reported but not carried out
   * (no files are deleted). Default is false, meaning that files are deleted. 
   */
  private boolean isTest = false;

  private boolean isDeleteFolders = false;

  /**
   * @param set a fileset to add to the list to purge.
   */
  public void addConfiguredFileset(final FileSet set) {
    filesets.add(set);
  }

  /**
   * @param numToKeep the number of files to keep per fileset.
   */
  public void setKeep(final int numToKeep) {
    this.numToKeep = numToKeep;
  }

  /**
   * @param isTestMode true if we just show the actions we would
   *                        take (no files deleted).  Default is false.
   */
  public void setTest(final boolean isTestMode) {
    this.isTest = isTestMode;
  }

  public void setDeleteFolders(final boolean isDeleteFolders) {
    this.isDeleteFolders = isDeleteFolders;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute() throws BuildException {
    for (final FileSet fileset : filesets) {
      purge(fileset);
    }
  }

  /**
   * Purge a given fileset by deleting all but the most 
     * recent <code>numToKeep</code> files.
   * 
   * @param fs the fileset to purge.
   */
  private void purge(final FileSet fs) {
    final File dir = fs.getDir(getProject());
    log("Purging " + dir.getAbsolutePath(), Project.MSG_INFO);

    // All the files in the fileset to process:
    final List<File> files = new ArrayList<File>();

    final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
    if (!isDeleteFolders) {
      final String[] fileNames = ds.getIncludedFiles();
      files.addAll(addCandidateFiles(dir, fileNames));
    }
    else {
      final String[] folderNames = ds.getIncludedDirectories();
      files.addAll(addCandidateFiles(dir, folderNames));
      //Increase numToKeep to cope with inclusion of root directory.
      numToKeep++;
    }

    // If we have less than numToKeep files, there's nothing to do:
    if (files.size() <= numToKeep) {
      return;
    }

    // Sort by modification timestamp:
    Collections.sort(files, new TimeStampComparator());

    // At this point the files[0 .. numToKeep-1] are the files to keep.  Any
    // other files can be deleted.

    if (isTest) {
      // Show the files we'd keep:
      for (int i = 0; i < numToKeep; i++) {
        log("Would keep " + (files.get(i)).getAbsolutePath(), Project.MSG_INFO);
      }
    }
    // Delete all but the most recent numToKeep:
    for (int i = numToKeep, n = files.size(); i < n; i++) {
      final File f = files.get(i);
      if (isTest) {
        log("Would delete " + f.getAbsolutePath(), Project.MSG_INFO);
      }
      else {
        // Really delete the file:  
        log("Deleting " + f.getAbsolutePath(), Project.MSG_INFO);
        try {
          forceDelete(f);
        }
        catch (final IOException e) {
          //TODO: Upgrade to Ant 1.7, log exception directly
          log("Failed to delete " + f.getAbsoluteFile(), Project.MSG_ERR);
        }
      }
    }
  }

  private ArrayList<File> addCandidateFiles(final File dir, final String[] fileNames) {
    final ArrayList<File> list = new ArrayList<File>();
    for (final String name : fileNames) {
      final File candidate = new File(dir, name);
      list.add(candidate);
    }
    return list;
  }

  /**Unconditionally deletes a file. Purges directories. Taken from Apache Commons FileUtilities,
   * since the FileUtilities included in Ant do not include these methods*/
  public static void forceDelete(final File file) throws IOException {
    if (file.isDirectory()) {
      deleteDirectory(file);
    }
    else {
      if (!file.exists()) {
        throw new FileNotFoundException("File does not exist: " + file);
      }
      if (!file.delete()) {
        final String message = "Unable to delete file: " + file;
        throw new IOException(message);
      }
    }
  }

  /**
  * Recursively delete a directory. Taken from Apache Commons FileUtilities,
   * since the FileUtilities included in Ant do not include these methods
  * @throws IOException in case deletion is unsuccessful
  */
  public static void deleteDirectory(final File directory) throws IOException {
    if (!directory.exists()) {
      return;
    }

    cleanDirectory(directory);
    if (!directory.delete()) {
      final String message = "Unable to delete directory " + directory + ".";
      throw new IOException(message);
    }
  }

  /**
   * Clean a directory without deleting it. Taken from Apache Commons FileUtilities,
   * since the FileUtilities included in Ant do not include these methods
   * @throws IOException in case cleaning is unsuccessful
   */
  public static void cleanDirectory(final File directory) throws IOException {
    if (!directory.exists()) {
      final String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      final String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    final File[] files = directory.listFiles();
    if (files == null) { // null if security restricted
      throw new IOException("Failed to list contents of " + directory);
    }

    IOException exception = null;
    for (final File file : files) {
      try {
        forceDelete(file);
      }
      catch (final IOException ioe) {
        exception = ioe;
      }
    }

    if (null != exception) {
      throw exception;
    }
  }

  /**
   * Order two files such that the most recently modified file will appear at the start of the list of files.
   * {@inheritDoc}
   */
  private class TimeStampComparator implements Comparator<File> {

    public int compare(final File o1, final File o2) {

      final long f1 = o1.lastModified();
      final long f2 = o2.lastModified();

      if (f1 > f2) {
        return -1;
      }
      else if (f1 < f2) {
        return 1;
      }
      else {
        return 0;
      }
    }
  }
}