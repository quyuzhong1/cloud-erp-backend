package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "erp-wms", contextId = "soReturnNotice",configuration = {FeignErrorDecoder.class})
public interface SoReturnNoticeFeign {
    /**
     * 根据来源id查询销售退货通知单主表
     * @Author Luo_WG
     * @Date 2023/5/15 12:29
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeEntity>
     **/
    @PostMapping("/feign/soReturnNotice/listBySourceId")
    List<SoReturnNoticeEntity> listBySourceId(@RequestBody List<String> sourceIds);

    /**
     * 根据来源详情id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 12:29
     * @param sourceDetailIds sourceDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeEntity>
     **/
    @PostMapping("/feign/soReturnNotice/listDetailBySourceDetailIds")
    List<SoReturnNoticeDetailEntity> listDetailBySourceDetailIds(@RequestBody List<String> sourceDetailIds);

    @PostMapping("/feign/soReturnNotice/generateSoB2cReturnNotice")
    void generateSoB2cReturnNotice(@RequestBody List<SoB2cReturnDTO.GenerateSoReturnNoticeView> list);
}
