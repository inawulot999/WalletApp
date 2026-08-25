package com.inawulot.wallet.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inawulot.wallet.domain.WalletUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
@Service public class JwtService {
 private final ObjectMapper mapper; private final byte[] secret; private final String issuer; private final long ttl;
 public JwtService(ObjectMapper mapper,@Value("${app.security.jwt-secret}") String secret,@Value("${app.security.jwt-issuer}") String issuer,@Value("${app.security.jwt-ttl-minutes}") long ttl){if(secret.length()<32)throw new IllegalStateException("WALLET_JWT_SECRET must be at least 32 characters");this.mapper=mapper;this.secret=secret.getBytes(StandardCharsets.UTF_8);this.issuer=issuer;this.ttl=ttl;}
 public Token issue(WalletUser user){try{Instant now=Instant.now(),expires=now.plus(ttl,java.time.temporal.ChronoUnit.MINUTES);String h=enc(Map.of("alg","HS256","typ","JWT"));String p=enc(Map.of("sub",user.getId().toString(),"iss",issuer,"iat",now.getEpochSecond(),"exp",expires.getEpochSecond()));String unsigned=h+"."+p;return new Token(unsigned+"."+sign(unsigned),expires);}catch(Exception e){throw new IllegalStateException("Unable to issue token",e);}}
 public UUID subject(String token){try{String[] p=token.split("\\.");if(p.length!=3||!MessageDigest.isEqual(sign(p[0]+"."+p[1]).getBytes(StandardCharsets.US_ASCII),p[2].getBytes(StandardCharsets.US_ASCII)))throw new IllegalArgumentException();Map<String,Object> c=mapper.readValue(Base64.getUrlDecoder().decode(p[1]),new TypeReference<>(){});if(!issuer.equals(c.get("iss"))||((Number)c.get("exp")).longValue()<=Instant.now().getEpochSecond())throw new IllegalArgumentException();return UUID.fromString((String)c.get("sub"));}catch(Exception e){throw new IllegalArgumentException("Invalid access token",e);}}
 private String enc(Map<String,?> v)throws Exception{return Base64.getUrlEncoder().withoutPadding().encodeToString(mapper.writeValueAsBytes(v));} private String sign(String value)throws Exception{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret,"HmacSHA256"));return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.US_ASCII)));} public record Token(String value,Instant expiresAt){}
}
