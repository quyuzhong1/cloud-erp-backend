package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.model.mrp.entity.LabelInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.LabelInfoMapper;
import com.erp.server.mrp.service.LabelInfoService;
import com.erp.server.mrp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 标签信息表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@Service
public class LabelInfoServiceImpl extends SuperServiceImpl<LabelInfoMapper, LabelInfoEntity> implements LabelInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LabelInfoDTO.AddDTO addDTO) {
        LabelInfoEntity labelInfoEntity = new LabelInfoEntity();
        BeanMapperUtils.copy(addDTO, labelInfoEntity);

        // 数据处理
        checkData(labelInfoEntity);

        log.info("开始新增标签信息单");
        boolean save = super.save(labelInfoEntity);
        if(!save) {
            throw new ServiceException("标签信息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "标签信息单" , labelInfoEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LABEL_INFO.getCode(), labelInfoEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(labelInfoEntity.getId(), labelInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LabelInfoDTO.UpdateDTO updateDTO) {
        LabelInfoEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "标签信息单"));
        LabelInfoEntity labelInfoEntity =  BeanMapperUtils.map(LabelInfoEntity.class, updateDTO);

        // 数据处理
        checkData(labelInfoEntity);
        log.info("编辑 开始修改标签信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(labelInfoEntity);
        if(!save) {
            throw new ServiceException("标签信息单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录标签信息单日志数据，id：【{}】", labelInfoEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), labelInfoEntity.getId(), "标签信息单");
        operateLogService.addModuleOperateLogByObj(old, labelInfoEntity, ModuleTypeEnum.LABEL_INFO.getCode(), labelInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 根据名称查询
     *
     *
     *
     * @author will
     * @date 2024/8/30 11:45
     * @param name
     * @return LabelInfoEntity
     */
    private LabelInfoEntity getByName (String name) {
      return lambdaQuery().eq(LabelInfoEntity::getName,name).last("limit 1").one();
    }

    /**
    * 新增修改处理数据
    */
    private void checkData(LabelInfoEntity labelInfoEntity) {
        //根据名称查询标签
        LabelInfoEntity old = this.getByName(labelInfoEntity.getName());
        if (ObjectUtil.isNotEmpty(old) && !StrUtil.equals(labelInfoEntity.getId(),old.getId())) {
            throw new ServiceException("标签名称重复，不支持新增");
        }

    }
}
