package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.InventoryClosedRecordEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;
import com.erp.model.wms.entity.InventoryClosedRecordEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.InventoryClosedRecordMapper;
import com.erp.server.wms.service.InventoryClosedRecordService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 库存关账记录表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-10-12
 */
@Slf4j
@Service
public class InventoryClosedRecordServiceImpl extends SuperServiceImpl<InventoryClosedRecordMapper, InventoryClosedRecordEntity> implements InventoryClosedRecordService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public Map<String, LocalDate> mapByOrgId(String category) {
        return lambdaQuery()
                .eq(InventoryClosedRecordEntity::getCategory,category)
                .list()
                .stream()
                .collect(Collectors.toMap(InventoryClosedRecordEntity::getInventoryOrgId, InventoryClosedRecordEntity::getClosedDate));
    }

    @Override
    public LocalDate checkClosed(String inventoryOrgId, LocalDate billDate) {
        if (null == billDate){
            // 兼容无对比时间
            return null;
        }
        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = this.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());
        LocalDate closeDate = closedDateMap.get(inventoryOrgId);
        if(null == closeDate) {
            return null;
        }
        if (billDate.isBefore(closeDate) || billDate.equals(closeDate)) {
            // 单据已关账返回关账时间
            return closeDate;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void actionBatch(List<InventoryClosedRecordEntity> newEntityList, List<InventoryClosedRecordEntity> oldEntityList) {
        // 1: 新增列表
        List<InventoryClosedRecordEntity> saveEntityList = newEntityList.stream()
                .filter(e -> oldEntityList.stream().noneMatch(o -> CharSequenceUtil.equals(o.getCategory(),e.getCategory()) && o.getInventoryOrgId().equalsIgnoreCase(e.getInventoryOrgId())))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(saveEntityList)) {
            boolean result = this.saveBatch(saveEntityList);
            if (!result) {
                throw new ServiceException("[InventoryClosedRecordEntity]批量保存失败");
            }
        }

        // 2: 删除不存在的列表
        List<InventoryClosedRecordEntity> deleteEntityList = oldEntityList.stream()
                .filter(e -> newEntityList.stream().noneMatch(o -> CharSequenceUtil.equals(o.getCategory(),e.getCategory()) && o.getInventoryOrgId().equalsIgnoreCase(e.getInventoryOrgId())))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(deleteEntityList)) {
            List<String> entityIds = deleteEntityList.stream().map(InventoryClosedRecordEntity::getId).collect(Collectors.toList());
            boolean result = this.removeByIds(entityIds);
            if (!result) {
                throw new ServiceException("[InventoryClosedRecordEntity]批量删除失败");
            }
        }

        // 3: 更新列表
        // 过滤得到需要更新的列表
        List<InventoryClosedRecordEntity> updateEntityList = oldEntityList.stream()
                .filter(e -> e.isUpdateClosedDate(newEntityList))
                .map(e -> e.setClosedDateByMap(newEntityList))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(updateEntityList)) {
            boolean result = this.updateBatchById(updateEntityList);
            if (!result) {
                throw new ServiceException("[InventoryClosedRecordEntity]批量更新失败");
            }
        }
    }

    @Override
    public void checkHsClosed (List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList) {
        if (CollectionUtils.isEmpty(closedParamList)) {
            return;
        }
        List<String> orgIdList = closedParamList.stream().map(InventoryClosedRecordDTO.ClosedParamDTO::getOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);

        Map<String, LocalDate> closedDateMap = this.mapByOrgId(InventoryClosedRecordEnum.HS.getCode());
        for (InventoryClosedRecordDTO.ClosedParamDTO closedParamDTO :closedParamList) {
            LocalDate localDate = closedDateMap.get(closedParamDTO.getOrgId());
            if (ObjectUtil.isNull(localDate)) {
                continue;
            }
            LocalDate date = closedParamDTO.getBillDate();
            if (date.isBefore(localDate) || localDate.isEqual(date)) {
                String orgName = accountingCompanyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), closedParamDTO.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
                throw new ServiceException(CharSequenceUtil.format("组织【{}】已于{}关账",orgName,localDate));
            }
        }
    }

    @Override
    public Map<String, InventoryClosedRecordEntity> mapByCategory(String category) {
        return lambdaQuery()
                .eq(InventoryClosedRecordEntity::getCategory,category)
                .list()
                .stream()
                .collect(Collectors.toMap(InventoryClosedRecordEntity::getInventoryOrgId, e -> e));
    }


}
