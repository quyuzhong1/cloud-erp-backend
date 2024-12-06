package com.erp.server.plm.service.impl;


import com.common.business.utils.ApplicationContextUtils;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.MouldDocInfoEntity;
import com.erp.server.plm.mapper.MouldDocInfoMapper;
import com.erp.server.plm.service.MouldDocInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 模具文档信息 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldDocInfoServiceImpl extends SuperServiceImpl<MouldDocInfoMapper, MouldDocInfoEntity> implements MouldDocInfoService {

    @Override
    public void add(List<MouldInfoDTO.DocDTO> docList, String id) {

        List<MouldDocInfoEntity> docInfoEntityList = docList.stream()
                .map(v -> {
                    MouldDocInfoEntity entity = BeanMapperUtils.map(MouldDocInfoEntity.class, v);
                    entity.setMouldInfoId(id);
                    return entity;
                }).collect(Collectors.toList());

        ApplicationContextUtils.getBean(MouldDocInfoServiceImpl.class).saveBatch(docInfoEntityList);
    }
}
