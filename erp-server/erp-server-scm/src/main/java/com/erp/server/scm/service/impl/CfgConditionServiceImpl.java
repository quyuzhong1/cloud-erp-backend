package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.CfgConditionEntity;
import com.erp.server.scm.mapper.CfgConditionMapper;
import com.erp.server.scm.service.CfgConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.CfgConditionDTO;
import java.util.*;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-16
 */
@Slf4j
@Service
public class CfgConditionServiceImpl extends SuperServiceImpl<CfgConditionMapper, CfgConditionEntity> implements CfgConditionService {

    @Override
    public List<CfgConditionDTO.CommonDTO> listByType(String type) {
        return baseMapper.listByType(type);
    }
}
