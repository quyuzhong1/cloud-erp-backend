package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
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
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.server.scm.service.PurchasePriceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 采购价目管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@RestController
@LogSystemModule("采购价目表")
@RequestMapping("/purchase/price")
public class PurchasePriceController extends BaseController {

    @Resource
    private PurchasePriceService purchasePriceService;


    /**
     * 采购价目分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:paging",
            tableAlias = "pp")
    public ApiResult<PagingVO<PurchasePriceDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        PagingVO<PurchasePriceDTO.PagingViewDTO> pagingVO = purchasePriceService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加采购价目表
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加采购价目表")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:add",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        String id = purchasePriceService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 列表tab数据
     *
     * @param dto
     * @return
     */
    @PostMapping("/tab/list")
    public ApiResult tabList(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        return success();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "提交并审核采购价目")
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        Boolean result = purchasePriceService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 采购价目详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:view",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult<PurchasePriceDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        PurchasePriceDTO.ViewDTO view = purchasePriceService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改采购价目
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购价目")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:update",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PurchasePriceDTO.UpdateDTO dto) {
        String id = purchasePriceService.updatePurchasePrice(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核采购价目")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:update",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchasePriceDTO.UpdateDTO dto) {
        Boolean result = purchasePriceService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 删除采购价目
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购价目")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:delete",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 采购价目提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购价目")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:submit",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceService.submitApprove(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购价目")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:approve",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = purchasePriceService.approve(dto);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购价目")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:cancelProcess",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 采购价目数据导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购价目数据导出")
    @PostMapping("/exportPurchasePrice")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:paging",
            tableAlias = "pp")
    public ApiResult exportPurchasePrice(@RequestBody @Valid PurchasePriceDTO.ExportDTO dto, HttpServletResponse response) {
        purchasePriceService.exportPurchasePrice(dto, response);
        return success();
    }

    /**
     * 批量导入
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购价目数据批量导入")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = purchasePriceService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板采购价目")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        purchasePriceService.downloadTemplate(response);
        return success();
    }

    /**
     * 反审核
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核采购价目")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:disApprove",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids"
    )
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceService.disApprove(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 更新明细备注
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新明细备注采购价目:ids={ids},明细备注={remark}")
    @PostMapping("/updateDetailRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:updateDetailRemark",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids"
    )
    public ApiResult updateDetailRemark(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto) {
        Boolean result = purchasePriceService.updateDetailRemark(dto.getIds(),dto.getRemark());
        return result ? success() : failure();
    }

}
