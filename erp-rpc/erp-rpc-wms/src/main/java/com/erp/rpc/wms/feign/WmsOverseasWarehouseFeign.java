package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "overseasWarehouseFeign",configuration = {FeignErrorDecoder.class})
public interface WmsOverseasWarehouseFeign {

    /**
     * 根据入库状态查询入库单号
     **/
    @PostMapping("/feign/overseasWarehouse/getReceiptNumbersForStatus")
    List<String> getReceiptNumbersForStatus(@RequestParam(value = "statusList") List<String> statusList,@RequestParam(value = "authId") String authId);

    /**
     * 根据仓库code查询仓库信息
     **/
    @PostMapping("/feign/overseasWarehouse/getOverseasWarehouseListByPlatformCodes")
    List<OverseasProviderWarehouseEntity> getOverseasWarehouseListByPlatformCodes(@RequestParam(value = "warehouseCodeList") List<String> warehouseCodeList, @RequestParam(value = "platform")String platform);

    /**
     * 根据erp仓库id 集合获取到海外仓
     * @return
     */
    @PostMapping("/feign/overseasWarehouse/listByWarehouseId")
    List<OverseasProviderWarehouseDTO.ViewDTO> listByWarehouseIdList(@RequestBody List<String> warehouseIdList);

    /**
     * 远程搜索
     *
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/feign/overseasWarehouse/pagingSelect")
    PagingVO<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(@RequestBody @Validated PagingDTO<OverseasProviderWarehouseDTO.SelectDTO> dto);

    @PostMapping("/feign/overseasWarehouse/pageWarehouseProduct")
    PagingVO<SkuMappingDTO.SyncWarehouseProductView> pageWarehouseProduct(@RequestBody @Validated PagingDTO<AdvanceQueryContainer> advanceQueryDTO);

    @PostMapping("/feign/overseasWarehouse/addThirdWarehouse")
    BaseResultDTO.AddDTO addThirdWarehouse(@RequestBody @Validated ThirdWarehouseDTO.AddDTO addDTO);
}
