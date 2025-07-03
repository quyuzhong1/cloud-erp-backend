package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.model.plm.dto.AttachmentDTO;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.plm.mapper.PlmAttachmentMapper;
import com.erp.server.plm.service.PlmAttachmentService;
import com.erp.server.plm.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Slf4j
@Service
public class PlmAttachmentServiceImpl extends SuperServiceImpl<PlmAttachmentMapper, PlmAttachmentEntity> implements PlmAttachmentService {

    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private FileFeign fileFeign;

    /**
     * 批量保存附件信息
     *
     * @param attachUrlList
     * @param attachNameList
     * @param type
     * @param businessId
     * @return void
     * @author yl
     * @date 2023-06-20 15:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<String> attachUrlList, List<String> attachNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachNameList) ? attachNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachUrlList)) {
            List<PlmAttachmentEntity> addList = new ArrayList<>(attachUrlList.size());
            for (int i = 0; i < attachUrlList.size(); i++) {
                PlmAttachmentEntity entity = new PlmAttachmentEntity();
                entity.setAttachUrl(attachUrlList.get(i));
                if (CollectionUtils.isNotEmpty(attachNameList) && nameSize > i) {
                   entity.setAttachName(attachNameList.get(i));
                }
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }
    }

    /**
     * 根据业务表ids 获取附件信息
     *
     * @param businessIdList
     * @return void
     * @author yl
     * @date 2023-06-21 12:01
     */
    @Override
    public List<PlmAttachmentEntity> listByBusinessIds(List<String> businessIdList) {
        if (CollectionUtils.isEmpty(businessIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PlmAttachmentEntity::getBusinessId, businessIdList).orderByDesc(PlmAttachmentEntity::getId).list();

    }

    @Override
    public PlmAttachmentEntity upload(MultipartFile multipartFile,String type) {
        double size = multipartFile.getSize();
        double fileSize = size / (1024 * 1024);
        fileSize = (double) Math.round(fileSize * 100) / 100;
        if (fileSize > 300) {
            throw new ServiceException(ApiError.ERROR_95160, 300);
        }
        String fileName = multipartFile.getOriginalFilename();
        if (org.springframework.util.StringUtils.isEmpty(fileName)) {
            throw new ServiceException(ApiError.ERROR_1018);
        }
        if (fileName.length() > 200) {
            throw new ServiceException(ApiError.ERROR_1018);
        }
        String fileUrl = fileFeign.uploadFile(multipartFile);
        if (StringUtils.isBlank(fileUrl)) {
            throw new ServiceException(ApiError.ERROR_95018);
        }
        PlmAttachmentEntity attachmentEntity = new PlmAttachmentEntity();
        attachmentEntity.setAttachUrl(fileUrl);
        attachmentEntity.setAttachName(fileName.toLowerCase());
        attachmentEntity.setAttachSize(BigDecimal.valueOf(fileSize));
        attachmentEntity.setType(type);
        this.save(attachmentEntity);
        return attachmentEntity;
    }

    @Override
    public void removeAttachment(BaseIdDTO dto) {
        PlmAttachmentEntity plmAttachmentEntity = this.getById(dto.getId());
        if (ObjectUtil.isEmpty(plmAttachmentEntity)) {
            return;
        }
        //删除附件id
        this.removeById(plmAttachmentEntity.getId());
        //删除fastdfs
        fileFeign.deleteFile(plmAttachmentEntity.getAttachUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlmAttachmentEntity> batchUpload(List<MultipartFile> multipartFileList, String type) {
        if (CollectionUtils.isEmpty(multipartFileList)) {
            throw new ServiceException(ApiError.ERROR_95018);
        }
        List<PlmAttachmentEntity> resultList = new ArrayList<>();
        for(MultipartFile multipartFile :multipartFileList) {
            PlmAttachmentEntity entity = upload(multipartFile, type);
            resultList.add(entity);
        }
        return resultList;
    }

    @Override
    public void removeAttachmentByUrl(AttachmentDTO.DeleteDTO dto) {
        this.lambdaUpdate()
                .eq(PlmAttachmentEntity::getAttachUrl, dto.getAttachUrl())
                .eq(StringUtils.isNotBlank(dto.getBusinessId()), PlmAttachmentEntity::getBusinessId, dto.getBusinessId())
                .remove();
    }

    @Override
    public List<AttachmentDTO.CommonDTO> getUrlById(String id) {
        List<PlmAttachmentEntity> entities = this.lambdaQuery().eq(PlmAttachmentEntity::getBusinessId, id).list();
        if(CollectionUtils.isEmpty(entities)){
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(entities,AttachmentDTO.CommonDTO.class);
    }

    @Override
    public List<AttachmentDTO.CommonDTO> getSkuUrlByPid(String id) {
        if(StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        List<ProductDetailEntity> productDetailEntities = productDetailService.getSkuListByProductId(id);
        if(CollectionUtils.isEmpty(productDetailEntities)){
            return new ArrayList<>();
        }
        List<String> ids = productDetailEntities.stream().map(v->v.getId()).collect(Collectors.toList());
        List<PlmAttachmentEntity> entities = this.lambdaQuery().in(PlmAttachmentEntity::getBusinessId, ids).eq(PlmAttachmentEntity::getType, SourceTypeEnum.PRODUCT_DETAIL.getTableName()).list();
        if(CollectionUtils.isEmpty(entities)){
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(entities,AttachmentDTO.CommonDTO.class);
    }
}
