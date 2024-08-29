package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.server.mrp.mapper.CfgPlatformMappingMapper;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 平台映射表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@Service
public class CfgPlatformMappingServiceImpl extends SuperServiceImpl<CfgPlatformMappingMapper, CfgPlatformMappingEntity> implements CfgPlatformMappingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CustomerFeign customerFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgPlatformMappingDTO.AddDTO addDTO) {
        CfgPlatformMappingEntity cfgPlatformMappingEntity = new CfgPlatformMappingEntity();
        BeanMapperUtils.copy(addDTO, cfgPlatformMappingEntity);

        // 数据处理
        handleData(cfgPlatformMappingEntity);

        log.info("开始新增平台映射单");
        boolean save = super.save(cfgPlatformMappingEntity);
        if(!save) {
            throw new ServiceException("平台映射单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "平台映射单" , cfgPlatformMappingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgPlatformMappingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgPlatformMappingEntity.getId(), cfgPlatformMappingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgPlatformMappingDTO.UpdateDTO updateDTO) {
        CfgPlatformMappingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "平台映射单"));
        CfgPlatformMappingEntity cfgPlatformMappingEntity =  BeanMapperUtils.map(CfgPlatformMappingEntity.class, updateDTO);

        // 数据处理
        handleData(cfgPlatformMappingEntity);
        log.info("编辑 开始修改平台映射单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgPlatformMappingEntity);
        if(!save) {
            throw new ServiceException("平台映射单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录平台映射单日志数据，id：【{}】", cfgPlatformMappingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgPlatformMappingEntity.getId(), "平台映射单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgPlatformMappingEntity, null, cfgPlatformMappingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgPlatformMappingDTO.ListDTO> selectPlatformMapping(CfgPlatformMappingDTO.SelectDTO dto) {
        List<CfgPlatformMappingDTO.ListDTO> list = baseMapper.selectPlatformMapping(dto);
        handleSelectPaging(list);
        return list;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgPlatformMappingEntity cfgPlatformMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 下拉数据处理
     */
    private void handleSelectPaging(List<CfgPlatformMappingDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //平台信息
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

       for (CfgPlatformMappingDTO.ListDTO listDTO : records) {
            //平台名称
           String platformName = platformViewList.stream().filter(obj -> StrUtil.equals(listDTO.getPlatform(), obj.getValue())).map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");
            listDTO.setPlatformName(platformName);
       }
    }
}
