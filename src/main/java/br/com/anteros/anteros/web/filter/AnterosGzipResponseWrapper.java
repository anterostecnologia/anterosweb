/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.com.anteros.anteros.web.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author Edson Martins - Relevant Solutions
 *
 */
public class AnterosGzipResponseWrapper extends HttpServletResponseWrapper {
	
	protected HttpServletResponse origResponse = null;
	
	protected ServletOutputStream stream = null;
	
	protected PrintWriter writer = null;
	
	public AnterosGzipResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}
	
	public ServletOutputStream createOutputStream() throws IOException {
		return (new AnterosGzipResponseStream(origResponse));
	}
	
	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (stream != null) {
					stream.close();
				}
			}
		}
		catch (IOException e) {}
	}
	
	public void flushBuffer() throws IOException {
		stream.flush();
	}
	
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() já foi chamado!");
		}
		
		if (stream == null)
			stream = createOutputStream();
		return (stream);
	}
	
	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			return (writer);
		}
		
		if (stream != null) {
			throw new IllegalStateException("getOutputStream() já foi chamado!");
		}
		
		stream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
		return (writer);
	}
	
	
}
