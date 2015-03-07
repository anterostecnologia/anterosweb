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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import br.com.anteros.anteros.web.listener.AnterosSQLSessionFactoryContextListener;
import br.com.anteros.core.log.Logger;
import br.com.anteros.core.log.LoggerProvider;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.context.ManagedSQLSessionContext;

/**
 * Filtro que gerencia uma SQLSession do Anteros durante uma conversação.
 * 
 * @author Christian Bauer 
 * @author modified by Edson Martins edsonmartins2005@gmail.com
 */
public class AnterosSQLSessionConversationFilter
		implements Filter {

	private static Logger log = LoggerProvider.getInstance().getLogger(
			AnterosSQLSessionConversationFilter.class.getName());

	private SQLSessionFactory sf;

	public static final String ANTEROS_SESSION_KEY = "anterosSQLSession";
	public static final String END_OF_CONVERSATION_FLAG = "endOfConversation";

	public void doFilter(ServletRequest request,
			ServletResponse response,
			FilterChain chain)
			throws IOException, ServletException {

		SQLSession currentSession;

		/*
		 * Tentando obter a sessão do HttpSession
		 */
		HttpSession httpSession =
				((HttpServletRequest) request).getSession();
		SQLSession disconnectedSession =
				(SQLSession) httpSession.getAttribute(ANTEROS_SESSION_KEY);

		try {

			if (disconnectedSession == null) {
				log.debug(">>> Nova conversação");
				log.debug("Abrindo sessão, desabilitando flush automático.");
				currentSession = sf.openSession();
			} else {
				log.debug("< Continuando conversação.");
				currentSession = disconnectedSession;
			}

			log.debug("Vinculando Sessão corrente.");
			ManagedSQLSessionContext.bind(currentSession);

			log.debug("Iniciando a transação no banco de dados.");
			currentSession.getTransaction().begin();

			log.debug("Processando o filtro");
			chain.doFilter(request, response);

			log.debug("Desvinculando a Sessão após o processamento.");
			currentSession = ManagedSQLSessionContext.unbind(sf);

			if (request.getAttribute(END_OF_CONVERSATION_FLAG) != null ||
					request.getParameter(END_OF_CONVERSATION_FLAG) != null) {

				log.debug("Executando flush na Sessão");
				currentSession.flush();

				log.debug("Executando commit no banco de dados.");
				currentSession.getTransaction().commit();

				log.debug("Fechando a Sessão.");
				currentSession.close();

				log.debug("Limpando atributo Sessão da HttpSession");
				httpSession.setAttribute(ANTEROS_SESSION_KEY, null);

				log.debug("<<< Fim da conversação");

			} else {

				log.debug("Executando commit no banco de dados.");
				currentSession.getTransaction().commit();

				log.debug("Armazenando Sessão na HttpSession");
				httpSession.setAttribute(ANTEROS_SESSION_KEY, currentSession);

				log.debug("> Retornando ao usuário");
			}

		} catch (Throwable ex) {
			try {
				if (sf.getCurrentSession().getTransaction().isActive()) {
					log.debug("Tentando reverter transação após exceção.");
					sf.getCurrentSession().getTransaction().rollback();
				}
			} catch (Throwable rbEx) {
				log.error("Não foi possível reverter a transação após a exceção!", rbEx);
			} finally {
				log.error("Limpeza após exceção!");
				log.debug("Desvinculando a Sessão após o exceção.");
				currentSession = ManagedSQLSessionContext.unbind(sf);

				log.debug("Fechando Sessão após a exceção.");
				try {
					currentSession.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				log.debug("Removing Session from HttpSession");
				httpSession.setAttribute(ANTEROS_SESSION_KEY, null);

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
