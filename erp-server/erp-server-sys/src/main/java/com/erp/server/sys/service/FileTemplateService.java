package com.erp.server.sys.service;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.FileTemplateDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 文件模板url表 服务类
 * </p>
 *
 * @author wangwei
 * @since 2023-12-25
 */
public interface FileTemplateService extends SuperService<FileTemplateEntity> {

    /**
    * 新增
    * @author wangwei
    * @date: 2023-12-25
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FileTemplateDTO.AddDTO dto);

    /**
    * 修改
    * @author wangwei
    * @date: 2023-12-25
    * @param dto
    * @return
    */
    Boolean update(FileTemplateDTO.UpdateDTO dto);

    /**
     * @description: 查询模板
     * @author Will
     * @date: 2023/12/25 12:14
     * @param getOneDTO
     * @return FileTemplateEntity
     */
    FileTemplateEntity getByFileTemplate (FileTemplateDTO.GetOneDTO getOneDTO);

    /**
     * @description: fastdfs新增货修改
     * @author Will
     * @date: 2023/12/25 14:29
     * @param fastdfsAddDTO
     */
    void fastdfsAddOrUpdate (FileTemplateDTO.FastdfsAddOrUpdateDTO fastdfsAddDTO);
    /**
     * @description: 下载模板文件
     * @author Will
     * @date: 2023/12/27 9:38
     * @param id
     */
    void downLoadFdfsFileTemplate(String id);
}
