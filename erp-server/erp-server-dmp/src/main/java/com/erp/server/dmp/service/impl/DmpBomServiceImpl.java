package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.mabang.ComboSkuInfoEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.server.dmp.mapper.DmpBomMapper;
import com.erp.server.dmp.service.DmpBomService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * sku bom关系表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
 */
@Slf4j
@Service
public class DmpBomServiceImpl extends SuperServiceImpl<DmpBomMapper, DmpBomEntity> implements DmpBomService {

    @Resource
    private MQProducerService mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrder(ComboSkuInfoEntity ext) {
         List<DmpBomEntity> bomEntityList = getBomEntityList(ext);
        if(CollectionUtil.isEmpty(bomEntityList)){
            return;
        }
        List<DmpBomEntity> bomList = lambdaQuery()
//                .eq(DmpBomEntity::getParentSku, ext.getComboSku())
                .eq(DmpBomEntity::getFinancialCode, ext.getFinancialCode())
                .eq(DmpBomEntity::getPlatformSign, ext.getPlatformSign())
                .eq(DmpBomEntity::getRelationType, ext.getRelationType())
                .list();
        // 已存在数据进行删除
        if (CollectionUtil.isNotEmpty(bomList)) {
            //如果数据有变动需要更新数据库订单信息 此处使用物理删除避免出现大量重复数
            baseMapper.deletePhysicalBatchIds(bomList.stream().map(DmpBomEntity::getId).collect(Collectors.toList()));
        }
        this.saveBatch(bomEntityList);
        // 预警
        if("machining".equals(ext.getRelationType()) && CollectionUtil.isEmpty(bomList)){
            // 预警
            WarnMsgInfoDTO warnMsgInfo = getWarnMsgInfoDTO(ext, bomList);
            mqProducerService.sendWarnMsg(warnMsgInfo);
        }
    }

    @Override
    public List<DmpBomEntity> findBom(String sku, String platformSign, String relationType) {
        List<DmpBomEntity> bomList = lambdaQuery()
                .eq(DmpBomEntity::getParentSku, sku)
                .eq(DmpBomEntity::getPlatformSign, platformSign)
                .eq(DmpBomEntity::getRelationType, relationType)
                .list();
        return bomList;
    }

    @Override
    public List<DmpBomEntity> listFindBomBySkuList(List<String> skuList, String platformSign, String relationType) {
        List<DmpBomEntity> bomList = lambdaQuery()
                .in(DmpBomEntity::getParentSku, skuList)
                .eq(DmpBomEntity::getPlatformSign, platformSign)
                .eq(DmpBomEntity::getRelationType, relationType)
                .list();
        return bomList;
    }

    private static WarnMsgInfoDTO getWarnMsgInfoDTO(ComboSkuInfoEntity ext, List<DmpBomEntity> bomList) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setTitle(StrUtil.format("加工SKU变更:sku:【{}】", ext.getComboSku()));
        warnMsgInfo.setBizName(StrUtil.format("加工SKU{}:sku:【{}】", CollectionUtil.isNotEmpty(bomList)? "更新" : "新增", ext.getComboSku()));
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTableName(SqlHelper.table(DmpBomEntity.class).getTableName());
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(StrUtil.format("【{}】平台加工SKU【{}】发生变更，请及时更新plm BOM信息系统", ext.getPlatformSign(), ext.getComboSku()));
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.MACHINING_SKU_NOTICE);
        return warnMsgInfo;
    }

    private List<DmpBomEntity> getBomEntityList(ComboSkuInfoEntity ext) {
        if(CollectionUtil.isEmpty(ext.getComboProductDetail())){
            return null;
        }
        DmpBomEntity modelEntity = new DmpBomEntity();
        modelEntity.setParentSku(ext.getComboSku());
        modelEntity.setParentSkuName(ext.getName());
        modelEntity.setPlatformSign(ext.getPlatformSign());
        modelEntity.setRelationType(ext.getRelationType());
        modelEntity.setFinancialCode(ext.getFinancialCode());
        return ext.getComboProductDetail().stream().map(detail -> {
            DmpBomEntity entity = new DmpBomEntity();
            BeanUtil.copyProperties(modelEntity, entity);
            entity.setSkuNo(detail.getStockSku());
            entity.setQty(detail.getQuantity());
            entity.setName(detail.getNameCN());
            return entity;
        }).collect(Collectors.toList());
    }
}
