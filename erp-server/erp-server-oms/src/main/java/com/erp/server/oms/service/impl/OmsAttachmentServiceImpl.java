package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.AttachDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.OmsAttachmentDTO;
import com.erp.model.oms.entity.OmsAttachmentEntity;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.oms.mapper.OmsAttachmentMapper;
import com.erp.server.oms.service.OmsAttachmentService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-19
 */
@Service
public class OmsAttachmentServiceImpl extends SuperServiceImpl<OmsAttachmentMapper, OmsAttachmentEntity> implements OmsAttachmentService {
    @Resource
    private FileFeign filefeign;
    @Override
    public void batchSaveOrUpdate(List<String> attachmentUrlList, List<String> attachmentNameList, String type, String businessId) {
        int nameSize = CollectionUtils.isNotEmpty(attachmentNameList) ? attachmentNameList.size() : 0;
        if (CollectionUtils.isNotEmpty(attachmentUrlList)) {
            List<OmsAttachmentEntity> addList = new ArrayList<>(attachmentUrlList.size());
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                OmsAttachmentEntity entity = new OmsAttachmentEntity();
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
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveOrUpdate(List<AttachDTO> attachDTOS, String type) {
        List<OmsAttachmentEntity> omsAttachmentList = new ArrayList<>();
        for (AttachDTO attachDTO : attachDTOS) {
            OmsAttachmentEntity entity = new OmsAttachmentEntity();
            entity.setAttachUrl(attachDTO.getAttachUrl());
            entity.setAttachName(attachDTO.getAttachName());
            entity.setType(type);
            entity.setBusinessId(attachDTO.getBusinessId());
            omsAttachmentList.add(entity);
        }
        List<String> businessIdList = omsAttachmentList.stream().map(OmsAttachmentEntity::getBusinessId).distinct().collect(Collectors.toList());
        List<OmsAttachmentEntity> oldList = lambdaQuery().in(OmsAttachmentEntity::getBusinessId,businessIdList)
                .eq(OmsAttachmentEntity::getType,type)
                .list();
        List<OmsAttachmentEntity> deleteList = oldList.stream().filter(old -> omsAttachmentList.stream().noneMatch(obj -> obj.getBusinessId().equals(old.getBusinessId()) && obj.getAttachUrl().equals(old.getAttachUrl()))).collect(Collectors.toList());
        for (OmsAttachmentEntity omsAttachmentEntity : omsAttachmentList) {
            OmsAttachmentEntity attachmentEntity = oldList.stream().filter(old -> old.getBusinessId().equals(omsAttachmentEntity.getBusinessId()) && old.getAttachUrl().equals(omsAttachmentEntity.getAttachUrl())).findFirst().orElse(null);
            if (ObjUtil.isNotEmpty(attachmentEntity)) {
                omsAttachmentEntity.setId(attachmentEntity.getId());
            }
        }
        this.saveOrUpdateBatch(omsAttachmentList);
        if(CollUtil.isNotEmpty(deleteList)) {
            List<String> urlList = deleteList.stream().map(OmsAttachmentEntity::getAttachUrl).collect(Collectors.toList());
            //批量删除fastdfs 数据
            filefeign.deleteBatchFile(urlList);
            this.removeByIds(deleteList.stream().map(OmsAttachmentEntity::getId).collect(Collectors.toList()));
        }
    }

    @Override
    public void batchAddOrUpdate(List<OmsAttachmentDTO.UpdateDTO> addOrUpdateList) {
        if (CollUtil.isEmpty(addOrUpdateList)) {
            return;
        }
        List<OmsAttachmentEntity> omsAttachmentList = BeanUtil.copyToList(addOrUpdateList, OmsAttachmentEntity.class);
        //id赋值
        handleAddOrUpdate(omsAttachmentList);
        this.saveOrUpdateBatch(omsAttachmentList);
    }

    /**
     * 新增数据处理
     * @author will
     * @date 2025/4/23 11:29
     * @param omsAttachmentList
     * @return void
     */
    private void handleAddOrUpdate(List<OmsAttachmentEntity> omsAttachmentList) {
        if (CollUtil.isEmpty(omsAttachmentList)) {
            return;
        }
        List<String> businessIdList = omsAttachmentList.stream().map(OmsAttachmentEntity::getBusinessId).distinct().collect(Collectors.toList());
        List<String> typeList = omsAttachmentList.stream().map(OmsAttachmentEntity::getType).distinct().collect(Collectors.toList());
        List<String> attachUrlList = omsAttachmentList.stream().map(OmsAttachmentEntity::getAttachUrl).distinct().collect(Collectors.toList());
        List<OmsAttachmentEntity> oldList = this.listByBusinessIdAndType(businessIdList, typeList, attachUrlList);
        if (CollUtil.isEmpty(oldList)) {
            return;
        }
        Map<String, OmsAttachmentEntity> map = oldList.stream().collect(Collectors.toMap(obj -> obj.getBusinessId() + obj.getType() + obj.getAttachUrl(), obj -> obj));
        for (OmsAttachmentEntity entity :omsAttachmentList) {
            OmsAttachmentEntity attachmentEntity = map.get(entity.getBusinessId() + entity.getType() + entity.getAttachUrl());
            if (ObjUtil.isNotEmpty(attachmentEntity)) {
                entity.setId(attachmentEntity.getId());
            }
        }
    }

    /**
     * 批量查询
     * @param businessIdList
     * @param typeList
     * @param attachUrlList
     * @return List<OmsAttachmentEntity>
     */
    private List<OmsAttachmentEntity> listByBusinessIdAndType(List<String> businessIdList,List<String> typeList,List<String> attachUrlList) {
        return lambdaQuery().in(OmsAttachmentEntity::getBusinessId,businessIdList)
                .in(OmsAttachmentEntity::getType,typeList)
                .in(OmsAttachmentEntity::getAttachUrl,attachUrlList)
                .list();
    }

    /**
     * 先删除
     *
     * @param type
     * @param businessId
     */
    private void delete(String type, String businessId) {
        LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OmsAttachmentEntity::getBusinessId, businessId);
        queryWrapper.eq(OmsAttachmentEntity::getType, type);
        this.remove(queryWrapper);
    }

    @Override
    public List<OmsAttachmentDTO.UpdateDTO> getByBusinessIds(List<String> businessIds) {
        LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OmsAttachmentEntity::getBusinessId, businessIds);
        List<OmsAttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapper.copyList(list, OmsAttachmentDTO.UpdateDTO.class);

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
    public void removeAttachment(OmsAttachmentDTO.DeleteDTO dto) {
        LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OmsAttachmentEntity::getAttachUrl, dto.getAttachUrl());
        if (StringUtils.isNotBlank(dto.getBusinessId())) {
            queryWrapper.eq(OmsAttachmentEntity::getBusinessId, dto.getBusinessId());
        }
        this.remove(queryWrapper);
    }


    /**
     * 根据业务表id 集合删除
     *
     * @param businessIdList
     * @return void
     * @author yl
     * @date 2023-03-20 11:52
     */
    @Override
    public void deleteByBusinessIds(List<String> businessIdList) {
        if (CollectionUtils.isNotEmpty(businessIdList)) {
            LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(OmsAttachmentEntity::getBusinessId, businessIdList);
            List<OmsAttachmentEntity> list = this.list(queryWrapper);
            List<String> urlList = list.stream().map(OmsAttachmentEntity::getAttachUrl).collect(Collectors.toList());
            //批量删除fastdfs 数据
            filefeign.deleteBatchFile(urlList);
            this.removeByIds(list.stream().map(OmsAttachmentEntity::getId).collect(Collectors.toList()));
        }
    }



    /**
     * 根据业务表id 获取附件信息
     *
     * @param businessId
     * @return com.erp.model.scm.dto.AttachmentDTO.UpdateDTO
     * @author yl
     * @date 2023-03-27 9:37
     */
    @Override
    public List<AttachmentDTO.UpdateDTO> getByBusinessId(String businessId) {
        LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OmsAttachmentEntity::getBusinessId, businessId);
        List<OmsAttachmentEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeanMapper.copyList(list, AttachmentDTO.UpdateDTO.class);
    }

    @Override
    public List<OmsAttachmentEntity> listByBusinessIdsAndType(List<String> businessIds, String type) {
        if (CollectionUtils.isNotEmpty(businessIds) && StringUtils.isNotBlank(type)) {
            LambdaQueryWrapper<OmsAttachmentEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(OmsAttachmentEntity::getBusinessId, businessIds);
            queryWrapper.eq(OmsAttachmentEntity::getType, type);
            return this.list(queryWrapper);
        }
        return Collections.emptyList();
    }
}
