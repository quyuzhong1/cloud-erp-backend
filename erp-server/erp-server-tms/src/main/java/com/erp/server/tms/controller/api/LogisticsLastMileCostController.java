package com.erp.server.tms.controller.api;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.common.business.dto.base.*;
import com.common.business.enums.ImportTypeEnum;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.EditDataDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.EditViewDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO.PushDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.server.tms.query.LogisticsLastMileCostQueryHandler;
import com.erp.server.tms.service.LogisticsBillCostService;
import com.erp.server.tms.service.LogisticsLastMileCostService;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 尾程费用
 *
 * @author Will
 * @since 2023-11-06
 */
@Slf4j
@RestController
@LogSystemModule("尾程费用")
@RequestMapping("/logisticsLastMileCost")
public class LogisticsLastMileCostController extends BaseController {


    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private LogisticsLastMileCostService logisticsLastMileCostService;


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
            shopTableField = "lb.shop_id",
            menuCode = "tms:logisticsLastMileCost:paging",
            tableAlias = "lbc"
    )
    public ApiResult<List<LogisticsBillCostDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsBillCostDTO.TabListDTO> tabList = logisticsBillCostService.tabList(dto, DictCostAttributionEnum.LAST_MILE);
        return success(tabList);
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
            shopTableField = "lb.shop_id",
            menuCode = "tms:logisticsLastMileCost:paging",
            tableAlias = "lbc"
    )
    @WebAdvanceQuery(handler = LogisticsLastMileCostQueryHandler.class)
    public ApiResult<PagingVO<LogisticsBillCostDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsBillCostDTO.ListDTO> pagingVO = logisticsBillCostService.paging(dto);
        return success(pagingVO);
    }

    /**
    * 修改
    * @author Will
    * @date:  2023-11-06
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "尾程费用修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsLastMileCost:update",
        serviceClass = LogisticsBillCostService.class,
        keyIdName = "id")
    public ApiResult<Object>update(@RequestBody @Validated LogisticsBillCostDTO.UpdateDTO dto) {
        logisticsBillCostService.update(dto,Boolean.FALSE);
        return success();
    }
    
    /**
     *查询详情
     * @author Will
     * @date: 2024/3/25 11:45
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<LogisticsBillCostDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(logisticsBillCostService.view(id));
    }

    /**
     * 对账状态
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:idList={idList}")
    @PostMapping("/updateReconciliationStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsLastMileCost:updateReconciliationStatus",
            serviceClass = LogisticsBillCostService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> updateReconciliationStatus(@RequestBody @Validated LogisticsBillCostDTO.UpdateStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = logisticsBillCostService.updateReconciliationStatus(id,dto.getReconciliationStatus(),dto.getConfirmTime());
            }catch (Exception e){
                log.error("尾程费用 状态变更",e);
                LogisticsBillCostEntity entity = logisticsBillCostService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "尾程费用不存在, 状态变更");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    
    /**
     * 支付状态
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:idList={idList}")
    @PostMapping("/updatePayStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:logisticsLastMileCost:updatePayStatus",
    serviceClass = LogisticsBillCostService.class,
    keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> updatePayStatus(@RequestBody @Validated LogisticsBillCostDTO.PayStatusDTO dto) {
    	List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
    	for (String id : dto.getIds()) {
    		BatchResultDTO submit;
    		try {
    			submit = logisticsBillCostService.updatePayStatus(id,dto.getPayStatus(),dto.getPayTime());
    		}catch (Exception e){
    			log.error("尾程费用 状态变更",e);
    			LogisticsBillCostEntity entity = logisticsBillCostService.getById(id);
    			if (ObjectUtil.isEmpty(entity)) {
    				submit = BatchResultDTO.fail(id, id, "尾程费用不存在, 状态变更");
    				resultDTOS.add(submit);
    				continue;
    			}
    			submit = BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), e.getMessage());
    		}
    		resultDTOS.add(submit);
    	}
    	return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载模板
     * @author Will
     * @date: 2023/11/13 15:14
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载尾程费用模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object>downloadTemplate(HttpServletResponse response) {
    	logisticsLastMileCostService.downloadTemplate(response);
        return success();
    }

    /**
     *  异步导入
     * @author zdy
     * @date: 2025/07/18 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入尾程费用模板")
    @PostMapping(value = "/importExcel")
    public ApiResult<Object> importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean flag = logisticsLastMileCostService.importExcel(dto);
        return flag ? success() : failure();
    }

    /**
     *  导出
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出尾程费用模板")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Object>exportExcel(@RequestBody LogisticsBillCostDTO.PagingParamDTO dto) {
        Boolean flag = logisticsLastMileCostService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增付款/退款（仅创建）
     * @author Will
     * @date:  2023-11-06
     * @param dto
     * @return ApiResult
     */
     @PostMapping("/addPayAndRefund")
     @LogAction(value = LogActionEnum.INSERT, desc = "新增付款/退款")
         @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
         tableField = "create_user_id",
         menuCode = "tms:logisticsLastMileCost:update",
         serviceClass = LogisticsBillCostService.class,
         keyIdName = "id")
     public ApiResult<Object> addPayAndRefund(@RequestBody @Validated List<LogisticsBillCostDTO.AddDataDTO> dto) {
         logisticsBillCostService.addPayAndRefund(dto);
         return success();
     }
     
     /**
      * 新增付款/退款（对账已确认）
      * @author Will
      * @date:  2023-11-06
      * @param dto
      * @return ApiResult
      */
     @PostMapping("/addPayAndRefundConfirm")
     @LogAction(value = LogActionEnum.INSERT, desc = "新增付款/退款")
     @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
     tableField = "create_user_id",
     menuCode = "tms:logisticsLastMileCost:update",
     serviceClass = LogisticsBillCostService.class,
     keyIdName = "id")
     public ApiResult<Object> addPayAndRefundConfirm(@RequestBody @Validated LogisticsBillCostDTO.ConfirmAddDataDTO dto) {
    	 logisticsBillCostService.addPayAndRefundConfirm(dto);
    	 return success();
     }
    
     /**
      * 编辑付款/退款 数据显示
      * @author Will
      * @date:  2023-11-06
      * @param dto
      * @return ApiResult
      */
     @PostMapping("/editView")
     @LogAction(value = LogActionEnum.UPDATE, desc = "编辑付款/退款")
     public ApiResult<List<EditViewDTO>> editView(@RequestBody @Validated BaseIdDTO dto) {
     	return success(logisticsBillCostService.editView(dto.getId()));
     }
     
     /**
      * 编辑付款/退款 保存
      * @author Will
      * @date:  2023-11-06
      * @param dtoList
      * @return ApiResult
      */
     @PostMapping("/edit")
     @LogAction(value = LogActionEnum.UPDATE, desc = "编辑付款/退款")
     @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
     tableField = "create_user_id",
     menuCode = "tms:logisticsLastMileCost:edit",
     serviceClass = LogisticsBillCostService.class,
     keyIdName = "id")
     public ApiResult<Object> edit(@RequestBody @Validated List<EditDataDTO> dtoList) {
     	logisticsBillCostService.edit(dtoList);
     	return success();
     }
     
     /**
      * 下推分摊
      * @author Will
      * @date:  2023-11-06
      * @param dto
      * @return ApiResult
      */
     @PostMapping("/pushAllocation")
     @LogAction(value = LogActionEnum.INSERT, desc = "下推分摊")
     @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
     tableField = "create_user_id",
     menuCode = "tms:logisticsLastMileCost:pushAllocation",
     serviceClass = LogisticsBillCostService.class,
     keyIdName = "id")
     public ApiResult<List<BatchResultDTO>> pushAllocation(@RequestBody @Validated PushDTO dto) {
    	 List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
         for (String id : dto.getIds()) {
             BatchResultDTO submit;
             try {
                 submit = logisticsBillCostService.pushAllocation(id,dto.getReportDate());
             }catch (Exception e){
                 log.error("尾程费用 状态变更",e);
                 LogisticsBillCostEntity entity = logisticsBillCostService.getById(id);
                 if (ObjectUtil.isEmpty(entity)) {
                     submit = BatchResultDTO.fail(id, id, "尾程费用不存在, 下推分摊");
                     resultDTOS.add(submit);
                     continue;
                 }
                 submit = BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), e.getMessage());
             }
             resultDTOS.add(submit);
         }
         return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
     }
     
     /**
      * 删除
      * @author Will
      * @date: 2023/11/13 15:35
      * @param dto
      * @return ApiResult<List<BatchResultDTO>>
      */
     @LogAction(value = LogActionEnum.DELETE, desc = "状态变更:idList={idList}")
     @PostMapping("/delete")
     @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
     tableField = "create_user_id",
     menuCode = "tms:logisticsLastMileCost:updatePayStatus",
     serviceClass = LogisticsBillCostService.class,
     keyIdName = "id")
     public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
     	List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
     	for (String id : dto.getIds()) {
     		BatchResultDTO submit;
     		try {
     			submit = logisticsBillCostService.delete(id);
     		}catch (Exception e){
     			log.error("尾程费用 状态变更",e);
     			LogisticsBillCostEntity entity = logisticsBillCostService.getById(id);
     			if (ObjectUtil.isEmpty(entity)) {
     				submit = BatchResultDTO.fail(id, id, "尾程费用不存在, 状态变更");
     				resultDTOS.add(submit);
     				continue;
     			}
     			submit = BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), e.getMessage());
     		}
     		resultDTOS.add(submit);
     	}
     	return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
     }
}
