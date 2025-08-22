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
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.server.wms.service.FbaInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * FBA库存
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("FBA库存")
@RequestMapping("/fbaInventory")
public class FbaInventoryController extends BaseController {

    @Resource
    private FbaInventoryService fbaInventoryService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/10/31 10:32
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.FbaInventoryDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "fi.warehouse_id",
            menuCode = "wms:fbaInventory:paging",
            tableAlias = "fi"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<FbaInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaInventoryDTO.PagingParamDTO> dto) {
        PagingVO<FbaInventoryDTO.ListDTO> list = fbaInventoryService.paging(dto);
        return success(list);
    }

    /**
     * 列表汇总数量
     * @Author Luo_WG
     * @Date 2023/11/9 11:42
     * @param pagingParamDTO
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.FbaInventoryDTO.SummaryNumber>
     **/
    @PostMapping("/summaryNumber")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "fi.warehouse_id",
            menuCode = "wms:fbaInventory:paging",
            tableAlias = "fi"
    )
    public ApiResult<FbaInventoryDTO.SummaryNumber> summaryNumber(@RequestBody @Validated PagingDTO<FbaInventoryDTO.PagingParamDTO> pagingParamDTO) {
        FbaInventoryDTO.SummaryNumber result = fbaInventoryService.summaryNumber(pagingParamDTO);
        return success(result);
    }

    /**
     * 导出Excel数据
     * @author Luo_WG
     * @date:  2023-10-30
     * @param dto
     * @return
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "FBA库存导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated FbaInventoryDTO.ExportDTO dto) {
        fbaInventoryService.exportList(dto);
        return success(true);
    }

    /**
     * 查询预留明细
     * @Author Luo_WG
     * @Date 2023/11/8 18:26
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaInventoryDTO.InventoryReservedView>>
     **/
    @GetMapping("/listInventoryReserved")
    public ApiResult<FbaInventoryDTO.InventoryReservedView> listInventoryReserved(@RequestParam(value = "id") String id) {
        FbaInventoryDTO.InventoryReservedView result = fbaInventoryService.listInventoryReserved(id);
        return success(result);
    }

    /**
     * 获取FBA库存信息
     * @Author zdy
     * @Date 2025/08/21 11:42
     * @param queryDTO
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.FbaInventoryDTO.SummaryNumber>
     **/
    @PostMapping("/listFbaInventory")
    public ApiResult<List<FbaInventoryDTO.InventoryDTO>> listFbaInventory(@RequestBody @Validated FbaInventoryDTO.QueryDTO queryDTO) {
        List<FbaInventoryDTO.InventoryDTO> list = fbaInventoryService.listFbaInventory(queryDTO);
        return success(list);
    }

    /**
     * 获取FBA库存树状结构
     * @Author zdy
     * @Date 2025/08/21 11:42
     * @param queryDTO
     * @return
     */
    @PostMapping("/fbaInventoryTree")
    public ApiResult<HashMap<String, List<FbaInventoryDTO.InventoryDTO>>> fbaInventoryTree(@RequestBody @Validated FbaInventoryDTO.QueryDTO queryDTO) {
        HashMap<String, List<FbaInventoryDTO.InventoryDTO>> map = fbaInventoryService.fbaInventoryTree(queryDTO);
        return success(map);
    }
}
