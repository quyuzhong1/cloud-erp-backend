package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDetailDTO;
import com.erp.server.wms.query.SoReturnPrestockQueryHandler;
import com.erp.server.wms.service.SoReturnPrestockDetailService;
import com.erp.server.wms.service.SoReturnPrestockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 预入库单
 *
 * @author auto
 * @since 2026-06-30
 */
@Slf4j
@RestController
@LogSystemModule("预入库单")
@RequestMapping("/soReturnPrestock")
public class SoReturnPrestockController extends BaseController {

    @Resource
    private SoReturnPrestockService soReturnPrestockService;

    /**
     * 分页查询
     */
    @PostMapping("/paging")
    @DataPermission(
            operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "srp.warehouse_id",
            menuCode = "wms:soReturnPrestock:paging",
            tableAlias = "srp"
    )
    @WebAdvanceQuery(handler = SoReturnPrestockQueryHandler.class)
    public ApiResult<PagingVO<SoReturnPrestockDTO.PagingView>> paging(
            @RequestBody @Validated PagingDTO<SoReturnPrestockDTO.PagingParam> dto) {
        return success(soReturnPrestockService.paging(dto));
    }

    /**
     * 手动新增
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增预入库单")
    @PostMapping("/save")
    public ApiResult<String> add(@RequestBody @Validated SoReturnPrestockDTO.Add dto) {
        String id = soReturnPrestockService.add(dto);
        return success(id);
    }

    /**
     * 由【退货入库单-新增】表单参数创建预入库单
     * <p>前提：退货客户为空（否则应直接保存退货入库单）；退货物流单号非空。</p>
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "由退货入库单新增页发起-新增预入库单")
    @PostMapping("/saveFromInstockAdd")
    public ApiResult<String> saveFromInstockAdd(@RequestBody @Validated SoReturnInstockDTO.Add dto) {
        return success(soReturnPrestockService.addFromReturnInstockAdd(dto));
    }

    /**
     * 由【退货入库单-修改】表单参数创建预入库单
     * <p>前提同上；仅使用表单字段值新建预入库单，不影响原退货入库单记录。</p>
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "由退货入库单修改页发起-新增预入库单")
    @PostMapping("/saveFromInstockUpdate")
    public ApiResult<String> saveFromInstockUpdate(@RequestBody @Validated SoReturnInstockDTO.Update dto) {
        return success(soReturnPrestockService.addFromReturnInstockUpdate(dto));
    }

    /**
     * 修改
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改预入库单")
    @PostMapping("/update")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:update",
            serviceClass = SoReturnPrestockService.class,
            keyIdName = "id"
    )
    public ApiResult<Boolean> update(@RequestBody @Validated SoReturnPrestockDTO.Update dto) {
        return success(soReturnPrestockService.update(dto));
    }

    /**
     * 详情查询
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:view",
            serviceClass = SoReturnPrestockService.class,
            keyIdName = "id"
    )
    public ApiResult<SoReturnPrestockDTO.View> view(@RequestParam("id") String id) {
        return success(soReturnPrestockService.view(id));
    }

    /**
     * 关联售后单
     * <p>支持拆行：本次关联数量 &lt; 当前行退货数量时，自动拆分剩余数量为新行。</p>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "关联售后单")
    @PostMapping("/linkAfterSale")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:linkAfterSale",
            serviceClass = SoReturnPrestockDetailService.class,
            keyIdName = "detailId"
    )
    public ApiResult<BatchResultDTO> linkAfterSale(
            @RequestBody @Validated SoReturnPrestockDetailDTO.LinkAfterSale dto) {
        BatchResultDTO result = soReturnPrestockService.linkAfterSale(dto);
        return result.getSuccess() ? success(result) : failure(result);
    }

    /**
     * 关联店铺
     * <p>支持拆行：本次关联数量 &lt; 当前行退货数量时，自动拆分剩余数量为新行。</p>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "关联店铺")
    @PostMapping("/linkShop")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:linkShop",
            serviceClass = SoReturnPrestockDetailService.class,
            keyIdName = "detailId"
    )
    public ApiResult<BatchResultDTO> linkShop(
            @RequestBody @Validated SoReturnPrestockDetailDTO.LinkShop dto) {
        BatchResultDTO result = soReturnPrestockService.linkShop(dto);
        return result.getSuccess() ? success(result) : failure(result);
    }

    /**
     * 批量删除（软删）
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除预入库单 id：{ids}")
    @PostMapping("/delete")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:delete",
            serviceClass = SoReturnPrestockService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(
            @RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        List<BatchResultDTO> results = soReturnPrestockService.deleteByIds(idsDTO.getIds());
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }
}
