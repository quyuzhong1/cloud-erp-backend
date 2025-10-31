package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpRefPlatformFileEntity;
import com.erp.server.dmp.mapper.DmpRefPlatformFileMapper;
import com.erp.server.dmp.service.DmpRefPlatformFileService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpRefPlatformFileDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 平台文件转存FastDFS关系记录表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
 */
@Slf4j
@Service
public class DmpRefPlatformFileServiceImpl extends SuperServiceImpl<DmpRefPlatformFileMapper, DmpRefPlatformFileEntity> implements DmpRefPlatformFileService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpRefPlatformFileDTO.AddDTO addDTO) {
        DmpRefPlatformFileEntity dmpRefPlatformFileEntity = new DmpRefPlatformFileEntity();
        BeanMapperUtils.copy(addDTO, dmpRefPlatformFileEntity);

        // 数据处理
        handleData(dmpRefPlatformFileEntity);

        log.info("开始新增平台文件转存FastDFS关系记录单");
        boolean save = super.save(dmpRefPlatformFileEntity);
        if (!save) {
            throw new ServiceException("平台文件转存FastDFS关系记录单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "平台文件转存FastDFS关系记录单", dmpRefPlatformFileEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpRefPlatformFileEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpRefPlatformFileEntity.getId(), dmpRefPlatformFileEntity.getId());
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpRefPlatformFileDTO.UpdateDTO addOrUpdateDTO) {
        DmpRefPlatformFileEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "平台文件转存FastDFS关系记录单"));
        DmpRefPlatformFileEntity dmpRefPlatformFileEntity = BeanMapperUtils.map(DmpRefPlatformFileEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpRefPlatformFileEntity);
        log.info("编辑 开始修改平台文件转存FastDFS关系记录单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpRefPlatformFileEntity);
        if (!save) {
            throw new ServiceException("平台文件转存FastDFS关系记录单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录平台文件转存FastDFS关系记录单日志数据，id：【{}】", dmpRefPlatformFileEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpRefPlatformFileEntity.getId(), "平台文件转存FastDFS关系记录单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpRefPlatformFileEntity, null, dmpRefPlatformFileEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(DmpRefPlatformFileEntity dmpRefPlatformFileEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public Map<String, DmpRefPlatformFileEntity> mapByFileKey(String sourceSystem, List<String> fileKeyList) {
        if (CollUtil.isEmpty(fileKeyList)) {
            return Collections.emptyMap();
        }
        return this.lambdaQuery()
                .eq(DmpRefPlatformFileEntity::getSourceSystem, sourceSystem)
                .in(DmpRefPlatformFileEntity::getFileKey, fileKeyList)
                .list()
                .stream()
                .collect(Collectors.toMap(DmpRefPlatformFileEntity::getFileKey, e->e, (e1, e2) -> e1));
    }

}
