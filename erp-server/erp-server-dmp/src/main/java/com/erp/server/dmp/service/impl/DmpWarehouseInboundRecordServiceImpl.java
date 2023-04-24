package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncKingdeeOmsStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.entity.DmpWarehouseInboundItemEntity;
import com.erp.server.dmp.entity.DmpWarehouseInboundRecordEntity;
import com.erp.server.dmp.mapper.DmpWarehouseInboundRecordMapper;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.DmpWarehouseInboundItemService;
import com.erp.server.dmp.service.DmpWarehouseInboundRecordService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.OperatorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 海外仓上架记录表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-03-29
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DmpWarehouseInboundRecordServiceImpl extends SuperServiceImpl<DmpWarehouseInboundRecordMapper, DmpWarehouseInboundRecordEntity> implements DmpWarehouseInboundRecordService {

    @Resource
    private DmpWarehouseInboundItemService dmpWarehouseInboundItemService;

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    public Boolean saveOrder(GoodcangDTO.MessageDTO ext) {
        // 入库单是否存在 已存在报异常
        getByOrderCode(ext.getReceivingCode()).ifPresent(x -> {
            throw new RuntimeException("海外仓入库单已存在");
        });
        if (CollectionUtil.isEmpty(ext.getReceivingDetail())) {
            throw new RuntimeException("海外仓入库单不存在详情信息");
        }
        // 不存在新增
        DmpWarehouseInboundRecordEntity insertRecord = new DmpWarehouseInboundRecordEntity(ext);
        boolean result  = save(insertRecord);
        // 存在更新
        if(!result){
            throw new RuntimeException("海外仓入库单主数据保存/更新失败");
        }
        List<DmpWarehouseInboundItemEntity> insertList = ext.getReceivingDetail()
                .stream()
                .map(x -> new DmpWarehouseInboundItemEntity(x, insertRecord.getId()))
                .collect(Collectors.toList());
        dmpWarehouseInboundItemService.saveBatch(insertList);
        return Boolean.TRUE;
    }

    /**
     * String receivingCode, KingdeeApiUtils apiUtils, Integer code, String desc, Map<String, Object> beanToMap
     * @param paramExt
     * @param apiUtils
     * @return
     */
    @Override
    public String addKingdeeTransferRecord(GoodcangDTO.MessageDTO paramExt, KingdeeApiUtils apiUtils) {
        // 修改本地状态
        // 根据录入值和字段配置生成JSONObject
        List<GoodcangDTO.ReceivingDetailDTO> collect = paramExt.getReceivingDetail().stream().peek(x -> x.setUpdateTime(paramExt.getUpdateTime())).collect(Collectors.toList());
        paramExt.setReceivingDetail(collect);
        Map<String, Object> beanToMap = BeanUtil.beanToMap(paramExt);
        saveOrder(paramExt);
        // 保存金碟记录
        String dataId = kingdeeCommonService.addKingdeeRecord(paramExt.getReceivingCode(), apiUtils, ApiModuleTypeEnum.STOCK_OVERSEAS.getCode(), PlatformEnum.KINGDEE.getDesc(), beanToMap);
        return dataId;
    }

    @Override
    public Optional<DmpWarehouseInboundRecordEntity> getByOrderCode(String receivingCode) {
        // 入库单是否存在
        return lambdaQuery()
                .eq(DmpWarehouseInboundRecordEntity::getReceivingCode, receivingCode)
                .oneOpt();
    }

    @Override
    public Boolean submitKingdeeTransferRecord(String saveId, String receivingCode, KingdeeApiUtils apiUtils) {
        // 修改本地订单状态
        updateSyncStatusByCode(receivingCode, SyncKingdeeOmsStatusEnum.BE_AUDIT);
        // 提交到金碟
        List<String> ids = Arrays.asList(saveId);
        OperatorResult submitResult = apiUtils.submit(ids);
        boolean submit = submitResult.isSuccessfully();
        kingdeeCommonService.insertSyncLog(new PlatformEntity(PlatformEnum.KINGDEE), receivingCode, JSONUtil.toJsonStr(submitResult),"提交直接调拨单", ApiModuleTypeEnum.STOCK_OVERSEAS.getCode(), ApiSendStatusEnum.FAILURE.getCode());
        if(!submit){
            throw new RuntimeException("直接调拨单提交金碟失败");
        }
        return submit;
    }

    @Override
    public Boolean updateSyncStatusByCode(String receivingCode, SyncKingdeeOmsStatusEnum statusEnum) {
        return lambdaUpdate()
                .set(DmpWarehouseInboundRecordEntity::getSyncKingdeeStatus, statusEnum)
                .eq(DmpWarehouseInboundRecordEntity::getReceivingCode, receivingCode)
                .update();
    }

    @Override
    public Boolean auditKingdeeTransferRecord(String saveId, String receivingCode, KingdeeApiUtils apiUtils) {
        // 修改本地订单状态
        updateSyncStatusByCode(receivingCode, SyncKingdeeOmsStatusEnum.SYNC_SUCCESS);
        // 审核金碟订单
        List<String> ids = Arrays.asList(saveId);
        OperatorResult auditResult = apiUtils.auditById(ids);
        boolean audit = auditResult.isSuccessfully();
        kingdeeCommonService.insertSyncLog(new PlatformEntity(PlatformEnum.KINGDEE), receivingCode, JSONUtil.toJsonStr(auditResult),"审核直接调拨单", ApiModuleTypeEnum.STOCK_OVERSEAS.getCode(), ApiSendStatusEnum.FAILURE.getCode());

        if(!audit){
            throw new RuntimeException("直接调拨单审核金碟失败");
        }
        return audit;
    }

    @Override
    public void pushDirectTransferToKingdee(DmpWarehouseInboundRecordEntity recordEntity, KingdeeApiUtils apiUtils) {
        // 1.根据当前状态判断是否推送金蝶步骤
        // 调拨单已存在金蝶会直接抛异常退出
        Dict dict = new Dict();
        dict.put("number", recordEntity.getReceivingCode());
        dict.put("CreateOrgId", ApiKingdeeOrganizationEnum.ORGANIZATION_HK.getCode());
        JSONObject viewJson = apiUtils.getViewJson(JSONUtil.toJsonStr(dict));
        String kingdeeId = viewJson.getStr("Id");
        if(SyncKingdeeOmsStatusEnum.BE_SUBMIT.equals(recordEntity.getSyncKingdeeStatus())){
            // 金蝶接口调用 提交
            submitKingdeeTransferRecord(kingdeeId, recordEntity.getReceivingCode(), apiUtils);
        }
        // 金蝶接口调用 审核
        auditKingdeeTransferRecord(kingdeeId, recordEntity.getReceivingCode(), apiUtils);
    }


}
