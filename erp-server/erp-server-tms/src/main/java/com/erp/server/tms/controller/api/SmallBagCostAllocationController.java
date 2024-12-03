package com.erp.server.tms.controller.api;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

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
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import com.erp.server.tms.service.SmallBagCostAllocationService;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 小包费用分摊
 *
 * @author shukai
 * @since 2024-12-02
 */
@Slf4j
@RestController
@LogSystemModule("小包费用分摊")
@RequestMapping("/smallBagCostAllocation")
public class SmallBagCostAllocationController extends BaseController {

    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-12-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "小包费用分摊新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SmallBagCostAllocationDTO.AddDTO dto) {
        return success(smallBagCostAllocationService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-12-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "小包费用分摊修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:smallBagCostAllocation:update",
        serviceClass = SmallBagCostAllocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SmallBagCostAllocationDTO.UpdateDTO dto) {
        smallBagCostAllocationService.update(dto);
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
            menuCode = "tms:smallBagCostAllocation:paging",
            tableAlias = "lbc"
    )
    public ApiResult<List<SmallBagCostAllocationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(smallBagCostAllocationService.tabList(dto));
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
            menuCode = "tms:smallBagCostAllocation:paging",
            tableAlias = "lbc"
    )
    @WebAdvanceQuery()
    public ApiResult<PagingVO<SmallBagCostAllocationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto) {
        return success(smallBagCostAllocationService.paging(dto));
    }
    
    /**
     * 核算状态
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:idList={idList}")
    @PostMapping("/updateReportStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:smallBagCostAllocation:updateReportStatus",
            serviceClass = SmallBagCostAllocationService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> updateReportStatus(@RequestBody @Validated SmallBagCostAllocationDTO.UpdateStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = smallBagCostAllocationService.updateReportStatus(id,dto.getReportDate(),dto.getReportStatus());
            }catch (Exception e){
                log.error("小包分摊 状态变更",e);
                SmallBagCostAllocationEntity entity = smallBagCostAllocationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "小包分摊不存在, 核算状态");
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:idList={idList}")
    @PostMapping("/reAllocation")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:smallBagCostAllocation:reAllocation",
    serviceClass = SmallBagCostAllocationService.class,
    keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> reAllocation(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
    	for (String id : dto.getIds()) {
    		BatchResultDTO submit;
    		try {
    			submit = smallBagCostAllocationService.reAllocation(id);
    		}catch (Exception e){
    			log.error("小包分摊 重新分摊",e);
    			SmallBagCostAllocationEntity entity = smallBagCostAllocationService.getById(id);
    			if (ObjectUtil.isEmpty(entity)) {
    				submit = BatchResultDTO.fail(id, id, "小包分摊不存在, 重新分摊");
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:idList={idList}")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:smallBagCostAllocation:delete",
    serviceClass = SmallBagCostAllocationService.class,
    keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
    	for (String id : dto.getIds()) {
    		BatchResultDTO submit;
    		try {
    			submit = smallBagCostAllocationService.delete(id);
    		}catch (Exception e){
    			log.error("小包分摊 重新分摊",e);
    			SmallBagCostAllocationEntity entity = smallBagCostAllocationService.getById(id);
    			if (ObjectUtil.isEmpty(entity)) {
    				submit = BatchResultDTO.fail(id, id, "小包分摊不存在, 重新分摊");
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
    public ApiResult<Object> exportExcel(@RequestBody @Validated SmallBagCostAllocationDTO.PagingParamDTO dto) {
        return success();
    }
    
    /**
     * 生成物流大表
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:idList={idList}")
    @PostMapping("/pushBigTable")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:smallBagCostAllocation:pushBigTable",
    serviceClass = SmallBagCostAllocationService.class,
    keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> pushBigTable(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
    	for (String id : dto.getIds()) {
    		BatchResultDTO submit;
    		try {
    			submit = smallBagCostAllocationService.pushBigTable(id);
    		}catch (Exception e){
    			log.error("小包分摊 生成物流大表",e);
    			SmallBagCostAllocationEntity entity = smallBagCostAllocationService.getById(id);
    			if (ObjectUtil.isEmpty(entity)) {
    				submit = BatchResultDTO.fail(id, id, "小包分摊不存在,  生成物流大表");
    				resultDTOS.add(submit);
    				continue;
    			}
    			submit = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
    		}
    		resultDTOS.add(submit);
    	}
    	return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
