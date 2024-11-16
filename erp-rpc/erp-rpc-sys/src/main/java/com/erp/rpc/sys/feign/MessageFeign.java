package com.erp.rpc.sys.feign;

import com.erp.model.sys.entity.MessageEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 系统消息
 * @Author Luo_WG
 * @Date 2023/8/21 11:02
 **/
@FeignClient(name = "erp-sys", contextId = "message")
public interface MessageFeign {

    /**
     * 添加通知消息
     * @Author Luo_WG
     * @Date 2023/8/21 11:03
     * @param entity
     * @return java.util.List<com.erp.model.sys.entity.MessageEntity>
     **/
    @PostMapping("/feign/message/save")
    String save(@RequestBody MessageEntity entity);

}
