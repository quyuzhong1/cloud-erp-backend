package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.server.sys.service.MessageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/message")
public class MessageFeignController extends BaseController {
    @Resource
    private MessageService messageService;

    /**
     * 添加消息
     * @Author Luo_WG
     * @Date 2023/8/21 10:59
     * @param entity
     * @return java.lang.Boolean
     **/
    @PostMapping("/save")
    public String save(@RequestBody MessageEntity entity) {
        messageService.save(entity);
        return entity.getId();
    }
}
