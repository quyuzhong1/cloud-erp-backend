package com.erp.server.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.DictBasicEntity;
import com.erp.server.workflow.mapper.DictBasicMapper;
import com.erp.server.workflow.service.DictBasicService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {

    @Override
    public List<DictBasicDTO.DropDownDTO> listByType(String type, String remark) {
        List<DictBasicEntity> list = lambdaQuery().eq(DictBasicEntity::getType, type)
                .eq(StrUtil.isNotBlank(remark), DictBasicEntity::getRemark, remark)
                .list();
       List<DictBasicDTO.DropDownDTO> result = list.stream().map(DictBasicDTO.DropDownDTO::new).collect(Collectors.toList());
        return result;
    }
}
