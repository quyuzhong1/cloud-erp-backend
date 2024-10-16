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
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseChangeDetailEntity;
import com.erp.model.scm.entity.PurchaseChangeEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.service.PurchaseChangeDetailService;
import com.erp.server.scm.service.PurchaseChangeService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采购变更管理
 *
 * @author will
 * @since 2023-03-16
 */
@Slf4j
@RestController
@LogSystemModule("采购变更单")
@RequestMapping("/purchaseChange")
public class PurchaseChangeController extends BaseController {

    @Resource
    private PurchaseChangeService purchaseChangeService;
    @Resource
    private PurchaseChangeDetailService purchaseChangeDetailService;
    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<List<ScmSalesDemandDTO>>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:paging",
            tableAlias = "pc")
    @WebAdvanceQuery
    public ApiResult<PagingVO<PurchaseChangeDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseChangeDTO.ListDTO> pagingVO = purchaseChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查询数量
     * @author Will
     * @date: 2023/3/15 17:34
     * @return ApiResult
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:paging",
            tableAlias = "pc")
    public ApiResult<List<ListStatusCountDTO.PurchaseChangeCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<ListStatusCountDTO.PurchaseChangeCountDTO> list = purchaseChangeService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购变更单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:add",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated PurchaseChangeDTO.AddDTO dto) {
        purchaseChangeService.add(dto);
        return success();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购变更单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:update",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PurchaseChangeDTO.UpdateDTO dto) {
        Boolean flag = purchaseChangeService.update(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 新增并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购变更单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:add",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseChangeDTO.AddDTO dto) {
        Boolean flag = purchaseChangeService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交采购变更单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:update",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchaseChangeDTO.UpdateDTO dto) {
        Boolean flag = purchaseChangeService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<ScmPurchaseChangeDTO>
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:view",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "id")
    public ApiResult<PurchaseChangeDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseChangeDTO.ViewDTO dto = purchaseChangeService.view(id);
        return success(dto);
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购变更单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:cancelProcess",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchaseChangeService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "批量作废采购变更单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:invalid",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = purchaseChangeService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }


    /**
     * 批量提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "批量提交采购变更单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:submit",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = purchaseChangeService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审核采购变更单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:approve",
            serviceClass = PurchaseChangeService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchaseChangeEntity> entityList = purchaseChangeService.listByIds(dto.getIds());
        //查询原采购订单明细信息
        List<PurchaseChangeDetailEntity> purchaseChangeDetailEntityList = purchaseChangeDetailService.listByPurchaseChangeIds(dto.getIds());
        List<String> purchaseOrderDetailIds = purchaseChangeDetailEntityList.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList =  purchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        //修改到货状态
        List<String> podIds = purchaseChangeDetailEntityList.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PoReturnDetailEntity> returnDetailEntityList = wmsTaskFeign.listReturnOrderDetailByPodIds(podIds);
        List<WarehouseReceiveDetailEntity> receiveDetailEntityList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);
        for (String id : dto.getIds()) {
            PurchaseChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购变更单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchaseChangeService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),
                        purchaseChangeDetailEntityList, purchaseOrderDetailEntityList,returnDetailEntityList, receiveDetailEntityList));
            }catch (Exception e){
                log.error("采购变更单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购变更单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseChangeDTO.SearchParamDTO dto) {
        Boolean flag = purchaseChangeService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 采购订单-关联变更单
     * @author Will
     * @date: 2023/4/3 14:32
     * @param dto
     * @return ApiResult<AssociatedDocumentDTO>
     */
    @PostMapping("/viewAssociatedDocuments")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "change_user_id",
            menuCode = "scm:purchaseChange:paging",
            tableAlias = "pc")
    public ApiResult<List<PurchaseChangeDTO.ListDTO>> viewAssociatedDocuments(@RequestBody @Validated BaseIdDTO dto) {
        List<PurchaseChangeDTO.ListDTO> resultDTO = purchaseChangeService.list(dto);
        return success(resultDTO);
    }

}
