/*
 * The MIT License
 * 
 * Copyright (c) 2007 The Codehaus
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.codehaus.mojo.native2ascii;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Converts files with characters in any supported character encoding to one with ASCII and/or Unicode escapes.
 * 
 * @goal native2ascii
 * @phase process-classes
 */
public class Native2AsciiMojo
    extends AbstractMojo
{
    /**
     * The directory to find files in (default is basedir)
     * @parameter default-value="${basedir}/src/main/native2ascii"
     */   
    protected File src;

    /**
     * The directory to output file to
     * @parameter default-value="${project.build.directory}/native2ascii"
     */   
    protected File dest;

    /**
     * The native encoding the files are in.
     * 
     * @parameter default-value="${project.build.sourceEncoding}"
     * @since 1.0-alpha-1
     */
    protected String encoding;

    /**
     * Patterns of files that must be included. Default is "**\/*.properties".
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    protected String[] includes;

    /**
     * Patterns of files that must be excluded.
     * 
     * @parameter
     * @since 1.0-alpha-2
     */
    protected String[] excludes;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !src.exists() )
        {
            return;
        }

        if ( StringUtils.isEmpty( encoding ) )
        {
            encoding = System.getProperty( "file.encoding" );
            getLog().warn( "Using platform encoding (" + encoding + " actually) to convert resources!" );
        }

        if ( includes == null || includes.length == 0 )
        {
            includes = new String[] { "**/*.properties" };
        }
        if ( excludes == null )
        {
            excludes = new String[0];
        }

        Iterator files;
        try
        {
            getLog().info( "Includes: " + Arrays.asList( includes ) );
            getLog().info( "Excludes: " + Arrays.asList( excludes ) );
            String incl = StringUtils.join( includes, "," );
            String excl = StringUtils.join( excludes, "," );
            files = FileUtils.getFiles( src, incl, excl ).iterator();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to get list of files" );
        }

        while ( files.hasNext() )
        {
            File srcFile = (File) files.next();
            getLog().info( "Processing " + srcFile.getAbsolutePath() );
            try
            {
                // Convert file in-place
            	File destFile = getDestFile(srcFile);
            	if (!dest.exists()) {
            		dest.mkdirs();
            	}
            	if (!destFile.exists()) {
            		destFile.createNewFile();
            	}
                Native2Ascii.nativeToAscii( srcFile, destFile, encoding );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to convert " + srcFile.getAbsolutePath(), e );
            }
        }
    }

	private File getDestFile(File srcFile) throws IOException {
		String file = srcFile.getAbsolutePath().substring(src.getAbsolutePath().length());
		File result = new File(dest, file);
		File dir = new File(result.getParent());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!result.exists()) {
			result.createNewFile();
		}
		return result;
	}
}
