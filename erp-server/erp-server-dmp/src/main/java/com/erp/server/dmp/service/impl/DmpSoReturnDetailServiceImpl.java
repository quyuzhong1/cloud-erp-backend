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
import com.erp.model.dmp.dto.DmpSoReturnDetailDTO;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.server.dmp.mapper.DmpSoReturnDetailMapper;
import com.erp.server.dmp.service.DmpSoReturnDetailService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 中台销售退货订单明细表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
 */
@Slf4j
@Service
public class DmpSoReturnDetailServiceImpl extends SuperServiceImpl<DmpSoReturnDetailMapper, DmpSoReturnDetailEntity> implements DmpSoReturnDetailService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoReturnDetailDTO.AddDTO addDTO) {
        DmpSoReturnDetailEntity dmpSoReturnDetailEntity = new DmpSoReturnDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpSoReturnDetailEntity);

        // 数据处理
        handleData(dmpSoReturnDetailEntity);

        log.info("开始新增中台销售退货订单明细单");
        boolean save = super.save(dmpSoReturnDetailEntity);
        if(!save) {
            throw new ServiceException("中台销售退货订单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售退货订单明细单" , dmpSoReturnDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoReturnDetailEntity.getId(), dmpSoReturnDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoReturnDetailDTO.UpdateDTO updateDTO) {
        DmpSoReturnDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售退货订单明细单"));
        DmpSoReturnDetailEntity dmpSoReturnDetailEntity =  BeanMapperUtils.map(DmpSoReturnDetailEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoReturnDetailEntity);
        log.info("编辑 开始修改中台销售退货订单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoReturnDetailEntity);
        if(!save) {
            throw new ServiceException("中台销售退货订单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台销售退货订单明细单日志数据，id：【{}】", dmpSoReturnDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoReturnDetailEntity.getId(), "中台销售退货订单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoReturnDetailEntity dmpSoReturnDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
