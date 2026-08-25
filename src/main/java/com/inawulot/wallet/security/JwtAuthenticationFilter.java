package com.inawulot.wallet.security;
import com.inawulot.wallet.service.JwtService;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.List;
@Component public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwt; public JwtAuthenticationFilter(JwtService jwt){this.jwt=jwt;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{String h=req.getHeader("Authorization");if(h!=null&&h.startsWith("Bearer ")&&SecurityContextHolder.getContext().getAuthentication()==null){try{var auth=new UsernamePasswordAuthenticationToken(jwt.subject(h.substring(7)).toString(),null,List.of(new SimpleGrantedAuthority("ROLE_USER")));auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));SecurityContextHolder.getContext().setAuthentication(auth);}catch(IllegalArgumentException ignored){}}chain.doFilter(req,res);}
}
