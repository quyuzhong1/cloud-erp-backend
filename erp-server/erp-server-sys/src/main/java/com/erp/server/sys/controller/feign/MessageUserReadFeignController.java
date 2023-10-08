package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.MessageEntity;
import com.erp.model.sys.entity.MessageUserReadEntity;
import com.erp.server.sys.service.MessageService;
import com.erp.server.sys.service.MessageUserReadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/feign/messageUserRead")
public class MessageUserReadFeignController extends BaseController {
    @Resource
    private MessageUserReadService messageUserReadService;

    /**
     * 添加读取信息
     * @Author Luo_WG
     * @Date 2023/8/21 10:59
     * @param entityList
     * @return java.lang.Boolean
     **/
    @PostMapping("/saveBatch")
    public Boolean saveBatch(@RequestBody List<MessageUserReadEntity> entityList) {
        return messageUserReadService.saveBatch(entityList);
    }
}
