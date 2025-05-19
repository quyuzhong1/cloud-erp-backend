package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.sys.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.server.workflow.mapper.CfgQueryOptionMapper;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@Service
public class CfgQueryOptionServiceImpl extends SuperServiceImpl<CfgQueryOptionMapper, CfgQueryOptionEntity> implements CfgQueryOptionService {


    @Override
    public List<CfgQueryOptionDTO.ViewDTO> proDropDown(String bussinessKey) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = this.list(new LambdaQueryWrapper<CfgQueryOptionEntity>().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey).eq(CfgQueryOptionEntity::getIsDeleted, false));
        return BeanUtil.copyToList(cfgQueryOptionEntities, CfgQueryOptionDTO.ViewDTO.class);
    }


    @Override
    public List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO> cfgApproveSyncDropDown(String bussinessKey) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = lambdaQuery().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey).orderByDesc(CfgQueryOptionEntity::getFieldBelongsType).list();
        return BeanUtil.copyToList(cfgQueryOptionEntities, CfgQueryOptionDTO.cfgApproveSyncDropDownDTO.class);
    }
}
