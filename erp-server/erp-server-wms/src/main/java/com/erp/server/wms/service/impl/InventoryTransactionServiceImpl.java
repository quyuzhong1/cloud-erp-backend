package com.erp.server.wms.service.impl;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.InventoryTransactionEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryRedisOpEnum;
import com.erp.model.wms.enums.inventory.InventoryRedisOpKeyEnum;
import com.erp.server.wms.config.InventoryTransactionSynchronizationAdapter;
import com.erp.server.wms.mapper.InventoryTransactionMapper;
import com.erp.server.wms.service.InventoryHisService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.InventoryTradingService;
import com.erp.server.wms.service.InventoryTransactionService;
import com.erp.server.wms.service.TransactionFlowService;
import com.erp.server.wms.utils.InventoryRedisUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 库存事务表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-10-13
 */
@Slf4j
@Service
public class InventoryTransactionServiceImpl extends SuperServiceImpl<InventoryTransactionMapper, InventoryTransactionEntity> implements InventoryTransactionService {
    @Autowired
    private InventoryHisService inventoryHisService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryRedisUtil inventoryRedisUtil;
    @Autowired
    private TransactionFlowService transactionFlowService;
    @Resource
    private MQProducerService mqProducerService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void inventoryTransactionToInventoryHis(String inventoryId , int size) {
    	RedissonMultiLock tryLock = inventoryRedisUtil.tryLock(InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.HISTORY, inventoryId));
    	if(tryLock != null) {
    		try {
				List<InventoryTransactionEntity> inventoryTransactionEntityList = lambdaQuery().eq(InventoryTransactionEntity::getInventoryId, inventoryId)
						.orderByAsc(InventoryTransactionEntity::getCreateTime).last(size > 0 , " limit " + size + " ").list();
				if(CollUtil.isNotEmpty(inventoryTransactionEntityList)) {
					Set<String> transactionIdSet = new HashSet<>();
					for(InventoryTransactionEntity inventoryTransactionEntity : inventoryTransactionEntityList) {
						//1、补偿提交redis库存
						String transactionId = inventoryTransactionEntity.getTransactionId();
						if(!transactionIdSet.add(transactionId)) {
							this.commitRedis(inventoryTransactionEntity.getTransactionId());
						}
						//2、更新历史库存
						InventoryTransactionDTO transactionDTO = BeanUtil.copyProperties(inventoryTransactionEntity, InventoryTransactionDTO.class);
						this.updateInventoryHis(transactionDTO);
						//3、更新流水的结余库存
						int lastTransactionInventoryQty = this.getLastTransactionInventoryQty(transactionDTO);
						String flowId = inventoryTransactionEntity.getFlowId();
						boolean updateFlow = transactionFlowService.lambdaUpdate().set(TransactionFlowEntity::getCurInventoryQty, lastTransactionInventoryQty + transactionDTO.getQty())
									.eq(TransactionFlowEntity::getId, flowId).update();
						if(!updateFlow) {
							throw new ServiceException("未更新到库存流水，库存flowId={}" , flowId);
						}
						//4、更新单据日期之后流水的结余库存
						this.updateInventoryTransaction(transactionDTO, inventoryTransactionEntity.getOperationMode().equals(InventoryTradingService.APPROVE));
					}
					//5、更新最新历史库存到即时库存
					this.inventoryHisToInventory(inventoryId);
					//6、删除库存交易
					removeByIds(inventoryTransactionEntityList.stream().map(InventoryTransactionEntity::getId).collect(Collectors.toSet()));
				}
			} catch (Exception e) {
				log.error("迁移历史库存失败" , e);
				sendFeishuMsg(inventoryId, e);
				throw e;
			} finally{
 				inventoryRedisUtil.unLock(tryLock);
 			}
    	}
    }
    
    private void sendFeishuMsg(String inventoryId , Exception e) {
    	WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("预警消息");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle("迁移redis历史库存失败");
        warnMsgInfo.setTableName("inventory_transaction");
        warnMsgInfo.setTableId(inventoryId);
        warnMsgInfo.setKeyInfo(e.getMessage());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
    
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void addInventoryTransaction(List<InventoryTransactionDTO> transactionList, String approveType) {
    	if(CollUtil.isEmpty(transactionList)) {
    		ServiceException.runError("库存流水不能为空");
    	}
    	if(transactionList.stream().anyMatch(t -> StringUtils.isBlank(t.getId()))) {
    		ServiceException.runError("库存流水id不能为空");
    	}
    	if(transactionList.stream().anyMatch(t -> StringUtils.isBlank(t.getInventoryId()))) {
    		ServiceException.runError("即时库存id不能为空");
    	}
    	Set<String> flowIds = transactionList.stream().map(InventoryTransactionDTO::getId).collect(Collectors.toSet());
		List<TransactionFlowEntity> transactionFlowEntityList = transactionFlowService.listByIds(flowIds);
		if(transactionFlowEntityList.size() != flowIds.size()) {
			ServiceException.runError("库存流水缺少");
		}
		String transactionId = "";
		String transactionType = "";
		boolean inGlobalTransaction = RootContext.inGlobalTransaction();
		if(inGlobalTransaction) {
			transactionId = RootContext.getXID();
			transactionType = "global";
		}else {
			transactionId = MDC.get("traceId");
			if(StringUtils.isBlank(transactionId)) {
				transactionId = transactionFlowEntityList.get(0).getId();
				MDC.put("traceId", transactionId);
			}
			transactionType = "local";
		}
		
		this.tryRedis(transactionId , transactionList);
		
		List<InventoryTransactionEntity> inventoryTransactionEntityList = new ArrayList<>();
		for(TransactionFlowEntity transactionFlowEntity : transactionFlowEntityList) {
			InventoryTransactionEntity inventoryTransactionEntity = BeanUtil.copyProperties(transactionFlowEntity, InventoryTransactionEntity.class, 
					InventoryTransactionEntity.FIELD_ID,
					InventoryTransactionEntity.CREATE_TIME,
					InventoryTransactionEntity.UPDATE_TIME,
					InventoryTransactionEntity.FIELD_VERSION,
					InventoryTransactionEntity.IS_DELETED,
					InventoryTransactionEntity.UPDATE_USER_ID,
					InventoryTransactionEntity.UPDATE_USER_NAME
					);
			inventoryTransactionEntity.setFlowId(transactionFlowEntity.getId());
			inventoryTransactionEntity.setTransactionId(transactionId);
			inventoryTransactionEntity.setTransactionType(transactionType);
			inventoryTransactionEntity.setOperationMode(approveType);
			inventoryTransactionEntityList.add(inventoryTransactionEntity);
		}
		this.saveBatch(inventoryTransactionEntityList);
		InventoryTransactionSynchronizationAdapter.register(transactionId);
	}

    @Override
    public void tryRedis(String transactionId , List<InventoryTransactionDTO> transactionList) {
    	Map<String, List<InventoryTransactionDTO>> inventoryIdMaps = transactionList.stream().collect(Collectors.groupingBy(InventoryTransactionDTO::getInventoryId));
    	List<String> transactionRedisParam = new ArrayList<>();
    	for(Map.Entry<String, List<InventoryTransactionDTO>> inventoryIdMap : inventoryIdMaps.entrySet()) {
    		List<InventoryTransactionDTO> value = inventoryIdMap.getValue();
    		Integer totalQty = value.stream().map(InventoryTransactionDTO::getQty).reduce(Integer::sum).orElse(0);
    		if(totalQty != 0) {
    			StringBuilder sb = new StringBuilder();
    			InventoryTransactionDTO transactionDTO = value.get(0);
    			sb.append(transactionDTO.getInventoryId());
    			sb.append(InventoryRedisUtil.atSign);
    			sb.append(totalQty);
    			if(!transactionDTO.isAllowNegativeInventory()) {
    				sb.append(InventoryRedisUtil.atSign);
    				sb.append(CharSequenceUtil.format("库存不足：sku=[{}],仓库=[{}],仓位=[{}],库存状态=[{}],库存:{},交易数:{},缺少数：{}\n"
                            , transactionDTO.getSkuNo()
                            , transactionDTO.getWarehouseName()
                            , transactionDTO.getWarehouseLocationName()
                            , transactionDTO.getInventoryStatusName()
                            , "ss1ss"
                            , totalQty
                            , "ss2ss"));
    			}
    			transactionRedisParam.add(sb.toString());
    		}
    	}
    	if(CollUtil.isNotEmpty(transactionRedisParam)) {
    		inventoryRedisUtil.execute(InventoryRedisOpEnum.TRY , transactionId  , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.OVERRIDE, ""),
    				InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, ""),
    				InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.TRANSACTION, ""),
    				transactionRedisParam.stream().collect(Collectors.joining(InventoryRedisUtil.splitSign)));
    	}
    }
    
	@Override
	public void commitRedis(String transactionId) {
		if(StringUtils.isBlank(transactionId)) {
			log.error("提交redis库存事务transactionId不能为空");
			throw new ServiceException("提交redis库存事务transactionId不能为空");
		}
		Integer count = lambdaQuery().eq(InventoryTransactionEntity::getTransactionId, transactionId).count();
		if(count == null || count == 0) {
			throw new ServiceException("没有库存交易记录，不允许提交redis库存transactionId={}" , transactionId);
		}
		InventoryRedisOpEnum commit = InventoryRedisOpEnum.COMMIT;
		inventoryRedisUtil.execute(commit , commit.getCode() , transactionId , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.TRANSACTION, transactionId) 
				, InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, transactionId));
	}

	@Override
	public void rollbackRedis(String transactionId) {
		if(StringUtils.isBlank(transactionId)) {
			log.error("回滚redis库存事务transactionId不能为空");
			throw new ServiceException("回滚redis库存事务transactionId不能为空");
		}
		Integer count = lambdaQuery().eq(InventoryTransactionEntity::getTransactionId, transactionId).count();
		if(count != null && count > 0) {
			throw new ServiceException("存在库存交易记录，不允许回滚redis库存transactionId={}" , transactionId);
		}
		InventoryRedisOpEnum rollback = InventoryRedisOpEnum.ROLLBACK;
		inventoryRedisUtil.execute(rollback , rollback.getCode() , transactionId , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.TRANSACTION, transactionId) 
				, InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, transactionId));
	}
	
	/**
     * 更新库存历史
     * @param transactionDTO    库存交易信息
     */
    private void updateInventoryHis(InventoryTransactionDTO transactionDTO) {
        if(null == transactionDTO.getInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getWarehouseLocationName(),transactionDTO.getInventoryStatusName());
        }

        // 查询当天历史库存
        saveInventoryCurrentday(transactionDTO);

        // 更新当天之后的历史库存
        LambdaUpdateWrapper<InventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("qty = qty + " + transactionDTO.getQty())
                .set(InventoryHisEntity::getUpdateTime, LocalDateTime.now())
                .set(InventoryHisEntity::getUpdateUserId, transactionDTO.getUserId())
                .set(InventoryHisEntity::getUpdateUserName, transactionDTO.getUserName())
                //条件
                .eq(InventoryHisEntity::getInfoId, transactionDTO.getInventoryId())
                .gt(InventoryHisEntity::getBillDate, transactionDTO.getBillDate());

        inventoryHisService.update(wrapper);
    }
	
	/**
     * 保存当天历史库存
     * @param transactionDTO    库存交易信息
     */
    private void saveInventoryCurrentday(InventoryTransactionDTO transactionDTO) {
        InventoryHisEntity inventoryHis = this.queryInventoryHisLast(transactionDTO.getInventoryId(), transactionDTO.getBillDate(),true);

        if(null == inventoryHis) {
            // 查询当天以前的库存
            inventoryHis = this.queryInventoryHisLast(transactionDTO.getInventoryId(), transactionDTO.getBillDate(),false);
            int inventoryQty = (null == inventoryHis) ? 0 : inventoryHis.getQty();

            inventoryHis = new InventoryHisEntity();
            inventoryHis.setInfoId(transactionDTO.getInventoryId());
            inventoryHis.setBillDate(transactionDTO.getBillDate());
            inventoryHis.setQty(inventoryQty+ transactionDTO.getQty());
            inventoryHis.setCreateUserId(transactionDTO.getUserId());
            inventoryHis.setCreateUserName(transactionDTO.getUserName());
            inventoryHis.setCreateTime(LocalDateTime.now());
            inventoryHis.setUpdateUserId(transactionDTO.getUserId());
            inventoryHis.setUpdateUserName(transactionDTO.getUserName());
            inventoryHis.setUpdateTime(LocalDateTime.now());

            inventoryHisService.save(inventoryHis);
        }else {
            // 更新当天历史库存
            LambdaUpdateWrapper<InventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.setSql("qty = qty + " + transactionDTO.getQty())
                    .set(InventoryHisEntity::getUpdateTime, LocalDateTime.now())
                    .set(InventoryHisEntity::getUpdateUserId, transactionDTO.getUserId())
                    .set(InventoryHisEntity::getUpdateUserName, transactionDTO.getUserName())
                    //条件
                    .eq(InventoryHisEntity::getId, inventoryHis.getId());

            inventoryHisService.update(wrapper);
        }
    }
    
    /**
     * 查询库存历史
     * @param inventoryId   库存id
     * @param billDate      交易日期
     * @param isOnlyCurrBillDate   是否只查询当天的历史
     * @return  库存历史
     */
    private InventoryHisEntity queryInventoryHisLast(String inventoryId, LocalDate billDate, boolean isOnlyCurrBillDate) {
        LambdaQueryWrapper<InventoryHisEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(InventoryHisEntity::getInfoId, inventoryId)
                .orderByDesc(InventoryHisEntity::getBillDate)
                .orderByDesc(InventoryHisEntity::getId)
                .last("limit 1");
        if(isOnlyCurrBillDate) {
            queryWrapper.eq(InventoryHisEntity::getBillDate, billDate);
        }else{
            queryWrapper.lt(InventoryHisEntity::getBillDate, billDate);
        }
        return inventoryHisService.getOne(queryWrapper);

    }
    
    /**
     * 最新历史库存同步即时库存
     * @param inventoryId
     */
    private void inventoryHisToInventory(String inventoryId) {
    	InventoryHisEntity inventoryHisEntity = inventoryHisService.findLastInventory(inventoryId, LocalDate.now());
		inventoryService.lambdaUpdate().set(InventoryEntity::getQty, inventoryHisEntity.getQty()).eq(InventoryEntity::getId, inventoryHisEntity.getInfoId()).update();
    }
    
    /**
     * 获取最后一次交易记录的剩余库存数量
     * @param transactionDTO   交易记录
     * @return  最后一次交易记录的剩余库存数量
     */
    private int getLastTransactionInventoryQty(InventoryTransactionDTO transactionDTO) {
        int initInventoryQty = 0;
        LambdaQueryWrapper<TransactionFlowEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(TransactionFlowEntity::getInventoryId, transactionDTO.getInventoryId())
                .le(TransactionFlowEntity::getBillDate, transactionDTO.getBillDate())
                .eq(TransactionFlowEntity::getIsUnapproved, false)
                .orderByDesc(TransactionFlowEntity::getBillDate)
                .orderByDesc(TransactionFlowEntity::getId)
                .last("limit 1");

        TransactionFlowEntity transactionFlow = transactionFlowService.getOne(queryWrapper);
        return null == transactionFlow ? initInventoryQty : transactionFlow.getCurInventoryQty();
    }
    
    /**
     * 更新库存交易 的库存数量
     * @param transactionDTO    库存交易信息
     * @param isApprove     是否审批
     */
    private void updateInventoryTransaction(InventoryTransactionDTO transactionDTO,boolean isApprove) {
        if(null == transactionDTO.getInventoryId()) {
            ServiceException.runError("sku:[{}]仓库:[{}]仓位:[{}]库存状态：[{}],inventory_id为空,请让【实施工程师】协调开发人员处理",
                    transactionDTO.getSkuNo(),transactionDTO.getWarehouseName(),transactionDTO.getWarehouseLocationName(),transactionDTO.getInventoryStatusName());
        }
        // 日期大于当前单据日期的流水更新
        LambdaUpdateWrapper<TransactionFlowEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.setSql("cur_inventory_qty = cur_inventory_qty + " + transactionDTO.getQty())
                .set(TransactionFlowEntity::getUpdateTime, LocalDateTime.now())
                //条件
                .eq(TransactionFlowEntity::getInventoryId, transactionDTO.getInventoryId())
                .gt(TransactionFlowEntity::getBillDate, transactionDTO.getBillDate());
        transactionFlowService.update(wrapper);

        if(!isApprove) {
            // 反审核如果当天存在晚于当前流水创建的流水,需要进行流水重算
            LambdaUpdateWrapper<TransactionFlowEntity> wrapperToday = new LambdaUpdateWrapper<>();
            wrapperToday.setSql("cur_inventory_qty = cur_inventory_qty + " + transactionDTO.getQty())
                    .set(TransactionFlowEntity::getUpdateTime, LocalDateTime.now())
                    //条件
                    .eq(TransactionFlowEntity::getInventoryId, transactionDTO.getInventoryId())
                    .eq(TransactionFlowEntity::getBillDate, transactionDTO.getBillDate())
                    .gt(TransactionFlowEntity::getId, transactionDTO.getId());
            transactionFlowService.update(wrapperToday);
        }
    }

    private static final Map<String, Date> rollbackTimeMap = new HashMap<>();
    
	@Override
	public void inventoryCheckRollback(int timeout) {
		Collection<String> keys = inventoryRedisUtil.keys(InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.TRANSACTION, "*"));
		if(CollUtil.isNotEmpty(keys)) {
			Set<String> transactions = keys.stream().map(k -> {
				String[] split = k.split(":");
				return split[split.length - 1];
			}).collect(Collectors.toSet());
			Set<String> dbTransactions = lambdaQuery().in(InventoryTransactionEntity::getId, transactions)
					.select(InventoryTransactionEntity::getId).list()
					.stream().map(InventoryTransactionEntity::getId).collect(Collectors.toSet());
			transactions.removeIf(dbTransactions::contains);
			if(CollUtil.isNotEmpty(transactions)) {
				transactions.forEach(t -> {
					Date date = rollbackTimeMap.get(t);
					if(date == null) {
						rollbackTimeMap.put(t, new Date());
					}else {
						if(DateUtil.offsetSecond(date, timeout).after(new Date())) {
							try {
								this.rollbackRedis(t);
							} catch (Exception e) {
								log.error("检查redis自动回滚执行失败：{}" , t , e);
							}
						}
					}
				});
			}
		}
	}
}
