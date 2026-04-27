package syb.moviepedia.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey secretKey;
    private static final Long accessTokenExpiresIn;
    private static final Long refreshTokenExpiresIn;
    // TODO: 리프레쉬 토큰 스케쥴링 추가하기
    static  {
        String secretKeyString = "dkssudgktpdywjsmstjddbsqjadlqslek";
        secretKey = new SecretKeySpec(secretKeyString.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());

        accessTokenExpiresIn = 3600L * 1000; // 1시간
        refreshTokenExpiresIn = 604800L * 1000; // 7일
    }

    // JWT(Access/Refresh) 생성
    public static String createJWT(String loginId, String role, Boolean isAccess) {

        long now = System.currentTimeMillis();
        long expiry = isAccess ? accessTokenExpiresIn : refreshTokenExpiresIn;
        String type = isAccess ? "access" : "refresh";

        return Jwts.builder()
                .claim("sub", loginId)
                .claim("role", role)
                .claim("type", type) // 액세스, 리프레쉬인지 여부
                .issuedAt(new Date(now)) // jwt가 언제 발급되었는지.
                .expiration(new Date(now + expiry))
                .signWith(secretKey)
                .compact();
    }

    // JWT 클레임 loginId 파싱
    public static String getLoginId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("sub", String.class);
    }

    // JWT 클레임 role 파싱
    public static String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // 서버에서 JWT를 받으면 우리가 만든 JWT인지 검증 - 유효 여부 (위조, 시간, Access/Refresh 여부)
    public static Boolean validateToken(String token, Boolean isAccess) {
        try { // try-catch 묶는 이유는 시간이 만료되었다면 예외가 던져지기때문.
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token) // 여기서 만료되었는지 검증하기 때문에 만료시간을 별도로 검증할 필요 없다.
                    .getPayload();

            String type = claims.get("type", String.class);
            if (type == null) return false;

            if (isAccess && !type.equals("access")) return false;
            if (!isAccess && !type.equals("refresh")) return false;

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
