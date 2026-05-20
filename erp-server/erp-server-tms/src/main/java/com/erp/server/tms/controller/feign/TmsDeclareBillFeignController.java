package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.server.tms.service.DeliveryDeclareDetailMidService;
import com.erp.server.tms.service.TmsDeclareBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("报关单")
@RequestMapping("/feign/tmsDeclareBill")
public class TmsDeclareBillFeignController {
    @Resource
    private TmsDeclareBillService tmsDeclareBillService;

    @Resource
    private DeliveryDeclareDetailMidService deliveryDeclareDetailMidService;


    /**
     * 根据来源id查询报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param sourceIds
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/listBySourceIds")
    public List<TmsDeclareBillEntity> listBySourceIds(@RequestBody List<String> sourceIds) {
        return tmsDeclareBillService.listBySourceIds(sourceIds);
    }

    /**
     * 删除报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/delete")
    public List<BatchResultDTO> delete(@RequestBody TmsDeclareBillDTO.DeleteDTO dto) {
        return tmsDeclareBillService.delete(dto);
    }

    /**
     * 删除tms发货明细
     * @author will
     * @date 2026/4/24 14:55
     * @param dto 删除参数
     * @return java.lang.Boolean
     */
    @PostMapping("/deleteDeliveryDeclareDetailMid")
    public Boolean deleteDeliveryDeclareDetailMid(@RequestBody TmsDeclareBillDTO.DeleteDeliveryDeclareDetailMidDTO dto){
        return deliveryDeclareDetailMidService.deleteDeliveryDeclareDetailMid(dto.getSourceIds());
    }

    /**
     * 自动生成报关单预览
     **/
    @PostMapping("/autoMergeDeclareBillView")
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> autoMergeDeclareBillView(@RequestBody TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO viewDTO){
        return tmsDeclareBillService.autoMergeDeclareBillView(viewDTO);
    }
}
