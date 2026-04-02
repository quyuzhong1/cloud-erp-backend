package com.erp.server.sys.controller.pda;

import com.common.business.threadlocal.UserContext;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.MessageUserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
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

    @Resource
    private MessageUserReadService messageUserReadService;

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
                    String uid = UserContext.getDefaultLoginUser().getUid();
                    List<MessageUserReadEntity> unReadUsers = messageUserReadService.lambdaQuery()
                            .eq(MessageUserReadEntity::getUserId, uid)
                            .eq(MessageUserReadEntity::getIsRead, Boolean.FALSE)
                            .orderByDesc(MessageUserReadEntity::getCreateTime)
                            .list();

                    MessageEntity message = null;
                    MessageUserReadEntity firstUnReadUser = null;
                    for (MessageUserReadEntity unReadUser : unReadUsers) {
                        message = messageService.getById(unReadUser.getMessageId());
                        if (message != null) {
                            firstUnReadUser = unReadUser;
                            break;
                        }
                    }

                    if (message == null) {
                        emitter.complete();
                        isComplete.set(true);
                        return;
                    }

                    try {
                        String eventData = message.getDataJson();
                        emitter.send(SseEmitter.event().data(eventData));

                        if (firstUnReadUser != null) {
                            messageUserReadService.lambdaUpdate()
                                    .eq(MessageUserReadEntity::getUserId, uid)
                                    .eq(MessageUserReadEntity::getMessageId, firstUnReadUser.getMessageId())
                                    .set(MessageUserReadEntity::getIsRead, Boolean.TRUE)
                                    .update();
                        }
                        emitter.complete();
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
