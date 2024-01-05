package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.sys.entity.CfgQueryConditionEntity;
import com.erp.server.sys.mapper.CfgQueryConditionMapper;
import com.erp.server.sys.service.CfgQueryConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 查询条件配置表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-03
 */
@Slf4j
@Service
public class CfgQueryConditionServiceImpl extends SuperServiceImpl<CfgQueryConditionMapper, CfgQueryConditionEntity> implements CfgQueryConditionService {

    @Autowired
    private CommonService commonService;

    @Override
    public Boolean add(CfgQueryConditionDTO.AddDTO dto) {
        CfgQueryConditionEntity entity = new CfgQueryConditionEntity();
        BeanUtil.copyProperties(dto,entity);
        return this.save(entity);
    }

    @Override
    public List<CfgQueryConditionDTO.ViewDTO> getQueryCondition(String code) {
        List<CfgQueryConditionDTO.ViewDTO> viewDTO = baseMapper.getQueryConditionByCode(code);
        return viewDTO;
    }
}
