package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.erp.server.wms.service.WmsDeliveryPlanDetailService;
import com.erp.server.wms.service.WmsDeliveryPlanService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 发货计划
 * @author will
 * @date 2024/10/18 10:32
 */
@AllArgsConstructor
@RestController
@RequestMapping(value = "/feign/deliveryPlan")
public class DeliveryPlanFeignController extends BaseController {

    private  WmsDeliveryPlanService wmsDeliveryPlanService;

    @Resource
    private WmsDeliveryPlanDetailService wmsDeliveryPlanDetailService;

   /**
    * 添加发货计划
    * @author will
    * @date 2024/10/18 10:32
    * @param addDTO
    * @return AddDTO
    */
    @PostMapping("/addDeliveryPlan")
    public BaseResultDTO.AddDTO addDeliveryPlan(@RequestBody @Valid WmsDeliveryPlanDTO.AddDTO addDTO){
        return wmsDeliveryPlanService.add(addDTO);
    }

    /**
     * 根据来源id集合查询
     * @author will
     * @date 2024/10/23 10:00
     * @param idList
     * @return List<WmsDeliveryPlanDetailEntity>
     */
    @PostMapping("/listBySourceIdList")
    public List<WmsDeliveryPlanDetailEntity> listBySourceIdList(@RequestBody List<String> idList){
        return wmsDeliveryPlanDetailService.listBySourceIdList(idList);
    }
}