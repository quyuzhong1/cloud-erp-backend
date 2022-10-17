package com.erp.server.plm.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.CountDTO;
import com.erp.model.plm.dto.ProductOperateRecordDTO;
import com.erp.model.plm.dto.TaskChangeFileDTO;
import com.erp.model.plm.dto.TaskUploadFileDTO;
import com.erp.model.plm.entity.TaskDocsFinishEntity;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TaskDocsFinishMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.DocsChangeRecordService;
import com.erp.server.plm.service.ProductOperateRecordService;
import com.erp.server.plm.service.TaskDocsFinishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


    @Autowired
    private CommonService commonService;

    @Autowired
    private DocsChangeRecordService docsChangeRecordService;

    @Autowired
    private ProductOperateRecordService productOperateRecordService;

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
        LoginUser loginUser = commonService.getUserInfo();
        MultipartFile multipartFile = dto.getFile();
        double size = multipartFile.getSize();
        double fileSize = size / (1024 * 1024);
        fileSize = (double) Math.round(fileSize * 100) / 100;
        String fileName = dto.getFile().getOriginalFilename().toLowerCase();
        String fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
        File file = FileUtil.multiToFile(multipartFile);
        String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException(ApiError.ERROR_95018);
        }
        TaskDocsFinishEntity finishEntity = new TaskDocsFinishEntity();
        finishEntity.setCreateUserName(loginUser.getUserName());
        finishEntity.setFileName(fileName);
        finishEntity.setProductId(dto.getProductId());
        finishEntity.setTaskDocsId(dto.getTaskDocsId());
        finishEntity.setTaskId(dto.getTaskId());
        finishEntity.setFileUrl(fileUrl);
        finishEntity.setCreateUserId(loginUser.getUid());
        finishEntity.setFileType(TaskConstant.FILE_TYPE);
        finishEntity.setFileSuffix(fileSuffix);
        finishEntity.setFileSize(fileSize);

        //新增产品操作日志
        ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
        productOperateRecordDTO.setProductId(dto.getProductId());
        List<String> remarkList = new ArrayList<>();
        remarkList.add("上传文件：[" + fileName + "]");
        productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
        productOperateRecordService.saveOrUpdate(productOperateRecordDTO);

        return this.save(finishEntity);
    }

    /**
     * 项目任务-任务详情-删除文件
     * @Author Luo_WG
     * @Date 2022/10/14 16:08
     * @param id 主键
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean removeById(String id) {
        TaskDocsFinishEntity entity = this.getById(id);
        //新增产品操作日志
        ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
        productOperateRecordDTO.setProductId(entity.getProductId());
        List<String> remarkList = new ArrayList<>();
        remarkList.add("删除文件：[" + entity.getFileName() + "]");
        productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
        productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
        return this.removeById(id);
    }

    @Override
    public List<CountDTO> getTaskDocsCountByProductId() {
        return baseMapper.getTaskDocsCountByProductId();
    }

    /**
     * 变更文档
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-14 11:52
     */
    @Override
    @Transactional
    public Boolean changeFile(TaskChangeFileDTO dto) {
        String finishDocsId=dto.getFinishDocsId();
        TaskDocsFinishEntity finishEntity = this.getById(finishDocsId);
        if (Objects.isNull(finishEntity)) {
            throw new ServiceException(ApiError.ERROR_95028);
        }
        LoginUser loginUser = commonService.getUserInfo();
        String originalFileName = finishEntity.getFileName();

        MultipartFile multipartFile = dto.getFile();
        double size = multipartFile.getSize();
        double fileSize = size / (1024 * 1024);
        fileSize = (double) Math.round(fileSize * 100) / 100;
        String fileName = dto.getFile().getOriginalFilename().toLowerCase();
        String fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
        File file = FileUtil.multiToFile(multipartFile);
        String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException(ApiError.ERROR_95018);
        }

        finishEntity.setFileName(fileName);
        finishEntity.setFileUrl(fileUrl);
        finishEntity.setUpdateUserId(loginUser.getUid());
        finishEntity.setFileSuffix(fileSuffix);
        finishEntity.setUpdateUserName(loginUser.getUserName());
        finishEntity.setFileSize(fileSize);

        //这里需要启动一个变更流程

        StringBuffer sb = new StringBuffer(originalFileName);
        Boolean flag = this.updateById(finishEntity);
        //当更新成功后 保存记录
        if (flag) {
            sb.append("变更为").append(fileName);
            docsChangeRecordService.addRecord(sb.toString(),finishEntity.getTaskId(),finishDocsId,"");
        }

        //新增产品操作日志
        ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
        productOperateRecordDTO.setProductId(finishEntity.getProductId());
        List<String> remarkList = new ArrayList<>();
        remarkList.add("变更文档：[" + fileName + "]");
        productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
        productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
        return flag;
    }


}
