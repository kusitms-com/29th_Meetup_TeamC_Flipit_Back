package com.web.baebaeBE.global.firebase;

import com.google.firebase.ErrorCode;
import com.web.baebaeBE.domain.fcm.entity.FcmToken;
import com.web.baebaeBE.domain.fcm.repository.FcmTokenRepository;
import com.web.baebaeBE.domain.fcm.service.FcmService;
import com.web.baebaeBE.domain.member.entity.Member;
import com.web.baebaeBE.domain.answer.entity.Answer;
import com.web.baebaeBE.domain.question.entity.Question;
import com.web.baebaeBE.domain.reaction.entity.MemberAnswerReaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FirebaseNotificationService {
    private final FirebaseMessagingService firebaseMessagingService;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmService fcmService;


    public void notifyNewQuestion(Member member, Question question) {
        String notificationTitle = question.getNickname() + "님이 질문을 남겼어요!\n";
        String notificationBody = question.getContent();

        // 모든 fcm 토큰 가져오기
        List<FcmToken> fcmTokens = fcmTokenRepository.findByMemberId(member.getId());

        for (FcmToken fcmToken : fcmTokens) {
            fcmService.updateLastUsedTime(fcmToken);
            sendNotificationToUser(fcmToken, notificationTitle, notificationBody);
        }
    }
    public void notifyNewAnswer(Member member, Question question, Answer answer) {
        String notificationTitle = member.getNickname() + "님이 질문에 답변을 남겼어요!\n";
        String notificationBody = answer.getContent();

        // 모든 fcm 토큰 가져오기
        List<FcmToken> fcmTokens = fcmTokenRepository.findByMemberId(question.getSender().getId());

        for (FcmToken fcmToken : fcmTokens) {
            fcmService.updateLastUsedTime(fcmToken);
            sendNotificationToUser(fcmToken, notificationTitle, notificationBody);
        }
    }


    public void notifyReaction(Member member, Answer answer, MemberAnswerReaction reaction) {
        String emoticon = "";
        switch (reaction.getReaction()) {
            case HEART:
                emoticon = "❤";
                break;
            case CURIOUS:
                emoticon = "👀";
                break;
            case SAD:
                emoticon = "😢";
                break;
            case CONNECT:
                emoticon = "👉👈";
                break;
        }
        String notificationTitle = member.getNickname()+"님이 " + emoticon + "을 남겼어요!\n";

        // 모든 fcm 토큰 가져오기
        List<FcmToken> fcmTokens = fcmTokenRepository.findByMemberId(answer.getMember().getId());

        for (FcmToken fcmToken : fcmTokens) {
            fcmService.updateLastUsedTime(fcmToken);
            sendNotificationToUser(fcmToken, notificationTitle, "");
        }
    }

    private void sendNotificationToUser(FcmToken fcmToken, String title, String body) {
        String response = firebaseMessagingService.sendNotification(fcmToken.getToken(), title, body);
        if (ErrorCode.INVALID_ARGUMENT.name().equals(response)) {
            // 토큰이 유효하지 않은 경우, 토큰을 삭제
            fcmTokenRepository.delete(fcmToken);
            log.info("Invalid token {} has been deleted", fcmToken.getToken());
        } else {
            log.info(fcmToken.getToken());
        }
    }
}