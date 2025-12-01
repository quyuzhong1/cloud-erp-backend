package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
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
import com.erp.server.oms.service.KolFeedbackService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolFeedbackDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * KOL回片列表
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("KOL回片列表")
@RequestMapping("/kolFeedback")
public class KolFeedbackController extends BaseController {

    @Resource
    private KolFeedbackService kolFeedbackService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "KOL回片列表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolFeedbackDTO.AddDTO dto) {
        return success(kolFeedbackService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "KOL回片列表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolFeedback:update",
        serviceClass = KolFeedbackService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolFeedbackDTO.UpdateDTO dto) {
        kolFeedbackService.update(dto);
        return success();
    }

    /**
    * 分页查询
    * @author wuhaotian
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<PagingVO<KolFeedbackDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:paging",
            tableAlias = "kf")
    @WebAdvanceQuery
    public ApiResult<PagingVO<KolFeedbackDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolFeedbackDTO.ParamDTO> dto) {
        return success(kolFeedbackService.paging(dto));
    }

    /**
     * 获取状态统计
     * @author wuhaotian
     * @date:  2025-12-01
     * @param dto
     * @return ApiResult<List<KolFeedbackDTO.TabListDTO>>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:paging",
            tableAlias = "kf")
    @WebAdvanceQuery
    public ApiResult<List<KolFeedbackDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(kolFeedbackService.tabList(dto));
    }

    /**
     * 批量删除
     * @author wuhaotian
     * @date:  2025-12-01
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "KOL回片列表批量删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:batchDelete",
            serviceClass = KolFeedbackService.class,
            keyIdName = "ids")
    public ApiResult<?> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        kolFeedbackService.batchDelete(dto);
        return success();
    }

    /**
     * 导出
     * @author wuhaotian
     * @date:  2025-12-01
     * @param dto
     * @param response
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "KOL回片列表导出")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolFeedback:export",
            tableAlias = "kf")
    public void export(@RequestBody @Validated PagingDTO<KolFeedbackDTO.ParamDTO> dto, HttpServletResponse response) {
        kolFeedbackService.export(dto, response);
    }

    /**
     * 导入
     * @author wuhaotian
     * @date:  2025-12-01
     * @param file
     * @return ApiResult
     */
    @PostMapping("/import")
    @LogAction(value = LogActionEnum.IMPORT, desc = "KOL回片列表导入")
    public ApiResult<?> importData(@RequestParam("file") MultipartFile file) {
        kolFeedbackService.importData(file);
        return success();
    }

    /**
     * 下载导入模板
     * @author wuhaotian
     * @date:  2025-12-01
     * @param response
     */
    @PostMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        kolFeedbackService.downloadTemplate(response);
        return success();
    }

}
