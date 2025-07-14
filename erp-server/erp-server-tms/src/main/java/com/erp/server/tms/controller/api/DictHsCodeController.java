package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.DictHsCodeEntity;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.tms.service.DictHsCodeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.DictHsCodeDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 出口申报要素表
 *
 * @author jack
 * @since 2025-07-11
 */
@Slf4j
@RestController
@LogSystemModule("出口申报要素表")
@RequestMapping("/dictHsCode")
public class DictHsCodeController extends BaseController {

    @Resource
    private DictHsCodeService dictHsCodeService;

    /**
    * 新增
    * @author jack
    * @date:  2025-07-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "出口申报要素表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DictHsCodeDTO.AddDTO dto) {
        return success(dictHsCodeService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-07-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "出口申报要素表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:dictHsCode:update",
        serviceClass = DictHsCodeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DictHsCodeDTO.UpdateDTO dto) {
        dictHsCodeService.update(dto);
        return success();
    }

    /**
     * 分页查询
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:dictHsCode:paging",
            tableAlias = "dhc"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<DictHsCodeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DictHsCodeDTO.PagingParamDTO> dto) {
        PagingVO<DictHsCodeDTO.ListDTO> pagingVO = dictHsCodeService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 删除
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:dictHsCode:delete",
            serviceClass = DictHsCodeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "出口申报要素删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<DictHsCodeEntity> list = dictHsCodeService.listByIds(ids);
        Map<String, DictHsCodeEntity> idEntityMap = list.stream().collect(Collectors.toMap(DictHsCodeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = dictHsCodeService.delete(id);
            }catch (Exception e){
                log.error("出口申报要素删除失败",e);
                DictHsCodeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "出口申报要素不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }else{
                    deleteResult = BatchResultDTO.fail(entity.getId(), entity.getHsCode(), e.getMessage());
                }
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-06-21
     * @return ApiResult<DictHsCodeDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:dictHsCode:view",
            serviceClass = DictHsCodeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DictHsCodeDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        return success(dictHsCodeService.view(id));
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:dictHsCode:export",
            tableAlias = "dhc"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated DictHsCodeDTO.PagingParamDTO dto, HttpServletResponse response) {
        dictHsCodeService.exportList(dto, response);
        return success();
    }

    /**
     * 导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入出口申报要素")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = dictHsCodeService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        dictHsCodeService.downloadTemplate(response);
        return success();
    }

}
