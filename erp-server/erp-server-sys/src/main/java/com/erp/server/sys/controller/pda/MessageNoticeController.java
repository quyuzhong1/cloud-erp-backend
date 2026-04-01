package com.erp.server.sys.controller.pda;

import com.erp.model.sys.entity.MessageEntity;
import com.erp.server.sys.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public ResponseBodyEmitter streamEvents() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            try {
                int count = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    count++;
                    MessageEntity message = messageService.lambdaQuery()
                            .select(MessageEntity::getDataJson)
                            .last(" limit 1 ")
                            .one();
                    String eventData = message.getDataJson().toString();
                    log.debug("eventData = {}",eventData);
                    emitter.send(eventData);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });

        return emitter;
    }
}
