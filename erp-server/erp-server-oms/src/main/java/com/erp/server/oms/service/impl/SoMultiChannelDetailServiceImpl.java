package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.entity.SoMultiChannelDetailEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.mapper.SoMultiChannelDetailMapper;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.SoMultiChannelDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 多渠道订单明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SoMultiChannelDetailServiceImpl extends SuperServiceImpl<SoMultiChannelDetailMapper, SoMultiChannelDetailEntity> implements SoMultiChannelDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SkuMappingService skuMappingService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoMultiChannelDetailDTO.AddDTO addDTO) {
        SoMultiChannelDetailEntity soMultiChannelDetailEntity = new SoMultiChannelDetailEntity();
        BeanMapperUtils.copy(addDTO, soMultiChannelDetailEntity);

        // 数据处理
        handleData(soMultiChannelDetailEntity);

        log.info("开始新增多渠道订单明细");
        boolean save = super.save(soMultiChannelDetailEntity);
        if (!save) {
            throw new ServiceException("多渠道订单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "多渠道订单明细", soMultiChannelDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soMultiChannelDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soMultiChannelDetailEntity.getId(), soMultiChannelDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoMultiChannelDetailDTO.UpdateDTO addOrUpdateDTO) {
        SoMultiChannelDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "多渠道订单明细"));
        SoMultiChannelDetailEntity soMultiChannelDetailEntity = BeanMapperUtils.map(SoMultiChannelDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(soMultiChannelDetailEntity);
        log.info("编辑 开始修改多渠道订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(soMultiChannelDetailEntity);
        if (!save) {
            throw new ServiceException("多渠道订单明细保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录多渠道订单明细日志数据，id：【{}】", soMultiChannelDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soMultiChannelDetailEntity.getId(), "多渠道订单明细");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soMultiChannelDetailEntity, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelDetailEntity.getMainId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SoMultiChannelDetailEntity> addDetail(SoMultiChannelEntity soMultiChannelEntity, List<SoMultiChannelDetailDTO.AddDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return null;
        }
        List<String> skuIds = detailList.stream().map(SoMultiChannelDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        detailList.forEach(detail -> {
            detail.setMainId(soMultiChannelEntity.getId());
            skuVOS.stream().filter(e -> e.getSkuId().equals(detail.getSkuId())).findFirst().ifPresent(skuVO -> {
                detail.setProductName(skuVO.getSkuName());
                detail.setSkuNo(skuVO.getSkuNo());
            });
        });
        List<SoMultiChannelDetailEntity> soMultiChannelDetailEntities = BeanMapperUtils.copyList(SoMultiChannelDetailEntity.class, detailList);
        //平台SKU校验
//        checkData(soMultiChannelEntity,soMultiChannelDetailEntities);
        super.saveBatch(soMultiChannelDetailEntities);
        return soMultiChannelDetailEntities;
    }

    @Override
    public List<SoMultiChannelDetailEntity> listByMainIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return null;
        }
        return baseMapper.selectList(new LambdaQueryWrapper<SoMultiChannelDetailEntity>().in(SoMultiChannelDetailEntity::getMainId, ids));
    }

    @Override
    public void removeByMainId(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return;
        }
        baseMapper.delete(new LambdaQueryWrapper<SoMultiChannelDetailEntity>().eq(SoMultiChannelDetailEntity::getMainId, id));
    }

    @Override
    public void updateDetail(SoMultiChannelEntity soMultiChannelEntity, List<SoMultiChannelDetailDTO.UpdateDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        detailList.forEach(e -> {
            SoMultiChannelDetailEntity old = super.getById(e.getId());
            old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "多渠道订单明细"));
            SoMultiChannelDetailEntity soMultiChannelDetailEntity = BeanMapperUtils.map(SoMultiChannelDetailEntity.class, e);
            // 数据处理
            handleData(soMultiChannelDetailEntity);
            log.info("编辑 开始修改多渠道订单明细数据，id：【{}】", old.getId());
            boolean save = this.lambdaUpdate()
                    .set(SoMultiChannelDetailEntity::getPlatformSkuNo, soMultiChannelDetailEntity.getPlatformSkuNo())
                    .set(SoMultiChannelDetailEntity::getPlatformProductName, soMultiChannelDetailEntity.getPlatformProductName())
                    .set(SoMultiChannelDetailEntity::getPlatformSpuNo, soMultiChannelDetailEntity.getPlatformSpuNo())
                    .set(SoMultiChannelDetailEntity::getFbaInventoryId, soMultiChannelDetailEntity.getFbaInventoryId())
                    .set(SoMultiChannelDetailEntity::getFnSku, soMultiChannelDetailEntity.getFnSku())
                    .eq(SoMultiChannelDetailEntity::getId, soMultiChannelDetailEntity.getId()).update();
            if (!save) {
                throw new ServiceException("多渠道订单明细保存失败");
            }
            // 记录主单操作日志
            log.info("编辑 多渠道订单明细日志数据，平台sku由【{}】改为【{}】", old.getPlatformSkuNo(), soMultiChannelDetailEntity.getPlatformSkuNo());
            String msg = StrUtil.format("用户【{}】编辑平台sku由【{}】改为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getPlatformSkuNo(), soMultiChannelDetailEntity.getPlatformSkuNo(), "多渠道订单明细");
            // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelDetailEntity.getMainId(), "编辑明细");
        });
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(SoMultiChannelDetailEntity soMultiChannelDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
