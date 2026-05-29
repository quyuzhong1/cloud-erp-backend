package com.erp.server.wms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import com.erp.server.wms.query.CfgQcUserQueryHandler;
import com.erp.server.wms.service.CfgQcUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 质检员配置
 *
 * @author wtr
 * @since 2026-05-27
 */
@Slf4j
@RestController
@LogSystemModule("质检员配置")
@RequestMapping("/cfgQcUser")
public class CfgQcUserController extends BaseController {

    @Resource
    private CfgQcUserService cfgQcUserService;

    /**
    * 新增
    * @author wtr
    * @date:  2026-05-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增质检员配置")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgQcUserDTO.AddDTO dto) {
        return success(cfgQcUserService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2026-05-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改质检员配置")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:cfgQcUser:update",
            serviceClass = CfgQcUserService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgQcUserDTO.UpdateDTO dto) {
        cfgQcUserService.update(dto);
        return success();
    }

    /**
    * 列表查询
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return ApiResult<PagingVO<CfgQcUserDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:cfgQcUser:paging",
            tableAlias = "cqu"
    )
    @WebAdvanceQuery(handler = CfgQcUserQueryHandler.class)
    public ApiResult<PagingVO<CfgQcUserDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgQcUserDTO.PagingParamDTO> dto) {
        return success(cfgQcUserService.paging(dto));
    }

    /**
    * 详情
    * @author wtr
    * @date:  2026-05-27
    * @param id
    * @return ApiResult<CfgQcUserDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:cfgQcUser:view",
            serviceClass = CfgQcUserService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<CfgQcUserDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(cfgQcUserService.view(id));
    }

    /**
     * 导入Excel数据
     * @author wtr
     * @date:  2026-05-27
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入质检员配置")
    @PostMapping("/importFile")
    public ApiResult importFile(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = cfgQcUserService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author wtr
     * @date: 2026-05-28
     * @param request
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载质检员配置模板")
    @GetMapping("/exportTemplate")
    public ApiResult<Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/cfgQcUserTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
        return success();
    }

    /**
    * 导出Excel数据
    * @author wtr
    * @date:  2026-05-27
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:cfgQcUser:export",
            tableAlias = "cqu"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出质检员配置")
    public ApiResult<Object> exportList(@RequestBody @Validated CfgQcUserDTO.ExportDTO dto, HttpServletResponse response) {
        cfgQcUserService.exportList(dto, response);
        return success();
    }

    /**
    * 获取质检员下拉列表（根据组织查询）
    * @author wtr
    * @date: 2026-05-27
    * @param warehouseId
    * @return
    */
    @GetMapping("/qcUserList")
    public ApiResult<List<CfgQcUserDTO.QcUserSelectDTO>> qcUserList(@RequestParam(value = "warehouseId", required = false) String warehouseId) {
        return success(cfgQcUserService.qcUserList(warehouseId));
    }

    /**
    * 删除
    * @author wtr
    * @date: 2026-05-27
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:cfgQcUser:delete",
            serviceClass = CfgQcUserService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除质检员配置")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<CfgQcUserEntity> list = cfgQcUserService.lambdaQuery().in(CfgQcUserEntity::getId, ids).list();
        Map<String, CfgQcUserEntity> idEntityMap = list.stream().collect(Collectors.toMap(CfgQcUserEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgQcUserService.delete(id);
            } catch (Exception e) {
                log.error("删除失败", e);
                CfgQcUserEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSupplierId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
