package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.sys.entity.ImlDictCityEntity;
import com.erp.server.sys.mapper.ImlDictCityMapper;
import com.erp.server.sys.service.ImlDictCityService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.ImlDictCityDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 城市字典表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2023-11-22
 */
@Slf4j
@Service
public class ImlDictCityServiceImpl extends SuperServiceImpl<ImlDictCityMapper, ImlDictCityEntity> implements ImlDictCityService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ImlDictCityDTO.AddDTO addDTO) {
        ImlDictCityEntity imlDictCityEntity = new ImlDictCityEntity();
        BeanMapperUtils.copy(addDTO, imlDictCityEntity);

        // 数据处理
        handleData(imlDictCityEntity);

        log.info("开始新增城市字典单");
        boolean save = super.save(imlDictCityEntity);
        if(!save) {
            throw new ServiceException("城市字典单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "城市字典单" , imlDictCityEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, imlDictCityEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(imlDictCityEntity.getId(), imlDictCityEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ImlDictCityDTO.UpdateDTO updateDTO) {
        ImlDictCityEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "城市字典单"));
        ImlDictCityEntity imlDictCityEntity =  BeanMapperUtils.map(ImlDictCityEntity.class, updateDTO);

        // 数据处理
        handleData(imlDictCityEntity);
        log.info("编辑 开始修改城市字典单数据，id：【{}】", old.getId());
        boolean save = super.updateById(imlDictCityEntity);
        if(!save) {
            throw new ServiceException("城市字典单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录城市字典单日志数据，id：【{}】", imlDictCityEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), imlDictCityEntity.getId(), "城市字典单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, imlDictCityEntity, null, imlDictCityEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ImlDictCityEntity imlDictCityEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
