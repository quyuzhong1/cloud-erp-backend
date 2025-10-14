package com.erp.server.wms.service.impl;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.redisson.RedissonMultiLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.InventoryHisEntity;
import com.erp.model.wms.entity.InventoryTransactionEntity;
import com.erp.model.wms.enums.inventory.InventoryRedisOpEnum;
import com.erp.model.wms.enums.inventory.InventoryRedisOpKeyEnum;
import com.erp.server.wms.mapper.InventoryTransactionMapper;
import com.erp.server.wms.service.InventoryHisService;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.InventoryTransactionService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.utils.InventoryRedisUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
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
    private OperateLogService operateLogService;
    @Autowired
    private InventoryHisService inventoryHisService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryRedisUtil inventoryRedisUtil;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InventoryTransactionDTO.AddDTO addDTO) {
        InventoryTransactionEntity inventoryTransactionEntity = new InventoryTransactionEntity();
        BeanMapperUtils.copy(addDTO, inventoryTransactionEntity);

        // 数据处理
        handleData(inventoryTransactionEntity);

        log.info("开始新增库存事务单");
        boolean save = super.save(inventoryTransactionEntity);
        if(!save) {
            throw new ServiceException("库存事务单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "库存事务单" , inventoryTransactionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, inventoryTransactionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(inventoryTransactionEntity.getId(), inventoryTransactionEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InventoryTransactionDTO.UpdateDTO addOrUpdateDTO) {
        InventoryTransactionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "库存事务单"));
        InventoryTransactionEntity inventoryTransactionEntity =  BeanMapperUtils.map(InventoryTransactionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(inventoryTransactionEntity);
        log.info("编辑 开始修改库存事务单数据，id：【{}】", old.getId());
        boolean save = super.updateById(inventoryTransactionEntity);
        if(!save) {
            throw new ServiceException("库存事务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录库存事务单日志数据，id：【{}】", inventoryTransactionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), inventoryTransactionEntity.getId(), "库存事务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, inventoryTransactionEntity, null, inventoryTransactionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InventoryTransactionEntity inventoryTransactionEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public void inventoryHisToInventory(String inventoryId) {
    	InventoryHisEntity inventoryHisEntity = inventoryHisService.findLastInventory(inventoryId, LocalDate.now());
		inventoryService.lambdaUpdate().set(InventoryEntity::getQty, inventoryHisEntity.getQty()).eq(InventoryEntity::getId, inventoryHisEntity.getInfoId()).update();
    }
    
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
						String transactionId = inventoryTransactionEntity.getTransactionId();
						if(!transactionIdSet.add(transactionId)) {
							this.commitRedis(inventoryTransactionEntity.getTransactionId());
						}
						com.erp.model.wms.dto.inventory.InventoryTransactionDTO transactionDTO = BeanUtil.copyProperties(inventoryTransactionEntity, 
								com.erp.model.wms.dto.inventory.InventoryTransactionDTO.class);
						ApplicationContextUtils.getBean(InventoryTradingRedisServiceImpl.class).updateInventoryHis(transactionDTO);
					}
					removeByIds(inventoryTransactionEntityList.stream().map(InventoryTransactionEntity::getId).collect(Collectors.toSet()));
					this.inventoryHisToInventory(inventoryId);
				}
			} catch (Exception e) {
				log.error("迁移历史库存失败" , e);
				throw e;
			} finally{
 				inventoryRedisUtil.unLock(tryLock);
 			}
    	}
    }

	@Override
	public void commitRedis(String transactionId) {
		Integer count = lambdaQuery().eq(InventoryTransactionEntity::getTransactionId, transactionId).count();
		if(count == null || count == 0) {
			throw new ServiceException("没有库存交易记录，不允许提交redis库存transactionId={}" , transactionId);
		}
		InventoryRedisOpEnum commit = InventoryRedisOpEnum.COMMIT;
		inventoryRedisUtil.execute(commit , commit.getCode() , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.TRANSACTION, transactionId));
	}
}
