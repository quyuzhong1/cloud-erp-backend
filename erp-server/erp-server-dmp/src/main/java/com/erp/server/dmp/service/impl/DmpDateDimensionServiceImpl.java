package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.erp.model.dmp.entity.DmpDateDimensionEntity;
import com.erp.server.dmp.mapper.DmpDateDimensionMapper;
import com.erp.server.dmp.service.DmpDateDimensionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpDateDimensionDTO;

import java.time.LocalDateTime;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 时间维度表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2023-12-08
 */
@Slf4j
@Service
public class DmpDateDimensionServiceImpl extends SuperServiceImpl<DmpDateDimensionMapper, DmpDateDimensionEntity> implements DmpDateDimensionService {
//    @Autowired
//    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpDateDimensionDTO.AddDTO addDTO) {
        DmpDateDimensionEntity dmpDateDimensionEntity = new DmpDateDimensionEntity();
        BeanMapperUtils.copy(addDTO, dmpDateDimensionEntity);

        // 数据处理
        handleData(dmpDateDimensionEntity);

        log.info("开始新增时间维度单");
        boolean save = super.save(dmpDateDimensionEntity);
        if(!save) {
            throw new ServiceException("时间维度单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "时间维度单" , dmpDateDimensionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, dmpDateDimensionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpDateDimensionEntity.getId(), dmpDateDimensionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpDateDimensionDTO.UpdateDTO updateDTO) {
        DmpDateDimensionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "时间维度单"));
        DmpDateDimensionEntity dmpDateDimensionEntity =  BeanMapperUtils.map(DmpDateDimensionEntity.class, updateDTO);

        // 数据处理
        handleData(dmpDateDimensionEntity);
        log.info("编辑 开始修改时间维度单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpDateDimensionEntity);
        if(!save) {
            throw new ServiceException("时间维度单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录时间维度单日志数据，id：【{}】", dmpDateDimensionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), dmpDateDimensionEntity.getId(), "时间维度单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, dmpDateDimensionEntity, null, dmpDateDimensionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpDateDimensionEntity dmpDateDimensionEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public void deleteByYear(String year) {
        baseMapper.deleteByYear(year);
    }

    @Override
    public void batchInsertDateDimensions(List<LocalDateTime> dateTimes) {
        List<DmpDateDimensionEntity> list = new ArrayList<>();
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        String uid = "";
        String userName = "";
        if (ObjectUtils.isNotEmpty(loginUser)) {
            uid = loginUser.getUid();
            userName = loginUser.getUserName();
        }

        String finalUid = uid;
        String finalUserName = userName;
        dateTimes.forEach(localDateTime -> {
            DmpDateDimensionEntity entity = new DmpDateDimensionEntity();
            entity.setDateTime(localDateTime);
            entity.setId(IdWorker.getIdStr());
            entity.setCreateTime(LocalDateTime.now());
            entity.setCreateUserId(finalUid);
            entity.setCreateUserName(finalUserName);
            list.add(entity);
        });
        baseMapper.batchInsertDateDimensions(list);
    }
}
