/*
   Copyright 2002-2007 Richard Dallaway <richard@dallaway.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.dallaway.ant;

import java.io.File;
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
 * Delete all but the most recent few files from a fileset.
 * 
 * <p>For example: 
 * 
 * 	<code>&lt;purge keep="5"&gt;&lt;fileset dir="logs"&gt;&lt;/purge&gt;</code>
 * 
 * ...would remove all but the five most recent files from the logs folder.
 * </p>
 * 
 * <p>If the attribute <code>test="true"</code> is supplied, then the Purge
 * action will report on what it would do, without removing any files.</p>
 * 
 * <p>Note that if multiple filesets are specified, then each fileset
 * will be purged individually.  For example, if the directors "logs" and "output"
 * are specified to be purged keeping just the most recent 5 files, then
 * the logs folder will be purged (keeping just 5 files) and then the 
 * output folder will be purged (also keeping just 5 files).  In total 10
 * files will be left.</p>
 * 
 * @author Richard Dallaway <a href="mailto:richard@dallaway.com">richard@dallaway.com</a>
 * @version $Revision$ $Date$
 */
public class Purge extends Task implements Comparator
{

	// List of all the filesets to process.
	private final List filesets = new ArrayList();
	
	// The number of files to keep (per fileset); default is 10. 
	private int numToKeep = 10;

	/* 
	 * Flag to allow sanity checking: when true actions are reported but not carried out
	 * (no files are deleted). Default is false, meaning that files are deleted. 
	 */
	private boolean isTest = false;

	/**
	 * @param set a fileset to add to the list to purge.
	 */
	public void addConfiguredFileset(final FileSet set)
	{
		filesets.add(set);
	}

	/**
	 * @param numToKeep the number of files to keep per fileset.
	 */
	public void setKeep(final int numToKeep)
	{
		this.numToKeep = numToKeep;	
	}
	
	/**
	 * @param isTestMode true if we just show the actions we would
	 * 						take (no files deleted).  Default is false.
	 */
	public void setTest(final boolean isTestMode)
	{
		this.isTest = isTestMode;	
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute() throws BuildException
	{
		for (int i=0, n=filesets.size(); i<n; i++)
		{
			FileSet fs = (FileSet)filesets.get(i);
			purge(fs);
		}

	}

	/**
	 * Purge a given fileset by deleting all but the most 
     * recent <code>numToKeep</code> files.
	 * 
	 * @param fs the fileset to purge.
	 */
	private void purge(final FileSet fs) 
	{
		File dir = fs.getDir(project);
		log("Purging "+dir.getAbsolutePath(), Project.MSG_INFO);

		// All the files in the fileset to process:
		List files = new ArrayList();

		DirectoryScanner ds = fs.getDirectoryScanner(project);
		String[] names = ds.getIncludedFiles();

		for(int f=0, numFiles=names.length; f<numFiles; f++)
		{
			File candidate = new File(dir, names[f]);
			if (candidate.isDirectory() == false)
			{
				// We don't delete directories because it's a pain when the "log" folder
				// you check suddenly vanishes during a purge.
				files.add(candidate);	
			}
		}
				
		// If we have less than numToKeep files, there's nothing to do:
		if (files.size() <= numToKeep)
		{
			return;	
		}

		// Sort by modification timestamp:
		Collections.sort(files, this);

		// At this point the files[0 .. numToKeep-1] are the files to keep.  Any
		// other files can be deleted.

		if (isTest)
		{
			// Show the files we'd keep:
			for(int i=0; i<numToKeep; i++)
			{
				log("Would keep "+((File)files.get(i)).getAbsolutePath(), Project.MSG_INFO);
			}
		}

		// Delete all but the most recent numToKeep:
		for(int i=numToKeep, n=files.size(); i<n; i++)
		{
			File f = (File)files.get(i);
			if (isTest)
			{
				log("Would delete "+f.getAbsolutePath(), Project.MSG_INFO);
			}
			else
			{
				// Really delete the file:	
				log("Deleting "+f.getAbsolutePath(), Project.MSG_INFO);
				if (f.delete()	== false)
				{
					log("Failed to delete "+f.getAbsoluteFile(), Project.MSG_ERR);
				}	
			}
		}
		
	}


	
	/**
	 * Order two files such that the most recently modified file
	 * will appear at the start of the list of files.
	 * 
	 * {@inheritDoc}
	 */
	public int compare(Object o1, Object o2)
	{
		
		long f1 = ((File)o1).lastModified();
		long f2 = ((File)o2).lastModified();
		
		if (f1 > f2)
		{
			return -1;
		}
		else if (f1 < f2)
		{
			return 1;	
		}
		else
		{
			return 0;
		}	
		
	}

}