package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.service.DictBasicService;
import com.sdk.third.lingxing.dto.OrderFastOutboundPackageDTO;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.TE_MU)
public class TemuShipOrder extends AbstractShipOrder {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        // 查询所有信息
        Tuple tuple = super.allSourceOrderInfo(dto);
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);
        //渠道
        String channelId = logisticsEntity.getLogisticsChannelId();
        //获取中台配置的渠道信息
        ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setSysId(channelId);
        viewParamDTO.setType(ThirdSysTypeEnum.LOGISTICS.getCode());
        ThirdMappingDTO.MappingViewDTO mappingViewDTO = dmpThirdMappingFeign.view(viewParamDTO);
        if (null == mappingViewDTO || CollectionUtils.isEmpty(mappingViewDTO.getThirdList())) {
            throw new ServiceException("找不到渠道信息");
        }
        ThirdMappingDTO.ViewDTO thirdView = mappingViewDTO.getThirdList().stream().filter(v->v.getSysType().equals(PlatformDictEnum.LING_XING.getCode())).findFirst().orElse(null);
        if(Objects.isNull(thirdView)){
            throw new ServiceException("未配置{}渠道信息",PlatformDictEnum.LING_XING.getName());
        }

        List<String> signShippedDetailList = new ArrayList<>();
        List<DictBasicEntity> widList = dictBasicService.getByKey("LingXingWid");
        String wid = CollectionUtils.isNotEmpty(widList)?widList.get(0).getValue():"";
        for (SoB2cEntity mainEntity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> currentDetailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(currentDetailEntityList)) {
                throw new ServiceException(ApiError.SO_B2C_DETAIL_NOT_FOUND);
            }
            if(StringUtils.isBlank(mainEntity.getThirdCode())){
                throw new ServiceException("【TEMU标记发货】操作失败，订单号【{}】领星单号为空",mainEntity.getThirdCode());
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            currentDetailEntityList = currentDetailEntityList.stream()
                    .filter(e -> CharSequenceUtil.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
            List<SoB2cDetailEntity> detailEntityList = super.handleSplit(currentDetailEntityList, dto.isFalseDeliveryFlag()).getDetailList();
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("【TEMU标记发货】订单【{}】所有明细来源ID为空,不请求领星接口", mainEntity.getCode());
                continue;
            }

            //获取渠道标发单号
            String logisticsNo = logisticsEntity.getCode();
            if (CharSequenceUtil.isBlank(logisticsNo)) {
                throw new ServiceException("【TEMU标记发货】操作失败，渠道标发单号为空");
            }
            try {
                OrderFastOutboundPackageDTO.PackageInfo packageInfo = new OrderFastOutboundPackageDTO.PackageInfo();
                packageInfo.setGlobalOrderNo(mainEntity.getThirdCode());
                packageInfo.setLogisticsTypeId(thirdView.getThirdLogisticsId());
                packageInfo.setWaybillNo(logisticsNo);
                packageInfo.setTrackingNo(logisticsEntity.getTrackNo());
                packageInfo.setWid(Long.valueOf(wid));
                LingxingApiUtils.fastOutbound(Collections.singletonList(packageInfo));
                signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            } catch (Exception e) {
                log.error("【TEMU标记发货】销售订单【{}】,平台订单【{}】领星标记发货API提示异常 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("TEMU标记发货失败:" + e.getMessage());
            }
        }
        return signShippedDetailList;
    }


    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        Boolean isCancel = dto.getOldIsCancel();
        if (isCancel) {
            //订单拦截
            soB2cFeign.deliveryIntercept(new SoB2cDTO.RemarkDTO(dto.getSoB2cId(), "平台取消或退款"));
        }
        return isCancel;

    }


    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList) {
        return false;
    }
}
