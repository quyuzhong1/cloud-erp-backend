package com.erp.server.sys.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.wms.dto.excel.QcReportDetailImportExcelDTO;
import com.erp.server.sys.mapper.FileTemplateMapper;
import com.erp.server.sys.service.FileTemplateService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.FileTemplateDTO;

import java.io.File;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
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
    public void fastdfsAddOrUpdate (FileTemplateDTO.FastdfsAddOrUpdateDTO fastdfsAddDTO) {
        FileTemplateEntity fileTemplateEntity =  BeanMapperUtils.map(FileTemplateEntity.class, fastdfsAddDTO);
        //原数据
        FileTemplateEntity entity = this.getByFileTemplate(new FileTemplateDTO.GetOneDTO(fastdfsAddDTO.getSourceType(),fastdfsAddDTO.getName(),fastdfsAddDTO.getFileType()));
        if (ObjectUtil.isNotEmpty(entity)) {
            fileTemplateEntity.setId(entity.getId());
        }
        //上传新文件模板
        String url = FastDFSClientUtil.uploadFile(fastdfsAddDTO.getFile());
        fileTemplateEntity.setUrl(url);
        this.saveOrUpdate(fileTemplateEntity);
        //删除原文件
        if (ObjectUtil.isNotEmpty(entity)) {
            FastDFSClientUtil.deleteFile(entity.getUrl());
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
