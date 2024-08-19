package com.erp.server.sys.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.DictCountryOrgEntity;
import com.erp.server.sys.mapper.DictCountryOrgMapper;
import com.erp.server.sys.service.DictCountryOrgService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.DictCountryOrgDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 国家-组织（政治经济）关系表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-05-21
 */
@Slf4j
@Service
public class DictCountryOrgServiceImpl extends SuperServiceImpl<DictCountryOrgMapper, DictCountryOrgEntity> implements DictCountryOrgService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictCountryOrgDTO.AddDTO addDTO) {
        DictCountryOrgEntity dictCountryOrgEntity = new DictCountryOrgEntity();
        BeanMapperUtils.copy(addDTO, dictCountryOrgEntity);

        // 数据处理
        handleData(dictCountryOrgEntity);

        log.info("开始新增国家-组织（政治经济）关系单");
        boolean save = super.save(dictCountryOrgEntity);
        if(!save) {
            throw new ServiceException("国家-组织（政治经济）关系单保存失败");
        }

        // 操作日志
//        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "国家-组织（政治经济）关系单" , dictCountryOrgEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, dictCountryOrgEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dictCountryOrgEntity.getId(), dictCountryOrgEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictCountryOrgDTO.UpdateDTO updateDTO) {
        DictCountryOrgEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "国家-组织（政治经济）关系单"));
        DictCountryOrgEntity dictCountryOrgEntity =  BeanMapperUtils.map(DictCountryOrgEntity.class, updateDTO);

        // 数据处理
        handleData(dictCountryOrgEntity);
        log.info("编辑 开始修改国家-组织（政治经济）关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictCountryOrgEntity);
        if(!save) {
            throw new ServiceException("国家-组织（政治经济）关系单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
//            log.info("编辑 开始记录国家-组织（政治经济）关系单日志数据，id：【{}】", dictCountryOrgEntity.getId());
//            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictCountryOrgEntity.getId(), "国家-组织（政治经济）关系单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, dictCountryOrgEntity, null, dictCountryOrgEntity.getId(), msg);
        return Boolean.TRUE;
    }
    /**
     * 根据组织编码获取国家组织关系列表
     * @param orgCode
     * @return
     */
    @Override
    public List<DictCountryOrgEntity> listCountryByOrgCode(String orgCode) {
        if (StringUtils.isEmpty(orgCode)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(DictCountryOrgEntity::getOrgCode, orgCode).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DictCountryOrgEntity dictCountryOrgEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
