package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.TaskUploadFileDTO;
import com.erp.model.plm.entity.TaskDocsFinishEntity;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TaskDocsFinishMapper;
import com.erp.server.plm.service.TaskDocsFinishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 任务文档交付表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
@Slf4j
public class TaskDocsFinishServiceImpl extends ServiceImpl<TaskDocsFinishMapper, TaskDocsFinishEntity> implements TaskDocsFinishService {


    /**
     * 根据任务id 集合获取对应数据
     *
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TaskDocsFinishEntity>
     * @author yl
     * @date 2022-09-22 10:45
     */
    @Override
    public List<TaskDocsFinishEntity> getByTaskIds(List<String> taskIds) {
        LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (CollectionUtils.isNotEmpty(taskIds)) {
            queryWrapper.in(TaskDocsFinishEntity::getTaskId, taskIds);
            return list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 交付文档 上传文件
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-23 17:15
     */
    @Override
    public Boolean uploadFile(TaskUploadFileDTO dto) {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        try {
            MultipartFile multipartFile = dto.getFile();
            double size=multipartFile.getSize();
            double fileSize=size/(1024*1024);
            fileSize=(double)Math.round(fileSize*100)/100;
            String fileName = dto.getFile().getOriginalFilename().toLowerCase();
            String fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
            File file = FileUtil.multiToFile(multipartFile);
            String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
            TaskDocsFinishEntity finishEntity = new TaskDocsFinishEntity();
            finishEntity.setCreateUserName(loginUser.getUserName());
            finishEntity.setFileName(fileName);
            finishEntity.setTaskDocsId(dto.getTaskDocsId());
            finishEntity.setTaskId(dto.getTaskId());
            finishEntity.setFileUrl(fileUrl);
            finishEntity.setFileType(TaskConstant.FILE_TYPE);
            finishEntity.setFileSuffix(fileSuffix);
            finishEntity.setFileSize(fileSize);
            return this.save(finishEntity);
        } catch (Exception e) {
            log.error("uploadFile  " + e);
        }

        return false;
    }


}
