package com.erp.server.sys.controller.pda;

import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.support.NoticeStreamEmitterManager;
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
    private NoticeStreamEmitterManager noticeStreamEmitterManager;

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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        String uid = UserContext.getDefaultLoginUser().getUid();
        log.info("Receive PDA notice SSE register request, uid={}, nodeId={}", uid, noticeStreamEmitterManager.getNodeId());
        SseEmitter emitter = noticeStreamEmitterManager.registerPda(uid);
        sendCompensationNotice(uid, "PDA", emitter);
        sendCompensationUpgradeNotice(uid, "PDA", emitter);
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

    private void sendCompensationUpgradeNotice(String userId, String application, SseEmitter emitter) {
        MessageDTO.NoticeDTO latestUpgradeNotice = messageService.getLatestUnreadUpgradeNotice(userId, application);
        if (latestUpgradeNotice == null) {
            log.info("No unread upgrade compensation notice found after SSE register, application={}, userId={}", application, userId);
            return;
        }
        boolean success = noticeStreamEmitterManager.sendCompensationNotice(emitter, application, userId, latestUpgradeNotice);
        if (!success) {
            return;
        }
        MessageDTO.ReadHistoryMessageDTO readDTO = new MessageDTO.ReadHistoryMessageDTO();
        readDTO.setId(latestUpgradeNotice.getId());
        messageService.readMessage(readDTO);
        log.info("Compensation upgrade notice marked as read after SSE send, application={}, userId={}, noticeId={}",
                application, userId, latestUpgradeNotice.getId());
    }
}
