package com.erp.server.mrp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.ReplenishmentRuleTypeEnum;
import com.erp.model.mrp.enums.ReplenishmentTypeEnum;
import com.erp.model.mrp.vo.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.ReplenishmentSuggestionMapper;
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 补货建议主表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Service
public class ReplenishmentSuggestionServiceImpl extends SuperServiceImpl<ReplenishmentSuggestionMapper, ReplenishmentSuggestionEntity> implements ReplenishmentSuggestionService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleStockUpService cfgRuleStockUpService;

    @Autowired
    private CfgRuleSalesQtyService cfgRuleSalesQtyService;

    @Override
    public PagingVO<ReplenishmentSuggestionVO.PagingView> paging(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        return null;
    }

    @Override
    public ReplenishmentSuggestionVO.View view(String detailId) {
        return null;
    }

    @Override
    public PagingVO<FbaInTransitDetailVO> fbaInTransitDetail(PagingDTO<String> params) {
        return null;
    }

    @Override
    public PagingVO<OverseasInTransitDetailVO> overseasInTransitDetail(PagingDTO<String> params) {
        return null;
    }

    @Override
    public PagingVO<LocalInTransitDetailVO> localInTransitDetail(PagingDTO<LocalInTransitDetailDTO> params) {
        return null;
    }

    @Override
    public PagingVO<EstimatedDeliveryVO> estimatedDelivery(PagingDTO<EstimatedDeliveryDTO> params) {
        return null;
    }

    @Override
    public PagingVO<EstimatedPurchaseVO> estimatedPurchase(PagingDTO<EstimatedPurchaseDTO> params) {
        return null;
    }

    @Override
    public Integer inventoryTotal(InventoryTotalDTO params) {
        return null;
    }

    @Override
    public InventoryDetailVO inventoryDetail(InventoryTotalDTO params) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO notRestockingReplenishment(String id,String replenishmentRemark) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (!ReplenishmentTypeEnum.NORMAL.getCode().equals(entity.getReplenishmentType())) {
            throw new ServiceException(ApiError.ERROR_NOT_RESTOCKING_REPLENISHMENT);
        }
        updateIsReplenishment(id,replenishmentRemark,ReplenishmentTypeEnum.NOT_RESTOCKING.getCode());

        // 操作日志
        String msg = StrUtil.format("用户【{}】SKU为【{}】的【{}】单据暂不补货操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getSkuNo(), "补货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "暂不补货操作");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO restoreReplenishment(String id,String replenishmentRemark) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        if (!ReplenishmentTypeEnum.NOT_RESTOCKING.getCode().equals(entity.getReplenishmentType())) {
            throw new ServiceException(ApiError.ERROR_RESTORE_REPLENISHMENT);
        }
        updateIsReplenishment(id,replenishmentRemark,ReplenishmentTypeEnum.NORMAL.getCode());
        // 操作日志
        String msg = StrUtil.format("用户【{}】SKU为【{}】的【{}】单据恢复补货操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getSkuNo(), "补货建议");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "恢复补货操作");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void updateRule(ReplenishmentSuggestionDTO.UpdateRuleDTO dto) {

    }

    @Override
    public BatchResultDTO batchUpdateRule(String id, CfgRuleStockUpDTO.UpdateDTO stockUpUpdateDTO, CfgRuleStockUpDTO.UpdateDTO stockUpUpdateDTO1) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO restoreRule(String id, List<String> ruleTypeList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        //恢复备货规则
        if (ruleTypeList.contains(ReplenishmentRuleTypeEnum.STOCK_UP.getCode())) {
            cfgRuleStockUpService.deleteByRefId(entity.getId());
        }
        //恢复销量规则
        if (ruleTypeList.contains(ReplenishmentRuleTypeEnum.SALES.getCode())) {
            cfgRuleSalesQtyService.deleteByRefId(entity.getId());
        }
        // 操作日志
        String ruleNames = ruleTypeList.stream().map(obj -> ReplenishmentRuleTypeEnum.getName(obj)).collect(Collectors.joining(","));
        String msg = StrUtil.format("用户【{}】SKU为【{}】的【{}】单据恢复【{}】规则配置操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getSkuNo(), "补货建议",ruleNames);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "恢复规则配置操作");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    /**
     * 根据id更新是否补货和补货原因
     * @author will
     * @date 2024/8/29 15:19
     * @param id
     * @param replenishmentRemark
     * @param replenishmentType
     */
    private Boolean updateIsReplenishment (String id,String replenishmentRemark,String replenishmentType) {
       return lambdaUpdate().eq(ReplenishmentSuggestionEntity::getId,id)
                .set(ReplenishmentSuggestionEntity::getReplenishmentRemark,replenishmentRemark)
                .set(ReplenishmentSuggestionEntity::getReplenishmentType,replenishmentType)
                .update();
    }
}
