package com.erp.server.wms.controller.pda;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:采购收货单
 * @Author Luo_WG
 * @Date 2023/8/11 10:08
 **/
@RestController
@RequestMapping(value = "/pdaPoReceive")
public class PdaPoReceiveController extends BaseController {
    @Resource
    private WarehouseReceiveService warehouseReceiveService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/11 10:17
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:pdaPoReceive:paging",
            tableAlias = "wr"
    )
    public ApiResult<PagingVO<WarehouseReceiveDTO.PdaPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<WarehouseReceiveDTO.PdaPagingParamDTO> dto) {
        PagingVO<WarehouseReceiveDTO.PdaPagingViewDTO> pagingVO = warehouseReceiveService.pdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/11 10:18
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPoReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:pdaPoReceive:paging",
            tableAlias = "wr"
    )
    public ApiResult<List<WarehouseReceiveDTO.PdaPoReceiveCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<WarehouseReceiveDTO.PdaPoReceiveCountDTO> warehouseReceiveCountDTOS = warehouseReceiveService.pdaListCount(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/11 10:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated WarehouseReceiveDTO.AddDTO dto) {
        String id = warehouseReceiveService.pdaAdd(dto);
        return StringUtils.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:pdaPoReceive:update",
            serviceClass = WarehouseReceiveService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WarehouseReceiveDTO.UpdateDTO dto) {
        Boolean flag = warehouseReceiveService.update(dto);
        return flag == true ? success() : failure();
    }
}
