package com.forum.authenticationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private final String key = "Forum";

    public String createToken(AuthUserDetail userDetails){
        System.out.println(userDetails.getUsername());
        //Claims is essentially a key-value pair, where the key is a string and the value is an object
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername()); // user identifier
        claims.put("permissions", userDetails.getAuthorities()); // user permission
        claims.put("userId", userDetails.getUserId()); // user id
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, key) // algorithm and key to sign the token
                .compact();
    }

    // resolves the token -> use the information in the token to create a userDetail object
    public Optional<AuthUserDetail> resolveToken(HttpServletRequest request){
        try {
            String prefixedToken = request.getHeader("Authorization"); // extract token value by key "Authorization"
            String token = prefixedToken.substring(7); // remove the prefix "Bearer "

            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody(); // decode
            Integer userId = (Integer) claims.get("userId");
            String username = claims.getSubject();
            List<LinkedHashMap<String, String>> permissions = (List<LinkedHashMap<String, String>>) claims.get("permissions");

            // convert the permission list to a list of GrantedAuthority
            List<GrantedAuthority> authorities = permissions.stream()
                    .map(p -> new SimpleGrantedAuthority(p.get("authority")))
                    .collect(Collectors.toList());

            //return a userDetail object with the permissions the user has
            return Optional.of(AuthUserDetail.builder()
                    .username(username)
                    .userId(userId)
                    .authorities(authorities)
                    .build());
        } catch (Exception e){
            return Optional.empty();
        }
    }

}
