package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.sys.entity.MessageUserReadEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 系统消息读取信息
 * @Author Luo_WG
 * @Date 2023/8/21 11:02
 **/
@FeignClient(name = "erp-sys", contextId = "messageUserReadFeign",configuration = {FeignErrorDecoder.class})
public interface MessageUserReadFeign {

    /**
     * 添加读取消息
     * @Author Luo_WG
     * @Date 2023/8/21 11:04
     * @param entityList
     * @return java.util.List<com.erp.model.sys.entity.MessageUserReadEntity>
     **/
    @PostMapping("/feign/messageUserRead/saveBatch")
    Boolean saveBatch(@RequestBody List<MessageUserReadEntity> entityList);

}
