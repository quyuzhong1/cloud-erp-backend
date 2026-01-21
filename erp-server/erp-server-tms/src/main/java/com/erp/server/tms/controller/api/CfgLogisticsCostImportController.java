package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.entity.KolB2cApplicationEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.server.tms.query.CfgLogisticsCostImportQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgLogisticsCostImportDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;

/**
 * 费用项配置
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@RestController
@LogSystemModule("费用项配置")
@RequestMapping("/cfgLogisticsCostImport")
public class CfgLogisticsCostImportController extends BaseController {

    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;

    /**
    * 新增
    * @author jack
    * @date:  2026-01-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "费用项配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgLogisticsCostImportDTO.AddDTO dto) {
        return success(cfgLogisticsCostImportService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2026-01-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "费用项配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:cfgLogisticsCostImport:update",
        serviceClass = CfgLogisticsCostImportService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgLogisticsCostImportDTO.UpdateDTO dto) {
        cfgLogisticsCostImportService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImport:paging",
            tableAlias = "clci"
    )
    public ApiResult<List<CfgLogisticsCostImportDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(cfgLogisticsCostImportService.tabList(dto));
    }

    /**
    * 列表查询
     * 高级查询：/api/sys/cfgQueryCondition/getQueryCondition?code=tms:cfgLogisticsCostImport:paging
    * @author jack
    * @date: 2026-01-20
    * @param dto
    * @return ApiResult<PagingVO<CfgLogisticsCostImportDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImport:paging",
            tableAlias = "clci"
    )
    @WebAdvanceQuery(handler = CfgLogisticsCostImportQueryHandler.class)
    public ApiResult<PagingVO<CfgLogisticsCostImportDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgLogisticsCostImportDTO.PagingParamDTO> dto) {
        return success(cfgLogisticsCostImportService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-01-20
    * @param id
    * @return ApiResult<CfgLogisticsCostImportDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImport:view",
            serviceClass = CfgLogisticsCostImportService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgLogisticsCostImportDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgLogisticsCostImportService.view(id));
    }



    /**
     * 删除
     * @author jack
     * @date:  2026-01-20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImport:delete",
            serviceClass = CfgLogisticsCostImportService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "B2C寄样申请单删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgLogisticsCostImportEntity> list = cfgLogisticsCostImportService.lambdaQuery().in(CfgLogisticsCostImportEntity::getId, ids).list();
        Map<String, CfgLogisticsCostImportEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgLogisticsCostImportEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgLogisticsCostImportService.delete(id);
            }catch (Exception e){
                log.error("B2C寄样申请单删除失败",e);
                CfgLogisticsCostImportEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "B2C寄样申请单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 更新启禁用
     * @Auther jack
     * @Date 2026-01-07
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateDisabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgLogisticsCostImport:updateDisabled",
            serviceClass = CfgLogisticsCostImportService.class,
            keyIdName = "ids")
    public ApiResult<?> updateDisabled(@RequestBody @Validated CfgLogisticsCostImportDTO.UpdateDisabledDTO dto) {
        cfgLogisticsCostImportService.updateDisabled(dto);
        return  success();
    }

    /**
     *  异步导入
     * @author jack
     * @date:  2025-08-20
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入B2C-KOL寄样申请")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = cfgLogisticsCostImportService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-08-20
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "B2C-KOL寄样申请下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/kolB2cApplicationTemplate.xlsx";
        String standardExcelName = "kolB2cApplicationTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

}
