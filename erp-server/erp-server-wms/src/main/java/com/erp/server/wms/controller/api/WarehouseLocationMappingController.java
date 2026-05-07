package com.erp.server.wms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WarehouseLocationMappingDTO;
import com.erp.server.wms.query.WarehouseLocationMappingQueryHandler;
import com.erp.server.wms.service.WarehouseLocationMappingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 仓位绑定
 */
@RestController
@RequestMapping("/warehouseLocationMapping")
@LogSystemModule("仓位绑定")
public class WarehouseLocationMappingController extends BaseController {

    @Resource
    private WarehouseLocationMappingService warehouseLocationMappingService;

    /**
     * 分页查询仓位绑定列表
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = WarehouseLocationMappingQueryHandler.class)
    public ApiResult<PagingVO<WarehouseLocationMappingDTO.ViewDTO>> paging(@RequestBody PagingDTO<WarehouseLocationMappingDTO.SearchDTO> dto) {
        return success(warehouseLocationMappingService.paging(dto));
    }

    /**
     * 新增仓位绑定
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓位绑定")
    public ApiResult<Void> add(@RequestBody @Validated WarehouseLocationMappingDTO.AddDTO dto) {
        warehouseLocationMappingService.add(dto);
        return success();
    }

    /**
     * 编辑仓位绑定
     */
    @PostMapping("/edit")
    @LogAction(value = LogActionEnum.UPDATE, desc = "编辑仓位绑定")
    public ApiResult<Void> edit(@RequestBody @Validated WarehouseLocationMappingDTO.UpdateDTO dto) {
        warehouseLocationMappingService.update(dto);
        return success();
    }

    /**
     * 删除仓位绑定
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除仓位绑定")
    public ApiResult<Void> delete(@RequestBody @Validated WarehouseLocationMappingDTO.IdsDTO dto) {
        warehouseLocationMappingService.delete(dto);
        return success();
    }

    /**
     * 创建仓位绑定异步导入任务
     */
    @PostMapping("/importExcel")
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入仓位绑定")
    public ApiResult<Boolean> importExcel(@RequestBody @Validated BaseDTO.ImportDTO dto) {
        return success(warehouseLocationMappingService.importFile(dto));
    }

    /**
     * 下载仓位绑定导入模板
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        warehouseLocationMappingService.downloadTemplate(response);
    }

    /**
     * 根据ERP仓库和第三方系统查询DMP绑定仓库，仅用于页面展示
     */
    @GetMapping("/bindWarehouse")
    public ApiResult<WarehouseLocationMappingDTO.BindWarehouseDTO> getBindWarehouse(@RequestParam String sysWarehouseId,
                                                                                   @RequestParam String dictPlatform) {
        return success(warehouseLocationMappingService.getBindWarehouse(sysWarehouseId, dictPlatform));
    }
}
