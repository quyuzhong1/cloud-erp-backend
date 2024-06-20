package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.PickingCartEntity;
import com.erp.server.wms.mapper.PickingCartMapper;
import com.erp.server.wms.service.PickingCartService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PickingCartDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 拣货车管理 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class PickingCartServiceImpl extends SuperServiceImpl<PickingCartMapper, PickingCartEntity> implements PickingCartService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PickingCartDTO.AddDTO addDTO) {
        PickingCartEntity pickingCartEntity = new PickingCartEntity();
        BeanMapperUtils.copy(addDTO, pickingCartEntity);

        // 数据处理
        handleData(pickingCartEntity);

        log.info("开始新增拣货车管理");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        pickingCartEntity.setCode(code);
        boolean save = super.save(pickingCartEntity);
        if(!save) {
            throw new ServiceException("拣货车管理保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拣货车管理" , pickingCartEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, pickingCartEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(pickingCartEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PickingCartDTO.UpdateDTO updateDTO) {
        PickingCartEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拣货车管理"));
        PickingCartEntity pickingCartEntity =  BeanMapperUtils.map(PickingCartEntity.class, updateDTO);

        // 数据处理
        handleData(pickingCartEntity);
        log.info("编辑 开始修改拣货车管理数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(pickingCartEntity);
        if(!save) {
            throw new ServiceException("拣货车管理保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录拣货车管理日志数据，单号：【{}】", pickingCartEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), pickingCartEntity.getCode(), "拣货车管理");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, pickingCartEntity, null, pickingCartEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PickingCartEntity pickingCartEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
