package com.erp.server.wms.service.impl;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.StringUtil;
import com.common.core.exception.ServiceException;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.wms.dto.InventoryTransactionDTO.CheckInventoryDTO;
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
import cn.hutool.core.lang.Pair;
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
    @Resource
    @Qualifier("transactionIdToInventoryHisPool")
    private ExecutorService transactionIdToInventoryHisPool;

    @Override
	public Map<String , Boolean> overrideDbInventory(LocalDate startDate , List<String> inventoryIds){
    	Map<String , Boolean> result = new HashMap<>();
    	List<CheckInventoryDTO> checkInventoryList = this.checkDbInventorySame(inventoryIds);
    	if(CollUtil.isNotEmpty(checkInventoryList)) {
    		List<Future<Pair<String, Boolean>>> futureList = new ArrayList<>(checkInventoryList.size());
    		for(CheckInventoryDTO dto : checkInventoryList) {
    			String inventoryId = dto.getInventoryId();
    			result.put(inventoryId, Boolean.FALSE);
    			futureList.add(transactionIdToInventoryHisPool.submit(() -> {
    				String logMsg = StringUtil.appendLogMsg("overrideDbInventory循环", inventoryId);
    				log.info("{}开始" , logMsg);
    				try {
    					Pair<String, Boolean> overrideDb = ApplicationContextUtils.getBean(InventoryTransactionService.class).overrideDb(startDate , inventoryId);
    					log.info("{}结束" , logMsg);
						return overrideDb;
					} catch (Exception e) {
						log.error("{}失败" , logMsg , e);
					}
    				return Pair.of(inventoryId, Boolean.FALSE);
    			}));
    		}
    		for(Future<Pair<String, Boolean>> future : futureList) {
    			try {
    				Pair<String, Boolean> pair = future.get();
					result.put(pair.getKey(), pair.getValue());
				} catch (Exception e) {
					log.error("overrideDbInventory获取结果失败" , e);
				}
    		}
    	}
    	return result;
	}
    
    @Transactional(rollbackFor = Exception.class)
    public Pair<String, Boolean> overrideDb(LocalDate startDate , String inventoryId){
    	transactionFlowService.overrideInventoryFlow(startDate, inventoryId, "");
		this.inventoryHisToInventory(inventoryId);
		return Pair.of(inventoryId, Boolean.TRUE);
    }

	@Override
	public Map<String , Boolean> overrideRedisInventory(List<String> inventoryIds) {
		Map<String , Boolean> result = new HashMap<>();
		List<CheckInventoryDTO> redisCheckInventoryList = this.checkRedisInventorySame(inventoryIds);
		if(CollUtil.isNotEmpty(redisCheckInventoryList)) {
			List<Future<Pair<String, Boolean>>> futureList = new ArrayList<>(redisCheckInventoryList.size());
			for(CheckInventoryDTO dto : redisCheckInventoryList) {
				String inventoryId = dto.getInventoryId();
				futureList.add(transactionIdToInventoryHisPool.submit(() -> {
					String logMsg = StringUtil.appendLogMsg("overrideRedisInventory循环", inventoryId);
    				log.info("{}开始" , logMsg);
    				try {
    					return overrideRedis(inventoryId);
					} catch (Exception e) {
						log.error("{}失败" , logMsg , e);
					}
    				log.info("{}结束" , logMsg);
    				return Pair.of(inventoryId, Boolean.FALSE);
				}));
			}
			for(Future<Pair<String, Boolean>> future : futureList) {
    			try {
    				Pair<String, Boolean> pair = future.get();
					result.put(pair.getKey(), pair.getValue());
				} catch (Exception e) {
					log.error("overrideRedisInventory获取结果失败" , e);
				}
    		}
		}
		return result;
	}
	
	private Pair<String, Boolean> overrideRedis(String id){
		Pair<String, Boolean> of = Pair.of(id, Boolean.TRUE);
		 RedissonMultiLock tryLock = inventoryRedisUtil.tryLock(InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.OVERRIDE, id));
		 if(tryLock != null) {
			 try {
				 int i = 0;
				 while(i < 3) {
					 try {
						 this.inventoryIdToInventoryHis(id , "");
						 Integer qty = 0;
						 QueryWrapper<TransactionFlowEntity> queryWrapper = new QueryWrapper<>();
						 queryWrapper.eq("inventory_id", id);
						 queryWrapper.groupBy("inventory_id");
						 queryWrapper.select(" sum(qty) qty ");
						 List<TransactionFlowEntity> transactionFlowEntityList = transactionFlowService.list(queryWrapper);
						 if(CollUtil.isNotEmpty(transactionFlowEntityList)) {
							 qty = transactionFlowEntityList.get(0).getQty();
						 }
						 inventoryRedisUtil.execute(InventoryRedisOpEnum.OVERRIDE , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, id) , qty.toString());
						 break;
					 } catch (Exception e) {
						 log.error("{}库存重算第{}次失败" , id , i , e);
						 of = Pair.of(id, Boolean.FALSE);
					 }
					 i = i + 1;
				 }
			 }catch (Exception e) {
				 log.error("{}库存重算最终失败" , id , e);
				 of = Pair.of(id, Boolean.FALSE);
			 } finally{
				 inventoryRedisUtil.unLock(tryLock);
			 }
		 }else {
			 log.error("{}库存重算获取锁失败" , id);
			 of = Pair.of(id, Boolean.FALSE);
		 }
		return of;
	}
    
    @Override
    public void inventoryIdToInventoryHis(String inventoryId  , String transactionId) {
    	String logMsg = StringUtil.appendLogMsg("inventoryIdToInventoryHis", inventoryId , transactionId);
    	log.info("{}开始" , logMsg);
    	String key = InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.HISTORY, inventoryId);
    	RedissonMultiLock tryLock = inventoryRedisUtil.tryLock(key , 5);
    	if(tryLock != null) {
    		try {
    			ApplicationContextUtils.getBean(InventoryTransactionService.class).innerInventoryIdToInventoryHis(inventoryId , transactionId);
    		} catch (Exception e) {
    			log.error("{}失败" , logMsg , e);
    			sendFeishuMsg("迁移redis历史库存失败" , inventoryId, e.getMessage());
    			throw e;
    		} finally{
    			inventoryRedisUtil.unLock(tryLock);
    		}
    	}else {
    		log.error("{}正在迁移中" , logMsg);
    		ServiceException.runError(logMsg + "正在迁移中");
    	}
    	log.info("{}结束" , logMsg);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void innerInventoryIdToInventoryHis(String inventoryId , String transactionId) {
		List<InventoryTransactionEntity> inventoryTransactionEntityList = lambdaQuery().eq(InventoryTransactionEntity::getInventoryId, inventoryId)
				.orderByAsc(InventoryTransactionEntity::getCreateTime).list();
		if(CollUtil.isNotEmpty(inventoryTransactionEntityList)) {
			//1、补偿提交redis库存
			Set<String> transactionIdSet = inventoryTransactionEntityList.stream()
					.map(InventoryTransactionEntity::getTransactionId)
					.filter(t -> !t.equals(transactionId))
					.collect(Collectors.toSet());
			transactionIdSet.forEach(t -> this.commitRedis(t , false));
			
			Integer allTotalQty = 0;
			//合并单据日期统一处理
			Map<LocalDate, List<InventoryTransactionEntity>> billDateEntityMaps = inventoryTransactionEntityList.stream().collect(Collectors.groupingBy(InventoryTransactionEntity::getBillDate));
			for(Map.Entry<LocalDate, List<InventoryTransactionEntity>> billDateEntityMap : billDateEntityMaps.entrySet()) {
				List<InventoryTransactionEntity> value = billDateEntityMap.getValue();
				Integer totalQty = value.stream().map(InventoryTransactionEntity::getQty).reduce(Integer::sum).orElse(0);
				allTotalQty = allTotalQty + totalQty;
				LocalDate billDate = billDateEntityMap.getKey();
				InventoryTransactionEntity v = value.get(0);
				//2、更新历史库存
				this.updateInventoryHis(inventoryId , billDate , totalQty , v.getUpdateUserId() , v.getUpdateUserName());
				//3、更新单据日期之后流水的结余库存
				this.updateInventoryTransaction(inventoryId, billDate , totalQty);
				//4、重算当天流水结余库存
				this.updateCurrInventoryTransaction(inventoryId, billDate);
			}
			if(allTotalQty != 0) {
				//5、更新最新历史库存到即时库存
				this.inventoryHisToInventory(inventoryId);
			}
			//6、删除库存交易
			removeByIds(inventoryTransactionEntityList.stream().map(InventoryTransactionEntity::getId).collect(Collectors.toSet()));
		}
    }
    
    @Override
    public void transactionIdToInventoryHis(String transactionId) {
    	String logMsg = StringUtil.appendLogMsg("transactionIdToInventoryHis", transactionId);
    	log.info("{}开始" , logMsg);
    	List<InventoryTransactionEntity> list = lambdaQuery().eq(InventoryTransactionEntity::getTransactionId, transactionId)
            	.last(" group by inventory_id ")
            	.select(InventoryTransactionEntity::getInventoryId)
            	.list();
    	if(CollUtil.isNotEmpty(list)) {
    		for(InventoryTransactionEntity l : list) {
    			String inventoryId = l.getInventoryId();
    			String forLogMsg = StringUtil.appendLogMsg("transactionIdToInventoryHis循环", transactionId , inventoryId);
    			log.info("{}开始" , forLogMsg);
    			try {
					ApplicationContextUtils.getBean(InventoryTransactionService.class).inventoryIdToInventoryHis(inventoryId , transactionId);
				} catch (Exception e) {
					log.error("{}失败" , forLogMsg , e);
				}
    			log.info("{}结束" , forLogMsg);
    		}
    	}
    	log.info("{}结束" , logMsg);
    }
    
    private void sendFeishuMsg(String title , String tableId , String keyInfo) {
    	WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("预警消息");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_WMS);
        warnMsgInfo.setTitle(title);
        warnMsgInfo.setTableName("inventory_transaction");
        warnMsgInfo.setTableId(tableId);
        warnMsgInfo.setKeyInfo(keyInfo);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
    
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void addInventoryTransaction(List<InventoryTransactionDTO> transactionList, String approveType) {
    	if(CollUtil.isEmpty(transactionList)) {
    		ServiceException.runError("库存流水不能为空");
    	}
    	String logMsg = StringUtil.appendLogMsg("addInventoryTransaction", transactionList.stream().map(InventoryTransactionDTO::getSourceCode)
    			.filter(Objects::nonNull).collect(Collectors.joining("、")) , approveType);
    	log.info("{}开始" , logMsg);
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
			transactionId = RootContext.getXID().replace(":", "_");
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
			InventoryTransactionEntity inventoryTransactionEntity = BeanUtil.copyProperties(transactionFlowEntity, InventoryTransactionEntity.class);
			inventoryTransactionEntity.setId(null);
			LocalDateTime now = LocalDateTime.now();
			inventoryTransactionEntity.setCreateTime(now);
			inventoryTransactionEntity.setUpdateTime(now);
			inventoryTransactionEntity.setCreateUserId(null);
			inventoryTransactionEntity.setCreateUserName(null);
			inventoryTransactionEntity.setUpdateUserId(null);
			inventoryTransactionEntity.setUpdateUserName(null);
			inventoryTransactionEntity.setVersion(null);
			inventoryTransactionEntity.setIsDeleted(null);
			inventoryTransactionEntity.setFlowId(transactionFlowEntity.getId());
			inventoryTransactionEntity.setTransactionId(transactionId);
			inventoryTransactionEntity.setTransactionType(transactionType);
			inventoryTransactionEntity.setOperationMode(approveType);
			if(!InventoryTradingService.APPROVE.equals(approveType)) {
				inventoryTransactionEntity.setQty(inventoryTransactionEntity.getQty() * -1);
			}
			inventoryTransactionEntityList.add(inventoryTransactionEntity);
		}
		this.saveBatch(inventoryTransactionEntityList);
		InventoryTransactionSynchronizationAdapter.register(transactionId);
		log.info("{}结束" , logMsg);
	}

    @Override
    public void tryRedis(String transactionId , List<InventoryTransactionDTO> transactionList) {
    	String logMsg = StringUtil.appendLogMsg("tryRedis", transactionId);
    	log.info("{}开始" , logMsg);
    	if(StringUtils.isBlank(transactionId)) {
			log.error("冻结redis库存事务transactionId不能为空");
			throw new ServiceException("冻结redis库存事务transactionId不能为空");
		}
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
    	log.info("{}结束" , logMsg);
    }
    
	@Override
	public void commitRedis(String transactionId , boolean toDoHis) {
		String logMsg = StringUtil.appendLogMsg("commitRedis", transactionId , toDoHis);
    	log.info("{}开始" , logMsg);
		if(StringUtils.isBlank(transactionId)) {
			log.error("提交redis库存事务transactionId不能为空");
			throw new ServiceException("提交redis库存事务transactionId不能为空");
		}
		Integer count = lambdaQuery().eq(InventoryTransactionEntity::getTransactionId, transactionId).count();
		if(count == null || count == 0) {
			return;
		}
		InventoryRedisOpEnum commit = InventoryRedisOpEnum.COMMIT;
		inventoryRedisUtil.execute(commit , commit.getCode() , transactionId , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.TRANSACTION, transactionId) 
				, InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, ""));
		if(toDoHis) {
			transactionIdToInventoryHisPool.execute(() -> ApplicationContextUtils.getBean(InventoryTransactionService.class).transactionIdToInventoryHis(transactionId));
		}
    	log.info("{}结束" , logMsg);
	}

	@Override
	public void rollbackRedis(String transactionId) {
		String logMsg = StringUtil.appendLogMsg("rollbackRedis", transactionId);
    	log.info("{}开始" , logMsg);
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
				, InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, ""));
		log.info("{}结束" , logMsg);
	}
	
	/**
     * 更新库存历史
     * @param transactionDTO    库存交易信息
     */
    private void updateInventoryHis(String inventoryId, LocalDate billDate, Integer qty , String userId , String userName) {
        // 查询当天历史库存
        saveInventoryCurrentday(inventoryId , billDate , qty , userId , userName);

        if(qty != 0) {
        	// 更新当天之后的历史库存
            LambdaUpdateWrapper<InventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.setSql("qty = qty + " + qty)
                    .set(InventoryHisEntity::getUpdateTime, LocalDateTime.now())
                    .set(InventoryHisEntity::getUpdateUserId, userId)
                    .set(InventoryHisEntity::getUpdateUserName, userName)
                    //条件
                    .eq(InventoryHisEntity::getInfoId, inventoryId)
                    .gt(InventoryHisEntity::getBillDate, billDate);

            inventoryHisService.update(wrapper);
        }
    }
	
	/**
     * 保存当天历史库存
     * @param transactionDTO    库存交易信息
     */
    private void saveInventoryCurrentday(String inventoryId, LocalDate billDate, Integer qty , String userId , String userName) {
        InventoryHisEntity inventoryHis = this.queryInventoryHisLast(inventoryId, billDate,true);

        if(null == inventoryHis) {
            // 查询当天以前的库存
            inventoryHis = this.queryInventoryHisLast(inventoryId, billDate,false);
            int inventoryQty = (null == inventoryHis) ? 0 : inventoryHis.getQty();

            inventoryHis = new InventoryHisEntity();
            inventoryHis.setInfoId(inventoryId);
            inventoryHis.setBillDate(billDate);
            inventoryHis.setQty(inventoryQty+ qty);
            inventoryHis.setCreateUserId(userId);
            inventoryHis.setCreateUserName(userName);
            inventoryHis.setCreateTime(LocalDateTime.now());
            inventoryHis.setUpdateUserId(userId);
            inventoryHis.setUpdateUserName(userName);
            inventoryHis.setUpdateTime(LocalDateTime.now());

            inventoryHisService.save(inventoryHis);
        }else {
        	if(qty != 0) {
        		// 更新当天历史库存
                LambdaUpdateWrapper<InventoryHisEntity> wrapper = new LambdaUpdateWrapper<>();
                wrapper.setSql("qty = qty + " + qty)
                        .set(InventoryHisEntity::getUpdateTime, LocalDateTime.now())
                        .set(InventoryHisEntity::getUpdateUserId, userId)
                        .set(InventoryHisEntity::getUpdateUserName, userName)
                        //条件
                        .eq(InventoryHisEntity::getId, inventoryHis.getId());

                inventoryHisService.update(wrapper);
        	}
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
    	int qty = 0;
    	List<InventoryHisEntity> hisList = inventoryHisService.lambdaQuery()
    			.eq(InventoryHisEntity::getInfoId, inventoryId)
    			.orderByDesc(InventoryHisEntity::getBillDate)
    			.last("limit 1")
    			.list();
    	if(CollUtil.isNotEmpty(hisList)) {
    		qty = hisList.get(0).getQty();
    	}
    	
		inventoryService.lambdaUpdate().set(InventoryEntity::getQty, qty).eq(InventoryEntity::getId, inventoryId).update();
    }
    
    
    /**
     * 更新库存交易 的库存数量
     * @param transactionDTO    库存交易信息
     */
    private void updateInventoryTransaction(String inventoryId , LocalDate billDate , Integer qty) {
        if(qty != 0) {
        	// 日期大于当前单据日期的流水更新
            LambdaUpdateWrapper<TransactionFlowEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.setSql("cur_inventory_qty = cur_inventory_qty + " + qty)
                    .set(TransactionFlowEntity::getUpdateTime, LocalDateTime.now())
                    //条件
                    .eq(TransactionFlowEntity::getInventoryId, inventoryId)
                    .gt(TransactionFlowEntity::getBillDate, billDate);
            transactionFlowService.update(wrapper);
        }
    }
    
    /**
     * 重算今天结余库存
     * @param transactionDTO    库存交易信息
     */
    private void updateCurrInventoryTransaction(String inventoryId , LocalDate billDate) {
        // 当前单据日期需要进行流水重算
        List<TransactionFlowEntity> toDayFlowList = transactionFlowService.lambdaQuery().eq(TransactionFlowEntity::getInventoryId, inventoryId)
        		.eq(TransactionFlowEntity::getBillDate, billDate)
        		.orderByAsc(TransactionFlowEntity::getId)
        		.list();
        if(CollUtil.isNotEmpty(toDayFlowList)) {
        	InventoryHisEntity hisEntity = inventoryHisService.findLastInventory(inventoryId, billDate.minusDays(1));
            int beforeQty = 0;
            if(hisEntity != null){
            	beforeQty = hisEntity.getQty();
            }
            List<TransactionFlowEntity> updateToDayFlowList = new ArrayList<>(toDayFlowList.size());
            for(TransactionFlowEntity toDayFlow : toDayFlowList) {
            	TransactionFlowEntity transactionFlowEntity = new TransactionFlowEntity();
            	transactionFlowEntity.setId(toDayFlow.getId());
            	transactionFlowEntity.setCurInventoryQty(beforeQty + toDayFlow.getQty());
            	beforeQty = transactionFlowEntity.getCurInventoryQty();
            	updateToDayFlowList.add(transactionFlowEntity);
            }
            transactionFlowService.updateBatchById(updateToDayFlowList);
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
					String logMsg = StringUtil.appendLogMsg("inventoryCheckRollback", t);
			    	log.info("{}开始" , logMsg);
					Date date = rollbackTimeMap.get(t);
					if(date == null) {
						rollbackTimeMap.put(t, new Date());
					}else {
						if(new Date().after(DateUtil.offsetSecond(date, timeout))) {
							try {
								log.info("{}自动回滚开始" , logMsg);
								this.rollbackRedis(t);
								log.info("{}自动回滚结束" , logMsg);
							} catch (Exception e) {
								log.error("检查redis自动回滚执行失败：{}" , t , e);
							}
						}
					}
					log.info("{}结束" , logMsg);
				});
			}
		}
	}

	@Override
	public void queryInventoryCheckSame(Integer warnSize) {
		List<CheckInventoryDTO> checkInventoryList = this.checkDbInventorySame(null);
		if(CollUtil.isNotEmpty(checkInventoryList)) {
            sendFeishuMsg("数据库库存不一致", "", checkInventoryList.stream().map(CheckInventoryDTO::toString).collect(Collectors.joining("\n")));
    	}
    	List<CheckInventoryDTO> redisCheckInventoryList = this.checkRedisInventorySame(null);
    	if(CollUtil.isNotEmpty(redisCheckInventoryList)) {
    		int size = redisCheckInventoryList.size();
    		String message = redisCheckInventoryList.stream().map(CheckInventoryDTO::redisToString).collect(Collectors.joining("\n"));
    		log.error("redis和数据库库存不一致：{}" , message);
    		if(size > warnSize) {
    			message = "大量redis和数据库库存不一致，不一致数量：" + size + "大于告警数据：" + warnSize;
        	}
    		sendFeishuMsg("redis和数据库库存不一致", "", message);
    	}
	}
	
	private List<CheckInventoryDTO> checkDbInventorySame(List<String> inventoryIds) {
		List<CheckInventoryDTO> checkInventoryList = new ArrayList<>();
		if(CollUtil.isNotEmpty(inventoryIds)) {
			for(String inventoryId : inventoryIds) {
				CheckInventoryDTO checkInventoryDTO = new CheckInventoryDTO();
				checkInventoryDTO.setInventoryId(inventoryId);
				checkInventoryList.add(checkInventoryDTO);
			}
		}
    	int i = 0;
    	while(i < 3) {
    		checkInventoryList = baseMapper.queryDbInventoryCheckSame(checkInventoryList.stream().map(CheckInventoryDTO::getInventoryId).collect(Collectors.toList()));
    		if(CollUtil.isEmpty(checkInventoryList)) {
    			break;
    		}else {
    			checkInventoryList.forEach(dto -> ApplicationContextUtils.getBean(InventoryTransactionService.class).inventoryIdToInventoryHis(dto.getInventoryId(), ""));
    		}
    		i = i + 1;
    	}
    	return checkInventoryList;
	}
	
	private List<CheckInventoryDTO> checkRedisInventorySame(List<String> inventoryIds) {
		Integer pageSize = 1000;
    	List<CheckInventoryDTO> redisCheckInventoryList = this.inventoryCheckRedisSame(pageSize , inventoryIds);
		
		if(CollUtil.isNotEmpty(redisCheckInventoryList)) {
			int i = 0;
			while(i < 3) {
				redisCheckInventoryList.forEach(dto -> ApplicationContextUtils.getBean(InventoryTransactionService.class).inventoryIdToInventoryHis(dto.getInventoryId(), ""));
				redisCheckInventoryList = this.inventoryCheckRedisSame(pageSize , redisCheckInventoryList.stream().map(CheckInventoryDTO::getInventoryId).collect(Collectors.toList()));
				i = i + 1;
			}
		}
		return redisCheckInventoryList;
	}
	
	private List<CheckInventoryDTO> inventoryCheckRedisSame(Integer pageSize , List<String> inventoryIds){
		List<CheckInventoryDTO> redisCheckInventoryList = new ArrayList<>();
    	String lastInventoryId = "0";
    	while(true) {
    		QueryWrapper<TransactionFlowEntity> queryWrapper = new QueryWrapper<>();
    		queryWrapper.select(" inventory_id,sum(qty) qty ");
    		if(CollUtil.isNotEmpty(inventoryIds)) {
    			queryWrapper.in("inventory_id", inventoryIds);
    		}
    		queryWrapper.gt("inventory_id", lastInventoryId);
			queryWrapper.groupBy("inventory_id");
			queryWrapper.last(" order by inventory_id limit " + pageSize + " ");
			List<TransactionFlowEntity> transactionFlowEntityList = transactionFlowService.list(queryWrapper);
			if(CollUtil.isEmpty(transactionFlowEntityList)) {
				break;
			}
			transactionFlowEntityList.forEach(t -> {
				Integer redisQty = 0;
				String inventoryId = t.getInventoryId();
				Object redisQtyObj = inventoryRedisUtil.get(InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, inventoryId));
				if(redisQtyObj != null) {
					redisQty = Integer.valueOf(redisQtyObj.toString().split(InventoryRedisUtil.splitSign)[0]);
				}
				Integer flowSumQty = t.getQty();
				if(flowSumQty.compareTo(redisQty) != 0) {
					CheckInventoryDTO dto = new CheckInventoryDTO();
					dto.setInventoryId(inventoryId);
					dto.setInventoryQty(redisQty);
					dto.setFlowSumQty(flowSumQty);
					redisCheckInventoryList.add(dto);
				}
			});
			lastInventoryId = transactionFlowEntityList.get(transactionFlowEntityList.size() - 1).getInventoryId();
    	}
    	return redisCheckInventoryList;
	}

}
