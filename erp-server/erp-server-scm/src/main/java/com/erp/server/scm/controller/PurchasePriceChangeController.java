package com.erp.server.scm.controller;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.server.scm.service.PurchasePriceChangeService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购价目变更管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@RestController
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
    public ApiResult<PagingVO<PurchasePriceChangeDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto) {
        PagingVO<PurchasePriceChangeDTO.PagingViewDTO> pagingVO = purchasePriceChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加采购变更
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:change:add",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult saveOrUpdate(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
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
    public ApiResult<PurchasePriceChangeDTO.ViewDTO> priceChangeDetail(@RequestBody @Validated BaseIdDTO dto) {
        PurchasePriceChangeDTO.ViewDTO view = purchasePriceDetailService.priceChangeDetail(dto.getId());
        return success(view);
    }


    /**
     * 新增变更  获取对应变更sku列表
     *
     * @return
     */
    @GetMapping("/getSkuChangeList")
    public ApiResult<List<PurchasePriceChangeDetailDTO.ViewDTO>> getSkuChangeList(@RequestParam(value = "purchasePriceId") String purchasePriceId) {
        List<PurchasePriceChangeDetailDTO.ViewDTO> list = purchasePriceChangeService.getSkuChangeList(purchasePriceId);
        return success(list);
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:addAndSubmit",
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
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:view",
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
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:updateAndSubmit",
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
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:delete",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
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
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:submit",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceChangeService.submitApprove(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 采购价目变更审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:approve",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = purchasePriceChangeService.approve(dto);
        return result == true ? success() : failure();
    }


    /**
     * 取消流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-23 17:57
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:cancelProcess",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceChangeService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }
}
