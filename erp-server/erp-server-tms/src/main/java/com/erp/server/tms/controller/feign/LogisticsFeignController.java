package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import io.seata.saga.statelang.domain.impl.BaseState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsFeignController
 * @description: TODO
 * @date 2023年11月03日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("物流feign接口")
@RequestMapping("/feign/logistics")
public class LogisticsFeignController {

    @Resource
    private LogisticsBaseService logisticsBaseService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @PostMapping("/queryOrderList")
    public List<LogisticsOrderResponseVO> queryOrderList(@RequestBody List<LogisticsQueryBaseVO> logisticsQueryVOList){
        return logisticsBaseService.queryOrderList(logisticsQueryVOList);
    }


    /**
     * 根据供应商id 获取对应渠道的信息
     * @param supplierId
     * @return
     */
    @PostMapping("/listBySupplierId")
    public List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId){
        return logisticsChannelService.listBySupplierId(supplierId);
    }

    /**
     * 更改物流商状态
     * @param dto
     * @return
     */
    @PostMapping("/updateDisabledBySupplierId")
    public Boolean updateDisabledBySupplierId(@RequestBody LogisticsSupplierDTO.UpdateDisabledDTO dto){
        return logisticsSupplierService.updateDisabledBySupplierId(dto);
    }

}
