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
 * Filter that manages a Anteros SQLSession for a request.
 * <p>
 * This filter should be used if your
 * <tt>current_session_context_class</tt> configuration is set to
 * <tt>thread</tt> and you are not using JTA or CMT.
 * <p>
 * With JTA you'd replace transaction demarcation with calls to the
 * <tt>UserTransaction</tt> API. With CMT you would remove transaction
 * demarcation code from this filter.
 * <p>
 * An alternative, more flexible solution is
 * <tt>SessionTransactionInterceptor</tt> that can be applied to any pointcut
 * with JBoss AOP.
 * <p>
 * Note that you should not use this interceptor out-of-the-box with enabled
 * optimistic concurrency control. Apply your own compensation logic for failed
 * conversations, this is totally dependent on your applications design.
 *
 * @see auction.persistence.SessionTransactionInterceptor
 *
 * @author Christian Bauer
 */
public class AnterosSQLSessionRequestFilter implements Filter {

	private static Logger log = LoggerProvider.getInstance().getLogger(AnterosSQLSessionRequestFilter.class.getName());

	private SQLSessionFactory sf;

	public void doFilter(ServletRequest request,
			ServletResponse response,
			FilterChain chain)
			throws IOException, ServletException {

		try {
			log.debug("Starting a database transaction");
			sf.getCurrentSession().getTransaction().begin();

			chain.doFilter(request, response);

			log.debug("Committing the database transaction");
			sf.getCurrentSession().getTransaction().commit();

		} catch (Throwable ex) {
			ex.printStackTrace();
			try {
				if (sf.getCurrentSession().getTransaction().isActive()) {
					log.debug("Trying to rollback database transaction after exception");
					sf.getCurrentSession().getTransaction().rollback();
				}
			} catch (Throwable rbEx) {
				log.error("Could not rollback transaction after exception!", rbEx);
			}

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