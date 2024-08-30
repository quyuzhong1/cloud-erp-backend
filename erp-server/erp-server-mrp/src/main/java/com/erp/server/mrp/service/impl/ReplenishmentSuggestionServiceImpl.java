package com.erp.server.mrp.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.ReplenishmentRuleTypeEnum;
import com.erp.model.mrp.enums.ReplenishmentTypeEnum;
import com.erp.model.mrp.vo.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.ReplenishmentSuggestionMapper;
import com.erp.server.mrp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Autowired
    private ReplenishmentSuggestionFavoriteService replenishmentSuggestionFavoriteService;

    @Autowired
    private ReplenishmentRefLabelService replenishmentRefLabelService;


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
        String msg = StrUtil.format("操作了暂不补货，原因：【{}】 ",replenishmentRemark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "暂不补货");
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
        String msg = StrUtil.format("操作了恢复补货，原因：【{}】 ",replenishmentRemark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "恢复补货");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public void updateRule(ReplenishmentSuggestionDTO.UpdateRuleDTO dto) {
        if (ObjectUtil.isEmpty(dto.getStockUpUpdateDTO()) && ObjectUtil.isEmpty(dto.getSalesQtyUpdateDTO())) {
            throw new ServiceException("备货、销量设置不能全部为空！");
        }
        //更新备货信息
        if (ObjectUtil.isEmpty(dto.getStockUpUpdateDTO())) {
            dto.getStockUpUpdateDTO().setRefId(dto.getId());
            dto.getStockUpUpdateDTO().setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            cfgRuleStockUpService.update(dto.getStockUpUpdateDTO());
        }
        //更新销量信息
        if (ObjectUtil.isEmpty(dto.getSalesQtyUpdateDTO())) {
            dto.getSalesQtyUpdateDTO().setRefId(dto.getId());
            dto.getSalesQtyUpdateDTO().setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            cfgRuleSalesQtyService.update(dto.getSalesQtyUpdateDTO());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO batchUpdateRule(String id, CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO, CfgRuleSalesQtyDTO.UpdateDetailDTO salesQtyUpdateDTO) {
        if (ObjectUtil.isEmpty(stockUpUpdateDTO) && ObjectUtil.isEmpty(salesQtyUpdateDTO)) {
            throw new ServiceException("备货、销量设置不能全部为空！");
        }
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //更新备货信息
        if (ObjectUtil.isEmpty(stockUpUpdateDTO)) {
            CfgRuleStockUpEntity ruleStockUpEntity = cfgRuleStockUpService.getByRefId(id);
            if (ObjectUtil.isNotEmpty(ruleStockUpEntity)) {
                stockUpUpdateDTO.setId(ruleStockUpEntity.getId());
            }
            stockUpUpdateDTO.setRefId(id);
            stockUpUpdateDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            CfgRuleStockUpDTO.UpdateDTO updateDTO = BeanMapperUtils.map(CfgRuleStockUpDTO.UpdateDTO.class, stockUpUpdateDTO);
            cfgRuleStockUpService.update(updateDTO);
        }
        //更新销量信息
        if (ObjectUtil.isEmpty(salesQtyUpdateDTO)) {
            salesQtyUpdateDTO.setRefId(id);
            salesQtyUpdateDTO.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            stockUpUpdateDTO.setIsCustom(Boolean.TRUE);
            cfgRuleSalesQtyService.update(salesQtyUpdateDTO);
        }
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
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
        String msg = StrUtil.format("恢复了系统默认规则");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "恢复默认规则");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO favorite(String id) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        Boolean isFavorite = replenishmentSuggestionFavoriteService.isFavorite(userInfo.getUid(), entity.getId());
        if (isFavorite) {
            return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "补货建议已关注，无需再次关注");
        }

        ReplenishmentSuggestionFavoriteDTO.AddDTO dto = new ReplenishmentSuggestionFavoriteDTO.AddDTO();
        dto.setUserId(userInfo.getUid());
        dto.setReplenishmentSuggestionId(entity.getId());
        replenishmentSuggestionFavoriteService.add(dto);

        // 操作日志
        String msg = StrUtil.format("设置了关注");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "关注");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelFavorite(String id) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));
        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        Boolean isFavorite = replenishmentSuggestionFavoriteService.isFavorite(userInfo.getUid(), entity.getId());
        if (!isFavorite) {
            return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), "补货建议未关注，无需取消关注");
        }
        replenishmentSuggestionFavoriteService.cancelFavorite(userInfo.getUid(),entity.getId());

        // 操作日志
        String msg = StrUtil.format("设置了取消关注");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), entity.getId(), "取消关注");
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO updateLabel(ReplenishmentSuggestionDTO.UpdateLabelDTO updateLabelDTO) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(updateLabelDTO.getId()).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //单个编辑标签
        ReplenishmentRefLabelDTO.UpdateDTO dto = new ReplenishmentRefLabelDTO.UpdateDTO();
        dto.setLabelIdList(updateLabelDTO.getLabelIdList());
        dto.setType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        replenishmentRefLabelService.update(dto,entity.getId());
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO batchAddLabel(String id, List<String> labelIdList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //新增标签
        ReplenishmentRefLabelDTO.UpdateDTO dto = new ReplenishmentRefLabelDTO.UpdateDTO();
        dto.setLabelIdList(labelIdList);
        dto.setType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
        replenishmentRefLabelService.update(dto,entity.getId());
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO cancelLabel(String id, List<String> labelIdList) {
        ReplenishmentSuggestionEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到补货建议数据"));

        //取消标签
        replenishmentRefLabelService.deleteLabel(labelIdList,entity.getId());
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo(), OperationTypeEnum.DELETE);
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
