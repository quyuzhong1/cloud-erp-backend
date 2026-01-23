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
import com.erp.server.plm.service.PlmAttachmentService;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
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
    
    @Resource
    private PlmAttachmentService plmAttachmentService;

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
     * 批量新增 调用前先调用 /plm/attachment/batchUpload
     * @author wuhaotian
     * @date: 2025-12-29
     * @param dto 批量新增参数（包含addDTOList）
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "图片分类附件关联表批量新增")
    public ApiResult<?> batchAdd(@RequestBody @Validated RefProductImgAttachmentDTO.BatchAddDTO dto) {
        List<RefProductImgAttachmentDTO.AddDTO> addDTOList = dto.getAddDTOList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(addDTOList.size());
        
        // 1. 收集所有attachmentId，批量查询附件，构建attachmentId -> attachName的map
        List<String> attachmentIds = addDTOList.stream()
                .map(RefProductImgAttachmentDTO.AddDTO::getAttachmentId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        Map<String, String> attachmentIdToNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(attachmentIds)) {
            List<PlmAttachmentEntity> attachments = plmAttachmentService.listByIds(attachmentIds);
            if (CollUtil.isNotEmpty(attachments)) {
                attachmentIdToNameMap = attachments.stream()
                        .filter(att -> StrUtil.isNotBlank(att.getAttachName()))
                        .collect(Collectors.toMap(
                                PlmAttachmentEntity::getId,
                                PlmAttachmentEntity::getAttachName,
                                (v1, v2) -> v1 // 如果有重复key，保留第一个
                        ));
            }
        }
        
        // 2. 如果有多张图片，将第一张移到最后处理，确保第一张图片成为主图
        // 因为add方法会把最新上传的图片设置为主图
        if (addDTOList.size() > 1) {
            RefProductImgAttachmentDTO.AddDTO firstDTO = addDTOList.remove(0);
            addDTOList.add(firstDTO);
        }
        
        // 3. 循环每个addDTO，调用单个add方法
        for (RefProductImgAttachmentDTO.AddDTO addDTO : addDTOList) {
            BatchResultDTO addResult;
            try {
                BaseResultDTO.AddDTO result = refProductImgAttachmentService.add(addDTO);
                // 使用附件名称作为code，如果找不到则使用attachmentId，再找不到则使用result.getId()
                String code = attachmentIdToNameMap.getOrDefault(
                        addDTO.getAttachmentId(),
                        StrUtil.isNotBlank(addDTO.getAttachmentId()) ? addDTO.getAttachmentId() : result.getId()
                );
                addResult = BatchResultDTO.success(result.getId(), code);
            } catch (Exception e) {
                log.error("图片分类附件关联表新增失败", e);
                // 使用附件名称作为code，如果找不到则使用attachmentId
                String code = attachmentIdToNameMap.getOrDefault(
                        addDTO.getAttachmentId(),
                        StrUtil.isNotBlank(addDTO.getAttachmentId()) ? addDTO.getAttachmentId() : ""
                );
                addResult = BatchResultDTO.fail("", code, e.getMessage());
            }
            resultDTOS.add(addResult);
        }
        
        // 4. 将最后一个结果（第一张图片的结果）移回第一个位置
        if (resultDTOS.size() > 1) {
            BatchResultDTO lastResult = resultDTOS.remove(resultDTOS.size() - 1);
            resultDTOS.add(0, lastResult);
        }
        
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
            tableAlias = "rpia"
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
     * 批量上传图片（异步）
     * @author wuhaotian
     * @date: 2025-12-29
     * @param dto 批量上传参数（包含zipUrl和categoryId）
     * @return ApiResult
     */
    @PostMapping("/batchUpload")
    @LogAction(value = LogActionEnum.INSERT, desc = "批量上传图片")
    public ApiResult<?> batchUpload(@RequestBody @Validated RefProductImgAttachmentDTO.BatchUploadDTO dto) {
        Boolean flag = refProductImgAttachmentService.importBatchUpload(dto);
        return flag == true ? success() : failure();
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

    /**
     * 上传产品主图（对比新增、删除、保留，自动生成缩略图，更新images_url）
     * @author wuhaotian
     * @date: 2025-12-29
     * @param dto 上传产品主图参数（包含skuId和imagesUrls）
     * @return ApiResult<Boolean>
     */
    @PostMapping("/uploadProductMainImage")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "上传产品主图")
    public ApiResult<Boolean> uploadProductMainImage(@RequestBody @Validated RefProductImgAttachmentDTO.UploadProductMainImageDTO dto) {
        return success(refProductImgAttachmentService.uploadProductMainImage(dto));
    }

    /**
     * 上传产品主图文件（保存原图和缩略图，返回缩略图URL）
     * @author wuhaotian
     * @date: 2025-12-29
     * @param multipartFile 图片文件数组
     * @param skuId SKU ID
     * @param request
     * @return ApiResult<List<String>> 返回缩略图URL列表
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "上传产品主图文件:文件名={name}")
    @PostMapping(value = "/uploadProductMainImageFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<List<String>> uploadProductMainImageFile(
            @RequestParam("multipartFile") MultipartFile[] multipartFile,
            @RequestParam("skuId") String skuId,
            HttpServletRequest request) {
        List<String> thumbnailUrls = refProductImgAttachmentService.uploadProductMainImageFile(multipartFile, skuId);
        return success(thumbnailUrls);
    }

}
