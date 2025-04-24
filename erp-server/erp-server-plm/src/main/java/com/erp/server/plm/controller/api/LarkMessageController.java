package com.erp.server.plm.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.LarkPressMessageDTO;
import com.erp.server.plm.service.LarkMessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 飞书提醒控制器
 *
 * @Author Cloud
 * @Date 2023/3/9 10:01
 **/
@RestController
@LogSystemModule("PLM系统")
@RequestMapping("lark/message")
public class LarkMessageController  extends BaseController {

    @Resource
    private LarkMessageService  larkMessageService;


    /**
     * 飞书催办消息
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "飞书催办消息：催办业务ID={businessId}，催办业务类型={businessType}")
    @PostMapping(value = "/press")
    public ApiResult<Object> larkPress(@RequestBody @Validated LarkPressMessageDTO dto) {
        Boolean result = larkMessageService.press(dto);
        return success(result);
    }

    /**
     * 飞书批量催办消息【PLM1.3】
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "飞书批量催办消息：催办业务ids={businessIdList}，催办业务类型={businessType}")
    @PostMapping(value = "/batchPress")
    public ApiResult<Object> batchPress(@RequestBody @Validated LarkPressMessageDTO.BatchLarkPressMessageDTO dto) {
        Boolean result = larkMessageService.batchPress(dto);
        return success(result);
    }

}
