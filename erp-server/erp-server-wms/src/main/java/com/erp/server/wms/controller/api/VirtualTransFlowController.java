package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.dto.VirtualTransFlowDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.server.wms.query.VirtualTransFlowQueryHandler;
import com.erp.server.wms.service.VirtualInventoryTransCoreService;
import com.erp.server.wms.service.VirtualTransFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟库存交易流水表
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟库存交易流水表")
@RequestMapping("/virtualTransFlow")
public class VirtualTransFlowController extends BaseController {

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;

    /**
     * 虚拟库存交易流水列表
     * @author will
     * @date 2024/6/3 17:07
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vtf.warehouse_id",
            menuCode = "wms:virtualTransFlow:paging"
    )
    @WebAdvanceQuery(handler = VirtualTransFlowQueryHandler.class)
    public ApiResult<PagingVO<VirtualTransFlowDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        return success(virtualTransFlowService.paging(dto));
    }


    /**
     * 虚拟库存交易流水导出
     * @author will
     * @date 2024/6/6 15:30
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "虚拟库存交易流水导出")
    public ApiResult exportExcel(@RequestBody VirtualTransFlowDTO.SearchParamDTO dto) {
        Boolean flag = virtualTransFlowService.exportExcel(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 测试
     * @author will
     * @date 2024/6/6 16:38
     * @return ApiResult
     */
    @PostMapping("/test")
    public ApiResult test() {
        VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.IN_USABLE.getCode());
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSourceId("1798605941281656834");
        outInStockDTO.setSourceCode("QTRK24060600002");
        outInStockDTO.setSourceType(InventorySourceTypeEnum.OTHER_INSTOCK);
        outInStockDTO.setSourceDetailId("1798605946956550146");
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId("1777913262612942850");
        outInStockDTO.setSkuNo("HU01");
        outInStockDTO.setWarehouseId("1683286797712363522");
        outInStockDTO.setVirtualWarehouseId("1797906126839144449");
        outInStockDTO.setQty(10);
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();
        paramList.add(outInStockDTO);
        dto.setParamList(paramList);
        virtualInventoryTransCoreService.approve(dto);
        return success();
    }
    /**
     * 测试
     * @author will
     * @date 2024/6/6 16:38
     * @return ApiResult
     */
    @PostMapping("/test1")
    public ApiResult test1() {
/*        VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.IN_USABLE.getCode());
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSourceId("1798605941281656834");
        outInStockDTO.setSourceCode("QTRK24060600002");
        outInStockDTO.setSourceType(InventorySourceTypeEnum.OTHER_INSTOCK);
        outInStockDTO.setSourceDetailId("1798605946956550146");
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId("1777913262612942850");
        outInStockDTO.setSkuNo("HU01");
        outInStockDTO.setWarehouseId("1683286797712363522");
        outInStockDTO.setVirtualWarehouseId("1797906126839144449");
        outInStockDTO.setQty(10);
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();
        paramList.add(outInStockDTO);
        dto.setParamList(paramList);*/
        InventoryUnApproveDTO dto = new InventoryUnApproveDTO();
        dto.setBillId("1798605941281656834");
        dto.setSourceType(InventorySourceTypeEnum.OTHER_INSTOCK);
        virtualInventoryTransCoreService.unApprove(dto);
        return success();
    }

    /**
     * 处理历史数据（直接生成库龄流水）
     * @author will
     * @date 2024/12/17 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/handleAddDetail")
    public ApiResult handleAddDetail(@RequestBody VirtualTransFlowDetailDTO.HandleDTO dto) {
        virtualTransFlowService.handleAddDetail(dto);
        return success() ;
    }
}
