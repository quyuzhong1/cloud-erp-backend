package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "soReturnReceive",configuration = {FeignErrorDecoder.class})
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

    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 16:55
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeDetailEntity>
     **/
    @PostMapping("feign/soReturnReceive/listDetailBySourceIds")
    List<SoReturnReceiveDetailEntity> listDetailBySourceIds(@RequestBody List<String> ids);

    /**
     * 根据来源名词id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 16:55
     * @param sourceDetailIds sourceDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeDetailEntity>
     **/
    @PostMapping("feign/soReturnReceive/listDetailBySourceDetailIds")
    List<SoReturnReceiveDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailIds);

    /**
     * 根据来源id查询销售退货通知单主表
     * @Author Luo_WG
     * @Date 2023/5/15 12:29
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveEntity>
     **/
    @PostMapping("/feign/soReturnReceive/listBySourceId")
    List<SoReturnReceiveEntity> listBySourceId(@RequestBody List<String> sourceIds);
}