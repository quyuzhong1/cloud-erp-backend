package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.CfgMouldSettingDTO;
import com.erp.model.plm.entity.CfgMouldSettingEntity;
import com.erp.model.plm.entity.MouldDetailEntity;
import com.erp.model.plm.entity.MouldDocInfoEntity;
import com.erp.model.plm.enums.MouldSettingTypeEnum;
import com.erp.server.plm.mapper.CfgMouldSettingMapper;
import com.erp.server.plm.service.CfgMouldSettingService;
import com.erp.server.plm.service.MouldDetailService;
import com.erp.server.plm.service.MouldDocInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 模具配置 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class CfgMouldSettingServiceImpl extends SuperServiceImpl<CfgMouldSettingMapper, CfgMouldSettingEntity> implements CfgMouldSettingService {

    @Resource
    private MouldDocInfoService mouldDocInfoService;

    @Resource
    private MouldDetailService mouldDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CfgMouldSettingDTO.AddDTO dto) {
        verifyData(dto);
        //移除原数据
        remove(Wrappers.emptyWrapper());
        int index = 1;
        List<CfgMouldSettingEntity> list = new ArrayList<>();
        for (CfgMouldSettingDTO.ParamDTO paramDTO : dto.getDocList()) {
            CfgMouldSettingEntity entity = new CfgMouldSettingEntity();
            entity.setIndex(index);
            entity.setType(MouldSettingTypeEnum.DOC.getCode());
            entity.setName(paramDTO.getName());
            entity.setId(paramDTO.getId());
            list.add(entity);
            index++;
        }
        int docIndex = 1;
        for (CfgMouldSettingDTO.ParamDTO paramDTO : dto.getDocList()) {
            CfgMouldSettingEntity entity = new CfgMouldSettingEntity();
            entity.setIndex(docIndex);
            entity.setType(MouldSettingTypeEnum.MOULD.getCode());
            entity.setName(paramDTO.getName());
            entity.setId(paramDTO.getId());
            list.add(entity);
            docIndex++;
        }
        ApplicationContextUtils.getBean(CfgMouldSettingServiceImpl.class).saveOrUpdateBatch(list);
    }

    /**
     * 参数校验
     * @param dto 参数
     */
    private void verifyData(CfgMouldSettingDTO.AddDTO dto) {
        List<CfgMouldSettingEntity> list = list();
        Map<String, List<CfgMouldSettingEntity>> settingMap = list.stream().collect(Collectors.groupingBy(CfgMouldSettingEntity::getType));
        verifyDocData(dto, settingMap);

        verifyMouldData(dto, settingMap);

    }

    /**
     * 校验模具类型
     * @param dto 参数
     * @param settingMap 参数
     */
    private void verifyMouldData(CfgMouldSettingDTO.AddDTO dto, Map<String, List<CfgMouldSettingEntity>> settingMap) {
        List<CfgMouldSettingEntity> mouldList = settingMap.get(MouldSettingTypeEnum.MOULD.getCode());
        if (CollectionUtils.isEmpty(mouldList)) {
            return;
        }
        List<String> mouldIds = dto.getMouldList()
                .stream()
                .map(CfgMouldSettingDTO.ParamDTO::getId)
                .collect(Collectors.toList());
        List<String> mouldIdList = mouldList.stream()
                .map(CfgMouldSettingEntity::getId)
                .filter(v -> !mouldIds.contains(v))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(mouldIdList)){
            return;
        }
        List<MouldDetailEntity> mouldInfoList = mouldDetailService.list(Wrappers.<MouldDetailEntity>lambdaQuery().in(MouldDetailEntity::getTypeId, mouldIdList));
        if (!CollectionUtils.isEmpty(mouldInfoList)) {
            List<String> mouldInfoIdList = mouldInfoList.stream()
                    .map(MouldDetailEntity::getTypeId)
                    .distinct()
                    .collect(Collectors.toList());
            String errorMsg = mouldList.stream()
                    .filter(v -> mouldInfoIdList.contains(v.getId()))
                    .map(CfgMouldSettingEntity::getName)
                    .collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_95280, errorMsg);
        }
    }

    /**
     * 校验文档类型
     * @param dto 参数
     * @param settingMap 参数
     */
    private void verifyDocData(CfgMouldSettingDTO.AddDTO dto, Map<String, List<CfgMouldSettingEntity>> settingMap) {
        List<String> docIds = dto.getDocList()
                .stream()
                .map(CfgMouldSettingDTO.ParamDTO::getId)
                .collect(Collectors.toList());
        //获取文档被删除的类型
        List<CfgMouldSettingEntity> docList = settingMap.get(MouldSettingTypeEnum.DOC.getCode());
        if (CollectionUtils.isEmpty(docList)) {
            return;
        }
        List<String> docIdList = docList.stream()
                .map(CfgMouldSettingEntity::getId)
                .filter(v -> !docIds.contains(v))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(docIdList)){
            return;
        }
        List<MouldDocInfoEntity> docInfoList = mouldDocInfoService.list(Wrappers.<MouldDocInfoEntity>lambdaQuery().in(MouldDocInfoEntity::getTypeId, docIdList));
        if (!CollectionUtils.isEmpty(docInfoList)) {
            List<String> docInfoIdList = docInfoList.stream()
                    .map(MouldDocInfoEntity::getTypeId)
                    .distinct()
                    .collect(Collectors.toList());
            String errorMsg = docList.stream()
                    .filter(v -> docInfoIdList.contains(v.getId()))
                    .map(CfgMouldSettingEntity::getName)
                    .collect(Collectors.joining(","));
            throw new ServiceException(ApiError.ERROR_95280, errorMsg);
        }
    }

    @Override
    public List<CfgMouldSettingEntity> mouldList() {
        return list(Wrappers.<CfgMouldSettingEntity>lambdaQuery()
                .eq(CfgMouldSettingEntity::getType, MouldSettingTypeEnum.MOULD.getCode())
                .orderByAsc(CfgMouldSettingEntity::getIndex)
        );
    }

    @Override
    public List<CfgMouldSettingEntity> docList() {
        return list(Wrappers.<CfgMouldSettingEntity>lambdaQuery()
                .eq(CfgMouldSettingEntity::getType, MouldSettingTypeEnum.DOC.getCode())
                .orderByAsc(CfgMouldSettingEntity::getIndex)
        );
    }
}
