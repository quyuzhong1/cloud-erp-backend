package com.erp.server.wms.service;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.entity.FileManagementEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FileManagementDTO;
import com.common.business.vo.PagingVO;

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
    * 新增
    * @author zdy
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FileManagementDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2026-03-20
    * @param dto
    * @return
    */
    Boolean update(FileManagementDTO.UpdateDTO dto);


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
}
