package com.web.baebaeBE.presentation.kakao;

import com.web.baebaeBE.application.kakao.KakaoApplication;
import com.web.baebaeBE.presentation.kakao.dto.KakaoDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping()
public class KakaoController {

  private final KakaoApplication kakaoApplication;

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String clientId;
  @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
  private String clientSecret;


  @GetMapping("/api/oauth/kakao")
  public String login() {
    return "oauthLogin";
  }


  @GetMapping("/oauth/kakao/callback")
  public ResponseEntity<KakaoDto.Response> loginCallback(@RequestParam("code") String code) {
    KakaoDto.Response kakaoToken = kakaoApplication.loginCallback(code);
    return ResponseEntity.ok(kakaoToken);
  }


  @Operation(summary = "백엔드용 TEST API입니다.")
  @GetMapping("api/test")
  @ResponseBody
  public void test(HttpServletRequest request) {
    //System.out.println(request.getAttribute("id"));
    return;
  }

}
