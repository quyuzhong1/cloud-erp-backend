package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.server.dmp.mapper.DmpCfgInputMapper;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 输入信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgInputServiceImpl extends SuperServiceImpl<DmpCfgInputMapper, DmpCfgInputEntity> implements DmpCfgInputService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputDTO.AddDTO addDTO) {
        DmpCfgInputEntity dmpCfgInputEntity = new DmpCfgInputEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputEntity);

        // 数据处理
        handleData(dmpCfgInputEntity);

        log.info("开始新增输入信息");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        dmpCfgInputEntity.setCode(code);
        boolean save = super.save(dmpCfgInputEntity);
        if(!save) {
            throw new ServiceException("输入信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "输入信息" , dmpCfgInputEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgInputEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputDTO.UpdateDTO updateDTO) {
        DmpCfgInputEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "输入信息"));
        DmpCfgInputEntity dmpCfgInputEntity =  BeanMapperUtils.map(DmpCfgInputEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputEntity);
        log.info("编辑 开始修改输入信息数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dmpCfgInputEntity);
        if(!save) {
            throw new ServiceException("输入信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录输入信息日志数据，单号：【{}】", dmpCfgInputEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputEntity.getCode(), "输入信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgInputEntity, null, dmpCfgInputEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputEntity dmpCfgInputEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
