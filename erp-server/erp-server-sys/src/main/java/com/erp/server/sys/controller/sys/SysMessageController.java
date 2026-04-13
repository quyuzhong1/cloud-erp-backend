package com.erp.server.sys.controller.sys;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.server.sys.handler.SysMessageQueryHandler;
import com.erp.server.sys.handler.SysVersionQueryHandler;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.MessageUserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import static com.common.core.controller.vo.ApiResult.error;
import static com.common.core.controller.vo.ApiResult.success;

/**
 * @Author: wtr
 * @Date: 2026/4/1 10:06
 * @Param:
 * @Return:
 * @Description: 系统通知-系统公告
 **/

@Slf4j
@RestController
@RequestMapping("/sysMessage")
public class SysMessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private MessageUserReadService messageUserReadService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 新增
     * @author wtr
     * @date: 202-04-10
     * @param dto
     * @return
     */
    @PostMapping("/release")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> release(@RequestBody @Validated MessageDTO.AddDTO dto) {
        return success(messageService.add(dto));
    }

    /**
     * 修改
     * @author wtr
     * @date: 202-04-10
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:sysMessage:update",
            serviceClass = MessageService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated MessageDTO.UpdateDTO dto) {
        messageService.update(dto);
        return success();
    }

    /**
     * 详情
     * @author wtr
     * @date:  2026-04-10
     * @param id
     * @return
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:sysMessage:view",
            serviceClass = MessageService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<MessageDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(messageService.view(id));
    }

    /**
     * 删除
     * @author wtr
     * @date:  2026-04-10
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:sysMessage:delete",
            serviceClass = MessageService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<MessageEntity> list = messageService.lambdaQuery().in(MessageEntity::getId, ids).list();
        Map<String, MessageEntity> idEntityMap = list.stream().collect(Collectors.toMap(MessageEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = messageService.deleteMessage(id);
            }catch (Exception e){
                log.error("删除失败",e);
                MessageEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), "", e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : error("",resultDTOS);
    }

    /**
     * PC端系统通知历史消息查询
     * @author wtr
     * @date: 202-04-10
     * @param dto
     * @return
     */
    @PostMapping("/pagingHistoryMessage")
    @WebAdvanceQuery(handler = SysMessageQueryHandler.class)
    public ApiResult<PagingVO<MessageDTO.ListHistoryMessageDTO>> pagingHistoryMessage(@RequestBody @Validated PagingDTO<MessageDTO.HistoryMessagePagingParamDTO> dto) {
        return success(messageService.pagingHistoryMessage(dto));
    }

    /**
     * PC端系统通知历史消息已读
     * @return
     */
    @PostMapping("/readHistoryMessage")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult<?> readHistoryMessage(@RequestBody MessageDTO.ReadHistoryMessageDTO dto) {
        messageService.readHistoryMessage(dto);
        return success();
    }

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
                    LocalDateTime now = LocalDateTime.now();
                    for (MessageUserReadEntity unReadUser : unReadUsers) {
                        message = messageService.getById(unReadUser.getMessageId());
                        if (message != null) {
                            // 检查消息是否过期
                            LocalDateTime expireTime = message.getExpireTime();
                            if (expireTime != null && expireTime.isBefore(now)) {
                                // 消息已过期，跳过
                                log.info("Message {} has expired, skipping", message.getId());
                                continue;
                            }
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
