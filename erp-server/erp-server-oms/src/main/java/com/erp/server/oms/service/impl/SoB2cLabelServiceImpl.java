package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.oms.entity.SoB2cLabelEntity;
import com.erp.model.oms.enums.SoB2cLabelSourceTypeEnum;
import com.erp.server.oms.mapper.SoB2cLabelMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cLabelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单标签，面单表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-04-18
 */
@Slf4j
@Service
public class SoB2cLabelServiceImpl extends SuperServiceImpl<SoB2cLabelMapper, SoB2cLabelEntity> implements SoB2cLabelService {
    @Resource
    private OperateLogService operateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveSoB2cLabel(List<SoB2cLabelDTO.UpdateDTO> dtoList) {
        List<SoB2cLabelEntity> soB2cLabelEntities = BeanMapperUtils.copyList(SoB2cLabelEntity.class, dtoList);
        // 数据处理
        handleData(soB2cLabelEntities);
        //新增标签
        return this.saveOrUpdateBatch(soB2cLabelEntities);
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(SoB2cLabelEntity::getMainId, mainIds).remove();
    }

    @Override
    public List<SoB2cLabelEntity> listSoB2cLabelByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoB2cLabelEntity::getMainId, mainIds).orderByDesc(SoB2cLabelEntity::getCreateTime).list();
    }

    @Override
    public void ManualUploadLabel(String base64, String id) {

        SoB2cLabelEntity existApiEntity = lambdaQuery().eq(SoB2cLabelEntity::getMainId, id).eq(SoB2cLabelEntity::getSourceType, SoB2cLabelSourceTypeEnum.API.getCode()).last("LIMIT 1").one();
        if(Objects.nonNull(existApiEntity)){
            throw new ServiceException("已存在物流商面单，无法上传");
        }
        //先删除，再新增
        List<SoB2cLabelEntity> manualEntityList = lambdaQuery().eq(SoB2cLabelEntity::getMainId, id).eq(SoB2cLabelEntity::getSourceType, SoB2cLabelSourceTypeEnum.MANUAL.getCode()).list();
        if(CollectionUtils.isNotEmpty(manualEntityList)){
            List<String> ids = manualEntityList.stream().map(SoB2cLabelEntity::getId).collect(Collectors.toList());
            this.removeByIds(ids);
        }
        SoB2cLabelEntity addEntity = new SoB2cLabelEntity();
        addEntity.setMainId(id);
        addEntity.setLogisticsLabelBase64(base64);
        addEntity.setSourceType(SoB2cLabelSourceTypeEnum.MANUAL.getCode());
        this.save(addEntity);
    }

    @Override
    public void updateCrossLabelUrl(String mainId, String crossLabelUrl) {
        if (CharSequenceUtil.isBlank(mainId)){
            return;
        }
        lambdaUpdate().set(SoB2cLabelEntity::getCrossLabelUrl, crossLabelUrl).eq(SoB2cLabelEntity::getMainId, mainId).update();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<SoB2cLabelEntity> entityList) {
        //删除原有标签
        List<String> soIds = entityList.stream().map(req -> req.getMainId()).collect(Collectors.toList());
        this.deleteByMainIds(soIds);
    }


}
