package com.erp.server.tms.controller.api;


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
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.erp.model.tms.entity.RemotePostcodeEntity;
import com.erp.server.tms.service.RemotePostcodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 偏远邮编组
 *
 * @author jack
 * @since 2024-11-29
 */
@Slf4j
@RestController
@LogSystemModule("偏远邮编组")
@RequestMapping("/remotePostcode")
public class RemotePostcodeController extends BaseController {

    @Resource
    private RemotePostcodeService remotePostcodeService;

    /**
    * 新增
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "偏远邮编组新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated RemotePostcodeDTO.AddDTO dto) {
        return success(remotePostcodeService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "偏远邮编组修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:remotePostcode:update",
        serviceClass = RemotePostcodeService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated RemotePostcodeDTO.UpdateDTO dto) {
        remotePostcodeService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:paging",
            tableAlias = ""
    )
    public ApiResult<List<RemotePostcodeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(remotePostcodeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2024-11-29
    * @param dto
    * @return ApiResult<PagingVO<RemotePostcodeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<RemotePostcodeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<RemotePostcodeDTO.PagingParamDTO> dto) {
        return success(remotePostcodeService.paging(dto));
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
            menuCode = "tms:remotePostcode:delete",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "偏远邮编组删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<RemotePostcodeEntity> list = remotePostcodeService.lambdaQuery().in(RemotePostcodeEntity::getId, ids).list();
		Map<String, RemotePostcodeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RemotePostcodeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = remotePostcodeService.delete(id);
            }catch (Exception e){
                log.error("偏远邮编组删除失败",e);
                RemotePostcodeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "偏远邮编组不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
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
    * @return ApiResult<RemotePostcodeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:remotePostcode:view",
            serviceClass = RemotePostcodeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<RemotePostcodeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(remotePostcodeService.view(id));
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
            menuCode = "tms:remotePostcode:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "偏远邮编组导出Excel数据")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated RemotePostcodeDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean flag = remotePostcodeService.exportList(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 邮编远程查询（分页型）
     * @author jack
     * @date:  2024-11-29
     * @param dto
     * @return ApiResult<PagingVO<RemotePostcodeDTO.ListDTO>>
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<RemotePostcodeDTO.ListDTO>> pagingSelect(@RequestBody @Validated PagingDTO<RemotePostcodeDTO.SelectDTO> dto) {
        return success(remotePostcodeService.pagingSelect(dto));
    }


    /**
     * 邮编下拉值
     * @author jack
     * @date:  2024-11-29
     * @return ApiResult<List<RemotePostcodeDTO.ListDTO>>
     */
    @PostMapping("/select")
    public ApiResult<List<RemotePostcodeDTO.ListDTO>> select(@RequestBody RemotePostcodeDTO.SelectDTO dto) {
        return success(remotePostcodeService.select(dto));
    }
}
