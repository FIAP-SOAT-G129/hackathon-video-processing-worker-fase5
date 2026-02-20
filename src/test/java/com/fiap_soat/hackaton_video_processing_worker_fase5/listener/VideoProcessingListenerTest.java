package com.fiap_soat.hackaton_video_processing_worker_fase5.listener;

import com.fiap_soat.hackaton_video_processing_worker_fase5.dto.VideoProcessingRequest;
import com.fiap_soat.hackaton_video_processing_worker_fase5.service.VideoProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

class VideoProcessingListenerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void receiveMessageShouldDeserializeAndDelegateToProcessingService() {
        RecordingVideoProcessingService videoProcessingService = new RecordingVideoProcessingService();
        VideoProcessingListener listener = new VideoProcessingListener(objectMapper, videoProcessingService);
        String payload = """
            {"videoId":"video-123","userId":"user-456","inputVideoPath":"/tmp/input.mp4"}
            """;
        Message message = new Message(payload.getBytes(), new MessageProperties());

        listener.receiveMessage(message);

        assertNotNull(videoProcessingService.receivedRequest);
        assertEquals("video-123", videoProcessingService.receivedRequest.videoId());
        assertEquals("user-456", videoProcessingService.receivedRequest.userId());
        assertEquals("/tmp/input.mp4", videoProcessingService.receivedRequest.inputVideoPath());
    }

    @Test
    void receiveMessageShouldRejectMessageWhenPayloadIsInvalid() {
        RecordingVideoProcessingService videoProcessingService = new RecordingVideoProcessingService();
        VideoProcessingListener listener = new VideoProcessingListener(objectMapper, videoProcessingService);
        Message invalidMessage = new Message("not-json".getBytes(), new MessageProperties());

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receiveMessage(invalidMessage));
        assertNull(videoProcessingService.receivedRequest);
    }

    private static class RecordingVideoProcessingService implements VideoProcessingService {
        private VideoProcessingRequest receivedRequest;

        @Override
        public void processVideo(VideoProcessingRequest videoProcessingRequest) {
            this.receivedRequest = videoProcessingRequest;
        }
    }
}
