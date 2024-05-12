package com.web.baebaeBE.integration.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.baebaeBE.global.jwt.JwtTokenProvider;
import com.web.baebaeBE.domain.member.entity.Member;
import com.web.baebaeBE.domain.member.entity.MemberType;
import com.web.baebaeBE.domain.member.repository.MemberRepository;
import com.web.baebaeBE.infra.question.entity.Question;
import com.web.baebaeBE.infra.question.repository.QuestionRepository;
import com.web.baebaeBE.presentation.question.dto.QuestionCreateRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest()
@AutoConfigureMockMvc
@WithMockUser
@Transactional
public class QuestionTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Member testMember;
    private String refreshToken;

    @BeforeEach
    void setup() {
        testMember = memberRepository.save(Member.builder()
                .email("test@gmail.com")
                .nickname("장지효")
                .memberType(MemberType.KAKAO)
                .refreshToken("null")
                .build());

        refreshToken = tokenProvider.generateToken(testMember, Duration.ofDays(14)); // 임시 refreshToken 생성

        testMember.updateRefreshToken(refreshToken);
        memberRepository.save(testMember);
    }

    @AfterEach
    void tearDown() {
        Optional<Member> member = memberRepository.findByEmail("test@gmail.com");
        member.ifPresent(memberRepository::delete);
    }

    @Test
    public void createQuestionTest() throws Exception {
        QuestionCreateRequest createRequest = new QuestionCreateRequest("이것은 질문입니다.", "장지효", true);
        String jsonRequest = objectMapper.writeValueAsString(createRequest);
        Long memberId = 1L;

        mockMvc.perform(post("/api/questions/member/{memberId}", memberId)
                        .header("Authorization", "Bearer "+ refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)) // 'content' 메소드는 여기서 사용
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("이것은 질문입니다."))
                .andExpect(jsonPath("$.nickname").value("장지효"))
                .andExpect(jsonPath("$.profileOnOff").value(true));

    }

    @Test
    @DisplayName("회원별 질문 조회 테스트(): 해당 회원의 질문을 조회한다.")
    public void getQuestionsByMemberIdTest() throws Exception {
        String content = "이것은 회원의 질문입니다.";
        Question question = questionRepository.save(new Question(null, testMember, content, "닉네임", true, LocalDateTime.now()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/questions")
                        .param("memberId", String.valueOf(testMember.getId()))
                        .header("Authorization", "Bearer " + refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(content));
    }

    @Test
    @DisplayName("질문 수정 테스트(): 질문을 수정한다.")
    public void updateQuestionTest() throws Exception {
        // 수정 전 질문 생성
        String content = "이것은 수정 전의 질문입니다.";
        Question question = questionRepository.save(new Question(null, testMember, content, "닉네임", true, LocalDateTime.now()));
        String updatedContent = "이것은 수정 후의 질문입니다.";

        // 질문 수정 요청을 보내고 응답을 확인
        mockMvc.perform(MockMvcRequestBuilders.put("/api/questions/{questionId}", question.getId())
                        .param("content", updatedContent)
                        .header("Authorization", "Bearer " + refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("질문 삭제 테스트(): 질문을 삭제한다.")
    public void deleteQuestionTest() throws Exception {

        String content = "이것은 삭제할 질문입니다.";
        Question question = questionRepository.save(new Question(null, testMember, content, "닉네임", true, LocalDateTime.now()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/questions/{questionId}", question.getId())
                        .header("Authorization", "Bearer " + refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNoContent());
    }
}
