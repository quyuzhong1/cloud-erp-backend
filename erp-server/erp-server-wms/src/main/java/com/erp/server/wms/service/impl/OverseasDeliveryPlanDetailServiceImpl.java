package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.OverseasDeliveryPlanDetailEntity;
import com.erp.server.wms.mapper.OverseasDeliveryPlanDetailMapper;
import com.erp.server.wms.service.OverseasDeliveryPlanDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货计划详情表 服务实现类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasDeliveryPlanDetailServiceImpl extends SuperServiceImpl<OverseasDeliveryPlanDetailMapper, OverseasDeliveryPlanDetailEntity> implements OverseasDeliveryPlanDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasDeliveryPlanDetailDTO.AddDTO addDTO) {
        OverseasDeliveryPlanDetailEntity overseasDeliveryPlanDetailEntity = new OverseasDeliveryPlanDetailEntity();
        BeanMapperUtils.copy(addDTO, overseasDeliveryPlanDetailEntity);

        // 数据处理
        handleData(overseasDeliveryPlanDetailEntity);

        log.info("开始新增发货计划详情单");
        boolean save = super.save(overseasDeliveryPlanDetailEntity);
        if(!save) {
            throw new ServiceException("发货计划详情单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "发货计划详情单" , overseasDeliveryPlanDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasDeliveryPlanDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasDeliveryPlanDetailEntity.getId(), overseasDeliveryPlanDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasDeliveryPlanDetailDTO.UpdateDTO updateDTO) {
        OverseasDeliveryPlanDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货计划详情单"));
        OverseasDeliveryPlanDetailEntity overseasDeliveryPlanDetailEntity =  BeanMapperUtils.map(OverseasDeliveryPlanDetailEntity.class, updateDTO);

        // 数据处理
        handleData(overseasDeliveryPlanDetailEntity);
        log.info("编辑 开始修改发货计划详情单数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasDeliveryPlanDetailEntity);
        if(!save) {
            throw new ServiceException("发货计划详情单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发货计划详情单日志数据，id：【{}】", overseasDeliveryPlanDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasDeliveryPlanDetailEntity.getId(), "发货计划详情单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasDeliveryPlanDetailEntity, null, overseasDeliveryPlanDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasDeliveryPlanDetailEntity overseasDeliveryPlanDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
