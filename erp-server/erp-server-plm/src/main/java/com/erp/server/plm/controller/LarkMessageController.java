package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
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
@RequestMapping("lark/message")
public class LarkMessageController  extends BaseController {

    @Resource
    private LarkMessageService  larkMessageService;


    /**
     * 飞书催办消息
     * @param dto
     * @return
     */
    @PostMapping(value = "/press")
    public ApiResult larkPress(@RequestBody @Validated LarkPressMessageDTO dto) {
        Boolean result = larkMessageService.press(dto);
        return success(result);
    }

}
