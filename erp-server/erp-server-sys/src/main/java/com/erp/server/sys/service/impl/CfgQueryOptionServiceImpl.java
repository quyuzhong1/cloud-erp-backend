package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.CfgQueryOptionEntity;
import com.erp.server.sys.mapper.CfgQueryOptionMapper;
import com.erp.server.sys.service.CfgQueryOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgQueryOptionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 查询option配置表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-04
 */
@Slf4j
@Service
public class CfgQueryOptionServiceImpl extends SuperServiceImpl<CfgQueryOptionMapper, CfgQueryOptionEntity> implements CfgQueryOptionService {

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgQueryOptionDTO.AddDTO addDTO) {
        if(StringUtils.isBlank(addDTO.getSelectLabel())){
            addDTO.setSelectLabel("value");
        }
        if(StringUtils.isBlank(addDTO.getSelectValue())){
            addDTO.setSelectValue("code");
        }
        if(StringUtils.isBlank(addDTO.getSelectDisabled())){
            addDTO.setSelectDisabled("disabled");
        }
        CfgQueryOptionEntity cfgQueryOptionEntity = new CfgQueryOptionEntity();
        BeanMapperUtils.copy(addDTO, cfgQueryOptionEntity);

        // 数据处理
        handleData(cfgQueryOptionEntity);

        log.info("开始新增查询option配置单");
        boolean save = super.save(cfgQueryOptionEntity);
        if(!save) {
            throw new ServiceException("查询option配置单保存失败");
        }
        return new BaseResultDTO.AddDTO(cfgQueryOptionEntity.getId(), cfgQueryOptionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgQueryOptionDTO.UpdateDTO updateDTO) {
        CfgQueryOptionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "查询option配置单"));
        CfgQueryOptionEntity cfgQueryOptionEntity =  BeanMapperUtils.map(CfgQueryOptionEntity.class, updateDTO);

        // 数据处理
        handleData(cfgQueryOptionEntity);
        log.info("编辑 开始修改查询option配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgQueryOptionEntity);
        if(!save) {
            throw new ServiceException("查询option配置单保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录查询option配置单日志数据，id：【{}】", cfgQueryOptionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), cfgQueryOptionEntity.getId(), "查询option配置单");
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgQueryOptionEntity cfgQueryOptionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
