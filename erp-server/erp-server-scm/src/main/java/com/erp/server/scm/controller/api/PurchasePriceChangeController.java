package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.server.scm.query.PurchasePriceChangeQueryHandler;
import com.erp.server.scm.service.PurchasePriceChangeService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 采购价目变更管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("采购价目表")
@RequestMapping("/purchase/price/change")
public class PurchasePriceChangeController extends BaseController {


    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;


    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;


    /**
     * 采购价目变更分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:paging",
            tableAlias = "pp")
    @WebAdvanceQuery(handler = PurchasePriceChangeQueryHandler.class)
    public ApiResult<PagingVO<PurchasePriceChangeDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto) {
        PagingVO<PurchasePriceChangeDTO.PagingViewDTO> pagingVO = purchasePriceChangeService.paging(dto);
        return success(pagingVO);
    }


    /**
     * tab列表
     * @author Will
     * @date: 2024/1/20 9:20
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tab/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:paging",
            tableAlias = "pp")
    public ApiResult<List<PurchasePriceChangeDTO.TabListDTO>> tabList(PermissionsDTO dto) {
        List<PurchasePriceChangeDTO.TabListDTO> list = purchasePriceChangeService.tabList(dto);
        return success(list);
    }

    /**
     * 添加采购变更
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加采购变更")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:change:add",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
        String id = purchasePriceChangeService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 采购价目表  点击变更报价获取详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/priceChangeDetail")
    public ApiResult<PurchasePriceChangeDTO.ViewDTO> priceChangeDetail(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        PurchasePriceChangeDTO.ViewDTO view = purchasePriceDetailService.priceChangeDetail(dto.getIds());
        return success(view);
    }


    /**
     * 新增变更  获取对应变更sku列表
     *
     * @return
     */
    @PostMapping("/getSkuChangeList")
    public ApiResult<List<PurchasePriceChangeDetailDTO.ViewDTO>> getSkuChangeList(@RequestBody PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto) {
        List<PurchasePriceChangeDetailDTO.ViewDTO> list = purchasePriceChangeService.getSkuChangeList(dto);
        return success(list);
    }



    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购变更")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:add",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
        Boolean result = purchasePriceChangeService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 采购价目变更详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:priceChangeDetail",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult<PurchasePriceChangeDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        PurchasePriceChangeDTO.ViewDTO view = purchasePriceChangeService.view(dto.getId());
        return success(view);
    }


    /**
     * 修改采购价目变更
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购价目变更")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:update",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PurchasePriceChangeDTO.UpdateDTO dto) {
        String id = purchasePriceChangeService.updatePurchasePriceChange(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核采购价目变更")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:update",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchasePriceChangeDTO.UpdateDTO dto) {
        Boolean result = purchasePriceChangeService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 删除采购价目
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购价目变更")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:delete",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceChangeService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 采购价目变更提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购价目变更")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:submit",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceChangeService.submitApprove(dto.getIds(),Boolean.TRUE);
        return result == true ? success() : failure();
    }


    /**
     * 采购价目变更审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购价目变更")
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceChangeEntity> entityList = purchasePriceChangeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目变更记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceChangeService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购价目审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 取消流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-23 17:57
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购价目变更")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:cancelProcess",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceChangeService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 更新明细备注
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新采购价目变更明细备注:ids={ids},备注={remark}")
    @PostMapping("/updateDetailRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:cancelProcess",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult updateDetailRemark(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto) {
        Boolean result = purchasePriceChangeService.updateDetailRemark(dto.getIds(),dto.getRemark());
        return result ? success() : failure();
    }

    /**
     * 采购调价表数据导出
     * @author Will
     * @date: 2023/10/18 16:30
     * @param dto
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购调价表数据导出")
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:change:paging",
            tableAlias = "pp")
    @WebAdvanceQuery(handler = PurchasePriceChangeQueryHandler.class)
    public ApiResult export(@RequestBody @Valid PurchasePriceChangeDTO.PagingParamDTO dto, HttpServletResponse response) {
        purchasePriceChangeService.export(dto, response);
        return success();
    }



    /**
     * 修复历史数据
     *
     * @return
     */
    @PostMapping("/updateHistoryDb")
    public ApiResult tempUpdateHistoryDb() {
        purchasePriceChangeService.tempUpdateHistoryDb();
        return success();
    }
}
