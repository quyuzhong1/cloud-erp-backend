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
     * 确认关联售后单（预入库单维度批量关联）
     * <p>页面点击"确定关联"时调用：afterSaleList 为在候选售后单列表
     * （{@code SoReturnController.pagingLinkAfterSale}）中勾选的售后单明细行。
     * 服务端按 SKU 比较本次勾选退货明细与预入库单未关联明细数量：完全一致则完成关联；
     * 预入库单明细多于退货明细则仅关联对应 SKU（部分关联）；退货明细超出预入库单则整批拒绝。</p>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "确认关联售后单")
    @PostMapping("/confirmLinkAfterSale")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:linkAfterSale",
            serviceClass = SoReturnPrestockService.class,
            keyIdName = "mainId"
    )
    public ApiResult<BatchResultDTO> confirmLinkAfterSale(
            @RequestBody @Validated SoReturnPrestockDetailDTO.ConfirmLinkAfterSale dto) {
        BatchResultDTO result = soReturnPrestockService.confirmLinkAfterSale(dto);
        return result.getSuccess() ? success(result) : failure(result);
    }

    /**
     * 批量关联店铺
     * <p>入参 ids 为预入库单主表 ID 列表，支持一次选中多张预入库单关联到同一店铺；
     * B2B 与 B2C 关联的店铺不同，混合单据类型的预入库单不允许在同一次操作中关联，整批拒绝。</p>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量关联店铺")
    @PostMapping("/linkShop")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:linkShop",
            serviceClass = SoReturnPrestockService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> linkShop(@RequestBody @Validated SoReturnPrestockDetailDTO.LinkShop dto) {
        List<BatchResultDTO> results = soReturnPrestockService.linkShop(dto);
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 确认关联店铺（明细维度，逐行选择店铺）
     * <p>页面点击"确定关联"时调用：shopList 为在产品明细中逐行选择了店铺的未关联行，每行填写认领数量
     * （默认 = 退货数量）。与 {@link #linkShop 批量关联店铺} 不同，本接口允许同一张预入库单内不同明细行
     * 分别关联到不同店铺，并支持按认领数量拆行；认领相同店铺的行合并生成一张《退货入库单》。</p>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "确认关联店铺")
    @PostMapping("/confirmLinkShop")
    @DataPermission(
            operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnPrestock:linkShop",
            serviceClass = SoReturnPrestockService.class,
            keyIdName = "mainId"
    )
    public ApiResult<BatchResultDTO> confirmLinkShop(
            @RequestBody @Validated SoReturnPrestockDetailDTO.ConfirmLinkShop dto) {
        BatchResultDTO result = soReturnPrestockService.confirmLinkShop(dto);
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
