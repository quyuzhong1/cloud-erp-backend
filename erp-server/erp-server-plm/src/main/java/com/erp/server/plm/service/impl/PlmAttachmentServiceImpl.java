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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.dto.AttachmentDTO;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.model.plm.entity.PlmAttachmentEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.plm.mapper.PlmAttachmentMapper;
import com.erp.server.plm.service.OperateLogService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    @Resource
    private OperateLogService operateLogService;

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
    public List<AttachmentDTO.CommonDTO> getSkuUrlByPid(String id, String logId) {
        if(StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        
        // 查询 operate_log，根据 pid（product_id）查询，创建人为 "system"
        LambdaQueryWrapper<OperateLogEntity> logQueryWrapper = new LambdaQueryWrapper<>();
        logQueryWrapper.eq(OperateLogEntity::getPid, id)
                .eq(OperateLogEntity::getCreateUserName, "system")
                .eq(StringUtils.isNotBlank(logId), OperateLogEntity::getId, logId)
                .orderByDesc(OperateLogEntity::getCreateTime);
        List<OperateLogEntity> operateLogs = operateLogService.list(logQueryWrapper);
        
        if(CollectionUtils.isEmpty(operateLogs)){
            return Collections.emptyList();
        }
        
        // 提取所有 SKU 号并构建日志到 SKU 的映射
        Pattern skuPattern = Pattern.compile("【([^】]+)】");
        Map<OperateLogEntity, Set<String>> logSkuMap = new LinkedHashMap<>();
        Set<String> allSkuNos = new HashSet<>();
        
        for(OperateLogEntity log : operateLogs) {
            if(StringUtils.isBlank(log.getContent()) || log.getCreateTime() == null) {
                continue;
            }
            Set<String> skuNos = new HashSet<>();
            Matcher matcher = skuPattern.matcher(log.getContent());
            while(matcher.find()) {
                String skuNo = matcher.group(1);
                skuNos.add(skuNo);
                allSkuNos.add(skuNo);
            }
            if(CollectionUtils.isNotEmpty(skuNos)) {
                logSkuMap.put(log, skuNos);
            }
        }
        
        if(CollectionUtils.isEmpty(allSkuNos)){
            return Collections.emptyList();
        }
        
        // 查询 product_detail 并构建 SKU 到 detailId 的映射
        List<ProductDetailEntity> productDetails = productDetailService.list(
                new LambdaQueryWrapper<ProductDetailEntity>()
                        .eq(ProductDetailEntity::getProductId, id)
                        .in(ProductDetailEntity::getSkuNo, allSkuNos));
        
        if(CollectionUtils.isEmpty(productDetails)){
            return Collections.emptyList();
        }
        
        Map<String, String> skuToDetailIdMap = productDetails.stream()
                .collect(Collectors.toMap(ProductDetailEntity::getSkuNo, ProductDetailEntity::getId, (v1, v2) -> v1));
        List<String> detailIds = new ArrayList<>(skuToDetailIdMap.values());
        
        // 查询所有相关的 attachment
        List<PlmAttachmentEntity> attachments = this.lambdaQuery()
                .in(PlmAttachmentEntity::getBusinessId, detailIds)
                .eq(PlmAttachmentEntity::getType, SourceTypeEnum.PRODUCT_DETAIL.getTableName())
                .orderByDesc(PlmAttachmentEntity::getCreateTime)
                .list();
        
        if(CollectionUtils.isEmpty(attachments)){
            return Collections.emptyList();
        }
        
        // 按 detailId 和 createTime 分组 attachment，便于快速查找
        Map<String, List<PlmAttachmentEntity>> attachmentMap = attachments.stream()
                .collect(Collectors.groupingBy(PlmAttachmentEntity::getBusinessId));
        
        // 筛选符合条件的 attachment
        List<AttachmentDTO.CommonDTO> result = new ArrayList<>();
        for(Map.Entry<OperateLogEntity, Set<String>> entry : logSkuMap.entrySet()) {
            OperateLogEntity log = entry.getKey();
            LocalDateTime logTime = log.getCreateTime();
            LocalDateTime startTime = logTime.minusSeconds(1);
            LocalDateTime endTime = logTime.plusSeconds(1);
            
            for(String skuNo : entry.getValue()) {
                String detailId = skuToDetailIdMap.get(skuNo);
                if(StringUtils.isBlank(detailId)) {
                    continue;
                }
                
                List<PlmAttachmentEntity> detailAttachments = attachmentMap.get(detailId);
                if(CollectionUtils.isEmpty(detailAttachments)) {
                    continue;
                }
                
                for(PlmAttachmentEntity attachment : detailAttachments) {
                    LocalDateTime attachTime = attachment.getCreateTime();
                    if(attachTime != null && !attachTime.isBefore(startTime) && !attachTime.isAfter(endTime)) {
                        AttachmentDTO.CommonDTO dto = BeanUtil.copyProperties(attachment, AttachmentDTO.CommonDTO.class);
                        dto.setLogId(log.getId());
                        result.add(dto);
                    }
                }
            }
        }
        
        return result;
    }
}
