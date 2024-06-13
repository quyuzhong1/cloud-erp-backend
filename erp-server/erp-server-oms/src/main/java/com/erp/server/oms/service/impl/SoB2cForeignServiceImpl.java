package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cForeignDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cOptionTypeEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SoB2cForeignService;
import com.erp.server.oms.service.SoB2cRefService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cForeignServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cForeignService {

    @Resource
    private ShopInfoService shopInfoService;
    
    @Resource
    private SoB2cRefService soB2cRefService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Override
    public PagingVO<List<SoB2cForeignDTO.OrderDeliveryResp>> getOrderDeliveryInfo(PagingDTO<SoB2cForeignDTO.OrderDeliveryReq> pagingDTO) {
        SoB2cForeignDTO.OrderDeliveryReq orderDeliveryReq = pagingDTO.getParams();
        if(Objects.isNull(orderDeliveryReq)){
            throw new ServiceException("参数不能为空");
        }
        if(Objects.isNull(pagingDTO.getCurrPage()) || Objects.isNull(pagingDTO.getPageSize())){
            throw new ServiceException("分页参数不能为空");
        }
        if(pagingDTO.getPageSize() > 200){
            throw new ServiceException("每页查询数量不能超过200");
        }
        if(StringUtils.isBlank(orderDeliveryReq.getShopId())){
            throw new ServiceException("店铺Id不能为空");
        }
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.getRelatedShopById(orderDeliveryReq.getShopId());
        if(CollectionUtils.isEmpty(shopInfoEntityList)){
            throw new ServiceException("店铺不存在或已取消授权");
        }
        ShopInfoEntity shopInfoEntity = shopInfoEntityList.get(0);
        orderDeliveryReq.setErpShopId(shopInfoEntity.getId());
        if(StringUtils.isNotBlank(orderDeliveryReq.getPlatformOrderCode())){
            orderDeliveryReq.setPlatformOrderCodeList(Arrays.asList(StringUtil.split(orderDeliveryReq.getPlatformOrderCode(), ",")));
        }
        if(StringUtils.isNotBlank(orderDeliveryReq.getSellerOrderCode())){
            orderDeliveryReq.setSellerOrderCodeList(Arrays.asList(StringUtil.split(orderDeliveryReq.getSellerOrderCode(), ",")));
        }
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<SoB2cForeignDTO.OrderDeliveryResp> pageData = baseMapper.getForeignOrderDeliveryInfo(query,orderDeliveryReq);
        this.fillDeliveryOrderInfo(pageData.getRecords(),orderDeliveryReq);
        return new PagingVO(pageData);
    }

    private void fillDeliveryOrderInfo(List<SoB2cForeignDTO.OrderDeliveryResp> records,SoB2cForeignDTO.OrderDeliveryReq orderDeliveryReq) {
        if(CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> ids = records.stream().map(SoB2cForeignDTO.OrderDeliveryResp::getId).collect(Collectors.toList());
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(ids);
        List<String> mergeIds = soB2cRefList.stream().filter(v->v.getType().equals(SoB2cOptionTypeEnum.ENUM_MERGE.getCode()) && ids.contains(v.getTargetId())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
        List<String> splitIds = soB2cRefList.stream().filter(v->v.getType().equals(SoB2cOptionTypeEnum.ENUM_SPLIT.getCode()) && ids.contains(v.getTargetId())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
        List<SoB2cEntity> allMergeSob2cList = CollectionUtils.isEmpty(mergeIds)?new ArrayList<>():this.listByIds(mergeIds);
        List<SoB2cEntity> allSplitSob2cList = CollectionUtils.isEmpty(splitIds)?new ArrayList<>():this.listByIds(splitIds);
        List<String> allChannelIds = records.stream().map(SoB2cForeignDTO.OrderDeliveryResp::getLogisticChannelId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> logisticBaseDTOList = CollectionUtils.isEmpty(allChannelIds)?new ArrayList<>(): logisticsFeign.listChannelInfoById(allChannelIds);
        for (SoB2cForeignDTO.OrderDeliveryResp record : records) {
            record.setShopId(orderDeliveryReq.getShopId());
            if(record.getOrderSource().equals("soB2c")){
                record.setOrderSource("thirdPlatform");
            }

            LogisticsChannelDTO.BaseDTO logisticBaseDTO = logisticBaseDTOList.stream().filter(v->v.getId().equals(record.getLogisticChannelId())).findFirst().orElse(new LogisticsChannelDTO.BaseDTO());
            record.setLogisticChannelCode(logisticBaseDTO.getCode());
            record.setLogisticChannelName(logisticBaseDTO.getName());
            //处理拆分合并数据
            if (CollectionUtils.isNotEmpty(soB2cRefList)) {
                //合并
                long mergeCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                        && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(record.getInvalidStatus())
                ).count();
                record.setIsMergeOrder(mergeCount > 0);
                if(record.getIsMergeOrder()){
                    List<String> sourceIds = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                            && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
                    List<SoB2cEntity> mergeSob2cList = allMergeSob2cList.stream().filter(v->sourceIds.contains(v.getId())).collect(Collectors.toList());
                    mergeSob2cList.forEach(v->{
                        SoB2cForeignDTO.MergeOrderInfo mergeOrderInfo = SoB2cForeignDTO.MergeOrderInfo.builder()
                                .mergeChildOrderErpOrderCode(v.getCode())
                                .mergeChildOrderPlatformOrderCode(v.getPlatformCode())
                                .mergeChildOrderSellerOrderCode(v.getSellerOrderCode())
                                .build();
                        if(CollectionUtils.isEmpty(record.getMergeOrderInfoList())){
                            List<SoB2cForeignDTO.MergeOrderInfo> mergeList = new ArrayList<>();
                            mergeList.add(mergeOrderInfo);
                            record.setMergeOrderInfoList(mergeList);
                        }else{
                            record.getMergeOrderInfoList().add(mergeOrderInfo);
                        }
                    });
                }
                //拆分
                long splitCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                        && SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(record.getInvalidStatus())
                ).count();
                record.setIsMergeOrder(splitCount > 0);
                if(record.getIsMergeOrder()){
                    List<String> sourceIds = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(record.getId()))
                            && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())).map(SoB2cRefEntity::getSourceId).collect(Collectors.toList());
                    List<SoB2cEntity> splitSob2cList = allSplitSob2cList.stream().filter(v->sourceIds.contains(v.getId())).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(splitSob2cList)){
                        SoB2cForeignDTO.SplitOrderInfo splitOrderInfo = SoB2cForeignDTO.SplitOrderInfo.builder()
                                .splitParentOrderErpOrderCode(splitSob2cList.get(0).getCode())
                                .splitParentOrderPlatformOrderCode(splitSob2cList.get(0).getPlatformCode())
                                .splitParentOrderSellerOrderCode(splitSob2cList.get(0).getSellerOrderCode())
                                .build();
                        record.setSplitOrderInfo(splitOrderInfo);
                    }
                }
            }
        }
    }

}
