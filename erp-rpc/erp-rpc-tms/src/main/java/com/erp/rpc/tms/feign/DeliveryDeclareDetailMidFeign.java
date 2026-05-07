package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 报关明细中间表Feign接口
 *
 * @author jack
 * @date 2026-04-29
 */
@FeignClient(name = "erp-tms", contextId = "deliveryDeclareDetailMidFeign",
             configuration = {FeignErrorDecoder.class})
public interface DeliveryDeclareDetailMidFeign {

    /**
     * 自动生成报关明细中间数据
     *
     * @param dto 自动生成参数
     * @return 是否成功
     * @throws RuntimeException 远程调用异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    @PostMapping("/feign/deliveryDeclareDetailMid/autoGenerateMidData")
    Boolean autoGenerateMidData(@RequestBody List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list);
}
