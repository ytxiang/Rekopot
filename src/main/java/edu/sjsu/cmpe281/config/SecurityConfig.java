package edu.sjsu.cmpe281.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import edu.sjsu.cmpe281.security.UrlSecurityInterceptor;
import edu.sjsu.cmpe281.security.UserDetailService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${rekopot.admin.account}")
    private String ADMIN_NAME;

    @Value("${rekopot.admin.password}")
    private String ADMIN_PASSWORD;

    @Autowired
    protected UserDetailService userDetailService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
	return configuration.getAuthenticationManager();
    }

    final static String[] WSC_DEFAULT_IGNORE = {"/**/*.js",
	    "/**/*.js.map", "/**/*.ts", "/**/*.css", "/**/*.css.map",
	    "/**/*.png", "/**/*.gif", "/**/*.jpg", "/**/*.fco",
	    "/**/*.woff", "/**/*.woff2", "/**/*.font", "/**/*.svg",
	    "/**/*.ttf","/*.ico", "/404", "/401","/403",
	    "/error", "/register", "/privacy/**",
	    "/signin/**", "/auth/**", "/signup/**"};

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.cors().disable();
	http.headers().disable();
	http.jee().disable();
	http.x509().disable();
	http.servletApi().disable();
	http.anonymous().disable();
	http.requestCache().disable();

	http.formLogin().loginProcessingUrl("/login").loginPage("/signon").defaultSuccessUrl("/")
		.successHandler(new AuthenticationSuccessHandler());
	http.logout().logoutSuccessHandler(new LogoutSuccessHandler());
	http.addFilterAt(urlSecurityInterceptor(), FilterSecurityInterceptor.class);
	http.exceptionHandling().authenticationEntryPoint(
		new MyAuthenticationEntryPoint()).accessDeniedHandler(new MyAccessDeniedHandler());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	provider.setPasswordEncoder(new BCryptPasswordEncoder());
	provider.setUserDetailsService(userDetailService);
	auth.authenticationProvider(provider);

	auth.inMemoryAuthentication()
	    .passwordEncoder(org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance())
	    .withUser(ADMIN_NAME)
	    .password(ADMIN_PASSWORD)
	    .authorities("ROLE_ADMIN");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
	    web.ignoring().antMatchers(WSC_DEFAULT_IGNORE);
    }

    public UrlSecurityInterceptor urlSecurityInterceptor() {
	return new UrlSecurityInterceptor();
    }

    protected boolean isAjax(HttpServletRequest request) {
	return StringUtils.isNotBlank(request.getHeader("X-Requested-With"));
    }


    private class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
					    HttpServletResponse response, Authentication authentication)
		throws ServletException, IOException {

	    clearAuthenticationAttributes(request);
	    if (!isAjax(request)) {
		super.onAuthenticationSuccess(request, response, authentication);
	    }
	}
    }

    private class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
				    HttpServletResponse response, Authentication authentication)
		throws IOException, ServletException {
	    if (!isAjax(request)) {
		super.onLogoutSuccess(request, response, authentication);
	    }
	}
    }

    private class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
			     HttpServletResponse response,
			     AuthenticationException authException) throws IOException {
	    if (isAjax(request)) {
		response.getWriter().println("Please first sign in");
	    } else {
		response.sendRedirect("/signon");
	    }

	}
    }

    private class MyAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
	    if (isAjax(request)) {
		response.getWriter().println("Permission not granted");
	    } else {
		response.sendRedirect("/403");
	    }
	}
    }

}
