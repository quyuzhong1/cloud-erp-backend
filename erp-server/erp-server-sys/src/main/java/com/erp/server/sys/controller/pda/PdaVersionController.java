package com.erp.server.sys.controller.pda;

import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.erp.model.sys.enums.MessageTypeEnum;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.MessageUserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.sys.service.PdaVersionService;
import com.common.core.controller.vo.ApiResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 系统通知-PDA升级通知
 * @author Luo_WG
 * @since 2023-08-14
 */
@Slf4j
@RestController
@LogSystemModule("PDA系统版本控制")
@RequestMapping("/pdaVersion")
public class PdaVersionController extends BaseController {

    @Autowired
    private PdaVersionService pdaVersionService;

    @Resource
    private MessageService messageService;

    @Resource
    private MessageUserReadService messageUserReadService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     *  系统通知列表分页查询
     * @Author Luo_WG
     * @Date 2023/9/11 16:01
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.sys.dto.PdaVersionDTO.PagingDTO>>
     **/
    @PostMapping("/paging")
    public ApiResult<PagingVO<PdaVersionDTO.PagingDTO>> paging(@RequestBody @Validated PagingDTO<PdaVersionDTO.PagingParamDTO> dto) {
        PagingVO<PdaVersionDTO.PagingDTO> pagingVO = pdaVersionService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取pda最新版本
     * @Author Luo_WG
     * @Date 2023/8/14 16:27
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.entity.PdaVersionEntity>
     **/
    @GetMapping(value = "/getPdaVersion")
    public ApiResult<PdaVersionEntity> getPdaVersion() {
        PdaVersionEntity version = pdaVersionService.getPdaVersion();
        return success(version);
    }

    /**
     * 发版
     * @Author Luo_WG
     * @Date 2023/8/14 16:27
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.entity.PdaVersionEntity>
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "发版")
    @PostMapping(value = "/release")
    public ApiResult release(@RequestBody PdaVersionDTO.AddDTO dto){
        Boolean flag = pdaVersionService.release(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 用户跳过此版本升级
     * @Author Luo_WG
     * @Date 2023/9/12 12:15
     * @param versionId
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "用户跳过此版本升级:版本id={versionId}")
    @GetMapping(value = "/skipVersion")
    public ApiResult skipVersion(@RequestParam("versionId") String versionId) {
        Boolean flag = pdaVersionService.skipVersion(versionId);
        return flag == true ? success() : failure();
    }

    /**
     * 详情
     * @Author
     * @Date
     * @param
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/view")
    @LogViewService
    public ApiResult<PdaVersionDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(pdaVersionService.view(id));
    }

    /**
     * 更新 PDA 版本信息
     * @Author 
     * @Date 
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新 PDA 版本信息")
    @PostMapping(value = "/update")
    public ApiResult update(@RequestBody @Validated PdaVersionDTO.UpdateDTO dto) {
        Boolean flag = pdaVersionService.update(dto);
        return flag == true ? success() : failure();
    }

    @CrossOrigin
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter(1800_000L); // 30分钟超时
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
                            if (!Objects.equals(MessageTypeEnum.PDA.getCode(),message.getType())) {
                                log.info("Message {} type is not sys, skipping", message.getId());
                                continue;
                            }

                            // 检查消息是否过期
                            LocalDateTime expireTime = message.getExpireTime();
                            if (expireTime != null && expireTime.isBefore(now)) {
                                // 消息已过期，跳过
                                log.info("Message {} has expired, skipping", message.getId());
                                continue;
                            }

                            // 检查定时通知时间
                            LocalDateTime noticeTime = message.getNoticeTime();
                            if (noticeTime != null && noticeTime.isAfter(now)) {
                                // 还没到通知时间，跳过
                                log.info("Message {} notice time not reached, skipping", message.getId());
                                continue;
                            }

                            firstUnReadUser = unReadUser;
                            break;
                        }
                    }

                    if (message != null) {
                        try {
                            // 构建返回的 JSON 对象
                            String eventData = String.format("{\"noticeTitle\":\"%s\",\"content\":\"%s\"}",
                                    message.getNoticeTitle(),
                                    message.getDataJson().replace("\"", "\\\""));
                            emitter.send(SseEmitter.event().data(eventData));

                            if (firstUnReadUser != null) {
                                messageUserReadService.lambdaUpdate()
                                        .eq(MessageUserReadEntity::getUserId, uid)
                                        .eq(MessageUserReadEntity::getMessageId, firstUnReadUser.getMessageId())
                                        .set(MessageUserReadEntity::getIsRead, Boolean.TRUE)
                                        .update();
                            }
                        } catch (IOException e) {
                            log.debug("Client disconnected, stopping SSE stream.");
                            isComplete.set(true);
                            return;
                        }
                    }

                    // 10秒检查一次
                    try {
                        Thread.sleep(10_000); // 30秒
                    } catch (InterruptedException e) {
                        log.debug("SSE stream thread interrupted");
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
