package com.erp.server.sys.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.CfgCountryPartitionDTO;
import com.erp.model.sys.entity.CfgCountryPartitionEntity;
import com.erp.server.sys.mapper.CfgCountryPartitionMapper;
import com.erp.server.sys.service.CfgCountryPartitionService;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 分区国家关联表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
 */
@Slf4j
@Service
public class CfgCountryPartitionServiceImpl extends SuperServiceImpl<CfgCountryPartitionMapper, CfgCountryPartitionEntity> implements CfgCountryPartitionService {


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgCountryPartitionDTO.AddDTO addDTO) {
        CfgCountryPartitionEntity cfgCountryPartitionEntity = new CfgCountryPartitionEntity();
        BeanMapperUtils.copy(addDTO, cfgCountryPartitionEntity);

        // 数据处理
        handleData(cfgCountryPartitionEntity);

        log.info("开始新增分区国家关联单");
        boolean save = super.save(cfgCountryPartitionEntity);
        if(!save) {
            throw new ServiceException("分区国家关联单保存失败");
        }

        return new BaseResultDTO.AddDTO(cfgCountryPartitionEntity.getId(), cfgCountryPartitionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgCountryPartitionDTO.UpdateDTO updateDTO) {
        CfgCountryPartitionEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "分区国家关联单"));
        CfgCountryPartitionEntity cfgCountryPartitionEntity =  BeanMapperUtils.map(CfgCountryPartitionEntity.class, updateDTO);

        // 数据处理
        handleData(cfgCountryPartitionEntity);
        log.info("编辑 开始修改分区国家关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgCountryPartitionEntity);
        if(!save) {
            throw new ServiceException("分区国家关联单保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public String getPartitionByCountry(String country) {
        if(StringUtils.isBlank(country)){
            return "";
        }
        CfgCountryPartitionEntity cfgCountryPartitionEntity = this.lambdaQuery().eq(CfgCountryPartitionEntity::getCountry,country).last("limit 1").one();
        return Objects.isNull(cfgCountryPartitionEntity)?"":cfgCountryPartitionEntity.getPartitionId();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgCountryPartitionEntity cfgCountryPartitionEntity) {
    }
}
