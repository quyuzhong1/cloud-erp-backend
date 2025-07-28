package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
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
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.server.wms.query.PoInStockQueryHandler;
import com.erp.server.wms.service.PoInstockService;
import com.erp.server.wms.service.PoReturnService;
import com.erp.server.wms.service.SubcontractIssueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采购入库单
 *
 * @author will
 * @since 2023-04-10
 */
@Slf4j
@RestController
@LogSystemModule("采购入库单")
@RequestMapping("/poInStock")
public class PoInStockController extends BaseController {

    @Resource
    private PoInstockService poInstockService;
    @Resource
    private PoReturnService poReturnService;
    @Resource
    private SubcontractIssueService subcontractIssueService;
    /**
     * 列表查询
     * @author Will
     * @date: 2023/4/11 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            warehouseTableField = "psi.delivery_warehouse_id",
            menuCode = "wms:poInStock:paging",
            tableAlias = "psi"
    )
    @WebAdvanceQuery(handler = PoInStockQueryHandler.class)
    public ApiResult<PagingVO<PoInstockDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoInstockDTO.SearchParamDTO> dto) {
        PagingVO<PoInstockDTO.ListDTO> pagingVO = poInstockService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表查询总数
     *
     * @param dto
     * @return
     */
    @PostMapping("/pagingTotal")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            warehouseTableField = "psi.delivery_warehouse_id",
            menuCode = "wms:poInStock:paging",
            tableAlias = "psi"
    )
    @WebAdvanceQuery(handler = PoInStockQueryHandler.class)
    public ApiResult<PoInstockDTO.PagingTotalDTO> pagingTotal(@RequestBody @Validated PoInstockDTO.SearchParamDTO  dto) {
        PoInstockDTO.PagingTotalDTO viewDTO = poInstockService.pagingTotal(dto);
        return success(viewDTO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/4/11 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            warehouseTableField = "psi.delivery_warehouse_id",
            menuCode = "wms:poInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<List<PoInstockDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PoInstockDTO.ListStatusCountDTO> list = poInstockService.listCount(dto);
        return success(list);
    }

   /**
    * 新增
    * @author Will
    * @date: 2023/4/11 19:58
    * @param dto
    * @return ApiResult<?>
    */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购入库单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:add",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<?> add(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        PoInstockEntity entity = poInstockService.add(dto,Boolean.FALSE);
        return success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode()));
    }

   /**
    * 新增并提交
    * @author Will
    * @date: 2023/4/11 19:59
    * @param dto
    * @return ApiResult
    */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购入库单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:add",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<?> addAndSubmit(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        PoInstockEntity entity = poInstockService.addAndSubmit(dto);
        return success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode()));
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/4/11 20:02
     * @param dto
     * @return ApiResult<?>
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购入库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:update",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<?>update(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/4/11 20:02
     * @param dto
     * @return ApiResult<?>
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交采购入库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:update",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<?>updateAndSubmit(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/4/11 20:00
     * @param dto
     * @return ApiResult<?>
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购入库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:submit",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<?> submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PoInstockEntity> entityMap = poInstockService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购入库单不存在"));
                continue;
            }
            try {
                resultDTOS.add(poInstockService.submitEntity(entity));
            }catch (Exception e){
                log.error("采购入库单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/4/11 20:10
     * @param id
     * @return ApiResult
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:view",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<PoInstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        PoInstockDTO.ViewDTO dto = poInstockService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/4/11 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购入库单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:delete",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<?>delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PoInstockEntity> entityMap = poInstockService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购入库单不存在"));
                continue;
            }
            try {
                resultDTOS.add(poInstockService.deleteEntity(entity));
            }catch (Exception e){
                log.error("采购入库单删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/4/11 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废采购入库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:invalid",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<?>invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PoInstockEntity> entityMap = poInstockService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购入库单不存在"));
                continue;
            }
            try {
                resultDTOS.add(poInstockService.invalidEntity(entity, dto.getRemark()));
            }catch (Exception e){
                log.error("采购入库单作废失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/4/11 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购入库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:approve",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto)     {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PoInstockEntity> entityList = poInstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购入库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(poInstockService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购入库单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/4/11 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核采购入库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:disApprove",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PoInstockEntity> entityList = poInstockService.listByIds(dto.getIds());
        List<PoReturnEntity> purchaseReturnOrderList = poReturnService.listBySourceIds(dto.getIds());
        List<SubcontractIssueEntity> subcontractIssueList = subcontractIssueService.listBySourceIdList(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购入库单记录不存在"));
                continue;
            }
            List<PoReturnEntity> returnEntityList = purchaseReturnOrderList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getSourceId()) && e.getSourceId().equals(id)).collect(Collectors.toList());
            List<SubcontractIssueEntity> issueEntityList = subcontractIssueList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getSourceId()) && e.getSourceId().equals(id)).collect(Collectors.toList());
            try {
                resultDTOS.add(poInstockService.disApprove(entity,returnEntityList, issueEntityList));
            }catch (Exception e){
                log.error("采购入库单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/4/11 20:24
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购入库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:cancelProcess",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<?>cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PoInstockEntity> entityMap = poInstockService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购入库单不存在"));
                continue;
            }
            try {
                resultDTOS.add(poInstockService.cancelProcess(entity));
            }catch (Exception e){
                log.error("采购入库单撤销失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 导出
     * @author Will
     * @date: 2023/4/11 20:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购入库单")
    @PostMapping(value = "/exportExcel")
    public ApiResult<?>exportExcel(@RequestBody PoInstockDTO.ExportParamDTO dto) {
        Boolean flag = poInstockService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货单数据显示
     * @author Will
     * @date: 2023/4/11 20:30
     * @param dto
     * @return ApiResult<ViewGeneratePurchaseReturnOrderDTO>
     */
    @PostMapping("/viewGeneratePurchaseReturnOrder")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id,create_user_id",
            menuCode = "wms:poInStock:viewGeneratePurchaseReturnOrder",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>> viewGeneratePurchaseReturnOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = poInstockService.viewGeneratePurchaseReturnOrder(dto.getIds());
        return success(list);
    }

    /**
     * 下推退货单数据保存
     * @author Will
     * @date: 2023/4/11 20:33
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推退货单数据保存")
    @PostMapping("/generatePurchaseReturnOrder")
    public ApiResult<?>generatePurchaseReturnOrder(@RequestBody @Validated PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        Boolean flag = poInstockService.generatePurchaseReturnOrder(dto);
        return flag == true ? success() : failure();
    }

   /**
    * 采购订单-关联入库单
    * @author Will
    * @date: 2023/4/19 16:28
    * @param dto
    * @return ApiResult<List<OrderRefStockInDTO>>
    */
    @PostMapping(value = "/purchaseOrderRefStockIn")
    public ApiResult<List<PoInstockDTO.OrderRefStockInDTO>> purchaseOrderRefStockIn(@RequestBody @Validated BaseIdDTO dto) {
        List<PoInstockDTO.OrderRefStockInDTO> list = poInstockService.purchaseOrderRefStockIn(dto.getId());
        return success(list);
    }

    /**
     * 下推采购入库单保存
     * @author Will
     * @date: 2023/4/13 11:37
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推采购入库单保存")
    @PostMapping("/generateStockIn")
    public ApiResult<?> generateStockIn(@RequestBody @Validated PurchaseOrderDTO.ListGenerateStockInDTO dto) {
        Boolean flag = poInstockService.generateStockIn(dto);
        return flag == true ? success() : failure();
    }
}
