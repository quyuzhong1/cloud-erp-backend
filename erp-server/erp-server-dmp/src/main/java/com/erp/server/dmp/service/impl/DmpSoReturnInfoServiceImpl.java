package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpSoReturnInfoDTO;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.server.dmp.mapper.DmpSoReturnInfoMapper;
import com.erp.server.dmp.service.DmpSoReturnInfoService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 销售退货订单主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
 */
@Slf4j
@Service
public class DmpSoReturnInfoServiceImpl extends SuperServiceImpl<DmpSoReturnInfoMapper, DmpSoReturnInfoEntity> implements DmpSoReturnInfoService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoReturnInfoDTO.AddDTO addDTO) {
        DmpSoReturnInfoEntity dmpSoReturnInfoEntity = new DmpSoReturnInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoReturnInfoEntity);

        // 数据处理
        handleData(dmpSoReturnInfoEntity);

        log.info("开始新增销售退货订单主单");
        boolean save = super.save(dmpSoReturnInfoEntity);
        if(!save) {
            throw new ServiceException("销售退货订单主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销售退货订单主单" , dmpSoReturnInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoReturnInfoEntity.getId(), dmpSoReturnInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoReturnInfoDTO.UpdateDTO updateDTO) {
        DmpSoReturnInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销售退货订单主单"));
        DmpSoReturnInfoEntity dmpSoReturnInfoEntity =  BeanMapperUtils.map(DmpSoReturnInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoReturnInfoEntity);
        log.info("编辑 开始修改销售退货订单主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoReturnInfoEntity);
        if(!save) {
            throw new ServiceException("销售退货订单主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销售退货订单主单日志数据，id：【{}】", dmpSoReturnInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoReturnInfoEntity.getId(), "销售退货订单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoReturnInfoEntity dmpSoReturnInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
