package com.basic.myrestapi.common.filter;

import com.basic.myrestapi.accounts.Account;
import com.basic.myrestapi.accounts.AccountAdapter;
import com.basic.myrestapi.accounts.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// CustomAuthorizationFilter Access Token을 검증하는 역할
public class CustomAuthorizationFilter extends BasicAuthenticationFilter {
    private AccountService accountService;
    private final String TOKEN_PREFIX = "Bearer";
    private Environment env;

    public CustomAuthorizationFilter(AuthenticationManager authenticationManager,
                                     AccountService accountService,
                                     Environment env) {
        super(authenticationManager);
        this.accountService = accountService;
        this.env = env;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("request.getServletPath() + \" \" + request.getMethod() = " + request.getServletPath() + " "
                + request.getMethod());
        if (request.getServletPath().equals("/login")) {    // 로그인은 그냥 건너 뛴다
            System.out.println("인증처리 않하기");
            filterChain.doFilter(request, response);
        }

        //1. 요청 헤더에서 인증 값을 가져온다.
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        //2. 인증 토큰이 존재하고, 그 값이 Bearer 토큰이면, 토큰을 decode 한다.
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            System.out.println("==> Token 있으면 인증처리 하기");
            String token = authorizationHeader.substring(TOKEN_PREFIX.length());
            System.out.println("token = " + token);

            String tokenSubject = getJwtSubject(token);
            if (tokenSubject == null) {
                error401Print(response, "JWT token is not valid");
            } else {
                //SecurityContextHolder.getContext().setAuthentication(token);
                Account account = accountService.getAccountByEmail(tokenSubject);
                AccountAdapter accountAdapter = new AccountAdapter(account);

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                accountAdapter, //나중에 컨트롤러에서 DI해서 쓸 때 사용하기 편함.
                                null, // 패스워드는 모르니까 null 처리, 어차피 지금 인증하는게 아니니까!!
                                accountAdapter.getAuthorities());

                // 강제로 시큐리티의 세션에 접근하여 값 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }
        } else {
            if ((request.getMethod().equals("GET") & request.getServletPath().contains("/api"))) {
                System.out.println("==> Token 없고, GET이면 Pass");
                filterChain.doFilter(request, response);
            }
            if (!(request.getMethod().equals("GET"))) {
                System.out.println("==> Token 없고, GET이 아니면 에러");
                error401Print(response, "JWT token 인증토큰이 필요합니다.");
            }
        }
    }

    private void error401Print(HttpServletResponse response, String errMsg) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", HttpStatus.UNAUTHORIZED.value());
        body.put("error", errMsg);

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

    private String getJwtSubject(String jwt) {
        System.out.println("jwt = " + jwt);
        String subject = null;

        try {
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();
            System.out.println("subject =" + subject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return subject;
    }

}