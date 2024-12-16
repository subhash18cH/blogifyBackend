package com.subhash.backend.Oauth2;

import com.subhash.backend.model.Userr;
import com.subhash.backend.security.jwt.JwtUtils;
import com.subhash.backend.security.service.MyUserDetails;
import com.subhash.backend.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;

    @Value("${frontend.url}")
    private String frontendUrl;

    String username;
    String idAttributeKey;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken= (OAuth2AuthenticationToken) authentication;

        if("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()) || "google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())){

            DefaultOAuth2User principle=(DefaultOAuth2User) authentication.getPrincipal();
            Map<String,Object> attributes=principle.getAttributes();
            String name= attributes.getOrDefault("name","").toString();
            String email= attributes.getOrDefault("email","").toString();

            if("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())){
                username=attributes.getOrDefault("login","").toString();
                idAttributeKey="id";
            }else if("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())){
                username=email.split("@")[0];
                idAttributeKey="sub";
            }else{
                username="";
                idAttributeKey="id";
            }
            userService.findByEmail(email)
                    .ifPresentOrElse(userr -> {
                                DefaultOAuth2User oAuth2User = new DefaultOAuth2User(
                                        List.of(new SimpleGrantedAuthority("USER")),
                                        attributes,idAttributeKey);
                                Authentication securityAuth= new OAuth2AuthenticationToken(oAuth2User,List.of(new SimpleGrantedAuthority("USER")), oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                                SecurityContextHolder.getContext().setAuthentication(securityAuth);
                            },()->{
                                Userr newUser= new Userr();
                                newUser.setUserName(username);
                                newUser.setEmail(email);
                                userService.registerUser(newUser);

                                DefaultOAuth2User oAuth2User = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("USER")),attributes,idAttributeKey);
                                Authentication securityAuth= new OAuth2AuthenticationToken(oAuth2User,List.of(new SimpleGrantedAuthority("USER")), oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                                SecurityContextHolder.getContext().setAuthentication(securityAuth);
                            }
                    );
        }
        this.setAlwaysUseDefaultTargetUrl(true);

        DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");

        Userr user=userService.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));

        MyUserDetails userDetails=new MyUserDetails(user);
        String jwtToken=jwtUtils.generateToken(userDetails);

        String targetUrl= UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect").queryParam("token",jwtToken).build().toUriString();
        this.setDefaultTargetUrl(targetUrl);

        super.onAuthenticationSuccess(request,response,authentication);
    }
}

