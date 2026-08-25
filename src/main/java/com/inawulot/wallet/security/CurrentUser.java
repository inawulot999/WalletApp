package com.inawulot.wallet.security;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component public class CurrentUser {
 public UUID id(Authentication auth){try{return UUID.fromString(auth.getName());}catch(Exception e){throw new AccessDeniedException("Authentication is required");}}
 public void require(UUID requested,Authentication auth){if(!id(auth).equals(requested))throw new AccessDeniedException("You are not allowed to access this resource");}
}
