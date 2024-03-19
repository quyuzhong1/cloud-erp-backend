package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.model.tms.entity.TmsWarehouseMappingEntity;
import com.erp.server.tms.mapper.TmsWarehouseMappingMapper;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsWarehouseMappingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsWarehouseMappingServiceImpl extends SuperServiceImpl<TmsWarehouseMappingMapper, TmsWarehouseMappingEntity> implements TmsWarehouseMappingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsWarehouseMappingDTO.AddDTO addDTO) {
        TmsWarehouseMappingEntity tmsWarehouseMappingEntity = new TmsWarehouseMappingEntity();
        BeanMapperUtils.copy(addDTO, tmsWarehouseMappingEntity);

        // 数据处理
        handleData(tmsWarehouseMappingEntity);

        log.info("开始新增");
        boolean save = super.save(tmsWarehouseMappingEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "" , tmsWarehouseMappingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsWarehouseMappingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsWarehouseMappingEntity.getId(), tmsWarehouseMappingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsWarehouseMappingDTO.UpdateDTO updateDTO) {
        TmsWarehouseMappingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        TmsWarehouseMappingEntity tmsWarehouseMappingEntity =  BeanMapperUtils.map(TmsWarehouseMappingEntity.class, updateDTO);

        // 数据处理
        handleData(tmsWarehouseMappingEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsWarehouseMappingEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", tmsWarehouseMappingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsWarehouseMappingEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsWarehouseMappingEntity, null, tmsWarehouseMappingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsWarehouseMappingDTO.ListDTO> paging(PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsWarehouseMappingEntity tmsWarehouseMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
