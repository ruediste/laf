package sampleApp;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Login;

@WebServlet("/test")
public class TestServlet extends HttpServlet{

	private static final long serialVersionUID = -2306626660188818275L;

	@EJB
	TestBean testBean;

	@Inject
	UserTransaction ut;
	
	@PersistenceUnit
	EntityManagerFactory emf;
	
	
	private void read(){}
	private void readWrite(){}
	private void render(){}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// testBean.test();
		
		Map<String,String> props=new HashMap<>();
		props.put("","");
		EntityManager em=emf.createEntityManager();
		em.unwrap(DatabaseSessionImpl.class).getLogin().setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		
		try {
			ut.begin();
			em.joinTransaction();
			
			System.out.println("Isolation: "+em.unwrap(Connection.class).getTransactionIsolation());
			em.find(Issue.class, 1L);
			readWrite();
			render();
			ut.commit();
		} catch (NotSupportedException | SystemException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// create result
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("html");
		PrintWriter out = resp.getWriter();
		out.print("<html><head></head><body>Hello World"+testBean.load()+"</body></html>");
		out.close();
	}
}
