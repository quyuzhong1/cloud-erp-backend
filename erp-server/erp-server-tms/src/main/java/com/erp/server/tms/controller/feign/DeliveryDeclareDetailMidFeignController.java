package com.erp.server.tms.controller.feign;

import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.server.tms.service.DeliveryDeclareDetailMidService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 报关明细中间表Feign控制器
 *
 * @author jack
 * @date 2026-04-29
 */
@RestController
@RequestMapping("/feign/deliveryDeclareDetailMid")
public class DeliveryDeclareDetailMidFeignController {

    @Resource
    private DeliveryDeclareDetailMidService service;

    /**
     * 自动生成报关明细中间数据
     *
     * @param dto 自动生成参数
     * @return 是否成功
     * @throws RuntimeException 自动生成失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    @PostMapping("/autoGenerateMidData")
    public Boolean autoGenerateMidData(@RequestBody TmsDeclareBillDTO.AutoGenerateMidDataDTO dto) {
        return service.autoGenerateMidData(dto.getSourceDetailList());
    }

    /**
     * 删除报关明细中间表数据
     *
     * @param dto 删除参数
     * @return 是否成功
     * @throws RuntimeException 删除失败时抛出
     * @author jack
     * @date 2026-05-20
     */
    @PostMapping("/deleteDeliveryDeclareDetailMid")
    public Boolean deleteDeliveryDeclareDetailMid(@RequestBody TmsDeclareBillDTO.DeleteDeliveryDeclareDetailMidDTO dto) {
        return service.deleteDeliveryDeclareDetailMid(dto.getSourceIds());
    }
}
