package org.example.backend.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Optional<Message> searchLatestMessageAfterLogin(String keyword, Timestamp loginRecord) {
        // 로그인 시간 이후 가장 최근 메시지를 찾습니다.
        return messageRepository.findTopByContentContainingIgnoreCaseAndTimestampAfterOrderByTimestampDesc(keyword, loginRecord);
    }

    public Optional<Message> searchLatestMessageAfterLoginAndBeforeTargetMessage(String keyword, Timestamp loginRecord, long messageId) {
        // 로그인 시간 이후 && 타켓 메세지 보다 앞선 메시지를 찾습니다.
        return messageRepository.findTopByContentContainingIgnoreCaseAndTimestampAfterAndIdLessThanOrderByTimestampDesc(keyword, loginRecord, messageId);
    }

    public List<Message> getMessagesBefore(Long messageId, Timestamp loginRecord) {
        // 타켓 메세지 이전의 10개의 메시지들을 가져옵니다.
        return messageRepository.findTop10ByIdLessThanAndTimestampAfterOrderByTimestampAsc(messageId, loginRecord);
    }

    public List<Message> getMessagesAfter(Long messageId, Timestamp loginRecord) {
        // 타켓 메세지 이후의 10개의 메시지들을 가져옵니다.
        return messageRepository.findTop10ByIdGreaterThanAndTimestampAfterOrderByTimestampAsc(messageId, loginRecord);
    }

}