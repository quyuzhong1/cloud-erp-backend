package com.erp.server.tms.controller.api;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.enums.TransferDeclareCostAllocationMainReportStatusEnum;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;
import com.erp.model.tms.entity.TransferDeclareCostAllocationMainEntity;
import com.erp.server.tms.query.TransferDeclareCostAllocationQueryHandler;
import com.erp.server.tms.service.SmallBagCostAllocationService;
import com.erp.server.tms.service.TransferDeclareCostAllocationMainService;
import com.erp.server.tms.service.TransferDeclareCostAllocationService;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 中转费用分摊
 *
 * @author shukai
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("中转费用分摊")
@RequestMapping("/transferDeclareCostAllocation")
public class TransferDeclareCostAllocationController extends BaseController {

    @Resource
    private TransferDeclareCostAllocationService transferDeclareCostAllocationService;
    @Resource
    private TransferDeclareCostAllocationMainService transferDeclareCostAllocationMainService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转费用分摊新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferDeclareCostAllocationDTO.AddDTO dto) {
        return success(transferDeclareCostAllocationService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转费用分摊修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:transferDeclareCostAllocation:update",
        serviceClass = TransferDeclareCostAllocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TransferDeclareCostAllocationDTO.UpdateDTO dto) {
        transferDeclareCostAllocationService.update(dto);
        return success();
    }

    /**
     * tab列表
     * @author Will
     * @date: 2023/11/13 15:12
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "j.shop_id",
            menuCode = "tms:transferDeclareCostAllocation:paging",
            tableAlias = "t"
    )
    public ApiResult<List<TransferDeclareCostAllocationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(transferDeclareCostAllocationService.tabList(dto));
    }

    /**
     * 分页查询
     * @author Will
     * @date: 2023/11/13 15:12
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "j.shop_id",
            menuCode = "tms:transferDeclareCostAllocation:paging",
            tableAlias = "t"
    )
    @WebAdvanceQuery(handler = TransferDeclareCostAllocationQueryHandler.class)
    public ApiResult<PagingVO<TransferDeclareCostAllocationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferDeclareCostAllocationDTO.PagingParamDTO> dto) {
        return success(transferDeclareCostAllocationService.paging(dto));
    }
    
    /**
     * 核算状态
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:ids={ids}")
    @PostMapping("/updateReportStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclareCostAllocation:updateReportStatus",
            serviceClass = SmallBagCostAllocationService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> updateReportStatus(@RequestBody TransferDeclareCostAllocationDTO.UpdateStatusDTO dto) {
        List<TransferDeclareCostAllocationEntity> entityList = null;
        if (CharSequenceUtil.isNotBlank(dto.getReportPeriodStr())){
            entityList = transferDeclareCostAllocationService.listByReportPeriodStr(dto.getReportPeriodStr(), null);
        }else if (CollUtil.isNotEmpty(dto.getIds())){
            entityList = transferDeclareCostAllocationService.listByIds(dto.getIds());
        }
        if (CollectionUtils.isEmpty(entityList)){
            return failure("批量更新中转分摊状态失败，未查询到中转分摊记录");
        }
    	List<String> ids = entityList.stream().map(TransferDeclareCostAllocationEntity::getMainId).distinct().collect(Collectors.toList());
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
    	for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = transferDeclareCostAllocationService.updateReportStatus(id,dto.getReportDate(),dto.getReportStatus());
            }catch (Exception e){
                log.error("中转分摊 状态变更",e);
                TransferDeclareCostAllocationMainEntity entity = transferDeclareCostAllocationMainService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "中转分摊不存在, 核算状态");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    
    /**
     * 重新分摊
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:ids={ids}")
    @PostMapping("/reAllocation")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:transferDeclareCostAllocation:reAllocation",
    serviceClass = SmallBagCostAllocationService.class,
    keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> reAllocation(@RequestBody FirstMileCostAllocationDTO.ResetIdsDTO dto) {
        List<TransferDeclareCostAllocationEntity> entityList = null;
        if (CharSequenceUtil.isNotBlank(dto.getReportPeriodStr())){
            entityList = transferDeclareCostAllocationService.listByReportPeriodStr(dto.getReportPeriodStr(), TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode());
        }else if (CollUtil.isNotEmpty(dto.getIds())){
            entityList = transferDeclareCostAllocationService.listByIds(dto.getIds());
        }
        if (CollectionUtils.isEmpty(entityList)){
            return failure("批量更新中转分摊状态失败，未查询到待确认中转分摊记录");
        }
    	List<String> ids = entityList.stream().map(TransferDeclareCostAllocationEntity::getMainId).distinct().collect(Collectors.toList());
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
    	for (String id : ids) {
    		BatchResultDTO submit;
    		try {
    			submit = transferDeclareCostAllocationService.reAllocation(id);
    		}catch (Exception e){
    			log.error("中转分摊 重新分摊",e);
    			TransferDeclareCostAllocationMainEntity entity = transferDeclareCostAllocationMainService.getById(id);
    			if (ObjectUtil.isEmpty(entity)) {
    				submit = BatchResultDTO.fail(id, id, "中转分摊不存在, 重新分摊");
    				resultDTOS.add(submit);
    				continue;
    			}
    			submit = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
    		}
    		resultDTOS.add(submit);
    	}
    	return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    
    /**
     * 批量删除
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:transferDeclareCostAllocation:delete",
    serviceClass = SmallBagCostAllocationService.class,
    keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody FirstMileCostAllocationDTO.ResetIdsDTO dto) {
        List<TransferDeclareCostAllocationEntity> entityList = null;
        if (CharSequenceUtil.isNotBlank(dto.getReportPeriodStr())){
            entityList = transferDeclareCostAllocationService.listByReportPeriodStr(dto.getReportPeriodStr(), TransferDeclareCostAllocationMainReportStatusEnum.TOBECONFIRM.getCode());
        }else if (CollUtil.isNotEmpty(dto.getIds())){
            entityList = transferDeclareCostAllocationService.listByIds(dto.getIds());
        }
        if (CollectionUtils.isEmpty(entityList)){
            return failure("批量更新中转分摊状态失败，未查询到待确认中转分摊记录");
        }
        List<String> ids = entityList.stream().map(TransferDeclareCostAllocationEntity::getMainId).distinct().collect(Collectors.toList());
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
    	for (String id : ids) {
    		BatchResultDTO submit;
    		try {
    			submit = transferDeclareCostAllocationService.delete(id);
    		}catch (Exception e){
    			log.error("中转分摊 重新分摊",e);
    			TransferDeclareCostAllocationMainEntity entity = transferDeclareCostAllocationMainService.getById(id);
    			if (ObjectUtil.isEmpty(entity)) {
    				submit = BatchResultDTO.fail(id, id, "中转分摊不存在, 删除分摊");
    				resultDTOS.add(submit);
    				continue;
    			}
    			submit = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
    		}
    		resultDTOS.add(submit);
    	}
    	return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    
    /**
     *  导出excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Object> exportExcel(@RequestBody @Validated TransferDeclareCostAllocationDTO.PagingParamDTO dto) {
    	transferDeclareCostAllocationService.exportExcel(dto);
        return success();
    }
    
}
