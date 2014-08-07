package br.com.anteros.anteros.web.listener;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import br.com.anteros.core.configuration.exception.AnterosConfigurationException;
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
