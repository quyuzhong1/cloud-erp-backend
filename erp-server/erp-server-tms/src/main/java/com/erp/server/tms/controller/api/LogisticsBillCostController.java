package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.server.tms.query.LogisticsBillCostQueryHandler;
import com.erp.server.tms.service.LogisticsBillCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 自发货费用
 *
 * @author Will
 * @since 2023-11-06
 */
@Slf4j
@RestController
@LogSystemModule("自发货费用")
@RequestMapping("/logisticsBillCost")
public class LogisticsBillCostController extends BaseController {

    @Resource
    private LogisticsBillCostService logisticsBillCostService;


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
            menuCode = "tms:logisticsBillCost:paging",
            tableAlias = "lbc"
    )
    public ApiResult<List<LogisticsBillCostDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsBillCostDTO.TabListDTO> tabList = logisticsBillCostService.tabList(dto, DictCostAttributionEnum.SELF_DELIVER);
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
            menuCode = "tms:logisticsBillCost:paging",
            tableAlias = "lbc"
    )
    @WebAdvanceQuery(handler = LogisticsBillCostQueryHandler.class)
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "自发货费用修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsBillCost:update",
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
     * 状态变更
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "状态变更:idList={idList}")
    @PostMapping("/updateReconciliationStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBillCost:updateReconciliationStatus",
            serviceClass = LogisticsBillCostService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> updateReconciliationStatus(@RequestBody @Validated LogisticsBillCostDTO.UpdateStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = logisticsBillCostService.updateReconciliationStatus(id,dto.getReconciliationStatus());
            }catch (Exception e){
                log.error("自发货费用 状态变更",e);
                LogisticsBillCostEntity entity = logisticsBillCostService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "自发货费用不存在, 状态变更");
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载自发货费用模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object>downloadTemplate(HttpServletResponse response) {
        logisticsBillCostService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入
     * @author Will
     * @date: 2023/11/13 15:14
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入自发货费用模板")
    @PostMapping("/import")
    public ApiResult<Object>importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = logisticsBillCostService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     *  导出
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出自发货费用模板")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Object>exportExcel(@RequestBody LogisticsBillCostDTO.PagingParamDTO dto) {
        Boolean flag = logisticsBillCostService.exportExcel(dto);
        return flag == true ? success() : failure();
    }
    /**
     * 初始化头程对账单汇率
     * @return
     */
    @PostMapping("/initExchangeRate")
    public ApiResult initExchangeRate(){
        logisticsBillCostService.initExchangeRate();
        return success();
    }
}
