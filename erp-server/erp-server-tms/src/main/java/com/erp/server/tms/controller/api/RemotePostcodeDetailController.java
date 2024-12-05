package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.RemotePostcodeDetailDTO;
import com.erp.model.tms.entity.RemotePostcodeDetailEntity;
import com.erp.server.tms.service.RemotePostcodeDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 偏远邮编明细表
 *
 * @author jack
 * @since 2024-11-29
 */
@Slf4j
@RestController
@LogSystemModule("偏远邮编明细表")
@RequestMapping("/remotePostcodeDetail")
public class RemotePostcodeDetailController extends BaseController {

    @Resource
    private RemotePostcodeDetailService remotePostcodeDetailService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcodeDetail:paging",
            tableAlias = ""
    )
    public ApiResult<List<RemotePostcodeDetailDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(remotePostcodeDetailService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return ApiResult<PagingVO<RemotePostcodeDetailDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcodeDetail:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<RemotePostcodeDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<RemotePostcodeDetailDTO.PagingParamDTO> dto) {
        return success(remotePostcodeDetailService.paging(dto));
    }

    /**
    * 删除
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcodeDetail:delete",
            serviceClass = RemotePostcodeDetailService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "偏远邮编明细表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RemotePostcodeDetailEntity> list = remotePostcodeDetailService.lambdaQuery().in(RemotePostcodeDetailEntity::getId, ids).list();
		Map<String, RemotePostcodeDetailEntity> idEntityMap = list.stream().collect(Collectors.toMap(RemotePostcodeDetailEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = remotePostcodeDetailService.delete(id);
            }catch (Exception e){
                log.error("偏远邮编明细单删除失败",e);
                RemotePostcodeDetailEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "偏远邮编明细单不存在, 删除失败");
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
    * 详情
    * @author jack
    * @date:  2024-11-29
    * @param id
    * @return ApiResult<RemotePostcodeDetailDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcodeDetail:view",
            serviceClass = RemotePostcodeDetailService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<RemotePostcodeDetailDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(remotePostcodeDetailService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcodeDetail:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "偏远邮编明细表导出Excel数据")
    public void exportList(@RequestBody @Validated RemotePostcodeDetailDTO.ExportDTO dto, HttpServletResponse response) {
        remotePostcodeDetailService.exportList(dto, response);
    }


    /**
     * 导入偏远邮编组
     * @author jack
     * @date:  2024-11-29
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入偏远邮编组")
    @PostMapping("/import")
    public ApiResult<RemotePostcodeDetailDTO.ImportResultDTO>importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success(remotePostcodeDetailService.importFile(excelFile, response));
    }

    /**
     * 下载模板
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载偏远邮编组模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        remotePostcodeDetailService.downloadTemplate(response);
        return success();
    }


}
