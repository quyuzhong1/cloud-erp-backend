package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.*;
import com.common.business.dto.WdtSoOutStockDetailDTO.PositionDetailsList;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.CurrencyEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.KolSubB2cApplicationDeliveryStatusEnum;
import com.erp.model.oms.enums.KolSubB2cApplicationOrderStatusEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.sys.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.*;
import com.sdk.wangdian.sdk.api.sales.dto.PushSelf2Request;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.core.enums.CountrySiteEnum.CHINA;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputWdtKolB2cApplicationRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
		for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if (CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if ("dmp_so_info".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
						dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
					}
				} else if ("dmp_so_detail".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoDetailEntity dmpSoDetailEntity = (DmpSoDetailEntity) v;
						String mainId = dmpSoDetailEntity.getMainId();
						List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
						if (CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoDetailEntity);
						dmpSoDetailEntityMap.put(mainId, list);
					}
				}
			}
		}

		Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
		Set<String> changeIds = new HashSet<>();
		for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
			if (CollUtil.isNotEmpty(value)) {
				String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
				if ("dmp_so_info".equals(storageName)) {
					for (BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				} else if ("dmp_so_detail".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
						changeIds.add(dmpSoReturnDetailEntity.getMainId());
					}
				}
			}
		}

		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			WdtKolB2cApplicationDTO orderDTO = this.convert(dmpSoInfoEntityMap.get(changId), dmpSoDetailEntityMap.get(changId), cfgOutputId);
			if(orderDTO != null) {
				map.put(changId, JSON.toJSONString(orderDTO));
			}
		}
		return map;
	}

	/**
	 * 解析订单数据
	 **/
	public WdtKolB2cApplicationDTO convert(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntityList , String cfgOutputId) {
		if (this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
			return null;
		}
		if (CollUtil.isEmpty(dmpSoDetailEntityList)) {
			return null;
		}

		WdtKolB2cApplicationDTO wdtKolB2cApplicationDTO = new WdtKolB2cApplicationDTO();
		wdtKolB2cApplicationDTO.setCode(dmpSoInfoEntity.getPlatformCode());
		wdtKolB2cApplicationDTO.setPlatformSoCode(dmpSoInfoEntity.getThirdCode());
		wdtKolB2cApplicationDTO.setPlatformOrderCode(dmpSoInfoEntity.getPlatformCode());

		String orderStatus = dmpSoInfoEntity.getOrderStatus();
		//销售订单关联状态
		if(Objects.equals(orderStatus,"55")){ //已审核
			wdtKolB2cApplicationDTO.setOrderStatus(KolSubB2cApplicationOrderStatusEnum.APPROVE.getCode());
		}else if(Objects.equals(orderStatus,"5")){ //已取消
			wdtKolB2cApplicationDTO.setOrderStatus(KolSubB2cApplicationOrderStatusEnum.NOT.getCode());
		}else if(Objects.equals(orderStatus,"4") || Objects.equals(orderStatus,"6") || Objects.equals(orderStatus,"7")
				|| Objects.equals(orderStatus,"10") || Objects.equals(orderStatus,"12") || Objects.equals(orderStatus,"15")
				|| Objects.equals(orderStatus,"16") || Objects.equals(orderStatus,"19") || Objects.equals(orderStatus,"20")
				|| Objects.equals(orderStatus,"21") || Objects.equals(orderStatus,"23") || Objects.equals(orderStatus,"24")
				|| Objects.equals(orderStatus,"25") || Objects.equals(orderStatus,"27") || Objects.equals(orderStatus,"30")
				|| Objects.equals(orderStatus,"35") || Objects.equals(orderStatus,"40")){
			//其余状态:4 线下退款6 待转预订单(待审核)7 待转已完成10未付款12待尾款15等未付16延时审核19预订单前处理20 审核前处理21自流转待发货23 异常订单24 换货预订单25 待处理预订单27待分配预订单 30待客审35待财审40审核中
			wdtKolB2cApplicationDTO.setOrderStatus(KolSubB2cApplicationOrderStatusEnum.NOTAPPROVE.getCode());
		}

		//发货状态
		if(Objects.equals(orderStatus,"95") || Objects.equals(orderStatus,"96") || Objects.equals(orderStatus,"101") || Objects.equals(orderStatus,"110")){
			//95已发货、96成本确认（待录入计划成本，订单结算时有货品无计划成本）、101已过账、110已完成
			wdtKolB2cApplicationDTO.setDeliveryStatus(KolSubB2cApplicationDeliveryStatusEnum.SHIPPED.getCode());
		} else {
			wdtKolB2cApplicationDTO.setDeliveryStatus(KolSubB2cApplicationDeliveryStatusEnum.WAITSHIPPED.getCode());
		}
		//物流单号
		wdtKolB2cApplicationDTO.setTrackNo(dmpSoInfoEntity.getLogisticsCode());

		List<WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO> details = new ArrayList<>();

		//订单详情
		for (int i = 0; i < dmpSoDetailEntityList.size(); i++) {
			DmpSoDetailEntity dmpSoDetailEntity = dmpSoDetailEntityList.get(i);
			WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO detailDTO = new WdtKolB2cApplicationDTO.WdtKolB2cApplicationDetailDTO();
			detailDTO.setPlatformDetailId(dmpSoDetailEntity.getPlatformDetailId());
			detailDTO.setThirdDetailId(dmpSoDetailEntity.getThirdDetailId());
			detailDTO.setSkuNo(dmpSoDetailEntity.getSkuNo());
			detailDTO.setApplyQty(dmpSoDetailEntity.getQty());
			details.add(detailDTO);
		}
		wdtKolB2cApplicationDTO.setDetails(details);
		return wdtKolB2cApplicationDTO;
	}

	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("code");
    }
}
