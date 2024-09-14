package com.erp.server.scm.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.server.scm.service.SubcontractOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 委外订单
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@RestController
@LogSystemModule("委外订单")
@RequestMapping("/subcontractOrder")
public class SubcontractOrderController extends BaseController {

    @Autowired
    private SubcontractOrderService subcontractOrderService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:paging",
            tableAlias = "so"
    )
    public ApiResult<List<SubcontractOrderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(subcontractOrderService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2023-06-08
    * @param dto
    * @return ApiResult<PagingVO<SubcontractOrderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:paging",
            tableAlias = "so"
    )
    public ApiResult<PagingVO<SubcontractOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SubcontractOrderDTO.PagingParamDTO> dto) {
        return success(subcontractOrderService.paging(dto));
    }

    /**
     * 列表根据明细id查询采购订单
     * @author Will
     * @date: 2023/6/9 16:16
     * @param dto
     * @return ApiResult<List<PurchaseOrderDTO.ListDTO>>
     */
    @PostMapping("/listPurchaseOrderByDetailId")
    public ApiResult<List<PurchaseOrderDTO.ListDTO>> listPurchaseOrderByDetailId(@RequestBody @Validated BaseIdDTO dto) {
        return success(subcontractOrderService.listPurchaseOrderByDetailId(dto.getId()));
    }


   /**
   * 新增
   * @author will
   * @date:  2023-06-08
   * @param dto
   * @return ApiResult<Void>
   */
   @LogAction(value = LogActionEnum.INSERT, desc = "新增委外订单")
   @PostMapping("/add")
   public ApiResult<Void> add(@RequestBody @Validated SubcontractOrderDTO.AddDTO dto) {
      subcontractOrderService.add(dto);
      return success();
   }

    /**
    * 修改
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改委外订单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:update",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated SubcontractOrderDTO.UpdateDTO dto) {
        subcontractOrderService.update(dto);
        return success();
    }

//    /**
//     * 新增编辑-批量获取列表采购单价
//     * @param dto
//     * @return
//     */
//    @PostMapping("/batchGetPurchasePrice")
//    public ApiResult<SubcontractOrderDTO.PurchasePriceDTO> batchGetPurchasePrice(@RequestBody @Validated SubcontractOrderDTO.UpdateDTO dto) {
//        return subcontractOrderService.batchGetPurchasePrice(dto);
//    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交委外订单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:submit",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SubcontractOrderDTO.AddDTO dto) {
        subcontractOrderService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交委外订单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:submit",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SubcontractOrderDTO.UpdateDTO dto) {
        subcontractOrderService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交委外订单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:submit",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractOrderService.submit(dto.getIds());
        return success();
    }

    /**
    * 审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核委外订单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:approve",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = subcontractOrderService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("采购订单审核失败",e);
                SubcontractOrderEntity entity = subcontractOrderService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "采购订单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核委外订单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:disApprove",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = subcontractOrderService.disApprove(id);
            }catch (Exception e){
                log.error("委外订单反审核失败",e);
                SubcontractOrderEntity entity = subcontractOrderService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "委外订单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 结束交货
     * @author Will
     * @date: 2023/6/9 16:33
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "结束交货委外订单:ids={ids},备注={remark}")
    @PostMapping("/finishDelivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:finishDelivery",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult finishDelivery(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = subcontractOrderService.finishDelivery(dto.getIds(),dto.getRemark());
        return result == true ? success() : failure();
    }


    /**
    * 删除
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除委外订单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:delete",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractOrderService.delete(dto.getIds());
        return success();
    }

    /**
    * 撤销
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销委外订单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:cancelProcess",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractOrderService.cancelProcess(dto.getIds());
        return success();
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023-06-08
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "批量作废委外订单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:invalid",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        subcontractOrderService.invalid(dto.getIds(),dto.getRemark());
        return success();
    }

    /**
    * 详情
    * @author will
    * @date:  2023-06-08
    * @param id
    * @return ApiResult<SubcontractOrderDTO.ViewDTO>>
    */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:view",
            serviceClass = SubcontractOrderService.class,
            keyIdName = "id")
    public ApiResult<SubcontractOrderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(subcontractOrderService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @param response
    * @return
    */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出委外订单")
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:export",
            tableAlias = "so"
    )
    public void exportList(@RequestBody @Validated SubcontractOrderDTO.ExportDTO dto, HttpServletResponse response) {
        subcontractOrderService.exportList(dto, response);
    }


    /**
     * 下推采购单显示
     * @author Will
     * @date: 2023/6/12 14:00
     * @param dto
     * @return ApiResult<List<ViewGeneratePoDTO>>
     */
    @PostMapping(value = "/viewGeneratePo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractOrder:viewGeneratePo",
            tableAlias = "so"
    )
    public ApiResult<List<SubcontractOrderDTO.ViewGeneratePoDTO>> viewGeneratePo(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SubcontractOrderDTO.ViewGeneratePoDTO> list = subcontractOrderService.viewGeneratePo(dto.getIds());
        return success(list);
    }

    /**
     * 下推采购单保存
     * @author Will
     * @date: 2023/6/12 14:00
     * @param list
     * @return ApiResult<Void>
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "委外订单下推采购单保存")
    @PostMapping(value = "/generatePo")
    public ApiResult<Void> generatePo(@RequestBody @Validated ValidList<SubcontractOrderDTO.GeneratePoDTO> list) {
         subcontractOrderService.generatePo(list,Boolean.FALSE);
        return success();
    }
//    /**
//     * 下推采购订单-批量获取列表采购单价
//     * @param list
//     * @return
//     */
//    @PostMapping("/batchGetPoPurchasePrice")
//    public ApiResult<SubcontractOrderDTO.SubcontractPurchasePriceDTO> batchGetPoPurchasePrice(@RequestBody ValidList<SubcontractOrderDTO.GeneratePoDTO> list) {
//        return subcontractOrderService.batchGetPoPurchasePrice(list);
//    }

    /**
     * 添加已有产品显示
     * @author Will
     * @date: 2023/6/12 16:00
     * @param dto
     * @return ApiResult<List<ViewAddDetailDTO>>
     */
    @PostMapping(value = "/viewAddDetail")
    public ApiResult<List<SubcontractOrderDTO.ViewAddDetailDTO>> viewAddDetail(@RequestBody @Validated SubcontractOrderDTO.ViewAddDetailParamDTO dto) {
        List<SubcontractOrderDTO.ViewAddDetailDTO> list = subcontractOrderService.viewAddDetail(dto);
        return success(list);
    }


    /**
     * 委外变更数据显示
     * @author Will
     * @date: 2023/6/12 16:00
     * @param id
     * @return ApiResult<AddDTO>
     */
    @GetMapping("/viewSubcontractChange")
    public ApiResult<SubcontractChangeDTO.ViewDTO> viewSubcontractChange(@RequestParam("id") String id) {
        SubcontractChangeDTO.ViewDTO viewDTO = subcontractOrderService.viewSubcontractChange(id);
        return success(viewDTO);
    }

    /**
     * 委外订单下拉
     * @author Will
     * @date: 2024/1/10 19:16
     * @return ApiResult<List<ListSelectDTO>>
     */
    @GetMapping("/list")
    public ApiResult<List<SubcontractOrderDTO.ListSelectDTO>> list() {
        List<SubcontractOrderDTO.ListSelectDTO> list = subcontractOrderService.listSubcontractOrder();
        return success(list);
    }

}
