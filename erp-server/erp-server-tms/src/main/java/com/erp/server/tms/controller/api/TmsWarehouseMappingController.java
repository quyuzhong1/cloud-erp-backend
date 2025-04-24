package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.model.tms.entity.TmsWarehouseMappingEntity;
import com.erp.server.tms.query.TmsCfgSailingQueryHandler;
import com.erp.server.tms.service.TmsWarehouseMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 仓库匹配
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/tmsWarehouseMapping")
public class TmsWarehouseMappingController extends BaseController {

    @Resource
    private TmsWarehouseMappingService tmsWarehouseMappingService;

    /**
     * 分页查询
     * @author Will
     * @date: 2024/3/19 11:52
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsWarehouseMapping:paging",
            tableAlias = "twm"
    )
    @WebAdvanceQuery(handler = TmsCfgSailingQueryHandler.class)
    public ApiResult<PagingVO<TmsWarehouseMappingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        return success(tmsWarehouseMappingService.paging(dto));
    }

    /**
    * 新增
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓库匹配")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsWarehouseMappingDTO.AddDTO dto) {
        return success(tmsWarehouseMappingService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改仓库匹配")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsWarehouseMapping:update",
        serviceClass = TmsWarehouseMappingService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TmsWarehouseMappingDTO.UpdateDTO dto) {
        tmsWarehouseMappingService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2024/3/19 11:55
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<TmsWarehouseMappingDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsWarehouseMappingService.view(id));
    }

    /**
     * 删除
     * @author Will
     * @date: 2024/3/19 11:58
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsWarehouseMapping:delete",
            serviceClass = TmsWarehouseMappingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除仓库匹配")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = tmsWarehouseMappingService.delete(id);
            }catch (Exception e){
                log.error("仓库匹配删除失败",e);
                TmsWarehouseMappingEntity entity = tmsWarehouseMappingService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "仓库匹配数据不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载仓库匹配模板
     * @author Will
     * @date: 2024/3/19 12:02
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载仓库匹配模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object>downloadTemplate(HttpServletResponse response) {
        tmsWarehouseMappingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入仓库匹配
     * @author Will
     * @date: 2024/3/19 12:04
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入仓库匹配")
    @PostMapping("/import")
    public ApiResult<Object>exportWarehouse(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = tmsWarehouseMappingService.importFile(excelFile, response);
        return result ? success() : failure();
    }


    /**
     * 导出
     * @author Will
     * @date: 2024/3/19 12:06
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出仓库匹配")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Object>exportExcel(@RequestBody TmsWarehouseMappingDTO.PagingParamDTO dto) {
        Boolean flag = tmsWarehouseMappingService.exportExcel(dto);
        return flag == true ? success() : failure();
    }
}
