package com.erp.server.dmp.service.impl;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpSoReturnInfoDTO;
import com.erp.model.dmp.dto.DmpSoReturnInfoDTO.AddGyyReturnOrderDTO;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.GyyReturnOrderEntity;
import com.erp.model.dmp.gyy.bean.ReturnOrderDetailsBean;
import com.erp.server.dmp.mapper.DmpSoReturnInfoMapper;
import com.erp.server.dmp.service.DmpSoReturnDetailService;
import com.erp.server.dmp.service.DmpSoReturnInfoService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 销售退货订单主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
 */
@Slf4j
@Service
public class DmpSoReturnInfoServiceImpl extends SuperServiceImpl<DmpSoReturnInfoMapper, DmpSoReturnInfoEntity> implements DmpSoReturnInfoService {

	@Resource
	private MongoService mongoService;
	@Resource
	private DmpSoReturnDetailService dmpSoReturnDetailService;
	
	@Autowired
	protected IdentifierGenerator identifierGenerator;
	
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoReturnInfoDTO.AddDTO addDTO) {
        DmpSoReturnInfoEntity dmpSoReturnInfoEntity = new DmpSoReturnInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoReturnInfoEntity);

        // 数据处理
        handleData(dmpSoReturnInfoEntity);

        log.info("开始新增销售退货订单主单");
        boolean save = super.save(dmpSoReturnInfoEntity);
        if(!save) {
            throw new ServiceException("销售退货订单主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销售退货订单主单" , dmpSoReturnInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoReturnInfoEntity.getId(), dmpSoReturnInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoReturnInfoDTO.UpdateDTO updateDTO) {
        DmpSoReturnInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销售退货订单主单"));
        DmpSoReturnInfoEntity dmpSoReturnInfoEntity =  BeanMapperUtils.map(DmpSoReturnInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoReturnInfoEntity);
        log.info("编辑 开始修改销售退货订单主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoReturnInfoEntity);
        if(!save) {
            throw new ServiceException("销售退货订单主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销售退货订单主单日志数据，id：【{}】", dmpSoReturnInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoReturnInfoEntity.getId(), "销售退货订单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoReturnInfoEntity dmpSoReturnInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Transactional(rollbackFor = Exception.class)
	@Override
	public void addGyyReturnOrder(List<GyyReturnOrderEntity> mongoData) {
    	if(CollUtil.isEmpty(mongoData)) {
    		return;
    	}
    	
    	List<String> thirdCodes = lambdaQuery().in(DmpSoReturnInfoEntity::getThirdCode, mongoData.stream().map(GyyReturnOrderEntity::getCode).collect(Collectors.toList()))
    		.select(DmpSoReturnInfoEntity::getThirdCode).list().stream().map(DmpSoReturnInfoEntity::getThirdCode).collect(Collectors.toList());
    	
    	List<DmpSoReturnInfoEntity> dmpSoReturnInfoEntityList = new ArrayList<>();
    	List<DmpSoReturnDetailEntity> dmpSoReturnDetailEntityList = new ArrayList<>();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	String inputTaskId = "管易历史数据处理";
    	for (GyyReturnOrderEntity gyyReturnOrderEntity : mongoData) {
    		String code = gyyReturnOrderEntity.getCode();
    		if(thirdCodes.contains(code)) {
    			continue;
    		}
    		
    		DmpSoReturnInfoEntity dmpSoReturnInfoEntity = new DmpSoReturnInfoEntity();
    		dmpSoReturnInfoEntity.setInputTaskId(inputTaskId);
    		dmpSoReturnInfoEntity.setId(identifierGenerator.nextId(dmpSoReturnInfoEntity).toString());
    		String createDate = gyyReturnOrderEntity.getCreateDate();
    		if(StringUtils.isNotBlank(createDate)) {
    			dmpSoReturnInfoEntity.setPlatformCreateTime(LocalDateTime.parse(createDate, formatter));
    		}
    		String modifyDate = gyyReturnOrderEntity.getModifyDate();
    		if(StringUtils.isNotBlank(modifyDate)) {
    			dmpSoReturnInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(modifyDate, formatter));
    		}
    		String receiveDate = gyyReturnOrderEntity.getReceiveDate();
    		if(StringUtils.isNotBlank(receiveDate)) {
    			dmpSoReturnInfoEntity.setReturnTime(LocalDateTime.parse(receiveDate, formatter));
    		}
    		dmpSoReturnInfoEntity.setSourceSystem("gyy");
    		dmpSoReturnInfoEntity.setThirdCode(code);
    		dmpSoReturnInfoEntity.setPlatformCode(gyyReturnOrderEntity.getPlatformCode());
    		dmpSoReturnInfoEntity.setShopId(gyyReturnOrderEntity.getShopCode());
    		dmpSoReturnInfoEntity.setShopName(gyyReturnOrderEntity.getShopName());
    		if(gyyReturnOrderEntity.getCancel()) {
    			dmpSoReturnInfoEntity.setStatus("5");
    		}else {
    			dmpSoReturnInfoEntity.setStatus("4");
    		}
    		dmpSoReturnInfoEntity.setPlatformStatus(gyyReturnOrderEntity.getPlatformStatus());
    		dmpSoReturnInfoEntity.setCountry("CN");
    		dmpSoReturnInfoEntity.setBuyerName(gyyReturnOrderEntity.getReceiverName());
    		dmpSoReturnInfoEntity.setRemark(gyyReturnOrderEntity.getReason());
    		dmpSoReturnInfoEntity.setCurrencyCode("CNY");
    		dmpSoReturnInfoEntity.setExchangeRate(BigDecimal.ONE);
    		dmpSoReturnInfoEntity.setSalesManName(gyyReturnOrderEntity.getBusinessMan());
    		dmpSoReturnInfoEntity.setThirdBillNo(gyyReturnOrderEntity.getPlatformRefundId());
    		String approveDate = gyyReturnOrderEntity.getApproveDate();
    		if(StringUtils.isNotBlank(approveDate)) {
    			dmpSoReturnInfoEntity.setBillDate(LocalDateTime.parse(approveDate, formatter));
    		}
    		dmpSoReturnInfoEntity.setStockinStatus(gyyReturnOrderEntity.getReceive());
    		dmpSoReturnInfoEntity.setLogisticsSupplierCode(gyyReturnOrderEntity.getExpressCode());
    		dmpSoReturnInfoEntity.setLogisticsSupplierName(gyyReturnOrderEntity.getExpressName());
    		dmpSoReturnInfoEntity.setTrackingNumber(gyyReturnOrderEntity.getExpressNum());
    		Integer refundType = gyyReturnOrderEntity.getRefundType();
    		if(refundType != null) {
    			dmpSoReturnInfoEntity.setReturnMethod(refundType.toString());
    		}
    		
    		BigDecimal allAmount = BigDecimal.ZERO;
    		
    		List<ReturnOrderDetailsBean> details = gyyReturnOrderEntity.getDetails();
    		for(ReturnOrderDetailsBean detail : details) {
    			DmpSoReturnDetailEntity dmpSoReturnDetailEntity = new DmpSoReturnDetailEntity();
    			dmpSoReturnDetailEntity.setMainId(dmpSoReturnInfoEntity.getId());
    			dmpSoReturnDetailEntity.setInputTaskId(inputTaskId);
    			Long detailId = detail.getId();
    			if(detailId != null) {
    				dmpSoReturnDetailEntity.setThirdDetailId(detailId.toString());
    			}
    			dmpSoReturnDetailEntity.setReason(detail.getNote());
    			dmpSoReturnDetailEntity.setSkuId(detail.getItemId());
    			dmpSoReturnDetailEntity.setSkuNo(detail.getItemCode());
    			dmpSoReturnDetailEntity.setSkuName(detail.getItemName());
    			dmpSoReturnDetailEntity.setQty(detail.getQty());
    			String price = detail.getPrice();
    			if(StringUtils.isNotBlank(price)) {
    				dmpSoReturnDetailEntity.setSellPrice(new BigDecimal(price));
    			}
    			BigDecimal amount = detail.getAmount();
    			dmpSoReturnDetailEntity.setAmount(amount);
    			if(amount != null) {
    				allAmount = allAmount.add(amount);
    			}
    			dmpSoReturnDetailEntity.setIsGift(detail.getIsGift() == 1);
    			dmpSoReturnDetailEntity.setWarehouseId(gyyReturnOrderEntity.getWarehouseinCode());
    			dmpSoReturnDetailEntity.setWarehouseName(gyyReturnOrderEntity.getWarehouseinName());
    			dmpSoReturnDetailEntity.setThirdOrderCode(gyyReturnOrderEntity.getOrderCode());
    			dmpSoReturnDetailEntity.setPlatformOrderCode(detail.getPlatformCode());
    			dmpSoReturnDetailEntity.setCurrency("CNY");
    			if(refundType != null) {
        			dmpSoReturnDetailEntity.setSolutionType(refundType.toString());
        			dmpSoReturnDetailEntity.setReturnOriginalType(refundType.toString());
        		}
    			
    			dmpSoReturnDetailEntityList.add(dmpSoReturnDetailEntity);
    		}
    		
    		dmpSoReturnInfoEntity.setAllAmount(allAmount);
    		dmpSoReturnInfoEntityList.add(dmpSoReturnInfoEntity);
    	}
    	if(CollUtil.isNotEmpty(dmpSoReturnInfoEntityList)) {
    		this.saveBatch(dmpSoReturnInfoEntityList);
    	}
    	if(CollUtil.isNotEmpty(dmpSoReturnDetailEntityList)) {
    		dmpSoReturnDetailService.saveBatch(dmpSoReturnDetailEntityList);
    	}
	}


	@Override
	public void sdyReturnOrderUpdate() {
		List<DmpSoReturnInfoEntity> tikTokList = this.lambdaQuery().eq(DmpSoReturnInfoEntity::getSourceSystem, PlatformDictEnum.TIK_TOK.getCode()).list();
		List<String> tikTokReturnIdList = tikTokList.stream().map(req -> req.getThirdCode()).distinct().collect(Collectors.toList());

		List<ParamData> tikTokParamDataList = new ArrayList<>();
		tikTokParamDataList.add(new ParamData("returnId", "returnId", PannoEnum.IN, tikTokReturnIdList));
		List<Map<String, Object>> tikTokFindMongoData = mongoService.findMongoData(tikTokParamDataList, "TikTok_returnOrder_data");
		if (CollUtil.isEmpty(tikTokFindMongoData)) {
			return;
		}
		for (Map<String, Object> findMongoDatum : tikTokFindMongoData) {
			DmpSoReturnInfoEntity soReturnInfoEntity = tikTokList.stream().filter(req -> req.getThirdCode().equals(String.valueOf(findMongoDatum.get("returnId")))).findFirst().orElse(null);
			if (ObjectUtil.isNotEmpty(soReturnInfoEntity)) {
				this.lambdaUpdate()
						.set(DmpSoReturnInfoEntity::getPlatformOrderCode, String.valueOf(findMongoDatum.get("orderId")))
						.eq(DmpSoReturnInfoEntity::getId, soReturnInfoEntity.getId())
						.update();
			}
		}


		List<DmpSoReturnInfoEntity> list = this.lambdaQuery().eq(DmpSoReturnInfoEntity::getSourceSystem, PlatformDictEnum.MERCADOLIBRE.getCode()).list();
		List<String> orderIdList = list.stream().map(req -> req.getThirdCode()).distinct().collect(Collectors.toList());

		List<ParamData> paramDataList = new ArrayList<>();
		paramDataList.add(new ParamData("fid", "fid", PannoEnum.IN, orderIdList));
		List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "mercadolibre_return_data");
		if (CollUtil.isEmpty(findMongoData)) {
			return;
		}
		for (Map<String, Object> findMongoDatum : findMongoData) {
			DmpSoReturnInfoEntity soReturnInfoEntity = list.stream().filter(req -> req.getThirdCode().equals(String.valueOf(findMongoDatum.get("fid")))).findFirst().orElse(null);
			if (ObjectUtil.isNotEmpty(soReturnInfoEntity)) {
				this.lambdaUpdate()
						.set(DmpSoReturnInfoEntity::getPlatformOrderCode, String.valueOf(findMongoDatum.get("resourceId")))
						.eq(DmpSoReturnInfoEntity::getId, soReturnInfoEntity.getId())
						.update();
			}
		}

	}
}
