package com.kunteng.cyria.dashboard.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunteng.cyria.dashboard.client.AuthServiceClient;
import com.kunteng.cyria.dashboard.utils.CommonResult;
import com.kunteng.cyria.dashboard.utils.RequestWrapper;

//@Component
@WebFilter(urlPatterns= {"/bashboards/*","/templates/*","/publish/*","/user/*"}, filterName="HTTPBearerAuthorizeAttribute")
public class HTTPBearerAuthorizeAttribute implements  Filter {
	@Autowired
	private AuthServiceClient client;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.getServletContext());
		System.out.println("filter init!");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("doFilter!");
		// TODO Auto-generated method stub
		CommonResult resultMsg;
		System.out.println("11");
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setCharacterEncoding("UTF-8");
		httpResponse.setContentType("application/json;charset=utf-8");
		httpResponse.setHeader("Access-Control-Allow-Origin", "*");
		httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
		httpResponse.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PATCH,PUT");
		httpResponse.setHeader("Access-Control-Max-Age", "3600");
		httpResponse.setHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,x-requested-with,X-Custom-Header,Content-Type,Accept,Authorization");
		System.out.println("22");
		HttpServletRequest httpRequest = (HttpServletRequest)request;

		String path = httpRequest.getServletPath();
		
		RequestWrapper wrapper = new RequestWrapper(httpRequest);
		
		System.out.printf("path: %s, body: %s\n", path, wrapper.getBody());
		if(path.equalsIgnoreCase("/user/login")) {
			chain.doFilter(wrapper, response);
			return;
		}
		String method = httpRequest.getMethod();
		if("OPTIONS".equalsIgnoreCase(method)) {
			httpResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
		}
		String auth = httpRequest.getHeader("Authorization");
		System.out.println("auth===="+auth);
		System.out.println("auth.length="+auth.length());
		if(auth != null && auth.length() > 6) {
			String HeadStr = auth.substring(0,5).toLowerCase();
			if(HeadStr.compareTo("cyria") == 0) {
				auth = auth.substring(6,auth.length());
				System.out.println("token===="+ auth);
				String tokenState = client.getJWTState(auth);
				System.out.println("tokenState===="+ tokenState);
				if(tokenState.equals("intime")) {
					chain.doFilter(request, response);
			 		return;
				}else if(tokenState.equals("refresh")) {
					String token = client.refreshToken(auth);
					if(token != null) {
						httpResponse.setHeader("Authorization", token);
						return;
					}
				}
			}
		}
		System.out.println("auth end here!");
		httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		
		ObjectMapper mapper = new ObjectMapper();
		resultMsg = new CommonResult().unauthorized("认证超时");
		httpResponse.getWriter().write(mapper.writeValueAsString(resultMsg));
		return;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
