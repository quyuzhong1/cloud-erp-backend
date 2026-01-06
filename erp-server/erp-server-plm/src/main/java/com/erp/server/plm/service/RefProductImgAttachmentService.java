package com.erp.server.plm.service;
import com.erp.model.plm.entity.RefProductImgAttachmentEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
import com.common.business.vo.PagingVO;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 图片分类附件关联表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
public interface RefProductImgAttachmentService extends SuperService<RefProductImgAttachmentEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(RefProductImgAttachmentDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    Boolean update(RefProductImgAttachmentDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-12-29
    * @param pagingParamDTO
    * @return PagingVO<RefProductImgAttachmentDTO.ListDTO>>
    */
    PagingVO<RefProductImgAttachmentDTO.ListDTO> paging(PagingDTO<RefProductImgAttachmentDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return List<RefProductImgAttachmentDTO.TabListDTO>>
    */
    List<RefProductImgAttachmentDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-12-29
    * @param id
    * @return
    */
    RefProductImgAttachmentDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(RefProductImgAttachmentDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 批量上传图片（异步入口）
     * @param dto 批量上传参数
     * @author wuhaotian
     * @date: 2025-12-29
     * @return Boolean
     */
    Boolean importBatchUpload(RefProductImgAttachmentDTO.BatchUploadDTO dto);

    /**
     * 批量上传图片（实际处理逻辑）
     * @param dto 批量上传参数
     * @author wuhaotian
     * @date: 2025-12-29
     */
    void batchUpload(RefProductImgAttachmentDTO.BatchUploadDTO dto);

    /**
     * 单个删除
     * @param id 关联记录ID
     * @author wuhaotian
     * @date: 2025-12-29
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);

    /**
     * 移动分类
     * @param dto 移动分类参数（包含ids和categoryId）
     * @author wuhaotian
     * @date: 2025-12-29
     * @return Boolean
     */
    Boolean moveCategory(RefProductImgAttachmentDTO.MoveCategoryDTO dto);

    /**
     * 批量下载图片
     * @param dto 批量下载参数（包含ids）
     * @author wuhaotian
     * @date: 2025-12-29
     * @return Boolean
     */
    Boolean batchDownload(RefProductImgAttachmentDTO.BatchDownloadDTO dto);

    /**
     * 构建产品图片文件夹结构并创建ZIP（用于批量下载）
     * @param dto 批量下载参数（包含ids）
     * @author wuhaotian
     * @date: 2025-12-29
     * @return ZIP文件的FastDFS URL
     */
    String buildProductImagesFolderStructure(RefProductImgAttachmentDTO.BatchDownloadDTO dto);

    /**
     * 上传产品主图（对比新增、删除、保留，自动生成缩略图，更新images_url）
     * @param dto 上传产品主图参数（包含skuId和imagesUrls）
     * @author wuhaotian
     * @date: 2025-12-29
     * @return Boolean
     */
    Boolean uploadProductMainImage(RefProductImgAttachmentDTO.UploadProductMainImageDTO dto);

    /**
     * 上传产品主图文件（保存原图和缩略图，返回缩略图URL）
     * @param multipartFileList 图片文件数组
     * @param skuId SKU ID
     * @author wuhaotian
     * @date: 2025-12-29
     * @return List<String> 返回缩略图URL列表
     */
    List<String> uploadProductMainImageFile(MultipartFile[] multipartFileList, String skuId);
}
