/*
 * Mpaxs, modular parallel execution system.
 * Copyright (C) 2010-2013, The authors of Mpaxs. All rights reserved.
 *
 * Project website: http://mpaxs.sf.net
 *
 * Mpaxs may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Mpaxs, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Mpaxs is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package net.sf.mpaxs.api.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.concurrent.ConfigurableRunnable;
import net.sf.mpaxs.api.concurrent.DefaultCallable;
import net.sf.mpaxs.api.concurrent.DefaultRunnable;

/**
 * Default implementation of a job.
 * A job can be created by directly passing a <code>Runnable</code> or <code>Callable</code>
 * to the appropriate constructors, or by providing a configuration file containing
 * the keys <code>STARTUP_CLASS</code> and <code>JAR_PATH</code> (file path to the jar containing
 * STARTUP_CLASS). The additional property <code>CODEBASE</code> allows to define a base
 * URL for the <code>URLClassLoader</code> to look for dependent jar files.
 *
 * @author Kai Bernd Stadermann
 * @param <T> the result type
 */
public class Job<T> implements IJob<T> {

	//configuration keys
	private static final String JAR_PATH = "JAR_PATH";
	private static final String STARTUP_CLASS = "STARTUP_CLASS";
	private static final String CONFIGURATION_FILE = "CONFIGURATION_FILE";
	private Map<String, String> config = new HashMap<String, String>();
	private ConfigurableRunnable<T> classToExecute;
	private UUID id;
	private String jobConfigFile = "";
	private Status status = Status.UNKNOWN;
	private int errorCounter = 0;
	private int priority = 0;

	/**
	 * The maximum priority = <code>Integer.MAX_VALUE</code>
	 */
	public static final int MAX_PRIORITY = Integer.MAX_VALUE;

	/**
	 * The minimum priority = <code>Integer.MIN_VALUE</code>
	 */
	public static final int MIN_PRIORITY = Integer.MIN_VALUE;
	private Throwable throwable = null;

	/**
	 * Create a new Job with priority 0, reading the contents of the supplied job configuration file.
	 *
	 * @param jobConfigFile the job configuration file
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public Job(final String jobConfigFile) throws ClassNotFoundException,
		MalformedURLException, InstantiationException, IllegalAccessException, IOException {
		this();
		setClassToExecute(jobConfigFile);
	}

	/**
	 * Create a new Job using the supplied instance for job execution.
	 *
	 * @param classToExecute the instance to run
	 */
	public Job(ConfigurableRunnable<T> classToExecute) {
		this();
		setClassToExecute(classToExecute);
	}

	/**
	 * Create a new Job using the supplied instance for job execution. The
	 * supplied returnObject is returned when the job finishes successfully.
	 *
	 * @param classToExecute the instance to run
	 * @param returnObject   the results to return on success
	 */
	public Job(Runnable classToExecute, T returnObject) {
		this();
		setClassToExecute(new DefaultRunnable<T>(classToExecute, returnObject));
	}

	/**
	 * Create a new Job using the supplied instance for job execution. The
	 * supplied instance is queried for its return value when the job finishes successfully.
	 *
	 * @param classToExecute the instance to run
	 */
	public Job(Callable<T> classToExecute) {
		this();
		setClassToExecute(new DefaultCallable<T>(classToExecute));
	}

	/**
	 * Default constructor. Creates a random unique id.
	 */
	public Job() {
		this.id = UUID.randomUUID();
	}

	private void addConfigFile(final String path) {
		Properties p = new Properties();

		FileInputStream fis;
		try {
			fis = new FileInputStream(path);
			p.load(fis);
			fis.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Job.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Job.class.getName()).log(Level.SEVERE, null, ex);
		}

		for (Object key : p.keySet()) {
			String s = (String) key;
			config.put(s, p.getProperty(s));
		}
	}

	private ConfigurableRunnable loadClass(final File JarFile, final String ClassToLoad)
		throws ClassNotFoundException, MalformedURLException, InstantiationException, IllegalAccessException, IOException {
		Set<URL> jarFiles = new LinkedHashSet<URL>();
		if (config.containsKey(ConfigurationKeys.KEY_CODEBASE)) {
			System.out.println("Using codebase " + config.get(ConfigurationKeys.KEY_CODEBASE));
			URL u = null;
			try {
				u = new URL(config.get(ConfigurationKeys.KEY_CODEBASE));
			} catch (MalformedURLException mex) {
				Logger.getLogger(Job.class.getName()).log(Level.WARNING, config.get(ConfigurationKeys.KEY_CODEBASE) + " is not a valid URL for codebase!", mex);
				File cb = new File(config.get(ConfigurationKeys.KEY_CODEBASE));
				if (cb.exists() && cb.isDirectory()) {
					Logger.getLogger(Job.class.getName()).log(Level.INFO, config.get(ConfigurationKeys.KEY_CODEBASE) + " is used as codebase directory!", mex);
					u = cb.toURI().toURL();
				}
			}
			if (u != null) {
				jarFiles.add(u);
			}
		}
		jarFiles.addAll(getDependentJars(JarFile));
		URL[] urls = new URL[jarFiles.size()];
		jarFiles.toArray(urls);
		Class<?> loadetClass = null;
		ConfigurableRunnable runFut = null;
		URLClassLoader loader = new URLClassLoader(urls);
		loadetClass = loader.loadClass(ClassToLoad);
		try {
			runFut = (ConfigurableRunnable) loadetClass.newInstance();
		} catch (ClassCastException cce) {
			throw new ClassNotFoundException();
		}
		return runFut;
	}

	private Set<URL> getDependentJars(final File startJarFile) throws IOException {
		Set<URL> ret = new LinkedHashSet<URL>();
		URL jar = new URL("jar:" + startJarFile.toURI() + "!/");
		ret.add(jar);
		JarURLConnection uc = (JarURLConnection) jar.openConnection();
		Attributes att = uc.getMainAttributes();
		if (att != null) {
			String classPath = att.getValue(Attributes.Name.CLASS_PATH);
			if (classPath != null && !classPath.isEmpty()) {
				String[] classPathArray = classPath.split(" ");
				for (int i = 0; i < classPathArray.length; i++) {
					File newJar = new File(startJarFile.getParent() + File.separator + classPathArray[i]);
					if (newJar.canRead()) {
						ret.addAll(getDependentJars(newJar));
					}
				}
			}
		}
		return ret;
	}

	@Override
	public ConfigurableRunnable<T> getClassToExecute() {
		return classToExecute;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public String getConfigurationFile() {
		if (config.containsKey(CONFIGURATION_FILE)) {
			return config.get(CONFIGURATION_FILE);
		} else {
			return "";
		}
	}

	@Override
	public String getJobConfigFile() {
		return jobConfigFile;
	}

	@Override
	public void setJobConfigFile(String jobConfigFile) {
		this.jobConfigFile = jobConfigFile;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public synchronized void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int getErrorCounter() {
		return errorCounter;
	}

	@Override
	public synchronized void errorOccurred() {
		errorCounter++;
	}

	@Override
	public final void setClassToExecute(final String jobConfigFile) throws ClassNotFoundException,
		MalformedURLException, InstantiationException, IllegalAccessException, IOException, IllegalStateException {
		if (!this.jobConfigFile.isEmpty() || classToExecute != null) {
			throw new IllegalStateException("Can not reassign job after first call to setClassToExecute!");
		}
		this.jobConfigFile = jobConfigFile;
		addConfigFile(jobConfigFile);
		classToExecute = loadClass(new File(config.get(JAR_PATH)),
			config.get(STARTUP_CLASS));
		if (!getConfigurationFile().isEmpty()) {
			File configureFile = new File(getConfigurationFile());
			if (configureFile.canRead()) {
				classToExecute.configure(configureFile);
			} else {
				throw new IOException("Configuration file " + configureFile.getAbsolutePath() + " not readable!");
			}

		}
	}

	@Override
	public final void setClassToExecute(ConfigurableRunnable<T> cr) throws IllegalStateException {
		if (classToExecute != null) {
			throw new IllegalStateException("Can not reassign job after first call to setClassToExecute!");
		}
		this.classToExecute = cr;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Job<T> other = (Job<T>) obj;
		return this.id == other.id || (this.id != null && this.id.equals(other.id));
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	@Override
	public synchronized void setThrowable(Throwable t) {
		this.throwable = t;
	}

	@Override
	public Throwable getThrowable() {
		return this.throwable;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public synchronized void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		return "Job{classToExecute=" + classToExecute + ", id=" + id + ", jobConfigFile=" + jobConfigFile + ", status=" + status + ", errorCounter=" + errorCounter + ", priority=" + priority + ", throwable=" + throwable + '}';
	}
}
