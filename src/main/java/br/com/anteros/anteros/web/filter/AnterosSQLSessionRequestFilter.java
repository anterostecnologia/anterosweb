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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import br.com.anteros.anteros.web.listener.AnterosSQLSessionFactoryContextListener;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.SQLSessionFactory;

/**
 * Filtro que gerencia uma SQLSession do Anteros durante uma requisição.
 * 
 * @author Christian Bauer 
 * @author modified by Edson Martins edsonmartins2005@gmail.com
 */
public class AnterosSQLSessionRequestFilter implements Filter {

	private static Logger log = LoggerProvider.getInstance().getLogger(AnterosSQLSessionRequestFilter.class.getName());

	private SQLSessionFactory sf;

	public void doFilter(ServletRequest request,
			ServletResponse response,
			FilterChain chain)
			throws IOException, ServletException {

		try {
			log.debug("Iniciando a transação no banco de dados.");
			sf.getCurrentSession().getTransaction().begin();

			chain.doFilter(request, response);

			log.debug("Executando commit no banco de dados.");
			sf.getCurrentSession().getTransaction().commit();

		} catch (Throwable ex) {
			ex.printStackTrace();
			try {
				if (sf.getCurrentSession().getTransaction().isActive()) {
					log.debug("Tentando reverter transação após exceção.");
					sf.getCurrentSession().getTransaction().rollback();
				}
			} catch (Throwable rbEx) {
				log.error("Não foi possível reverter a transação após a exceção!", rbEx);
			}

			throw new ServletException(ex);
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Inicializando filtro...");
		sf = (SQLSessionFactory) filterConfig.getServletContext().getAttribute(AnterosSQLSessionFactoryContextListener.ANTEROS_SESSIONFACTORY_KEY);
	}

	public void destroy() {
	}

}