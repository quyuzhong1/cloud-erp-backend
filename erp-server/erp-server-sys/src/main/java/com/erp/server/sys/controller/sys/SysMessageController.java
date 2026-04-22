package com.erp.server.sys.controller.sys;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
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
import com.erp.server.sys.handler.SysMessageQueryHandler;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.support.NoticeStreamEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private NoticeStreamEmitterManager noticeStreamEmitterManager;

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
     * @param dtoList
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "sys:sysMessage:delete",
            serviceClass = MessageService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated List<MessageDTO.DeleteDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return success(new ArrayList<>());
        }
        
        //按 releaseType 分组
        Map<String, List<String>> releaseTypeIdsMap = dtoList.stream()
                .collect(Collectors.groupingBy(
                        MessageDTO.DeleteDTO::getReleaseType,
                        Collectors.mapping(MessageDTO.DeleteDTO::getId, Collectors.toList())
                ));

        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : releaseTypeIdsMap.entrySet()) {
            String releaseType = entry.getKey();
            List<String> ids = entry.getValue();

            List<BatchResultDTO> batchResults = messageService.batchDelete(ids, releaseType);
            resultDTOS.addAll(batchResults);
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

    /**
     * 获取系统通知未读数量
     * @return
     */
    @GetMapping("/unreadCount")
    public ApiResult<Integer> getUnreadCount() {
        return success(messageService.getSysMessageUnreadCount());
    }

    @GetMapping("/streamStats")
    public ApiResult<MessageDTO.StreamStatsDTO> getStreamStats(
            @RequestParam(value = "detail", required = false, defaultValue = "true") Boolean detail,
            @RequestParam(value = "userLimit", required = false, defaultValue = "100") Integer userLimit,
            @RequestParam(value = "connectionLimitPerUser", required = false, defaultValue = "20") Integer connectionLimitPerUser) {
        return success(noticeStreamEmitterManager.getStreamStats(detail, userLimit, connectionLimitPerUser));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        String uid = UserContext.getDefaultLoginUser().getUid();
        log.info("Receive PC notice SSE register request, uid={}, nodeId={}", uid, noticeStreamEmitterManager.getNodeId());
        SseEmitter emitter = noticeStreamEmitterManager.registerPc(uid);
        sendCompensationNotice(uid, "PC", emitter);
        return emitter;
    }

    private void sendCompensationNotice(String userId, String application, SseEmitter emitter) {
        MessageDTO.NoticeDTO latestNotice = messageService.getLatestUnreadNotice(userId, application);
        if (latestNotice == null) {
            log.info("No unread compensation notice found after SSE register, application={}, userId={}", application, userId);
            return;
        }
        boolean success = noticeStreamEmitterManager.sendCompensationNotice(emitter, application, userId, latestNotice);
        if (!success) {
            return;
        }
        MessageDTO.ReadHistoryMessageDTO readDTO = new MessageDTO.ReadHistoryMessageDTO();
        readDTO.setId(latestNotice.getId());
        messageService.readMessage(readDTO);
        log.info("Compensation notice marked as read after SSE send, application={}, userId={}, noticeId={}",
                application, userId, latestNotice.getId());
    }

}
