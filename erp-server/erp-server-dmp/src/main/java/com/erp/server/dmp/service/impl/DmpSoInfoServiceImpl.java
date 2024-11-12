package com.erp.server.dmp.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.ObjectUtil;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.wms.dto.ShudiyunB2cOrderDTO;
import com.erp.server.dmp.service.DmpSoDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpSoInfoDTO;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.server.dmp.mapper.DmpSoInfoMapper;
import com.erp.server.dmp.service.DmpSoInfoService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * <p>
 * 中台销售订单表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
@Slf4j
@Service
public class DmpSoInfoServiceImpl extends SuperServiceImpl<DmpSoInfoMapper, DmpSoInfoEntity> implements DmpSoInfoService {

    @Resource
    private DmpSoDetailService dmpSoDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoInfoDTO.AddDTO addDTO) {
        DmpSoInfoEntity dmpSoInfoEntity = new DmpSoInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoInfoEntity);

        // 数据处理
        handleData(dmpSoInfoEntity);

        log.info("开始新增中台销售订单表");
        boolean save = super.save(dmpSoInfoEntity);
        if(!save) {
            throw new ServiceException("中台销售订单表保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售订单表" , dmpSoInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoInfoEntity.getId(), dmpSoInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoInfoDTO.UpdateDTO updateDTO) {
        DmpSoInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售订单表"));
        DmpSoInfoEntity dmpSoInfoEntity =  BeanMapperUtils.map(DmpSoInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoInfoEntity);
        log.info("编辑 开始修改中台销售订单表数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoInfoEntity);
        if(!save) {
            throw new ServiceException("中台销售订单表保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台销售订单表日志数据，id：【{}】", dmpSoInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoInfoEntity.getId(), "中台销售订单表");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoInfoEntity dmpSoInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }


    /**
    * 数帝云线上字段映射
    */
    public void shudiyunFieldDmpOrderHandler(String thirdCode) {
        DmpSoInfoEntity dmpSoInfoEntity = lambdaQuery().eq(DmpSoInfoEntity::getThirdCode, thirdCode).last("LIMIT 1").one();
        if (ObjectUtil.isEmpty(dmpSoInfoEntity)) {
            return;
        }

        //数帝云数据结构
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();

        //B2C订单详情
        List<DmpSoDetailEntity> dmpSoDetailEntities = dmpSoDetailService.listByMainId(dmpSoInfoEntity.getId());
        for (DmpSoDetailEntity dmpSoDetailEntity : dmpSoDetailEntities) {
            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
        }

    }


}
