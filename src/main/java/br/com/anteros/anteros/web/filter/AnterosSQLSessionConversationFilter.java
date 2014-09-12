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
import br.com.anteros.persistence.metadata.annotation.type.FlushMode;
import br.com.anteros.persistence.session.SQLSession;
import br.com.anteros.persistence.session.SQLSessionFactory;
import br.com.anteros.persistence.session.context.ManagedSQLSessionContext;

/**
 * Filter that manages a Anteros SQLSession for a conversation.
 * <p>
 * This filter should be used if your
 * <tt>current_session_context_class</tt> configuration is set to
 * <tt>managed</tt> and you are not using JTA or CMT.
 * <p>
 * With JTA you'd replace transaction demarcation with calls to the
 * <tt>UserTransaction</tt> API. With CMT you would remove transaction
 * demarcation code from this filter.
 * <p>
 * Use this filter for the <b>session-per-conversation</b> pattern with an
 * extended <tt>Session</tt>. Don't forget to set conversation boundaries in
 * your code! One way to do this is to put a marker attribute in the request,
 * and let the filter handle flushing and closing of the Session at the end of a
 * conversation.
 * <p>
 * We recommend the JBoss Seam framework with built-in support for conversations
 * in all environments.
 * <p>
 * Note that you should not use this interceptor out-of-the-box with enabled
 * optimistic concurrency control. Apply your own compensation logic for failed
 * conversations, this is totally dependent on your applications design.
 *
 * @author Christian Bauer
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

		// Try to get a Anteros SQLSession from the HttpSession
		HttpSession httpSession =
				((HttpServletRequest) request).getSession();
		SQLSession disconnectedSession =
				(SQLSession) httpSession.getAttribute(ANTEROS_SESSION_KEY);

		try {

			// Start a new conversation or in the middle?
			if (disconnectedSession == null) {
				log.debug(">>> New conversation");
				log.debug("Opening Session, disabling automatic flushing");
				currentSession = sf.openSession();
			} else {
				log.debug("< Continuing conversation");
				currentSession = disconnectedSession;
			}

			log.debug("Binding the current Session");
			ManagedSQLSessionContext.bind(currentSession);

			log.debug("Starting a database transaction");
			currentSession.getTransaction().begin();

			log.debug("Processing the event");
			chain.doFilter(request, response);

			log.debug("Unbinding Session after processing");
			currentSession = ManagedSQLSessionContext.unbind(sf);

			// End or continue the long-running conversation?
			if (request.getAttribute(END_OF_CONVERSATION_FLAG) != null ||
					request.getParameter(END_OF_CONVERSATION_FLAG) != null) {

				log.debug("Flushing Session");
				currentSession.flush();

				log.debug("Committing the database transaction");
				currentSession.getTransaction().commit();

				log.debug("Closing the Session");
				currentSession.close();

				log.debug("Cleaning Session from HttpSession");
				httpSession.setAttribute(ANTEROS_SESSION_KEY, null);

				log.debug("<<< End of conversation");

			} else {

				log.debug("Committing database transaction");
				currentSession.getTransaction().commit();

				log.debug("Storing Session in the HttpSession");
				httpSession.setAttribute(ANTEROS_SESSION_KEY, currentSession);

				log.debug("> Returning to user in conversation");
			}

//		} catch (StaleObjectStateException staleEx) {
//			log.error("This interceptor does not implement optimistic concurrency control!");
//			log.error("Your application will not work until you add compensation actions!");
//			// Rollback, close everything, possibly compensate for any permanent
//			// changes
//			// during the conversation, and finally restart business
//			// conversation. Maybe
//			// give the user of the application a chance to merge some of his
//			// work with
//			// fresh data... what you do here depends on your applications
//			// design.
//			throw staleEx;
		} catch (Throwable ex) {
			// Rollback only
			try {
				if (sf.getCurrentSession().getTransaction().isActive()) {
					log.debug("Trying to rollback database transaction after exception");
					sf.getCurrentSession().getTransaction().rollback();
				}
			} catch (Throwable rbEx) {
				log.error("Could not rollback transaction after exception!", rbEx);
			} finally {
				log.error("Cleanup after exception!");

				// Cleanup
				log.debug("Unbinding Session after exception");
				currentSession = ManagedSQLSessionContext.unbind(sf);

				log.debug("Closing Session after exception");
				try {
					currentSession.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				log.debug("Removing Session from HttpSession");
				httpSession.setAttribute(ANTEROS_SESSION_KEY, null);

			}

			// Let others handle it... maybe another interceptor for exceptions?
			throw new ServletException(ex);
		}

	}

	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializing filter...");
		sf = (SQLSessionFactory) filterConfig.getServletContext().getAttribute(AnterosSQLSessionFactoryContextListener.ANTEROS_SESSIONFACTORY_KEY);
	}

	public void destroy() {
	}

}
