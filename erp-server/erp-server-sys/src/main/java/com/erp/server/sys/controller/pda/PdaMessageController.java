package com.erp.server.sys.controller.pda;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.server.sys.service.MessageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:消息通知
 * @Author Luo_WG
 * @Date 2023/8/10 14:33
 **/
@RestController
@LogSystemModule("PDA消息通知")
@RequestMapping("/pdaMessage")
public class PdaMessageController extends BaseController {

    @Resource
    private MessageService messageService;

    /**
     * 查询消息分类列表
     * @Author Luo_WG
     * @Date 2023/8/10 10:11
     * @return com.common.core.controller.vo.ApiResult<java.lang.Integer>
     **/
    @GetMapping(value = "/listNotReadMessageNum")
    public ApiResult<List<MessageDTO.NotReadMessageNum>> listNotReadMessageNum() {
        List<MessageDTO.NotReadMessageNum> list = messageService.listNotReadMessageNum();
        return success(list);
    }

    /**
     * 查询未读消息详情
     * @Author Luo_WG
     * @Date 2023/8/10 10:11
     * @return com.common.core.controller.vo.ApiResult<java.lang.Integer>
     **/
    @GetMapping(value = "/listNotReadMessageDetail")
    public ApiResult<List<MessageDTO.NotReadMessageNumDetail>> listNotReadMessageDetail(@RequestParam("type") String type , 
    		@RequestParam(value = "pageNo" , required = false) Integer pageNo , 
    		@RequestParam(value = "pageSize" , required = false) Integer pageSize) {
        List<MessageDTO.NotReadMessageNumDetail> list = messageService.listNotReadMessageDetail(type , pageNo , pageSize);
        return success(list);
    }

    /**
     * 全部已读
     * @Author Luo_WG
     * @Date 2023/8/10 10:11
     * @return com.common.core.controller.vo.ApiResult<java.lang.Integer>
     **/
    @GetMapping(value = "/readAll")
    public ApiResult readAll() {
        Boolean flag = messageService.readAll();
        return flag == true ? success() : failure();
    }

    /**
     * 是否有新的消息
     * @Author Luo_WG
     * @Date 2023/8/11 16:44
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.dto.MessageDTO.IsMessageDTO>
     **/
    @GetMapping(value = "/isMessage")
    public ApiResult<MessageDTO.IsMessageDTO> isMessage() {
        MessageDTO.IsMessageDTO isMessageDTO = messageService.isMessage();
        return success(isMessageDTO);
    }

    /**
     * 关闭消息通知
     * @Author Luo_WG
     * @Date 2023/8/22 12:18
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "关闭消息通知")
    @GetMapping(value = "/closeMessageNotice")
    public ApiResult closeMessageNotice() {
        Boolean flag = messageService.closeMessageNotice();
        return flag == true ? success() : failure();
    }

    /**
     * PDA消息单条已读
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "PDA消息单条已读")
    @PostMapping(value = "/readMessage")
    public ApiResult<?> readMessage(@RequestBody MessageDTO.ReadHistoryMessageDTO dto) {
        messageService.readMessage(dto);
        return success();
    }
}
