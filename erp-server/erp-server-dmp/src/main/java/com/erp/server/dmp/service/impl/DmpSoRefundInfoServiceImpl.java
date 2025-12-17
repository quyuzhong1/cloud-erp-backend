package com.erp.server.dmp.service.impl;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

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
import com.erp.model.dmp.dto.DmpSoRefundInfoDTO;
import com.erp.model.dmp.entity.DmpSoRefundDetailEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.model.dmp.gyy.GyyRefundEntity;
import com.erp.model.dmp.gyy.bean.RefundDetailsBean;
import com.erp.server.dmp.mapper.DmpSoRefundInfoMapper;
import com.erp.server.dmp.service.DmpSoRefundDetailService;
import com.erp.server.dmp.service.DmpSoRefundInfoService;
import com.erp.server.dmp.service.OperateLogService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 中台销售退款单主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-12
 */
@Slf4j
@Service
public class DmpSoRefundInfoServiceImpl extends SuperServiceImpl<DmpSoRefundInfoMapper, DmpSoRefundInfoEntity> implements DmpSoRefundInfoService {
    @Autowired
    private OperateLogService operateLogService;
    
    @Autowired
	protected IdentifierGenerator identifierGenerator;
    
    @Resource
    private DmpSoRefundDetailService dmpSoRefundDetailService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoRefundInfoDTO.AddDTO addDTO) {
        DmpSoRefundInfoEntity dmpSoRefundInfoEntity = new DmpSoRefundInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoRefundInfoEntity);

        // 数据处理
        handleData(dmpSoRefundInfoEntity);

        log.info("开始新增中台销售退款单主单");
        boolean save = super.save(dmpSoRefundInfoEntity);
        if(!save) {
            throw new ServiceException("中台销售退款单主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台销售退款单主单" , dmpSoRefundInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpSoRefundInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoRefundInfoEntity.getId(), dmpSoRefundInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoRefundInfoDTO.UpdateDTO updateDTO) {
        DmpSoRefundInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台销售退款单主单"));
        DmpSoRefundInfoEntity dmpSoRefundInfoEntity =  BeanMapperUtils.map(DmpSoRefundInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoRefundInfoEntity);
        log.info("编辑 开始修改中台销售退款单主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoRefundInfoEntity);
        if(!save) {
            throw new ServiceException("中台销售退款单主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台销售退款单主单日志数据，id：【{}】", dmpSoRefundInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoRefundInfoEntity.getId(), "中台销售退款单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpSoRefundInfoEntity, null, dmpSoRefundInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoRefundInfoEntity dmpSoRefundInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Transactional(rollbackFor = Exception.class)
	@Override
	public void addGyyRefundOrder(List<GyyRefundEntity> mongoData) {
		if(CollUtil.isEmpty(mongoData)) {
    		return;
    	}
		List<String> thirdCodes = lambdaQuery().in(DmpSoRefundInfoEntity::getThirdCode, mongoData.stream().map(GyyRefundEntity::getCode).collect(Collectors.toList()))
	    		.select(DmpSoRefundInfoEntity::getThirdCode).list().stream().map(DmpSoRefundInfoEntity::getThirdCode).collect(Collectors.toList());
		List<DmpSoRefundInfoEntity> dmpSoRefundInfoEntityList = new ArrayList<>();
    	List<DmpSoRefundDetailEntity> dmpSoRefundDetailEntityList = new ArrayList<>();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	String inputTaskId = "管易历史数据处理";
    	for (GyyRefundEntity gyyRefundEntity : mongoData) {
    		String code = gyyRefundEntity.getCode();
    		if(thirdCodes.contains(code)) {
    			continue;
    		}
    		
    		DmpSoRefundInfoEntity dmpSoRefundInfoEntity = new DmpSoRefundInfoEntity();
    		dmpSoRefundInfoEntity.setId(identifierGenerator.nextId(dmpSoRefundInfoEntity).toString());
    		dmpSoRefundInfoEntity.setInputTaskId(inputTaskId);
    		String createDate = gyyRefundEntity.getCreateDate();
    		if(StringUtils.isNotBlank(createDate)) {
    			dmpSoRefundInfoEntity.setPlatformCreateTime(LocalDateTime.parse(createDate, formatter));
    		}
    		String modifyDate = gyyRefundEntity.getModifyDate();
    		if(StringUtils.isNotBlank(modifyDate)) {
    			dmpSoRefundInfoEntity.setPlatformUpdateTime(LocalDateTime.parse(modifyDate, formatter));
    		}
    		dmpSoRefundInfoEntity.setRefundTime(dmpSoRefundInfoEntity.getPlatformCreateTime());
    		dmpSoRefundInfoEntity.setSourcePlatform("gyy");
    		dmpSoRefundInfoEntity.setSourceSystem("gyy");
    		dmpSoRefundInfoEntity.setThirdCode(code);
    		dmpSoRefundInfoEntity.setPlatformCode(gyyRefundEntity.getPlatfromCode());
    		dmpSoRefundInfoEntity.setReason(gyyRefundEntity.getReason());
    		dmpSoRefundInfoEntity.setRemark(gyyRefundEntity.getRefundPhase());
    		dmpSoRefundInfoEntity.setStatus("SUCCESS".equals(gyyRefundEntity.getStatus()) ? "1" : "2");
    		dmpSoRefundInfoEntity.setPlatformOriginalStatus(gyyRefundEntity.getStatus());
    		dmpSoRefundInfoEntity.setShopId(gyyRefundEntity.getShopId());
    		dmpSoRefundInfoEntity.setShopName(gyyRefundEntity.getShopCode());
    		dmpSoRefundInfoEntity.setBuyerName(gyyRefundEntity.getVipCode());
    		dmpSoRefundInfoEntity.setCountry("CN");
    		dmpSoRefundInfoEntity.setCurrencyCode("CNY");
    		dmpSoRefundInfoEntity.setCurrencyRate(BigDecimal.ZERO);
    		dmpSoRefundInfoEntity.setAmount(gyyRefundEntity.getAmount());
    		
    		List<RefundDetailsBean> details = gyyRefundEntity.getDetails();
    		for(RefundDetailsBean detail : details) {
    			DmpSoRefundDetailEntity dmpSoRefundDetailEntity = new DmpSoRefundDetailEntity();
    			dmpSoRefundDetailEntity.setMainId(dmpSoRefundInfoEntity.getId());
    			dmpSoRefundDetailEntity.setInputTaskId(inputTaskId);
    			dmpSoRefundDetailEntity.setThirdDetailId(gyyRefundEntity.getRefundCode());
    			dmpSoRefundDetailEntity.setSkuId(detail.getItemId());
    			dmpSoRefundDetailEntity.setSkuNo(detail.getItemCode());
    			dmpSoRefundDetailEntity.setQty(detail.getQty());
    			String amount = detail.getAmount();
    			if(StringUtils.isNotBlank(amount)) {
    				dmpSoRefundDetailEntity.setAmount(new BigDecimal(amount));
    			}
    			dmpSoRefundDetailEntity.setPlatformOrderCode(gyyRefundEntity.getPlatfromCode());
    			
    			dmpSoRefundDetailEntityList.add(dmpSoRefundDetailEntity);
    		}
    		dmpSoRefundInfoEntityList.add(dmpSoRefundInfoEntity);
    	}
    	
    	if(CollUtil.isNotEmpty(dmpSoRefundInfoEntityList)) {
    		this.saveBatch(dmpSoRefundInfoEntityList);
    	}
    	if(CollUtil.isNotEmpty(dmpSoRefundDetailEntityList)) {
    		dmpSoRefundDetailService.saveBatch(dmpSoRefundDetailEntityList);
    	}
	}
}
