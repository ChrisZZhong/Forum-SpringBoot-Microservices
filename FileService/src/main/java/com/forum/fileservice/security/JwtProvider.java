package com.forum.fileservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class JwtProvider {

    private final String key = "Forum";

    public Optional<AuthUserDetail> resolveToken(HttpServletRequest request){
        try {
            String prefixedToken = request.getHeader("Authorization"); // extract token value by key "Authorization"
            String token = prefixedToken.substring(7); // remove the prefix "Bearer "
            System.out.println(token);
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody(); // decode

            String username = claims.getSubject();
            List<LinkedHashMap<String, String>> permissions = (List<LinkedHashMap<String, String>>) claims.get("permissions");

            // convert the permission list to a list of GrantedAuthority
            List<GrantedAuthority> authorities = permissions.stream()
                    .map(p -> new SimpleGrantedAuthority(p.get("authority")))
                    .collect(Collectors.toList());

            //return a userDetail object with the permissions the user has
            return Optional.of(AuthUserDetail.builder()
                    .username(username)
                    .authorities(authorities)
                    .build());
        } catch (Exception e){
            return Optional.empty();
        }
    }
}
