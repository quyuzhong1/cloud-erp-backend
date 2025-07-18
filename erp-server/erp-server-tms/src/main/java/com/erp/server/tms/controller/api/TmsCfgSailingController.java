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
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.model.tms.dto.TmsCfgSailingDTO;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import com.erp.server.tms.query.TmsCfgSailingQueryHandler;
import com.erp.server.tms.service.TmsCfgSailingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 截单开船配置
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@RestController
@LogSystemModule("截单开船配置")
@RequestMapping("/tmsCfgSailing")
public class TmsCfgSailingController extends BaseController {

    @Resource
    private TmsCfgSailingService tmsCfgSailingService;


    /**
     * 分页查询
     * @author Will
     * @date: 2024/3/18 9:07
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsCfgSailing:paging",
            tableAlias = "tcs"
    )
    @WebAdvanceQuery(handler = TmsCfgSailingQueryHandler.class)
    public ApiResult<PagingVO<TmsCfgSailingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsCfgSailingDTO.PagingParamDTO> dto) {
        return success(tmsCfgSailingService.paging(dto));
    }

    /**
    * 新增
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "截单开船配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsCfgSailingDTO.AddDTO dto) {
        return success(tmsCfgSailingService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "截单开船配置修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsCfgSailing:update",
        serviceClass = TmsCfgSailingService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TmsCfgSailingDTO.UpdateDTO dto) {
        tmsCfgSailingService.update(dto);
        return success();
    }


    /**
     * 查看详情
     * @author Will
     * @date: 2024/3/18 9:10
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<TmsCfgSailingDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsCfgSailingService.view(id));
    }


    /**
     * 删除
     * @author Will
     * @date: 2024/3/18 9:13
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsCfgSailing:delete",
            serviceClass = TmsCfgSailingService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "截单开船删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = tmsCfgSailingService.delete(id);
            }catch (Exception e){
                log.error("截单开船删除失败",e);
                TmsCfgSailingEntity entity = tmsCfgSailingService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "截单开船数据不存在, 删除失败");
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
     * 导出Excel数据
     * @author jack
     * @date:  2025-07-17
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsCfgSailing:export",
            tableAlias = "tcs"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery(handler = TmsCfgSailingQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated TmsCfgSailingDTO.PagingParamDTO dto, HttpServletResponse response) {
        tmsCfgSailingService.exportList(dto, response);
        return success();
    }
    /**
     * 导入
     * @author jack
     * @date:  2025-07-17
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入截单开船")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = tmsCfgSailingService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-07-17
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板截单开船")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        tmsCfgSailingService.downloadTemplate(response);
        return success();
    }
}
