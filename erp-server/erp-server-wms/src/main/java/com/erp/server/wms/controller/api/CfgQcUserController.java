package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
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
import com.erp.server.wms.service.CfgQcUserService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
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
            tableAlias = ""
    )
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
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出质检员配置")
    public void exportList(@RequestBody @Validated CfgQcUserDTO.ExportDTO dto, HttpServletResponse response) {
        cfgQcUserService.exportList(dto, response);
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
            }catch (Exception e){
                log.error("删除失败",e);
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