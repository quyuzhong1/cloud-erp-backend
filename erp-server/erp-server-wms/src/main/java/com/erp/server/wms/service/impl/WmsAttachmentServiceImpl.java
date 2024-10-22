package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.AttachDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FileUtil;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.WmsAttachmentMapper;
import com.erp.server.wms.service.PackingTaskService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.WmsAttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-19
 */
@Service
public class WmsAttachmentServiceImpl extends SuperServiceImpl<WmsAttachmentMapper, WmsAttachmentEntity> implements WmsAttachmentService {

    @Resource
    private PackingTaskService packingTaskService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        //先删除
        this.delete(type, businessId);
        int nameSize = CollectionUtils.isNotEmpty(attachmentNameList) ? attachmentNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachmentUrlList)) {
            List<WmsAttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity entity = new WmsAttachmentEntity();
                entity.setAttachUrl(attachmentUrlList.get(i));
                if (CollectionUtils.isNotEmpty(attachmentNameList)) {
                    if (nameSize > i) {
                        entity.setAttachName(attachmentNameList.get(i));
                    }
                }
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }

    }

    @Override
    public void batchSave(List<AttachDTO> attachmentList, String type, String businessId) {
        //先删除
        this.delete(type, businessId);
        if (CollectionUtils.isNotEmpty(attachmentList)) {
            List<WmsAttachmentEntity> addList = new ArrayList<>(attachmentList.size());
            for (int i = 0; i < attachmentList.size(); i++) {
                WmsAttachmentEntity entity = new WmsAttachmentEntity();
                entity.setAttachUrl(attachmentList.get(i).getAttachUrl());
                entity.setAttachName(attachmentList.get(i).getAttachName());
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }
    }

    @Override
    public void batchSaveNotDel(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachmentNameList) ? attachmentNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachmentUrlList)) {
            List<WmsAttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity entity = new WmsAttachmentEntity();
                entity.setAttachUrl(attachmentUrlList.get(i));
                if (CollectionUtils.isNotEmpty(attachmentNameList)) {
                    if (nameSize > i) {
                        entity.setAttachName(attachmentNameList.get(i));
                    }
                }
                entity.setType(type);
                entity.setBusinessId(businessId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }

    }

    /**
     * 先删除
     *
     * @param type
     * @param businessId
     */
    private void delete(String type, String businessId) {
        LambdaQueryWrapper<WmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmsAttachmentEntity::getBusinessId, businessId);
        queryWrapper.eq(WmsAttachmentEntity::getType, type);
        this.remove(queryWrapper);
    }

    @Override
    public List<WmsAttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<WmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(WmsAttachmentEntity::getBusinessId, businessIds);
        List<WmsAttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeanMapper.copyList(list, WmsAttachmentDTO.UpdateDTO.class);

    }


    /**
     * 删除附件信息
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-04-19 11:11
     */
    @Override
    public void removeAttachment(AttachmentDTO.DeleteDTO dto) {
        LambdaQueryWrapper<WmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmsAttachmentEntity::getAttachUrl, dto.getAttachUrl());
        if (StringUtils.isNotBlank(dto.getBusinessId())) {
            queryWrapper.eq(WmsAttachmentEntity::getBusinessId, dto.getBusinessId());

        }
        this.remove(queryWrapper);
    }

    @Override
    public void addByWarehouseEquipment(WmsAttachmentDTO.AddDTO dto) {
        String fileName = dto.getFileName();
        String url = dto.getUrl();
        if(StringUtils.isBlank(fileName) || StringUtils.isBlank(url)){
            return;
        }
        //去掉文件后缀名
        fileName = FileUtil.removeExtension(fileName);
        String[] fileNameArr = fileName.split("-");
        String code = fileNameArr[0];
        if(StringUtils.isBlank(code)){
            return;
        }
        WmsAttachmentEntity entity = new WmsAttachmentEntity();
        entity.setAttachUrl(url);
        entity.setAttachName(dto.getFileName());
        if(code.contains("FHD") || code.contains("YHSQ")){
            PackingTaskEntity packingTaskEntity = packingTaskService.getBySourceCode(code);
            if(Objects.isNull(packingTaskEntity)){
                return;
            }
            Class<PackingTaskEntity> aClass = PackingTaskEntity.class;
            TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
            entity.setType(tableName.value());
            entity.setBusinessId(packingTaskEntity.getId());
        }else{
            //发货单信息
            SoB2cDeliveryEntity soB2cDeliveryEntity = soB2cDeliveryService.getByBusinessCode(code);
            if(Objects.isNull(soB2cDeliveryEntity)){
                return;
            }
            Class<SoB2cDeliveryEntity> aClass = SoB2cDeliveryEntity.class;
            TableName tableName = aClass.getDeclaredAnnotation(TableName.class);
            entity.setType(tableName.value());
            entity.setBusinessId(soB2cDeliveryEntity.getId());
        }
        this.save(entity);
    }
}
