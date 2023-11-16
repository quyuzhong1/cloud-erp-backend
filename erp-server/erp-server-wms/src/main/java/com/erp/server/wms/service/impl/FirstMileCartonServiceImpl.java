package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.FirstMileCartonEntity;
import com.erp.server.wms.mapper.FirstMileCartonMapper;
import com.erp.server.wms.service.FirstMileCartonService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货单箱规信息 服务实现类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
@Slf4j
@Service
public class FirstMileCartonServiceImpl extends SuperServiceImpl<FirstMileCartonMapper, FirstMileCartonEntity> implements FirstMileCartonService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileCartonDTO.AddDTO addDTO) {
        FirstMileCartonEntity firstMileCartonEntity = new FirstMileCartonEntity();
        BeanMapperUtils.copy(addDTO, firstMileCartonEntity);

        // 数据处理
        handleData(firstMileCartonEntity);

        log.info("开始新增发货单箱规信息");
        boolean save = super.save(firstMileCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "发货单箱规信息" , firstMileCartonEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, firstMileCartonEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(firstMileCartonEntity.getId(), firstMileCartonEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileCartonDTO.UpdateDTO updateDTO) {
        FirstMileCartonEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货单箱规信息"));
        FirstMileCartonEntity firstMileCartonEntity =  BeanMapperUtils.map(FirstMileCartonEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileCartonEntity);
        log.info("编辑 开始修改发货单箱规信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发货单箱规信息日志数据，id：【{}】", firstMileCartonEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), firstMileCartonEntity.getId(), "发货单箱规信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, firstMileCartonEntity, null, firstMileCartonEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileCartonEntity firstMileCartonEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
