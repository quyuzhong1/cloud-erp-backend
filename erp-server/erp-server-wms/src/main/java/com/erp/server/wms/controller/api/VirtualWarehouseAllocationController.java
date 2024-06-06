package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.VirtualWarehouseAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;

import java.util.List;

/**
 * 虚拟仓分货单
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓分货单")
@RequestMapping("/virtualWarehouseAllocation")
public class VirtualWarehouseAllocationController extends BaseController {

    @Resource
    private VirtualWarehouseAllocationService virtualWarehouseAllocationService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓分货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseAllocationDTO.AddDTO dto) {
        return success(virtualWarehouseAllocationService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓分货单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:update",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehouseAllocationDTO.UpdateDTO dto) {
        virtualWarehouseAllocationService.update(dto);
        return success();
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date: 2024-06-05
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouseAllocation:paging",
            tableAlias = "vwa"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<VirtualWarehouseAllocationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualWarehouseAllocationDTO.PagingParamDTO> dto) {
        return success(virtualWarehouseAllocationService.paging(dto));
    }

    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交虚拟仓分货单信息")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:virtualWarehouseAllocation:submit",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> submit = virtualWarehouseAllocationService.submit(dto.getIds());
        return submit.stream().anyMatch(BatchResultDTO::getSuccess) ? success(submit) : failure(submit);
    }

    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交虚拟仓分货单信息")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:virtualWarehouseAllocation:submit",
            serviceClass = VirtualWarehouseAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> submit = virtualWarehouseAllocationService.invalid(dto);
        return submit.stream().anyMatch(BatchResultDTO::getSuccess) ? success(submit) : failure(submit);
    }

}
