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
        if(!save) {
            throw new ServiceException("多渠道订单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "多渠道订单明细" , soMultiChannelDetailEntity.getId());
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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "多渠道订单明细"));
        SoMultiChannelDetailEntity soMultiChannelDetailEntity =  BeanMapperUtils.map(SoMultiChannelDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(soMultiChannelDetailEntity);
        log.info("编辑 开始修改多渠道订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(soMultiChannelDetailEntity);
        if(!save) {
            throw new ServiceException("多渠道订单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录多渠道订单明细日志数据，id：【{}】", soMultiChannelDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soMultiChannelDetailEntity.getId(), "多渠道订单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soMultiChannelDetailEntity, null, soMultiChannelDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SoMultiChannelDetailEntity> addDetail(SoMultiChannelEntity soMultiChannelEntity, List<SoMultiChannelDetailDTO.AddDTO> detailList) {
        if(CollUtil.isEmpty(detailList)) {
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
        checkData(soMultiChannelEntity,soMultiChannelDetailEntities);
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

    private void checkData(SoMultiChannelEntity soMultiChannelEntity, List<SoMultiChannelDetailEntity> soMultiChannelDetailEntities) {
        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(soMultiChannelEntity.getDeliveryPlatform());
        paramDTO.setShopIdList(Collections.singletonList(soMultiChannelEntity.getShopId()));
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(soMultiChannelDetailEntities.stream().map(SoMultiChannelDetailEntity::getPlatformSkuNo).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
        List<ListingInfoWithSkuMappingDTO> listDto = skuMappingService.findListDto(paramDTO);
        //平台sku映射检查
        if (CollUtil.isEmpty(listDto)){
            throw new ServiceException("平台sku映射不存在");
        }
        soMultiChannelDetailEntities.forEach(detail -> {
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listDto.stream().filter(e -> e.getPlatformSkuNo().equals(detail.getPlatformSkuNo()) && e.getProductSkuId().equals(detail.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNull(listingInfoWithSkuMappingDTO)){
                throw new ServiceException("产品sku【{}】平台sku【{}】映射不存在", detail.getSkuNo(), detail.getPlatformSkuNo());
            }
        });
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoMultiChannelDetailEntity soMultiChannelDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
