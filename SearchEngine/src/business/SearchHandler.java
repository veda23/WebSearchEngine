package business;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Search
 */
@WebServlet("/SearchHandler")
public class SearchHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at:
		// ").append(request.getContextPath());
		String servletRootPath = this.getServletContext().getRealPath(".");

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String searchQuery = request.getParameter("k");
		if (searchQuery != null && searchQuery != "") {
//			ArrayList<Result> results = new ArrayList<Result>();
//			results.add(new Result(searchQuery, (new Date()).toString()));
//			results.add(new Result("http://www.ics.uci.edu", "UCI ICS Home Page"));
//			results.add(new Result("http://www.ics.uci.edu/~lopes", "Crista Lopes' Page"));
//			results.add(new Result("http://www.uci.edu", "UCI Home Page"));
//			results.add(new Result("http://www.ics.uci.edu/~lopes/teaching/cs221W16", "Information Retrieval Page"));
			List<Result> results = (new Search(servletRootPath)).getSearchResultsImproved(searchQuery);
			System.out.println("Reached here");
			request.setAttribute("results", results);
			RequestDispatcher view = request.getRequestDispatcher("/Search.jsp");
			view.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
