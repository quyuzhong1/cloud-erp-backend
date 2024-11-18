package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.server.dmp.mapper.DmpSkuInfoMapper;
import com.erp.server.dmp.service.DmpSkuInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpSkuInfoDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中台产品表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-23
 */
@Slf4j
@Service
public class DmpSkuInfoServiceImpl extends SuperServiceImpl<DmpSkuInfoMapper, DmpSkuInfoEntity> implements DmpSkuInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSkuInfoDTO.AddDTO addDTO) {
        DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSkuInfoEntity);

        // 数据处理
        handleData(dmpSkuInfoEntity);

        log.info("开始新增中台产品单");
        boolean save = super.save(dmpSkuInfoEntity);
        if(!save) {
            throw new ServiceException("中台产品单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台产品单" , dmpSkuInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpSkuInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSkuInfoEntity.getId(), dmpSkuInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSkuInfoDTO.UpdateDTO updateDTO) {
        DmpSkuInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台产品单"));
        DmpSkuInfoEntity dmpSkuInfoEntity =  BeanMapperUtils.map(DmpSkuInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSkuInfoEntity);
        log.info("编辑 开始修改中台产品单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSkuInfoEntity);
        if(!save) {
            throw new ServiceException("中台产品单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台产品单日志数据，id：【{}】", dmpSkuInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSkuInfoEntity.getId(), "中台产品单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpSkuInfoEntity, null, dmpSkuInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSkuInfoEntity dmpSkuInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
