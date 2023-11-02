package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.server.tms.mapper.DictBasicMapper;
import com.erp.server.tms.service.DictBasicService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.DictBasicDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictBasicDTO.AddDTO addDTO) {
        DictBasicEntity dictBasicEntity = new DictBasicEntity();
        BeanMapperUtils.copy(addDTO, dictBasicEntity);

        // 数据处理
        handleData(dictBasicEntity);

        log.info("开始新增字典单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        dictBasicEntity.setCode(code);
        boolean save = super.save(dictBasicEntity);
        if(!save) {
            throw new ServiceException("字典单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "字典单" , dictBasicEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dictBasicEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(fbaDeliveryEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictBasicDTO.UpdateDTO updateDTO) {
        DictBasicEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "字典单"));
        DictBasicEntity dictBasicEntity =  BeanMapperUtils.map(DictBasicEntity.class, updateDTO);

        // 数据处理
        handleData(dictBasicEntity);
        log.info("编辑 开始修改字典单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dictBasicEntity);
        if(!save) {
            throw new ServiceException("字典单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录字典单日志数据，单号：【{}】", dictBasicEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), dictBasicEntity.getCode(), "字典单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dictBasicEntity, null, dictBasicEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DictBasicEntity dictBasicEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
