package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

@FeignClient(name = "erp-wms", contextId = "soReturnReceive")
public interface SoReturnReceiveFeign {

    /**
     * 根据详情id查询退货签收单详情
     * @Author Luo_WG
     * @Date 2023/5/19 12:03
     * @param ids
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    @PostMapping("feign/soReturnReceive/listDetailByIds")
    List<SoReturnReceiveDetailEntity> listDetailByIds(@RequestBody List<String> ids);
}