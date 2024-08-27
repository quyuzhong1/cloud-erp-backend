package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.query.FirstMileCostAllocationQueryHandler;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

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

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;

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
            menuCode = "tms:firstMileCostAllocation:paging",
            tableAlias = "lb"
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
            menuCode = "tms:firstMileCostAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = FirstMileCostAllocationQueryHandler.class)
    public ApiResult<PagingVO<FirstMileCostAllocationDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto) {
        PagingVO<FirstMileCostAllocationDTO.PagingVO> pagingVO = firstMileCostAllocationService.paging(dto);
        return success(pagingVO);
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
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<FirstMileCostAllocationEntity> entityList = firstMileCostAllocationService.listByIds(ids);
        for (String id : ids) {
            FirstMileCostAllocationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"费用分摊记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(firstMileCostAllocationService.delete(entity));
            }catch (Exception e){
                log.error("费用分摊记录删除失败",e);
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
    @WebAdvanceQuery(handler = FirstMileCostAllocationQueryHandler.class)
    public ApiResult<?> exportExcel(@RequestBody @Valid FirstMileCostAllocationDTO.PagingParamDTO dto, HttpServletResponse response) {
        firstMileCostAllocationService.exportExcel(dto, response);
        return success();
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
    public ApiResult<List<BatchResultDTO>> calcAllocatedCost(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<FirstMileCostAllocationEntity> entityList = firstMileCostAllocationService.listByIds(ids);
        List<String> sourceIds = entityList.stream().map(FirstMileCostAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(sourceIds);
        //根据发货单获取费用分摊记录
        List<FirstMileCostAllocationEntity> firstMileCostAllocationEntityList = firstMileCostAllocationService.listBySourceIds(sourceIds, null);
        for (String sourceId : sourceIds) {
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> e.getId().equals(sourceId)).findFirst().orElse(null);
            if(Objects.isNull(firstMileDeliveryEntity)){
                resultDTOS.add(BatchResultDTO.fail(sourceId,sourceId,"费用分摊发货单记录不存在"));
                continue;
            }
            List<FirstMileCostAllocationEntity> entityList1 = firstMileCostAllocationEntityList.stream().filter(v->v.getSourceId().equals(sourceId))
                    .sorted(Comparator.comparing(FirstMileCostAllocationEntity::getReportPeriodMonth)).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(entityList1)){
                resultDTOS.add(BatchResultDTO.fail(sourceId,firstMileDeliveryEntity.getCode(),"费用分摊记录不存在"));
                continue;
            }
            List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = deliveryDetailEntityList.stream().filter(e -> e.getMainId().equals(sourceId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)){
                resultDTOS.add(BatchResultDTO.fail(sourceId,firstMileDeliveryEntity.getCode(),"费用分摊发货单明细记录不存在"));
                continue;
            }
            for (FirstMileCostAllocationEntity entity : entityList1){
                try {
                    resultDTOS.add(firstMileCostAllocationService.calcAllocatedCost(entity,firstMileDeliveryEntity, firstMileDeliveryDetailEntityList));
                }catch (Exception e){
                    log.error("费用分摊记录删除失败",e);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "重新分摊")
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
        List<FirstMileCostAllocationEntity> entityList = firstMileCostAllocationService.listBySourceIds(sourceIds,dto.getReportPeriodId());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(sourceIds);
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
            FirstMileCostAllocationEntity entity = entityList.stream().filter(v->v.getSourceId().equals(sourceId)).findFirst().orElse(new FirstMileCostAllocationEntity());
            entity.setSourceId(sourceId);
            entity.setReportPeriodId(dto.getReportPeriodId());
            try {
                resultDTOS.add(firstMileCostAllocationService.calcAllocatedCost(entity,firstMileDeliveryEntity, firstMileDeliveryDetailEntityList));
            }catch (Exception e){
                log.error("费用分摊记录删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(sourceId, sourceId, e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
