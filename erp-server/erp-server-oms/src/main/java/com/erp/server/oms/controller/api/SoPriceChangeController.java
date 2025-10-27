package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import com.erp.model.oms.entity.SoPriceChangeEntity;
import com.erp.server.oms.query.SoPriceChangeQueryHandler;
import com.erp.server.oms.service.SoPriceChangeDetailService;
import com.erp.server.oms.service.SoPriceChangeService;
import com.erp.server.oms.service.SoPriceDetailService;
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
 * 销售价变更表
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@RestController
@LogSystemModule("销售价变更表")
@RequestMapping("/soPriceChange")
public class SoPriceChangeController extends BaseController {
    @Resource
    private SoPriceChangeService soPriceChangeService;
    @Resource
    private SoPriceDetailService soPriceDetailService;
    @Resource
    private SoPriceChangeDetailService soPriceChangeDetailService;


    /**
     * 销售调价单分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:paging",
            tableAlias = "spc,spc")
    @WebAdvanceQuery(handler = SoPriceChangeQueryHandler.class)
    public ApiResult<PagingVO<SoPriceChangeDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto) {
        PagingVO<SoPriceChangeDTO.PagingViewDTO> pagingVO = soPriceChangeService.paging(dto);
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
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:paging",
            tableAlias = "spc,spc")
    public ApiResult<List<SoPriceChangeDTO.TabListDTO>> tabList(PermissionsDTO dto) {
        List<SoPriceChangeDTO.TabListDTO> list = soPriceChangeService.tabList(dto);
        return success(list);
    }

    /**
     * 添加销售变更
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加销售变更")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:add",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "id")
    public ApiResult<?> add(@RequestBody @Validated SoPriceChangeDTO.AddDTO dto) {
        SoPriceChangeEntity entity = soPriceChangeService.add(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * 销售调价表  点击变更报价获取详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/priceChangeDetail")
    public ApiResult<SoPriceChangeDTO.ViewDTO> priceChangeDetail(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        SoPriceChangeDTO.ViewDTO view = soPriceDetailService.priceChangeDetail(dto.getIds());
        return success(view);
    }


    /**
     * 新增变更  获取对应变更sku列表
     *
     * @return
     */
    @PostMapping("/getSkuChangeList")
    public ApiResult<List<SoPriceChangeDetailDTO.ViewDTO>> getSkuChangeList(@RequestBody SoPriceChangeDetailDTO.SkuChangeParamDTO dto) {
        List<SoPriceChangeDetailDTO.ViewDTO> list = soPriceDetailService.listPriceChangeDetail(dto);
        return success(list);
    }



    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售变更")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:add",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "id")
    public ApiResult<?> addAndSubmit(@RequestBody @Validated SoPriceChangeDTO.AddDTO dto) {
        SoPriceChangeEntity entity = soPriceChangeService.addAndSubmit(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * 销售调价单详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:view",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "id")
    public ApiResult<SoPriceChangeDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoPriceChangeDTO.ViewDTO view = soPriceChangeService.view(dto.getId());
        return success(view);
    }


    /**
     * 修改销售调价单
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售调价单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:update",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoPriceChangeDTO.UpdateDTO dto) {
        String id = soPriceChangeService.updateSoPriceChange(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核销售调价单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:update",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "id")
    public ApiResult<?> updateAndSubmit(@RequestBody @Validated SoPriceChangeDTO.UpdateDTO dto) {
        Boolean result = soPriceChangeService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 删除销售调价
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除销售调价单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:delete",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, SoPriceChangeEntity> entityMap = soPriceChangeService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceChangeEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = soPriceChangeService.deleteByIds(Collections.singletonList(id));
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "删除销售调价单成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "删除销售调价单失败"));
                }
            }catch (Exception e){
                log.error("删除销售调价单失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 销售调价单提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售调价单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:submit",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    public ApiResult<?> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, SoPriceChangeEntity> entityMap = soPriceChangeService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceChangeEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = soPriceChangeService.submit(Collections.singletonList(id), Boolean.TRUE);
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "提交销售调价单成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "提交销售调价单失败"));
                }
            }catch (Exception e){
                log.error("提交销售调价单失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 销售调价单审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售调价单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:approve",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoPriceChangeEntity> entityList = soPriceChangeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售调价单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soPriceChangeService.approve(entity,new ApproveOneDTO(id, dto.getType(),dto.getComment())));
            }catch (Exception e){
                log.error("销售调价审核失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售调价单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:cancelProcess",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    public ApiResult<?> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, SoPriceChangeEntity> entityMap = soPriceChangeService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceChangeEntity entity = entityMap.get(id);
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "销售调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = soPriceChangeService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(Collections.singletonList(id)));
                if (disabled) {
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "撤销销售调价单成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "撤销销售调价单失败"));
                }
            } catch (Exception e) {
                log.error("撤销销售调价单失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新明细备注
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新销售调价单明细备注:备注={remark}")
    @PostMapping("/updateDetailRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "adjust_user_id,create_user_id",
            menuCode = "oms:soPriceChange:updateDetailRemark",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids"
    )
    public ApiResult<?> updateDetailRemark(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoPriceChangeDetailEntity> detailList = soPriceChangeDetailService.listByIds(dto.getIds());
        Map<String, SoPriceChangeDetailEntity> detailMap = detailList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = detailList.stream().map(SoPriceChangeDetailEntity::getMainId).distinct().collect(Collectors.toList());
        Map<String, SoPriceChangeEntity> entityMap = soPriceChangeService.mapByIds(mainIds);
        for (String id : dto.getIds()) {
            SoPriceChangeDetailEntity detailEntity = detailMap.get(id);
            if(Objects.isNull(detailEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售调价明细不存在"));
                continue;
            }
            SoPriceChangeEntity entity = entityMap.get(detailEntity.getMainId());
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售调价单不存在"));
                continue;
            }
            try {
                Boolean disabled = soPriceChangeService.updateDetailRemark(Collections.singletonList(id), dto.getRemark());
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "更新销售调价单明细备注"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "更新销售调价单明细备注"));
                }
            }catch (Exception e){
                log.error("更新销售调价单明细备注失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 销售调价表数据导出
     * @author Will
     * @date: 2023/10/18 16:30
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "销售调价表数据导出")
    @PostMapping("/export")
    public ApiResult<?> export(@RequestBody @Valid SoPriceChangeDTO.PagingParamDTO dto) {
        soPriceChangeService.export(dto);
        return success();
    }

}
