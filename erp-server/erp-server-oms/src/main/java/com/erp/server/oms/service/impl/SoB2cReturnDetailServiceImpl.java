package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.server.oms.mapper.SoB2cReturnDetailMapper;
import com.erp.server.oms.service.SoB2cReturnDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.jgoodies.common.bean.Bean;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoB2cReturnDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c退货订单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
@Slf4j
@Service
public class SoB2cReturnDetailServiceImpl extends SuperServiceImpl<SoB2cReturnDetailMapper, SoB2cReturnDetailEntity> implements SoB2cReturnDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(List<SoB2cReturnDetailDTO.AddDTO> addDTO, String mainId) {
        List<SoB2cReturnDetailEntity> addList = BeanUtil.copyToList(addDTO, SoB2cReturnDetailEntity.class);
        addList.forEach(v->v.setMainId(mainId));
        boolean save = super.saveBatch(addList);
        if(!save) {
            throw new ServiceException("b2c退货订单明细保存失败");
        }
        return new BaseResultDTO.AddDTO();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cReturnDetailDTO.UpdateDTO updateDTO) {
        SoB2cReturnDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c退货订单明细"));
        SoB2cReturnDetailEntity soB2cReturnDetailEntity =  BeanMapperUtils.map(SoB2cReturnDetailEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cReturnDetailEntity);
        log.info("编辑 开始修改b2c退货订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cReturnDetailEntity);
        if(!save) {
            throw new ServiceException("b2c退货订单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2c退货订单明细日志数据，id：【{}】", soB2cReturnDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cReturnDetailEntity.getId(), "b2c退货订单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cReturnDetailEntity, null, soB2cReturnDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SoB2cReturnDetailEntity> listByMainIds(List<String> mainIds) {
        if(CollectionUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }

        return this.lambdaQuery().in(SoB2cReturnDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public boolean deleteByMainIds(List<String> mainIds) {
        if(CollectionUtil.isEmpty(mainIds)) {
            return true;
        }
        return this.lambdaUpdate().in(SoB2cReturnDetailEntity::getMainId,mainIds).remove();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cReturnDetailEntity soB2cReturnDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
