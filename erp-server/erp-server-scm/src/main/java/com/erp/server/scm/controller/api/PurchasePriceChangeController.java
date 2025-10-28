package com.erp.server.scm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.server.scm.query.PurchasePriceChangeQueryHandler;
import com.erp.server.scm.service.PurchasePriceChangeDetailService;
import com.erp.server.scm.service.PurchasePriceChangeService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购调价单管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("采购调价表")
@RequestMapping("/purchase/price/change")
public class PurchasePriceChangeController extends BaseController {


    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;
    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;
    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;


    /**
     * 采购调价单分页列表
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
    public ApiResult<?> add(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
        PurchasePriceChangeEntity entity = purchasePriceChangeService.add(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * 采购调价表  点击变更报价获取详情
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
    public ApiResult<Object> addAndSubmit(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
        //新增
        PurchasePriceChangeEntity entity;
        try {
            entity  = purchasePriceChangeService.add(dto);
            if (null == entity) {
                return failure(ApiError.ERROR_1019.msg,new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
            }
        } catch (ServiceException e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        } catch (Exception e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1019.msg,new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        }
        //提审
        try {
            entity = purchasePriceChangeService.getById(entity.getId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.NOT_EXIST_BILL,"采购价目变更");
            }
            purchasePriceChangeService.submitApprove(Collections.singletonList(entity.getId()), Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", entity.getId(), e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(entity.getId(),entity.getCode(),Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", entity.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(entity.getId(),entity.getCode(),Boolean.TRUE));
        }
        //发送消息
        try {
            //消息发送
            purchasePriceChangeService.sendMsg(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT,"");
        } catch (Exception e) {
            log.error("发送消息失败，ID: {}", entity.getId(), e);
        }

        return success(new BaseResultDTO.AddAndSubmmitDTO(entity.getId(), entity.getCode(),Boolean.TRUE));
    }


    /**
     * 采购调价单详情
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
     * 修改采购调价单
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购调价单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:update",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PurchasePriceChangeDTO.UpdateDTO dto) {
        String id = purchasePriceChangeService.updatePurchasePriceChange(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核采购调价单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:update",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> updateAndSubmit(@RequestBody @Validated PurchasePriceChangeDTO.UpdateDTO dto) {
        String id="";
        try {
            id = purchasePriceChangeService.updatePurchasePriceChange(dto);
            if (StringUtils.isBlank(id)) {
                return failure(ApiError.ERROR_1020.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
            }
        } catch (ServiceException e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(e.getMessage(), new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        } catch (Exception e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1020.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        }
        //提审
        try {
            purchasePriceChangeService.submitApprove(Arrays.asList(id), Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", id, e);
            return failure( e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", id, e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        }

        //发送消息
        try {
            //消息发送
            purchasePriceChangeService.sendMsg(Collections.singletonList(dto.getId()), ApproveStatusEnum.WAIT_SUBMIT,"");
        } catch (Exception e) {
            log.error("发送消息失败，ID: {}", id, e);
        }
        return success(new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
    }


    /**
     * 删除采购调价
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购调价单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:delete",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchasePriceChangeEntity> entityMap = purchasePriceChangeService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceChangeEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = purchasePriceChangeService.deleteByIds(Collections.singletonList(id));
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "删除采购调价单成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "删除采购调价单失败"));
                }
            }catch (Exception e){
                log.error("删除采购调价单失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 采购调价单提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购调价单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:submit",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids")
    public ApiResult<?> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchasePriceChangeEntity> entityMap = purchasePriceChangeService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceChangeEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = purchasePriceChangeService.submitApprove(Collections.singletonList(id), Boolean.TRUE);
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "提交采购调价单成功"));
                    purchasePriceChangeService.sendMsg(Collections.singletonList(id), ApproveStatusEnum.WAIT_SUBMIT,"");
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "提交采购调价单失败"));
                }
            }catch (Exception e){
                log.error("提交采购调价单失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 采购调价单审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购调价单")
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceChangeEntity> entityList = purchasePriceChangeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购调价单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceChangeService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购调价审核失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购调价单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:cancelProcess",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids")
    public ApiResult<?> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchasePriceChangeEntity> entityMap = purchasePriceChangeService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceChangeEntity entity = entityMap.get(id);
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "采购调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = purchasePriceChangeService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(Collections.singletonList(id)));
                if (disabled) {
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "撤销采购调价单成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "撤销采购调价单失败"));
                }
            } catch (Exception e) {
                log.error("撤销采购调价单失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新明细备注
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新采购调价单明细备注:备注={remark}")
    @PostMapping("/updateDetailRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id",
            menuCode = "scm:purchase:price:change:cancelProcess",
            serviceClass = PurchasePriceChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult<?> updateDetailRemark(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceChangeDetailEntity> detailList = purchasePriceChangeDetailService.listByIds(dto.getIds());
        Map<String, PurchasePriceChangeDetailEntity> detailMap = detailList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = detailList.stream().map(PurchasePriceChangeDetailEntity::getPurchasePriceChangeId).distinct().collect(Collectors.toList());
        Map<String, PurchasePriceChangeEntity> entityMap = purchasePriceChangeService.mapByIds(mainIds);
        for (String id : dto.getIds()) {
            PurchasePriceChangeDetailEntity detailEntity = detailMap.get(id);
            if(Objects.isNull(detailEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购调价明细不存在"));
                continue;
            }
            PurchasePriceChangeEntity entity = entityMap.get(detailEntity.getPurchasePriceChangeId());
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = purchasePriceChangeService.updateDetailRemark(Collections.singletonList(id), dto.getRemark());
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "更新采购调价单明细备注"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "更新采购调价单明细备注"));
                }
            }catch (Exception e){
                log.error("更新采购调价单明细备注失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 采购调价表数据导出
     * @author Will
     * @date: 2023/10/18 16:30
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购调价表数据导出")
    @PostMapping("/export")
    public ApiResult<?> export(@RequestBody @Valid PurchasePriceChangeDTO.PagingParamDTO dto) {
        purchasePriceChangeService.export(dto);
        return success();
    }



    /**
     * 修复历史数据
     *
     * @return
     */
    @PostMapping("/updateHistoryDb")
    public ApiResult<?> tempUpdateHistoryDb() {
        purchasePriceChangeService.tempUpdateHistoryDb();
        return success();
    }
}
