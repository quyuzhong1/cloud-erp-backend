package com.erp.server.wms.controller.feign;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.ThirdShopDTO;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 海外仓feign
 */
@RestController
@RequestMapping("/feign/overseasWarehouse")
public class OverseasWarehouseController extends BaseController {

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    @Resource
    private OverseasProviderService overseasProviderService;

    /**
     * 通过状态获取入库单号
     */
    @PostMapping("/getReceiptNumbersForStatus")
    public List<String> getReceiptNumbersForStatus(@RequestParam(value = "statusList") List<String> statusList,@RequestParam(value = "platform") String platform){
        if (CollectionUtils.isEmpty(statusList)) {
            return Collections.emptyList();
        }
        return overseasWarehouseInboundService.getReceiptNumbersForStatus(statusList, platform);
    }

    /**
     * 通过仓库编号获取海外仓
     */
    @PostMapping("/getOverseasWarehouseListByPlatformCodes")
    public List<OverseasProviderWarehouseEntity> getOverseasWarehouseListByPlatformCodes(@RequestParam(value = "warehouseCodeList") List<String> warehouseCodeList, @RequestParam(value = "platform")String platform){
        return overseasProviderWarehouseService.listByPlatformWarehouseCode(warehouseCodeList,platform);
    }


    /**
     * @description
     * @params  根据erp仓库id 集合获取到海外仓
     * @author Lambda
     * @return java.util.List<com.erp.model.wms.dto.OverseasProviderWarehouseDTO.ViewDTO>
     * @create 2023-12-13 17:08
     */
    @PostMapping("/listByWarehouseId")
    public List<OverseasProviderWarehouseDTO.ViewDTO> listByWarehouseId(@RequestBody List<String> warehouseIdList){
        return overseasProviderWarehouseService.listByWarehouseIdList(warehouseIdList);
    }


    /**
     * 远程搜索
     *
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/pagingSelect")
    public PagingVO<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(@RequestBody @Validated PagingDTO<OverseasProviderWarehouseDTO.SelectDTO> dto) {
        return overseasProviderWarehouseService.pagingSelect(dto);
    }

    /**
     * 三方仓分页
     *
     */
    @PostMapping("/pageWarehouseProduct")
    public PagingVO<SkuMappingDTO.SyncWarehouseProductView> pageWarehouseProduct(@RequestBody @Validated PagingDTO<AdvanceQueryContainer> advanceQueryDTO) {
        return overseasProviderService.pageWarehouseProduct(advanceQueryDTO);
    }

    /**
     * 新增三方仓
     *
     */
    @PostMapping("/addThirdWarehouse")
    public BaseResultDTO.AddDTO addThirdWarehouse(@RequestBody ThirdWarehouseDTO.AddDTO addDTO){
        return overseasProviderWarehouseService.addThirdWarehouse(addDTO);
    }
}