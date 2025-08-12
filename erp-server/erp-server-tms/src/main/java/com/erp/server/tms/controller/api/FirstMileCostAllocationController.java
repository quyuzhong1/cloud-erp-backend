package com.erp.server.tms.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.enums.ConfirmStatusEnum;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.model.tms.entity.ReportPeriodMonthEntity;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.query.FirstMileCostAllocationQueryHandler;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import com.erp.server.tms.service.ReportPeriodMonthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 头程费用分摊
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@RestController
@LogSystemModule("头程费用分摊")
@RequestMapping("/firstMileCostAllocation")
public class FirstMileCostAllocationController extends BaseController {

    public static final String MSG = "待确认费用分摊记录不存在";
    public static final String ERROR_MSG = "费用分摊记录删除失败";
    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;
    @Resource
    private ReportPeriodMonthService reportPeriodMonthService;
    @Resource
    private FirstMileChangeRecordService firstMileChangeRecordService;
    /**
     * tab 列表
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "a.shop_id",
            warehouseTableField = "a.from_warehouse_id,a.to_warehouse_id",
            menuCode = "tms:firstMileCostAllocation:paging",
            tableAlias = "a"
    )
    public ApiResult<List<FirstMileCostAllocationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<FirstMileCostAllocationDTO.TabListDTO> tabList = firstMileCostAllocationService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "a.shop_id",
            warehouseTableField = "a.from_warehouse_id,a.to_warehouse_id",
            menuCode = "tms:firstMileCostAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = FirstMileCostAllocationQueryHandler.class)
    public ApiResult<PagingVO<FirstMileCostAllocationDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto) {
        PagingVO<FirstMileCostAllocationDTO.PagingVO> pagingVO = firstMileCostAllocationService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 批量更新状态
     */
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:firstMileCostAllocation:updateStatus",
            serviceClass = FirstMileCostAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody FirstMileCostAllocationDTO.UpdateStatusDTO dto) {
        List<FirstMileCostAllocationEntity> entityList = null;
        if (CharSequenceUtil.isNotBlank(dto.getReportPeriodStr())){
            entityList = firstMileCostAllocationService.listByReportPeriodStr(dto.getReportPeriodStr(), null);
        }else if (CollUtil.isNotEmpty(dto.getIds())){
            entityList = firstMileCostAllocationService.listByIds(dto.getIds());
        }
        if (CollectionUtils.isEmpty(entityList)){
            return failure("批量更新状态失败，未查询到费用分摊记录");
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(entityList.size());
        for (FirstMileCostAllocationEntity entity : entityList) {
            try {
                resultDTOS.add(firstMileCostAllocationService.updateStatus(entity,dto.getStatus(),dto.getAccountPeriod()));
            }catch (Exception e){
                log.error("费用分摊记录更新状态失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 删除记录
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:firstMileCostAllocation:delete",
            serviceClass = FirstMileCostAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody FirstMileCostAllocationDTO.ResetIdsDTO dto) {
        List<FirstMileCostAllocationEntity> entityList = null;
        if (CharSequenceUtil.isNotBlank(dto.getReportPeriodStr())){
            entityList = firstMileCostAllocationService.listByReportPeriodStr(dto.getReportPeriodStr(), ConfirmStatusEnum.WAIT_CONFIRM.getCode());
        }else if (CollUtil.isNotEmpty(dto.getIds())){
            entityList = firstMileCostAllocationService.listByIds(dto.getIds());
        }
        if (CollectionUtils.isEmpty(entityList)){
            return failure("批量删除记录失败，未查询到待确认费用分摊记录");
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(entityList.size());
        for (FirstMileCostAllocationEntity entity : entityList) {
            try {
                resultDTOS.add(firstMileCostAllocationService.delete(entity));
            }catch (Exception e){
                log.error(ERROR_MSG,e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 导出Excel
     *
     * @param dto
     * @author zdy
     * @date 2024-8-15 10:54
     */
    @PostMapping("/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Valid FirstMileCostAllocationDTO.PagingParamDTO dto) {
        firstMileCostAllocationService.exportList(dto);
        return success(Boolean.TRUE);
    }

    /**
     * 重新分摊
     */
    @PostMapping("/calcAllocatedCost")
    @LogAction(value = LogActionEnum.UPDATE, desc = "重新分摊")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:firstMileCostAllocation:calcAllocatedCost",
            serviceClass = FirstMileCostAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> calcAllocatedCost(@RequestBody FirstMileCostAllocationDTO.ResetIdsDTO dto) {
        List<FirstMileCostAllocationEntity> entityList = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(dto.getReportPeriodStr())){
            entityList = firstMileCostAllocationService.listByReportPeriodStr(dto.getReportPeriodStr(), ConfirmStatusEnum.WAIT_CONFIRM.getCode());
        }else if (CollUtil.isNotEmpty(dto.getIds())){
            List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
            entityList = firstMileCostAllocationService.listByIds(ids);
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(entityList.size());
        if (CollectionUtils.isEmpty(entityList)){
            resultDTOS.add(BatchResultDTO.fail("","", MSG));
            return failure(resultDTOS);
        }

        List<String> sourceIds = entityList.stream().filter(e -> ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(e.getStatus())).map(FirstMileCostAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(sourceIds);
        if (CharSequenceUtil.isNotBlank(dto.getReportPeriodStr())){
            firstMileCostAllocationService.asyncResetAllocatedCost(entityList,firstMileDeliveryEntityList, deliveryDetailEntityList);
            return success();
        }else {
            for (FirstMileCostAllocationEntity entity : entityList) {
                if (ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())){
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"核算状态已确认，不可重新分摊"));
                    continue;
                }
                String sourceId = entity.getSourceId();
                FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> e.getId().equals(sourceId)).findFirst().orElse(null);
                if(Objects.isNull(firstMileDeliveryEntity)){
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"费用分摊发货单记录不存在"));
                    continue;
                }
                List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = deliveryDetailEntityList.stream().filter(e -> e.getMainId().equals(sourceId)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)){
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"费用分摊发货单明细记录不存在"));
                    continue;
                }
                try {
                    resultDTOS.add(firstMileCostAllocationService.calcAllocatedCost(entity,firstMileDeliveryEntity, firstMileDeliveryDetailEntityList));
                }catch (Exception e){
                    log.error(ERROR_MSG,e);
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage()));
                }
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下推费用分摊
     */
    @PostMapping("/pushAllocatedCost")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "重新分摊")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:firstMileCostAllocation:calcAllocatedCost",
            serviceClass = FirstMileCostAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> pushAllocatedCost(@RequestBody @Valid FirstMileCostAllocationDTO.IdsDTO dto) {
        List<FirstMileWeightAllocationEntity> firstMileWeightAllocationEntities = firstMileWeightAllocationService.listByIds(dto.getIds());
        List<String> sourceIds = firstMileWeightAllocationEntities.stream().filter(Objects::nonNull).map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(sourceIds.size());
        List<FirstMileCostAllocationEntity> entityList = firstMileCostAllocationService.listBySourceIds(sourceIds, null, null, null);
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(sourceIds);
        ReportPeriodMonthEntity reportPeriodMonth = reportPeriodMonthService.getById(dto.getReportPeriodId());
        if (Objects.isNull(reportPeriodMonth)){
            resultDTOS.add(BatchResultDTO.fail(dto.getReportPeriodId(),"","核算区间不存在"));
            return failure(resultDTOS);
        }
        for (String sourceId : sourceIds) {
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> e.getId().equals(sourceId)).findFirst().orElse(null);
            if(Objects.isNull(firstMileDeliveryEntity)){
                resultDTOS.add(BatchResultDTO.fail(sourceId,sourceId,"费用分摊发货单记录不存在"));
                continue;
            }
            List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = deliveryDetailEntityList.stream().filter(e -> e.getMainId().equals(sourceId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)){
                resultDTOS.add(BatchResultDTO.fail(sourceId,sourceId,"费用分摊发货单明细记录不存在"));
                continue;
            }
            //已生成的费用分摊记录
            if (!CollectionUtils.isEmpty(entityList)){
                FirstMileCostAllocationEntity entity = entityList.stream().filter(e -> Objects.nonNull(e)
                        && ConfirmStatusEnum.CONFIRM.getCode().equals(e.getStatus()) && Objects.equals(e.getSourceId(), sourceId))
                        .max(Comparator.comparing(FirstMileCostAllocationEntity::getReportPeriodMonth)).orElse(null);
                if (Objects.nonNull(entity) && !reportPeriodMonth.getMonth().isAfter(entity.getReportPeriodMonth())){
                    resultDTOS.add(BatchResultDTO.fail(sourceId,sourceId, CharSequenceUtil.format("已存在核算区间【{}】不能下推发货单【{}】的核算区间【{}】", entity.getReportPeriodMonth(),entity.getSourceCode(),reportPeriodMonth.getMonth())));
                    continue;
                }
            }
            FirstMileCostAllocationEntity entity = new FirstMileCostAllocationEntity()
                    .setSourceId(firstMileDeliveryEntity.getId()).setSourceCode(firstMileDeliveryEntity.getCode()).setReportPeriodMonth(reportPeriodMonth.getMonth()).setReportPeriodId(dto.getReportPeriodId());
//            FirstMileCostAllocationEntity entity = entityList.stream().filter(v->v.getSourceId().equals(sourceId) && dto.getReportPeriodId().equals(v.getReportPeriodId())).findFirst().orElse(new FirstMileCostAllocationEntity());
//            entity.setSourceId(sourceId);
//            entity.setReportPeriodId(dto.getReportPeriodId());
            try {
                resultDTOS.add(firstMileCostAllocationService.calcAllocatedCost(entity,firstMileDeliveryEntity, firstMileDeliveryDetailEntityList));
            }catch (Exception e){
                log.error(ERROR_MSG,e);
                resultDTOS.add(BatchResultDTO.fail(sourceId, sourceId, e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 修改费用分摊预览
     * @param dto
     * @return
     */
    @PostMapping("/viewCostAllocation")
    public ApiResult<List<FirstMileCostAllocationDTO.PagingVO>> viewCostAllocation(@RequestBody @Valid BaseIdsDTO.IdsDTO dto){
        List<FirstMileCostAllocationDTO.PagingVO> list = firstMileCostAllocationService.viewCostAllocation(dto.getIds());
        return success(list);
    }

    /**
     * 修改费用分摊字段保存
     * @param dtoValidList
     * @return
     */
    @PostMapping("/changeCostAllocation")
    public ApiResult<List<BatchResultDTO>> changeCostAllocation(@RequestBody @Valid ValidList<FirstMileCostAllocationDTO.CostAllocationDTO> dtoValidList){
        //批量校验是否存在相同维度的sku修改数据
        List<BatchResultDTO> batchResultDTOS = firstMileChangeRecordService.checkCostAllocationSameDimension(dtoValidList);
        //存在异常校验直接返回
        if (batchResultDTOS.stream().anyMatch(item -> !item.getSuccess())) {
            return failure(batchResultDTOS);
        }
        //批量保存修改记录
        firstMileChangeRecordService.saveCostAllocation(dtoValidList);
        //按照保存成功记录，进行按照单据进行重新重量分摊
        List<String> ids = dtoValidList.stream().filter(FirstMileCostAllocationDTO.CostAllocationDTO::getIsRetry).map(FirstMileCostAllocationDTO.CostAllocationDTO::getId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(ids)){
            FirstMileCostAllocationDTO.ResetIdsDTO dto = new FirstMileCostAllocationDTO.ResetIdsDTO();
            dto.setIds(ids);
            ApiResult<List<BatchResultDTO>> listApiResult = this.calcAllocatedCost(dto);
            batchResultDTOS.addAll(listApiResult.getData());
        }
        return batchResultDTOS.stream().allMatch(BatchResultDTO::getSuccess)? success(batchResultDTOS) : failure(batchResultDTOS);
    }
    /**
     * 下载费用分摊调整导入模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载费用分摊调整导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        firstMileCostAllocationService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入费用分摊调整
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入费用分摊调整")
    @PostMapping("/importExcel")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = firstMileCostAllocationService.importExcel(excelFile, response);
        return result?success():failure();
    }
}
