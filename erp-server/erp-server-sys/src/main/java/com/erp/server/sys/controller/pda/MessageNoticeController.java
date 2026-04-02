package com.erp.server.sys.controller.pda;

import com.erp.model.sys.entity.MessageEntity;
import com.erp.server.sys.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: wtr
 * @Date: 2026/4/1 10:06
 * @Param:
 * @Return:
 * @Description:
 **/

@Slf4j
@RestController
@RequestMapping("/messageNotice")
public class MessageNoticeController {

    @Resource
    private MessageService messageService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @CrossOrigin
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter(60_000L);
        AtomicBoolean isComplete = new AtomicBoolean(false);

        // 注册完成回调
        emitter.onCompletion(() -> isComplete.set(true));
        emitter.onTimeout(() -> {
            isComplete.set(true);
            log.info("SSE stream timeout");
        });

        executor.execute(() -> {
            try {
                while (!isComplete.get()) {  // 检查是否已完成
                    MessageEntity message = messageService.lambdaQuery()
                            .eq(MessageEntity::getIsSend, Boolean.FALSE)
                            .last("limit 1")
                            .one();

                    if (message == null) {
                        log.info("No data found, closing SSE stream.");
                        emitter.complete();
                        isComplete.set(true);
                        return;
                    }

                    try {
                        String eventData = message.getDataJson();
                        emitter.send(SseEmitter.event().data(eventData));

                        messageService.lambdaUpdate()
                                .eq(MessageEntity::getId, message.getId())
                                .set(MessageEntity::getIsSend, Boolean.TRUE)
                                .update();

                        log.debug("Data sent successfully: {}", eventData);
                    } catch (IOException e) {
                        log.debug("Client disconnected, stopping SSE stream.");
                        isComplete.set(true);
                        return;
                    }
                }
            } catch (Exception e) {
                log.error("SSE stream error", e);
                if (!isComplete.get()) {
                    emitter.completeWithError(e);
                    isComplete.set(true);
                }
            }
        });

        return emitter;
    }

}
