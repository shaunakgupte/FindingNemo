/*
 * Copyright 2015 Shaunak Gupte <gupte.shaunak@gmail.com>
 *
 * This file is part of Finding Nemo.
 *
 * Finding Nemo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Finding Nemo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Finding Nemo. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This file is part of Picasa Photo Uploader.
 *
 * Picasa Photo Uploader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Picasa Photo Uploader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Picasa Photo Uploader. If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.FindingNemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

/**
 * Class to create MultiPart related body for use in uploading to Picasa
 *
 * @author Jan Peter Hooiveld
 */
public class Multipart
{
  /**
   * Defines the boundary used in the body for each part
   */
  private String boundary;

  private ByteArrayBuffer buffer = new ByteArrayBuffer(0);

  private static final String CR_LF = "\r\n";

  /**
   * Dashes for the boundary
   */
  private static final String DASHES = "--";

  public Multipart(String description, String boundary) throws UnsupportedEncodingException
  {
    this.boundary = CR_LF+DASHES+boundary;
    mergePart(description);
  }

  /**
   * Returns the boundary
   * 
   * @return Boundary
   */
  public String getBoundary()
  {
    return boundary.substring(4);
  }

  /**
   * Adds new string part to the body
   *
   * @param content The content of the part
   * @param contentType The content type of the part
   * @throws UnsupportedEncodingException
   */
  public void addPart(String content, String contentType) throws UnsupportedEncodingException
  {
    mergePart(createStart(contentType) + content);
  }

  public void addPart(File file, String contentType) throws IOException
  {
    mergePart(createStart(contentType));

    InputStream is = new FileInputStream(file);
    byte[] bytes   = new byte[(int)file.length()];
    int offset     = 0;
    int numRead;

    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    if (offset < bytes.length) {
      throw new IOException("Could not completely read file "+file.getName());
    }

    is.close();

    mergePart(bytes);
  }

  public byte[] getContent() throws UnsupportedEncodingException
  {
    ByteArrayBuffer endBuffer = new ByteArrayBuffer(0);
    byte[] start              = buffer.toByteArray();
    byte[] end                = (boundary+DASHES).getBytes(HTTP.DEFAULT_CONTENT_CHARSET);

    endBuffer.append(start, 0, start.length);
    endBuffer.append(end, 0, end.length);

    return endBuffer.toByteArray();
  }

  private String createStart(String contentType)
  {
    return boundary+CR_LF+"Content-Type: "+contentType+CR_LF+CR_LF;
  }

  private void mergePart(Object input) throws UnsupportedEncodingException
  {
    byte[] bytes = null;

    if (input instanceof String) {
      bytes = ((String)input).getBytes(HTTP.DEFAULT_CONTENT_CHARSET);
    } else if (input instanceof byte[]) {
      bytes = (byte[])input;
    }

    if (bytes != null) {
      buffer.append(bytes, 0, bytes.length);
    }
  }
}
