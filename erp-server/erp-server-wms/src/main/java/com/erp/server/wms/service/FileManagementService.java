package com.erp.server.wms.service;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FileManagementDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.WmsAttachmentEntity;

import java.util.List;

/**
 * <p>
 * 文件管理 服务类
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
 */
public interface FileManagementService extends SuperService<FileManagementEntity> {

    /**
    * 分页列表查询
    * @author zdy
    * @date: 2026-03-20
    * @param pagingParamDTO
    * @return PagingVO<FileManagementDTO.ListDTO>>
    */
    PagingVO<FileManagementDTO.ListDTO> paging(PagingDTO<FileManagementDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 详情
    * @author zdy
    * @date: 2026-03-20
    * @param id
    * @return
    */
    FileManagementDTO.ViewDTO view(String id);

    /**
     * 版本记录
     * @param id
     * @return
     */
    List<FileManagementDTO.VersionDTO> history(String id);

    List<BatchResultDTO> genQcStandard(List<String> skuNoList, String attachUrl);

    QcStandardDTO.AddDTO genSingleQcStandard(String id);

    FileManagementDTO.AttachDTO getCategoryGeneralStandardFile(String skuId);

    /**
     * 根据附件地址查询sku信息
     * @param attachUrl
     * @return
     */
    List<SkuVO> getSkuVOS(String attachUrl);

    /**
     * 处理数据
     * @param fileManagementEntity
     * @param skuVOS
     * @return
     */
    List<FileManagementEntity> handleData(FileManagementEntity fileManagementEntity, List<SkuVO> skuVOS);

    BatchResultDTO addEntity(FileManagementEntity entity, WmsAttachmentEntity attachmentEntity);

    BatchResultDTO updateEntity(FileManagementEntity entity, WmsAttachmentEntity attachmentEntity);
}
