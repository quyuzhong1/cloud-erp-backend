package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.KingdeeOperatorTypeEntity;
import com.erp.server.sys.mapper.KingdeeOperatorTypeMapper;
import com.erp.server.sys.service.KingdeeOperatorTypeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.KingdeeOperatorTypeDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeeOperatorTypeServiceImpl extends SuperServiceImpl<KingdeeOperatorTypeMapper, KingdeeOperatorTypeEntity> implements KingdeeOperatorTypeService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeeOperatorTypeDTO.AddDTO addDTO) {
        KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity = new KingdeeOperatorTypeEntity();
        BeanMapperUtils.copy(addDTO, kingdeeOperatorTypeEntity);

        // 数据处理
        handleData(kingdeeOperatorTypeEntity);

        log.info("开始新增");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        kingdeeOperatorTypeEntity.setCode(code);
        boolean save = super.save(kingdeeOperatorTypeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "" , kingdeeOperatorTypeEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kingdeeOperatorTypeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kingdeeOperatorTypeEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeOperatorTypeDTO.UpdateDTO updateDTO) {
        KingdeeOperatorTypeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity =  BeanMapperUtils.map(KingdeeOperatorTypeEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeOperatorTypeEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kingdeeOperatorTypeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，单号：【{}】", kingdeeOperatorTypeEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), kingdeeOperatorTypeEntity.getCode(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kingdeeOperatorTypeEntity, null, kingdeeOperatorTypeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
