package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleEntity;
import com.erp.server.oms.mapper.DeliveryBoxRuleMapper;
import com.erp.server.oms.service.DeliveryBoxRuleService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@Service
public class DeliveryBoxRuleServiceImpl extends SuperServiceImpl<DeliveryBoxRuleMapper, DeliveryBoxRuleEntity> implements DeliveryBoxRuleService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryBoxRuleDTO.AddDTO addDTO) {
        DeliveryBoxRuleEntity deliveryBoxRuleEntity = new DeliveryBoxRuleEntity();
        BeanMapperUtils.copy(addDTO, deliveryBoxRuleEntity);

        // 数据处理
        handleData(deliveryBoxRuleEntity);

        log.info("开始新增");
        boolean save = super.save(deliveryBoxRuleEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , deliveryBoxRuleEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, deliveryBoxRuleEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(deliveryBoxRuleEntity.getId(), deliveryBoxRuleEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryBoxRuleDTO.UpdateDTO addOrUpdateDTO) {
        DeliveryBoxRuleEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DeliveryBoxRuleEntity deliverytBoxRuleEntity =  BeanMapperUtils.map(DeliveryBoxRuleEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(deliverytBoxRuleEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(deliverytBoxRuleEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", deliverytBoxRuleEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), deliverytBoxRuleEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, deliverytBoxRuleEntity, null, deliverytBoxRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public DeliveryBoxRuleDTO.ViewDTO view(String id) {
        DeliveryBoxRuleEntity deliveryBoxRuleEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        return null;
    }

    @Override
    public List<DeliveryBoxRuleDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return null;
    }

    @Override
    public PagingVO<DeliveryBoxRuleDTO.ListDTO> paging(PagingDTO<DeliveryBoxRuleDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> deleteByIds(BaseIdsDTO.IdsDTO dto) {
        return null;
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliveryBoxRuleEntity deliveryBoxRuleEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
