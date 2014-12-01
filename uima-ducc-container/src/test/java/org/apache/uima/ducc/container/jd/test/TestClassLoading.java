/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.apache.uima.ducc.container.jd.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.uima.ducc.common.container.FlagsHelper;
import org.apache.uima.ducc.container.common.classloader.PrivateClassLoader;
import org.apache.uima.ducc.container.dd.classload.ProxyDDGenerate;
import org.apache.uima.ducc.container.jd.classload.ProxyJobDriverCollectionReader;
import org.apache.uima.ducc.container.jd.classload.ProxyJobDriverDirective;
import org.apache.uima.ducc.container.jd.classload.ProxyJobDriverErrorHandler;
import org.apache.uima.ducc.container.jd.test.helper.Utilities;
import org.apache.uima.ducc.container.net.impl.MetaCas;
import org.junit.Test;

public class TestClassLoading extends ATest {

	@Test
	public void test_01() {
		if(isDisabled(this.getClass().getName())) {
			return;
		}
		try {
			String userClasspath = Utilities.getInstance().getUserCP();
			System.setProperty(FlagsHelper.Name.UserClasspath.pname(), userClasspath);
			ProxyJobDriverErrorHandler pjdeh = new ProxyJobDriverErrorHandler();
			Object serializedCAS = null;
			Object exception = null;
			pjdeh.handle(serializedCAS, exception);
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}

	@Test
	public void test_02() {
		if(isDisabled(this.getClass().getName())) {
			return;
		}
		try {
			String userClasspath = Utilities.getInstance().getUserCP();
			System.setProperty(FlagsHelper.Name.UserClasspath.pname(), userClasspath);
			URL urlXml = this.getClass().getResource("/CR100.xml");
			File file = new File(urlXml.getFile());
			String crXml = file.getAbsolutePath();
			System.setProperty(FlagsHelper.Name.CollectionReaderXml.pname(), crXml);
			ProxyJobDriverCollectionReader pjdcr = new ProxyJobDriverCollectionReader();
			MetaCas mc = pjdcr.getMetaCas();
			Object serializedCAS = mc.getSerializedCas();
			Object exception = null;
			ProxyJobDriverErrorHandler pjdeh = new ProxyJobDriverErrorHandler();
			pjdeh.handle(serializedCAS, exception);
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
	
	@Test
	public void test_03() {
		if(isDisabled(this.getClass().getName())) {
			return;
		}
		try {
			String userClasspath = Utilities.getInstance().getUserCP();
			System.setProperty(FlagsHelper.Name.UserClasspath.pname(), userClasspath);
			URL urlXml = this.getClass().getResource("/CR100.xml");
			File file = new File(urlXml.getFile());
			String crXml = file.getAbsolutePath();
			System.setProperty(FlagsHelper.Name.CollectionReaderXml.pname(), crXml);
			ProxyJobDriverCollectionReader pjdcr = new ProxyJobDriverCollectionReader();
			MetaCas mc = pjdcr.getMetaCas();
			Object serializedCAS = mc.getSerializedCas();
			Object exception = null;
			String className = "org.apache.uima.ducc.user.jd.test.helper.TestJdContainerErrorHandler";
			System.setProperty(FlagsHelper.Name.UserErrorHandlerClassname.pname(), className);
			ProxyJobDriverErrorHandler pjdeh = new ProxyJobDriverErrorHandler();
			ProxyJobDriverDirective directive = pjdeh.handle(serializedCAS, exception);
			assertTrue(directive.isKillJob() == true);
			assertTrue(directive.isKillProcess() == true);
			assertTrue(directive.isKillWorkItem() == false);
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
	
	private void delete(File directory) {
		try {
			for(File file : directory.listFiles()) {
				debug("delete: "+file.getName());
				file.delete();
			}
			debug("delete: "+directory.getName());
			directory.delete();
		}
		catch(Exception e) {
			//e.printStackTrace();
		}
	}
	
	@Test
	public void test_04() {
		if(isDisabled(this.getClass().getName())) {
			return;
		}
		try {
			String userClasspath = Utilities.getInstance().getUserCP();
			System.setProperty(FlagsHelper.Name.UserClasspath.pname(), userClasspath);
			ProxyDDGenerate pddGenerate = new ProxyDDGenerate();
			//
			URL url = this.getClass().getResource("/");
			File root = new File(url.getFile());
			String name = root.getAbsolutePath();
			debug(name);
			assertTrue(root.isDirectory());
			String nameWorking = name+File.separator+"working";
			File working = new File(nameWorking);
			delete(working);
			working.mkdir();
			//
			String directory = working.getAbsolutePath();
			String id = "12345";
			String ddName = "name";
			String ddDescription = "description";
			Integer ddThreadCount = new Integer(1);
			String ddBrokerURL = "brokerURL";
			String ddEndpoint = "endpoint";
			String cmDescriptor = null;
			List<String> cmOverrides = null;
			//String aeDescriptor = "org.apache.uima.ducc.test.randomsleep.FixedSleepAE";
			String aeDescriptor = "FixedSleepAE";
			List<String> aeOverrides = null;
			String ccDescriptor = null;
			List<String> ccOverrides = null;
			String dd = pddGenerate.generate(
					directory, 
					id, 
					ddName, 
					ddDescription,
					ddThreadCount,
					ddBrokerURL, 
					ddEndpoint, 
					cmDescriptor, 
					cmOverrides, 
					aeDescriptor, 
					aeOverrides, 
					ccDescriptor, 
					ccOverrides
					);
			debug(dd);
			//
			delete(working);
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
	
  @Test
  public void test_loader() throws URISyntaxException, IOException {

    // First set up a private class-loaded and verify that its resources exist 
    // and are not also in the system class-loader.
    String privateCP = "../uima-ducc-sm/target/classes";
    String privateClass = "org.apache.uima.ducc.sm.PingDriver";
    String publicClass = "org.apache.uima.ducc.container.common.Util";
    
    URLClassLoader scl = (URLClassLoader) ClassLoader.getSystemClassLoader();
    URLClassLoader pcl = PrivateClassLoader.create(privateCP);
    for (URL u : pcl.getURLs()) {
      if (!(new File(u.getPath())).exists()) {
        fail("Missing test classpath resource: " + u);
      }
      URI ur = u.toURI().normalize();
      for (URL uu : scl.getURLs()) {
        if (ur.equals(uu.toURI().normalize())) {
          fail("Test classpath resource: " + u + " is also in the system class-loader");
        }
      }
    }
    
    // Check that a private class can only be loaded from the private class-loader
    // i.e. no leakage from private to system
    try {
      scl.loadClass(privateClass);
      fail("Found private class in system class-loader");
    } catch (ClassNotFoundException e) {
    }
    try {
      pcl.loadClass(privateClass);
    } catch (ClassNotFoundException e) {
      fail("Cannot load private class");
    }

    // Check that a public class can only be loaded from the system class-loader
    // i.e. no leakage from public to private
    try {
      pcl.loadClass(publicClass);
      fail("Found public class in private class-loader");
    } catch (ClassNotFoundException e) {
    }
    try {
      scl.loadClass(publicClass);
    } catch (ClassNotFoundException e) {
      fail("Cannot load public class");
    }
    // pcl.close();   // Requires Java 1.7
  }
}