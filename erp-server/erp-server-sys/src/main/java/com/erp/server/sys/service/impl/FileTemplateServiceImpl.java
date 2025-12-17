package com.erp.server.sys.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.sys.mapper.FileTemplateMapper;
import com.erp.server.sys.service.FileTemplateService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * <p>
 * 文件模板url表 服务实现类
 * </p>
 *
 * @author wangwei
 * @since 2023-12-25
 */
@Slf4j
@Service
public class FileTemplateServiceImpl extends SuperServiceImpl<FileTemplateMapper, FileTemplateEntity> implements FileTemplateService {
    @Resource
    private FileFeign fileFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FileTemplateDTO.AddDTO addDTO) {
        FileTemplateEntity fileTemplateEntity = new FileTemplateEntity();
        BeanMapperUtils.copy(addDTO, fileTemplateEntity);

        // 数据处理
        handleData(fileTemplateEntity);

        log.info("开始新增文件url单");
        boolean save = super.save(fileTemplateEntity);
        if(!save) {
            throw new ServiceException("文件url单保存失败");
        }

        return new BaseResultDTO.AddDTO(fileTemplateEntity.getId(), fileTemplateEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FileTemplateDTO.UpdateDTO updateDTO) {
        FileTemplateEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "文件url单"));
        FileTemplateEntity fileTemplateEntity =  BeanMapperUtils.map(FileTemplateEntity.class, updateDTO);

        // 数据处理
        handleData(fileTemplateEntity);
        log.info("编辑 开始修改文件url单数据，id：【{}】", old.getId());
        boolean save = super.updateById(fileTemplateEntity);
        if(!save) {
            throw new ServiceException("文件url单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fastdfsAddOrUpdate (FileTemplateDTO.FastdfsAddOrUpdateDTO fastdfsAddDTO) {
        FileTemplateEntity fileTemplateEntity =  BeanMapperUtils.map(FileTemplateEntity.class, fastdfsAddDTO);
        //原数据
        FileTemplateEntity entity = this.getByFileTemplate(new FileTemplateDTO.GetOneDTO(fastdfsAddDTO.getSourceType(),fastdfsAddDTO.getName(),fastdfsAddDTO.getFileType()));
        if (ObjectUtil.isNotEmpty(entity)) {
            fileTemplateEntity.setId(entity.getId());
        }
        //上传新文件模板
        String url = "";
        try {
             url = fileFeign.uploadFile(fastdfsAddDTO.getFile());
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95018);
        }
        fileTemplateEntity.setUrl(url);
        this.saveOrUpdate(fileTemplateEntity);
        //删除原文件
        if (ObjectUtil.isNotEmpty(entity) && StrUtil.isNotBlank(entity.getUrl())) {
            try {
                fileFeign.deleteFile(entity.getUrl());
            } catch (Exception e) {
                throw new ServiceException(ApiError.ERROR_FILE_DELETE);
            }
        }
    }

    @Override
    public void downLoadFdfsFileTemplate(String id) {
        FileTemplateEntity fileTemplateEntity = this.getById(id);
        if (ObjectUtil.isEmpty(fileTemplateEntity) || StrUtil.isBlank(fileTemplateEntity.getUrl())) {
            throw new ServiceException(ApiError.ERROR_FILE_TEMPLATE_NOT_EXIST);
        }
        try {
            FastDFSClientUtil.downloadByte(fileTemplateEntity.getUrl(),fileTemplateEntity.getName(),"application/x-msdownload",Boolean.FALSE);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_FILE_TEMPLATE_DOWNLOAD);
        }
    }


    @Override
    public FileTemplateEntity getByFileTemplate (FileTemplateDTO.GetOneDTO getOneDTO) {
       return lambdaQuery().eq(FileTemplateEntity::getName,getOneDTO.getName())
                .eq(FileTemplateEntity::getFileType,getOneDTO.getFileType())
                .eq(FileTemplateEntity::getSourceType,getOneDTO.getSourceType())
                .last("limit 1")
                .one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FileTemplateEntity fileTemplateEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
