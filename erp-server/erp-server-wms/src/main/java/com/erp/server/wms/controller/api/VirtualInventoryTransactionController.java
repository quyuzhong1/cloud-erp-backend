package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.VirtualInventoryTransactionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualInventoryTransactionDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;

/**
 * 虚拟仓库存事务表
 *
 * @author shukai
 * @since 2025-12-18
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓库存事务表")
@RequestMapping("/virtualInventoryTransaction")
public class VirtualInventoryTransactionController extends BaseController {

    @Resource
    private VirtualInventoryTransactionService virtualInventoryTransactionService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-12-18
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓库存事务表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualInventoryTransactionDTO.AddDTO dto) {
        return success(virtualInventoryTransactionService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-12-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓库存事务表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualInventoryTransaction:update",
        serviceClass = VirtualInventoryTransactionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualInventoryTransactionDTO.UpdateDTO dto) {
        virtualInventoryTransactionService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualInventoryTransaction:paging",
            tableAlias = ""
    )
    public ApiResult<List<VirtualInventoryTransactionDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(virtualInventoryTransactionService.tabList(dto));
    }

    /**
    * 列表查询
    * @author shukai
    * @date: 2025-12-18
    * @param dto
    * @return ApiResult<PagingVO<VirtualInventoryTransactionDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualInventoryTransaction:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<VirtualInventoryTransactionDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualInventoryTransactionDTO.PagingParamDTO> dto) {
        return success(virtualInventoryTransactionService.paging(dto));
    }


    /**
    * 详情
    * @author shukai
    * @date:  2025-12-18
    * @param id
    * @return ApiResult<VirtualInventoryTransactionDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualInventoryTransaction:view",
            serviceClass = VirtualInventoryTransactionService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<VirtualInventoryTransactionDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(virtualInventoryTransactionService.view(id));
    }

    /**
    * 导出Excel数据
    * @author shukai
    * @date:  2025-12-18
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualInventoryTransaction:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "虚拟仓库存事务表导出Excel数据")
    public void exportList(@RequestBody @Validated VirtualInventoryTransactionDTO.ExportDTO dto, HttpServletResponse response) {
        virtualInventoryTransactionService.exportList(dto, response);
    }


}
