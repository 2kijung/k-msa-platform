package com.kmsa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * auth-service 진입점 (독립 실행 프로세스)
 * ============================================================================
 * [무엇을] 이 main()이 auth-service 하나를 독립 프로세스로 띄운다.
 * [왜/MSA] MSA의 2원칙 = "독립 프로세스". monolith와 별개의 main()을 가진다는 것은
 *          이 서비스가 자기 JVM에서 혼자 뜨고, 혼자 죽고, 혼자 재시작된다는 뜻이다.
 *          → 컨테이너 1개 = 프로세스 1개 = 서비스 1개.
 * [어떻게] @SpringBootApplication 하나로 컴포넌트 스캔 + 자동설정 + 설정 로딩을 켠다.
 *          (K-portfolio가 "Boot의 Auto Configuration이 뭘 자동으로 해주는지"를
 *           궁금해했던 그 자동설정이 바로 이 애노테이션에서 동작한다.)
 * ============================================================================
 */
@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
