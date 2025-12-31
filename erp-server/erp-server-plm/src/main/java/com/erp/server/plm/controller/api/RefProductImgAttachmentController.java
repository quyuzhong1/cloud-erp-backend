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
import com.erp.model.plm.entity.RefProductImgAttachmentEntity;
import cn.hutool.core.util.StrUtil;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

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
    * 新增 调用前先调用 /plm/attachment/upload
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
     * 批量上传图片
     * @author wuhaotian
     * @date: 2025-12-29
     * @param dto 批量上传参数（包含zipUrl和categoryId）
     * @return ApiResult
     */
    @PostMapping("/batchUpload")
    @LogAction(value = LogActionEnum.INSERT, desc = "批量上传图片")
    public ApiResult<?> batchUpload(@RequestBody @Validated RefProductImgAttachmentDTO.BatchUploadDTO dto) {
        refProductImgAttachmentService.batchUpload(dto);
        return success();
    }

    /**
     * 批量删除
     * @author wuhaotian
     * @date: 2025-12-29
     * @param dto 删除参数（包含ids）
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "图片分类附件关联表批量删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:refProductImgAttachment:batchDelete",
            serviceClass = RefProductImgAttachmentService.class,
            keyIdName = "ids")
    public ApiResult<?> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        
        // 批量查询所有记录，构建id到entity的映射
        List<RefProductImgAttachmentEntity> list = refProductImgAttachmentService.lambdaQuery()
                .in(RefProductImgAttachmentEntity::getId, ids).list();
        Map<String, RefProductImgAttachmentEntity> idEntityMap = list.stream()
                .collect(Collectors.toMap(RefProductImgAttachmentEntity::getId, w -> w));
        
        // 循环每个id，调用单个delete方法
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = refProductImgAttachmentService.delete(id);
            } catch (Exception e) {
                log.error("图片分类附件关联表删除失败", e);
                RefProductImgAttachmentEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "图片分类附件关联记录不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                String code = StrUtil.isNotBlank(entity.getSkuNo()) ? entity.getSkuNo() : id;
                deleteResult = BatchResultDTO.fail(entity.getId(), code, e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 移动分类
     * @author wuhaotian
     * @date: 2025-12-29
     * @param dto 移动分类参数（包含ids和categoryId）
     * @return ApiResult
     */
    @PostMapping("/moveCategory")
    @LogAction(value = LogActionEnum.UPDATE, desc = "图片分类附件关联表移动分类")
    public ApiResult<?> moveCategory(@RequestBody @Validated RefProductImgAttachmentDTO.MoveCategoryDTO dto) {
        refProductImgAttachmentService.moveCategory(dto);
        return success();
    }

    /**
     * 批量下载图片
     * @author wuhaotian
     * @date: 2025-12-29
     * @param dto 批量下载参数（包含ids）
     * @return ApiResult
     */
    @PostMapping("/batchDownload")
    @LogAction(value = LogActionEnum.EXPORT, desc = "批量下载图片")
    public ApiResult<?> batchDownload(@RequestBody @Validated RefProductImgAttachmentDTO.BatchDownloadDTO dto) {
        refProductImgAttachmentService.batchDownload(dto);
        return success();
    }

}
