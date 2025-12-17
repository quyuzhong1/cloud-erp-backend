package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.enums.DictBasicEnum;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.DictCredentialEntity;
import com.erp.server.scm.mapper.DictCredentialMapper;
import com.erp.server.scm.service.DictCredentialService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.DictCredentialDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 供应商资质字典表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-15
 */
@Slf4j
@Service
public class DictCredentialServiceImpl extends SuperServiceImpl<DictCredentialMapper, DictCredentialEntity> implements DictCredentialService {
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictCredentialDTO.AddDTO addDTO) {
        DictCredentialEntity dictCredentialEntity = new DictCredentialEntity();
        dictCredentialEntity.setName(StrUtil.trim(addDTO.getName()));

        //校验名称是否已存在
        Integer count = this.lambdaQuery()
                .eq(DictCredentialEntity::getName, dictCredentialEntity.getName())
                .eq(DictCredentialEntity::getDisabled, false)
                .count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98124);
        }

        log.info("开始新增供应商资质字典单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DC);
        dictCredentialEntity.setCode(code);
        boolean save = super.save(dictCredentialEntity);
        if (!save) {
            throw new ServiceException("供应商资质字典单保存失败");
        }
        return new BaseResultDTO.AddDTO(dictCredentialEntity.getId(), code);
    }

    @Override
    public List<DictCredentialDTO.ListDTO> listAll() {
        List<DictCredentialEntity> list = lambdaQuery()
                .eq(DictCredentialEntity::getDisabled, false)
                .orderByAsc(DictCredentialEntity::getSort)
                .orderByDesc(DictCredentialEntity::getCreateTime)
                .list();
        List<DictCredentialDTO.ListDTO> listDTOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            listDTOS = BeanMapper.copyList(list, DictCredentialDTO.ListDTO.class);
        }
        return listDTOS;
    }

}
