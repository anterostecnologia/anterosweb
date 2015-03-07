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
package br.com.anteros.anteros.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.configuration.AnterosPersistenceConfiguration;

public class AnterosSQLSessionFactoryContextListener implements ServletContextListener {

	public static String ANTEROS_SESSIONFACTORY_KEY = "sessionFactory";

	public static String ANTEROS_CONFIG_LOCATION = "anterosConfigLocation";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("######## INICIOU CONTEXTO ###########");
		String configLocation = sce.getServletContext().getInitParameter(ANTEROS_CONFIG_LOCATION);
		System.out.println(configLocation);
		System.out.println(this.getClass().getResource(configLocation));

		try {
			SQLSessionFactory sessionFactory = new AnterosPersistenceConfiguration().configure(configLocation)
					.buildSessionFactory();
			sce.getServletContext().setAttribute(ANTEROS_SESSIONFACTORY_KEY, sessionFactory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("######## TERMINOU CONTEXTO ###########");
	}

}
