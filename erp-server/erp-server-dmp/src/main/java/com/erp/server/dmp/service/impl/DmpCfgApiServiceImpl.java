package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgApiDTO;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.server.dmp.mapper.DmpCfgApiMapper;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 输入输出api信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgApiServiceImpl extends SuperServiceImpl<DmpCfgApiMapper, DmpCfgApiEntity> implements DmpCfgApiService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgApiDTO.AddDTO addDTO) {
        DmpCfgApiEntity dmpCfgApiEntity = new DmpCfgApiEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgApiEntity);

        // 数据处理
        handleData(dmpCfgApiEntity);

        log.info("开始新增输入输出api信息");
        boolean save = super.save(dmpCfgApiEntity);
        if (!save) {
            throw new ServiceException("输入输出api信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "输入输出api信息", dmpCfgApiEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgApiEntity.getId(), dmpCfgApiEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgApiDTO.UpdateDTO updateDTO) {
        DmpCfgApiEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "输入输出api信息"));
        DmpCfgApiEntity dmpCfgApiEntity = BeanMapperUtils.map(DmpCfgApiEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgApiEntity);
        log.info("编辑 开始修改输入输出api信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgApiEntity);
        if (!save) {
            throw new ServiceException("输入输出api信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录输入输出api信息日志数据，id：【{}】", dmpCfgApiEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgApiEntity.getId(), "输入输出api信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(DmpCfgApiEntity dmpCfgApiEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public DmpCfgApiDTO.ViewDTO view(String id) {
        DmpCfgApiEntity dmpCfgApiEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到输入输出api信息数据"));
        DmpCfgApiDTO.ViewDTO data = BeanMapperUtils.map(DmpCfgApiDTO.ViewDTO.class, dmpCfgApiEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(DmpCfgApiDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DmpCfgApiDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (DmpCfgApiDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }
}
