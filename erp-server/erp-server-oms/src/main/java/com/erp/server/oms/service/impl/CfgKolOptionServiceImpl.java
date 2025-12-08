package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgKolOptionDTO;
import com.erp.model.oms.entity.CfgKolOptionEntity;
import com.erp.model.oms.entity.KolPartnerInfoEntity;
import com.erp.server.oms.mapper.CfgKolOptionMapper;
import com.erp.server.oms.service.CfgKolOptionService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * kol类型表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class CfgKolOptionServiceImpl extends SuperServiceImpl<CfgKolOptionMapper, CfgKolOptionEntity> implements CfgKolOptionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgKolOptionDTO.AddDTO addDTO) {

        CfgKolOptionEntity cfgKolOptionEntity = new CfgKolOptionEntity();
        BeanMapperUtils.copy(addDTO, cfgKolOptionEntity);

        CfgKolOptionEntity one = lambdaQuery()
                .eq(CfgKolOptionEntity::getName, addDTO.getName())
                .eq(CfgKolOptionEntity::getType, addDTO.getType())
                .one();
        if(Objects.nonNull(one)){
            throw new ServiceException(StrUtil.format("【{}】已存在",addDTO.getName()));
        }

        // 数据处理
        handleData(cfgKolOptionEntity);

        log.info("开始新增kol类型单");
        boolean save = super.save(cfgKolOptionEntity);
        if(!save) {
            throw new ServiceException("kol类型单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "kol类型单" , cfgKolOptionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgKolOptionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgKolOptionEntity.getId(), cfgKolOptionEntity.getId());
    }

    @Override
    public List<CfgKolOptionDTO.ViewDTO> select(String type) {
        List<CfgKolOptionEntity> list = lambdaQuery().eq(CfgKolOptionEntity::getType, type).eq(CfgKolOptionEntity::getDisabled, Boolean.FALSE).list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(list, CfgKolOptionDTO.ViewDTO.class);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgKolOptionEntity cfgKolOptionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
