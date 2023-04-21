package com.erp.server.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.WorkMenuEntity;
import com.erp.server.workflow.mapper.WorkMenuMapper;
import com.erp.server.workflow.service.WorkMenuService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 工作台菜单基础表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class WorkMenuServiceImpl extends SuperServiceImpl<WorkMenuMapper, WorkMenuEntity> implements WorkMenuService {

    @Override
    public List<DictBasicDTO.DropDownDTO> listByCode(String code) {
        List<WorkMenuEntity> list = lambdaQuery()
                .eq(StrUtil.isNotBlank(code), WorkMenuEntity::getModuleCode, code)
                .list();
        List<DictBasicDTO.DropDownDTO> result = list.stream().map(DictBasicDTO.DropDownDTO::new).collect(java.util.stream.Collectors.toList());
        return result;
    }
}
