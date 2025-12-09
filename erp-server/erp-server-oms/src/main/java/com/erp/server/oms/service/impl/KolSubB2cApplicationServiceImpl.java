package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.dto.KolB2cApplicationDTO;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CfgKolOptionTypeEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationDeliveryStatusEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationOrderStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.service.CfgKolOptionService;
import com.erp.server.oms.service.KolSubB2cApplicationDetailService;
import com.erp.server.oms.mapper.KolSubB2cApplicationMapper;
import com.erp.server.oms.service.KolSubB2cApplicationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * <p>
 * B2C寄样申请单拆分单 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@Service
public class KolSubB2cApplicationServiceImpl extends SuperServiceImpl<KolSubB2cApplicationMapper, KolSubB2cApplicationEntity> implements KolSubB2cApplicationService {
    @Resource
    private KolSubB2cApplicationDetailService kolSubB2cApplicationDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private CfgKolOptionService cfgKolOptionService;


    @Override
    public List<KolSubB2cApplicationDTO.ListDTO> listSubBySourceId(String sourceId) {
        if(StringUtils.isBlank(sourceId)){
            return Collections.emptyList();
        }
        List<KolSubB2cApplicationDTO.ListDTO> list = this.baseMapper.listSubBySourceId(sourceId);
        // 数据处理
        fillList(list);
        return list;
    }

    private void fillList(List<KolSubB2cApplicationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }

        List<String> skuIds = list.stream().map(KolSubB2cApplicationDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName));

        List<CfgKolOptionEntity> cfgKolOptionEntities = cfgKolOptionService.lambdaQuery().in(CfgKolOptionEntity::getType, Arrays.asList(CfgKolOptionTypeEnum.KOL_SAMPLE_TYPE.getCode(), CfgKolOptionTypeEnum.PROJECT_TAG.getCode())).list();
        Map<String, String> map = cfgKolOptionEntities.stream().collect(Collectors.toMap(CfgKolOptionEntity::getId, CfgKolOptionEntity::getName));


        // 属性赋值
        for(KolSubB2cApplicationDTO.ListDTO data : list) {
            //平台
            data.setDictPlatformName(DmpBasicSystemCodeEnum.getName(data.getDictPlatform()));
            //订单状态
            data.setOrderStatusName(KolSubB2cApplicationOrderStatusEnum.getName(data.getOrderStatus()));
            //发货状态
            data.setDeliveryStatusName(KolSubB2cApplicationDeliveryStatusEnum.getName(data.getDeliveryStatus()));
            //SKU名称
            data.setProductName(skuMap.get(data.getSkuId()));
            //项目名称
            if(StringUtils.isNotBlank(data.getProjectTag())){
                String projectTagName = Arrays.stream(data.getProjectTag().split(",")).map(map::get).collect(Collectors.joining(","));
                data.setProjectTagName(projectTagName);
            }
        }
    }

    /**
     * 根据B2C寄样申请生成拆分单
     * 按达人维度生成拆分单和拆分单明细
     * 根据业务类型生成 国外=B2C订单  国内=旺店通销售订单
     * @author jack
     * @date: 2025-12-09
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateSplitOrder(KolB2cApplicationEntity entity, List<KolB2cApplicationDetailEntity> list) {
        KolSubB2cApplicationEntity kolSubB2cApplicationEntity = new KolSubB2cApplicationEntity();
        kolSubB2cApplicationEntity.setSourceId(entity.getId());
        kolSubB2cApplicationEntity.setDictPlatform(getDictPlatform(entity.getIsInternational()));
        kolSubB2cApplicationEntity.setDeliveryStatus(KolSubB2cApplicationDeliveryStatusEnum.WAITSHIPPED.getCode());
        kolSubB2cApplicationEntity.setOrderStatus(KolSubB2cApplicationOrderStatusEnum.NOTAPPROVE.getCode());
        kolSubB2cApplicationEntity.setRemark(entity.getRemark());
        Map<String, List<KolB2cApplicationDetailEntity>> partnerGroup = list.stream().collect(Collectors.groupingBy(KolB2cApplicationDetailEntity::getPartnerId));

        int index = 1;
        for (Map.Entry<String, List<KolB2cApplicationDetailEntity>> entry : partnerGroup.entrySet()) {
            List<KolB2cApplicationDetailEntity> value = entry.getValue();
            KolB2cApplicationDetailEntity kolB2cApplicationDetailEntity = value.get(0);

            kolSubB2cApplicationEntity.setCode(entity.getCode()+"_"+index);
            kolSubB2cApplicationEntity.setPartnerId(entry.getKey());
            kolSubB2cApplicationEntity.setNickname(kolB2cApplicationDetailEntity.getNickname());
            boolean save = super.save(kolSubB2cApplicationEntity);
            if(!save) {
                throw new ServiceException("B2C寄样申请单拆分单保存失败");
            }

            String id = kolSubB2cApplicationEntity.getId();
            List<KolSubB2cApplicationDetailEntity> detailList = new ArrayList<>();
            for (KolB2cApplicationDetailEntity b2cApplicationDetailEntity : value) {
                KolSubB2cApplicationDetailEntity subB2cApplicationDetailEntity = new KolSubB2cApplicationDetailEntity();

                subB2cApplicationDetailEntity.setSourceDetailId(b2cApplicationDetailEntity.getId());
                subB2cApplicationDetailEntity.setSkuId(b2cApplicationDetailEntity.getSkuId());
                subB2cApplicationDetailEntity.setSkuNo(b2cApplicationDetailEntity.getSkuNo());
                subB2cApplicationDetailEntity.setApplyQty(b2cApplicationDetailEntity.getApplyQty());
                subB2cApplicationDetailEntity.setRemark(b2cApplicationDetailEntity.getRemark());
                subB2cApplicationDetailEntity.setProjectTag(b2cApplicationDetailEntity.getProjectTag());
                subB2cApplicationDetailEntity.setMainId(id);
                detailList.add(subB2cApplicationDetailEntity);
            }

            kolSubB2cApplicationDetailService.saveBatch(detailList);
            //序号+1
            index+=1;
        }
    }

    /**
     *  根据业务类型判断是哪个平台
     */
    private String getDictPlatform(Boolean isInternational) {
        return Boolean.TRUE.equals(isInternational) ? DmpBasicSystemCodeEnum.ERP.getCode() : DmpBasicSystemCodeEnum.WDT.getCode();
    }

}
