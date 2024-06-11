package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;
import com.erp.server.dmp.mapper.DmpInputTaskFileMapper;
import com.erp.server.dmp.service.DmpInputTaskFileService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpInputTaskFileDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 拉取任务文件存储 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpInputTaskFileServiceImpl extends SuperServiceImpl<DmpInputTaskFileMapper, DmpInputTaskFileEntity> implements DmpInputTaskFileService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputTaskFileDTO.AddDTO addDTO) {
        DmpInputTaskFileEntity dmpInputTaskFileEntity = new DmpInputTaskFileEntity();
        BeanMapperUtils.copy(addDTO, dmpInputTaskFileEntity);

        // 数据处理
        handleData(dmpInputTaskFileEntity);

        log.info("开始新增拉取任务文件存储");
        boolean save = super.save(dmpInputTaskFileEntity);
        if(!save) {
            throw new ServiceException("拉取任务文件存储保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拉取任务文件存储" , dmpInputTaskFileEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpInputTaskFileEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputTaskFileEntity.getId(), dmpInputTaskFileEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputTaskFileDTO.UpdateDTO updateDTO) {
        DmpInputTaskFileEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拉取任务文件存储"));
        DmpInputTaskFileEntity dmpInputTaskFileEntity =  BeanMapperUtils.map(DmpInputTaskFileEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputTaskFileEntity);
        log.info("编辑 开始修改拉取任务文件存储数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputTaskFileEntity);
        if(!save) {
            throw new ServiceException("拉取任务文件存储保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录拉取任务文件存储日志数据，id：【{}】", dmpInputTaskFileEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputTaskFileEntity.getId(), "拉取任务文件存储");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpInputTaskFileEntity, null, dmpInputTaskFileEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputTaskFileEntity dmpInputTaskFileEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
