package cn.magicvector.common.application.filter;

import com.github.tbwork.anole.loader.Anole;
import cn.magicvector.common.basic.util.S;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CORSFilter implements Filter {

    // This is to be replaced with a list of domains allowed to access the server
    //You can include more than one origin here
    private List<String> allowedOrigins = null; 

    private String originListString = "*";
    
    public CORSFilter() {
    	allowedOrigins = new ArrayList<String>();
    	String allowDomainString = Anole.getProperty("spring.boot.tomcat.allow.domains");
    	if(allowDomainString != null && !allowDomainString.isEmpty()) {
    		String [] domains = allowDomainString.split(",");
    		for(String domain : domains) {
    			allowedOrigins.add(domain);
    		}
    	}
    	if(!allowedOrigins.isEmpty()) {
            originListString = S.join(",", allowedOrigins);
        }

    }
    
    public void destroy() {

    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // Lets make sure that we are working with HTTP (that is, against HttpServletRequest and HttpServletResponse objects)
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            // Access-Control-Allow-Origin
            String origin = request.getHeader("Origin");
            response.setHeader("Access-Control-Allow-Origin", allowedOrigins.contains(origin) ? origin : originListString);
            response.setHeader("Vary", "Origin");

            // Access-Control-Max-Age
            response.setHeader("Access-Control-Max-Age", "3600");

            // Access-Control-Allow-Credentials
            response.setHeader("Access-Control-Allow-Credentials", "true");

            // Access-Control-Allow-Methods
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");

            // Access-Control-Allow-Headers
            response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, " + "X-CSRF-TOKEN");
        }

        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {
    	
    }
}