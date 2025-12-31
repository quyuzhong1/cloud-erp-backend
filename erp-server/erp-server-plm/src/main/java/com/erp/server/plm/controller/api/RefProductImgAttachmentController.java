package com.erp.server.plm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.plm.query.RefProductImgAttachmentQueryHandler;
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
import com.erp.server.plm.service.RefProductImgAttachmentService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.RefProductImgAttachmentEntity;

/**
 * 图片分类附件关联表
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
@Slf4j
@RestController
@LogSystemModule("图片分类附件关联表")
@RequestMapping("/refProductImgAttachment")
public class RefProductImgAttachmentController extends BaseController {

    @Resource
    private RefProductImgAttachmentService refProductImgAttachmentService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-12-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "图片分类附件关联表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated RefProductImgAttachmentDTO.AddDTO dto) {
        return success(refProductImgAttachmentService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-12-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "图片分类附件关联表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:refProductImgAttachment:update",
        serviceClass = RefProductImgAttachmentService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated RefProductImgAttachmentDTO.UpdateDTO dto) {
        refProductImgAttachmentService.update(dto);
        return success();
    }



    /**
    * 列表查询
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return ApiResult<PagingVO<RefProductImgAttachmentDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:refProductImgAttachment:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = RefProductImgAttachmentQueryHandler.class)
    public ApiResult<PagingVO<RefProductImgAttachmentDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<RefProductImgAttachmentDTO.PagingParamDTO> dto) {
        return success(refProductImgAttachmentService.paging(dto));
    }


    /**
    * 详情
    * @author wuhaotian
    * @date:  2025-12-29
    * @param id
    * @return ApiResult<RefProductImgAttachmentDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:refProductImgAttachment:view",
            serviceClass = RefProductImgAttachmentService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<RefProductImgAttachmentDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(refProductImgAttachmentService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wuhaotian
    * @date:  2025-12-29
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:refProductImgAttachment:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "图片分类附件关联表导出Excel数据")
    public void exportList(@RequestBody @Validated RefProductImgAttachmentDTO.ExportDTO dto, HttpServletResponse response) {
        refProductImgAttachmentService.exportList(dto, response);
    }


}
