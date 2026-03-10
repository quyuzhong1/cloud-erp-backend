package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.DictLanguageEntity;
import com.erp.server.oms.mapper.DictLanguageMapper;
import com.erp.server.oms.service.DictLanguageService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.DictLanguageDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * ISO 639-1 语言标准 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-01
 */
@Slf4j
@Service
public class DictLanguageServiceImpl extends SuperServiceImpl<DictLanguageMapper, DictLanguageEntity> implements DictLanguageService {


    @Override
    public List<DictLanguageDTO.ListDTO> dropDown(DictLanguageDTO.SelectDTO dto) {
        LambdaQueryWrapper<DictLanguageEntity> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(dto.getSearchKeyword())){
            queryWrapper.like(DictLanguageEntity::getNameZh, dto.getSearchKeyword());
        }
        queryWrapper.eq(DictLanguageEntity::getIsDeleted, false);
        queryWrapper.orderByDesc(DictLanguageEntity::getCreateTime);
        List<DictLanguageEntity> list = this.list(queryWrapper);
        return BeanMapperUtils.copyList(DictLanguageDTO.ListDTO.class, list);
    }
}
