package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.server.tms.service.TransferLogisticsChannelService;
import com.erp.server.tms.service.TransferLogisticsChannelService;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("中转报关物流商feign接口")
@RequestMapping("/feign/transferLogistics")
public class TransferLogisticsFeignController {
    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;
    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;

    /**
     * 更改中转物流商启用状态
     * @param dto
     * @return
     */
    @PostMapping("/updateDisabledBySupplierId")
    public Boolean updateDisabledBySupplierId(@RequestBody TransferLogisticsSupplierDTO.UpdateDisabledDTO dto){
        return transferLogisticsSupplierService.updateDisabledBySupplierId(dto);
    }

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    @PostMapping("/listBySupplierId")
    public List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId) {
        return transferLogisticsSupplierService.listBySupplierId(supplierId);
    }

    /**
     * 根据id查询中转服务商
     * @param supplierId
     * @return
     */
    @GetMapping("/getLogisticsSupplierById")
    public TransferLogisticsSupplierEntity getLogisticsSupplierById(@RequestBody String supplierId) {
        return transferLogisticsSupplierService.getById(supplierId);
    }

    /**
     * 根据id查询中转服务商
     * @param supplierIds
     * @return
     */
    @PostMapping("/listLogisticsSupplierByIds")
    public List<TransferLogisticsSupplierEntity> listLogisticsSupplierByIds(@RequestBody List<String> supplierIds) {
        return transferLogisticsSupplierService.listByIds(supplierIds);
    }

    /**
     * 根据主标id查询渠道信息
     * @param supplierIdList
     * @return
     */
    @PostMapping("/listLogisticsChannelByMainId")
    public List<TransferLogisticsChannelEntity> listLogisticsChannelByMainId(@RequestBody List<String> supplierIdList) {
        return transferLogisticsChannelService.listByMainIds(supplierIdList);
    }

    /**
     * 查询平台单据状态
     * @param shippingOrderNo
     * @return
     */
    @GetMapping("/getPlatformTransferStatus")
    public TransferLogisticsStatusEnum getPlatformTransferStatus(@RequestParam("shippingOrderNo") String shippingOrderNo,
                                                                 @RequestParam("transferLogisticsSupplierId") String transferLogisticsSupplierId) {
        return transferLogisticsChannelService.getPlatformTransferStatus(shippingOrderNo, transferLogisticsSupplierId);
    }

    /**
     * 根据物流渠道id 获取到对应列表
     * @return
     */
    @PostMapping("/listByTransferChannelIds")
    public List<TransferLogisticsChannelDTO.ListSelectDTO> listByTransferChannelIds(@RequestBody List<String> channelIds){
        if(CollectionUtils.isEmpty(channelIds)){
            return new ArrayList<>();
        }
        return transferLogisticsChannelService.listByTransferChannelIds(channelIds);
    }
}
