package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * b2c发货单
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货单")
@RequestMapping("/feign/soB2cDelivery")
public class SoB2cDeliveryFeignController extends BaseController {

    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @PostMapping("/listBySoDetailIds")
    public List<SoB2cDeliveryDetailEntity> listBySoDetailIds(@RequestBody List<String> soDetailIdList) {
        List<SoB2cDeliveryDetailEntity> list = soB2cDeliveryDetailService.listBySoDetailIds(soDetailIdList);
        return list;
    }
 
     /** 
      * @description 添加B2C发货单
      * @param dto
      * @author Lambda
      * @return 
      * @create 2023-12-18 15:47
      */
    @PostMapping("/add")
    public Boolean listBySoDetailIds(@RequestBody SoB2cDeliveryDTO.AddDTO dto) {
        Boolean addResult=  soB2cDeliveryService.add(dto);
        return addResult;
    }
}
