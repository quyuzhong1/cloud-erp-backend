package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.dto.SoReturnReceiveDetailDTO;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoReturnReceiveDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售退货签收单明细表 服务实现类
 * @author Luo_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveDetailServiceImpl extends SuperServiceImpl<SoReturnReceiveDetailMapper, SoReturnReceiveDetailEntity> implements SoReturnReceiveDetailService {
    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoReturnNoticeDetailService soReturnNoticeDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean add(SoReturnReceiveDTO.Add dto, String id) {
        if("B2C".equals(dto.getType())){
            //B2C
            return addB2c(dto, id);
        }else {
            //B2B
            return addB2b(dto, id);
        }
    }

    private Boolean addB2b(SoReturnReceiveDTO.Add dto, String id){
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        //无退货订单号
        if (CharSequenceUtil.isBlank(dto.getSourceId())) {
            return notReturnOrderAdd(dto, id);
        } else {
            //退货订单id
            String soReturnId = dto.getSourceId();
            //B2B退货订单明细集合
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByMainId(soReturnId);
            //B2B退货通知单
            List<SoReturnNoticeDetailEntity> soReturnNoticeDetailEntities = soReturnNoticeDetailService.listDetailBySourceIds(Collections.singletonList(soReturnId));
            //B2B退货签收单
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Collections.singletonList(soReturnId));
            //sku
            List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
            //sku映射表
            SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
            skuParamDTO.setCutomerId(dto.getCustomerId());
            List<String> skuNos = skuInfoByIds.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
            skuParamDTO.setSkuNoList(skuNos);
            List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingFeign.listSkuBySkuNos(skuParamDTO);
            List<SoReturnReceiveDetailDTO.Add> detailList = dto.getDetailList();
            if(CollUtil.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_92173);
            }
            Map<String , Integer> returnDetailIdMap = new HashMap<>();
            for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
                SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
                detailEntity.setMainId(id);
                detailEntity.setSkuId(detailDto.getSkuId());
                detailEntity.setSkuNo(detailDto.getSkuNo());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setNoticeDetailId(detailDto.getNoticeDetailId());
                detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
                //获取平台sku
                if(StringUtils.isBlank(detailDto.getPlatformSkuNo())){
                    String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(skuVO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
                    detailEntity.setPlatformSkuNo(platformSkuNo);
                }else {
                    detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
                }
                detailEntity.setReturnAmount(detailDto.getReturnAmount());
                detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
                detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
                detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
                if(null == detailDto.getExchangeRate()){
                    detailEntity.setExchangeRate(dto.getExchangeRate());
                }else{
                    detailEntity.setExchangeRate(detailDto.getExchangeRate());
                }
                detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
                detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());

                if(Boolean.FALSE.equals(detailDto.getIsChildSkuNo()) //子sku不做数量校验
                        && SourceTypeEnum.SO_RETURN_NOTICE.getCode().equals(dto.getSourceType())
                        && StringUtils.isNotBlank(detailDto.getNoticeDetailId())){
                    SoReturnNoticeDetailEntity soReturnNoticeDetailEntity = soReturnNoticeDetailEntities.stream().filter(v -> v.getId().equals(detailDto.getNoticeDetailId())).findFirst().orElse(null);
                    if(null == soReturnNoticeDetailEntity){
                        throw new ServiceException(ApiError.ERROR_92169, detailDto.getSkuNo());
                    }
                    Integer returnQty = soReturnNoticeDetailEntity.getReturnQty();
                    //此单历史签收数量
                    Integer historyReceiveQty = soReturnReceiveDetailEntities.stream()
                            .filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                            .map(SoReturnReceiveDetailEntity::getReceiveQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
                    if (returnQty < detailDto.getReceiveQty() + historyReceiveQty) {
                        throw new ServiceException(ApiError.ERROR_92020, skuVO.getSkuNo());
                    }else if(historyReceiveQty > 0 && returnQty == detailDto.getReceiveQty() + historyReceiveQty){
                        BigDecimal returnAmount = soReturnNoticeDetailEntity.getReturnAmount();
                        BigDecimal taxReturnAmount = soReturnNoticeDetailEntity.getTaxReturnAmount();
                        BigDecimal returnAmountLocalCurrency = soReturnNoticeDetailEntity.getReturnAmountLocalCurrency();
                        BigDecimal taxReturnAmountLocalCurrency = soReturnNoticeDetailEntity.getTaxReturnAmountLocalCurrency();
                        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntityList = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).collect(Collectors.toList());
                        for (SoReturnReceiveDetailEntity soReturnReceiveDetail : soReturnReceiveDetailEntityList) {
                            returnAmount = returnAmount.subtract(soReturnReceiveDetail.getReturnAmount()) ;
                            taxReturnAmount = taxReturnAmount.subtract(soReturnReceiveDetail.getTaxReturnAmount());
                            returnAmountLocalCurrency = returnAmountLocalCurrency.subtract(soReturnReceiveDetail.getReturnAmountLocalCurrency());
                            taxReturnAmountLocalCurrency = taxReturnAmountLocalCurrency.subtract(soReturnReceiveDetail.getTaxReturnAmountLocalCurrency());
                        }
                        detailEntity.setReturnAmount(returnAmount);
                        detailEntity.setTaxReturnAmount(taxReturnAmount);
                        detailEntity.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
                        detailEntity.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
                    }
                }else if(Boolean.FALSE.equals(detailDto.getIsChildSkuNo()) //子sku不做数量校验
                        && StringUtils.isNotBlank(detailDto.getSourceDetailId())){
                    SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                        throw new ServiceException(ApiError.ERROR_92023, detailDto.getSkuNo());
                    }
                    Integer returnQty = 0;
                    if(CollectionUtils.isNotEmpty(soReturnDetailEntities)){
                        returnQty = soReturnDetailEntities.stream()
                                .filter(req -> req.getId().equals(detailDto.getSourceDetailId()))
                                .map(SoReturnDetailEntity::getReturnQty)
                                .reduce(MathUtil.ZERO, Integer::sum);
                    }
                    //此单历史签收数量
                    Integer historyReceiveQty = soReturnReceiveDetailEntities.stream()
                            .filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                            .map(SoReturnReceiveDetailEntity::getReceiveQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
                    //校验是否存在重复的明细并且数量大于退货数量
                    if(returnDetailIdMap.containsKey(detailDto.getSourceDetailId())){
                        Integer detailReturnQtySum = returnDetailIdMap.get(detailDto.getSourceDetailId()) + detailDto.getReceiveQty();
                        if (returnQty < detailReturnQtySum + historyReceiveQty) {
                            throw new ServiceException(ApiError.ERROR_92020, skuVO.getSkuNo());
                        }
                        returnDetailIdMap.put(detailDto.getSourceDetailId(),detailReturnQtySum);
                    }else {
                        returnDetailIdMap.put(detailDto.getSourceDetailId(),detailDto.getReceiveQty());
                    }
                    if (returnQty < detailDto.getReceiveQty() + historyReceiveQty) {
                        throw new ServiceException(ApiError.ERROR_92020, skuVO.getSkuNo());
                    }else if(historyReceiveQty > 0 && returnQty == detailDto.getReceiveQty() + historyReceiveQty){
                        BigDecimal returnAmount = soReturnDetailEntity.getReturnAmount();
                        BigDecimal taxReturnAmount = soReturnDetailEntity.getTaxReturnAmount();
                        BigDecimal returnAmountLocalCurrency = soReturnDetailEntity.getReturnAmountLocalCurrency();
                        BigDecimal taxReturnAmountLocalCurrency = soReturnDetailEntity.getTaxReturnAmountLocalCurrency();
                        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntityList = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).collect(Collectors.toList());
                        for (SoReturnReceiveDetailEntity soReturnReceiveDetail : soReturnReceiveDetailEntityList) {
                            returnAmount = returnAmount.subtract(soReturnReceiveDetail.getReturnAmount()) ;
                            taxReturnAmount = taxReturnAmount.subtract(soReturnReceiveDetail.getTaxReturnAmount());
                            returnAmountLocalCurrency = returnAmountLocalCurrency.subtract(soReturnReceiveDetail.getReturnAmountLocalCurrency());
                            taxReturnAmountLocalCurrency = taxReturnAmountLocalCurrency.subtract(soReturnReceiveDetail.getTaxReturnAmountLocalCurrency());
                        }
                        detailEntity.setReturnAmount(returnAmount);
                        detailEntity.setTaxReturnAmount(taxReturnAmount);
                        detailEntity.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
                        detailEntity.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
                    }
                }
                list.add(detailEntity);
            }
            return this.saveBatch(list);
        }
    }

    private Boolean addB2c(SoReturnReceiveDTO.Add dto, String id) {
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        //如果有退货订单号
        if (CharSequenceUtil.isNotBlank(dto.getSourceId())) {
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
            SoB2cReturnEntity soB2cReturnEntity = FeignQuery.getById(SoB2cReturnEntity.class,dto.getSourceId());
            List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class,returnDetailIds);
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Collections.singletonList(dto.getSourceId()));
            for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
                detailEntity.setMainId(id);
                detailEntity.setSkuId(detailDto.getSkuId());
                detailEntity.setSkuNo(detailDto.getSkuNo());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setNoticeDetailId(detailDto.getNoticeDetailId());
                Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                if("B2C".equals(dto.getType())){
                    returnQty = soB2cReturnDetailEntityList.stream().filter(v->v.getId().equals(detailDto.getSourceDetailId())).map(SoB2cReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if(Objects.nonNull(soB2cReturnEntity)){
                        detailEntity.setReturnTypeDict(soB2cReturnEntity.getType());
                        detailEntity.setReturnReasonDict(soB2cReturnEntity.getReason());
                    }
                }
                //此单历史签收数量
                Integer historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (detailDto.getReceiveQty() + historyReceiveQty > returnQty) {
                    throw new ServiceException(ApiError.ERROR_92020);
                }
                list.add(detailEntity);
            }
            return this.saveBatch(list);
        } else {
            List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
            for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
                SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
                detailEntity.setMainId(id);
                detailEntity.setSkuId(detailDto.getSkuId());
                detailEntity.setSkuNo(skuVO.getSkuNo());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
                detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
                list.add(detailEntity);
            }
            this.saveBatch(list);
            //标记SKU
            plmTaskFeign.updateOccupyStatus(skuIds);
            return Boolean.TRUE;
        }
    }

    /**
     * 无退货订单新增
     * @Author Luo_WG
     * @Date 2023/7/24 14:47
     * @param dto
     * @param id
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    private Boolean notReturnOrderAdd(SoReturnReceiveDTO.Add dto, String id) {
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Add::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
        //sku映射表
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setCutomerId(dto.getCustomerId());
        List<String> skuNos = skuInfoByIds.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        skuParamDTO.setSkuNoList(skuNos);
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingFeign.listSkuBySkuNos(skuParamDTO);
        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        for (SoReturnReceiveDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setMainId(id);
            detailEntity.setSkuId(detailDto.getSkuId());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            //获取平台sku
            if(StringUtils.isBlank(detailDto.getPlatformSkuNo())){
                String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(skuVO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
                detailEntity.setPlatformSkuNo(platformSkuNo);
            }else {
                detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            }
            detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
            detailEntity.setReturnAmount(detailDto.getReturnAmount());
            detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            if(null == detailDto.getExchangeRate()){
                detailEntity.setExchangeRate(dto.getExchangeRate());
            }else{
                detailEntity.setExchangeRate(detailDto.getExchangeRate());
            }
            list.add(detailEntity);
        }
        this.saveBatch(list);
        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        if("B2C".equals(dto.getType())){
            //B2C
            return updateB2c(dto);
        }else {
            //B2B
            return updateB2b(dto);
        }
    }

    private Boolean updateB2b(SoReturnReceiveDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
        //如果没有退货订单号
        if (CharSequenceUtil.isBlank(dto.getSourceId())) {
            return notReturnOrderUpdate(dto);
        }else {
            //退货订单id
            String soReturnId = dto.getSourceId();
            //B2B退货订单明细集合
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByMainId(soReturnId);
            //B2B退货通知单
            List<SoReturnNoticeDetailEntity> soReturnNoticeDetailEntities = soReturnNoticeDetailService.listDetailBySourceIds(Collections.singletonList(soReturnId));
            //B2B退货签收单
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Collections.singletonList(soReturnId));
            List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
            //原明细数据
            List<SoReturnReceiveDetailEntity> oldList = this.listDetailByMainId(dto.getId());
            List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                List<SoReturnReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
                //操作日志
                List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),pairList,"编辑操作");
                this.removeByIds(deleteIds);
            }
            List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);
            //sku映射表
            SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
            skuParamDTO.setCutomerId(dto.getCustomerId());
            List<String> skuNos = skuInfoByIds.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
            skuParamDTO.setSkuNoList(skuNos);
            List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingFeign.listSkuBySkuNos(skuParamDTO);
            List<SoReturnReceiveDetailDTO.Update> detailList = dto.getDetailList();
            if(CollUtil.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_92172);
            }
            for (SoReturnReceiveDetailDTO.Update detailDto : dto.getDetailList()) {
                SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
                if (CharSequenceUtil.isNotBlank(detailDto.getId())) {
                    detailEntity.setId(detailDto.getId());
                }
                detailEntity.setId(detailDto.getId());
                detailEntity.setMainId(dto.getId());
                detailEntity.setSkuId(detailDto.getSkuId());
                detailEntity.setSkuNo(skuVO.getSkuNo());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
                detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
                //获取平台sku
                if(StringUtils.isBlank(detailDto.getPlatformSkuNo())){
                    String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(skuVO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
                    detailEntity.setPlatformSkuNo(platformSkuNo);
                }else {
                    detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
                }
                detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
                detailEntity.setReturnAmount(detailDto.getReturnAmount());
                detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
                detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
                detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
                if(null == detailDto.getExchangeRate()){
                    detailEntity.setExchangeRate(dto.getExchangeRate());
                }else{
                    detailEntity.setExchangeRate(detailDto.getExchangeRate());
                }
                if(Boolean.FALSE.equals(detailDto.getIsChildSkuNo()) //子sku不做数量校验
                        && StringUtils.isNotBlank(detailDto.getSourceDetailId())){
                    SoReturnDetailEntity soReturnDetailEntity = soReturnDetailEntities.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                        throw new ServiceException(ApiError.ERROR_92023, detailDto.getSkuNo());
                    }
                    Integer returnQty = 0;
                    if(CollectionUtils.isNotEmpty(soReturnDetailEntities)){
                        returnQty = soReturnDetailEntities.stream()
                                .filter(req -> req.getId().equals(detailDto.getSourceDetailId()))
                                .map(SoReturnDetailEntity::getReturnQty)
                                .reduce(MathUtil.ZERO, Integer::sum);
                    }
                    //此单历史签收数量
                    Integer historyReceiveQty = soReturnReceiveDetailEntities.stream()
                            .filter(req -> !deleteIds.contains(req.getId()))
                            .filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                            .map(SoReturnReceiveDetailEntity::getReceiveQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
                    if(StringUtils.isNotBlank(detailDto.getId())){
                        historyReceiveQty = soReturnReceiveDetailEntities.stream()
                                .filter(req -> !deleteIds.contains(req.getId()))
                                .filter(req -> !req.getId().equals(detailDto.getId()) && req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                                .map(SoReturnReceiveDetailEntity::getReceiveQty)
                                .reduce(MathUtil.ZERO, Integer::sum);
                    }
                    if (returnQty < detailDto.getReceiveQty() + historyReceiveQty) {
                        throw new ServiceException(ApiError.ERROR_92020, skuVO.getSkuNo());
                    }else if(historyReceiveQty > 0 && returnQty == detailDto.getReceiveQty() + historyReceiveQty){
                        BigDecimal returnAmount = soReturnDetailEntity.getReturnAmount();
                        BigDecimal taxReturnAmount = soReturnDetailEntity.getTaxReturnAmount();
                        BigDecimal returnAmountLocalCurrency = soReturnDetailEntity.getReturnAmountLocalCurrency();
                        BigDecimal taxReturnAmountLocalCurrency = soReturnDetailEntity.getTaxReturnAmountLocalCurrency();

                        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntityList = soReturnReceiveDetailEntities.stream()
                                .filter(req -> !deleteIds.contains(req.getId()))
                                .filter( req -> !req.getId().equals(detailDto.getId()) && req.getSourceDetailId().equals(detailDto.getSourceDetailId()))
                                .collect(Collectors.toList());
                        for (SoReturnReceiveDetailEntity soReturnReceiveDetail : soReturnReceiveDetailEntityList) {
                            returnAmount = returnAmount.subtract(soReturnReceiveDetail.getReturnAmount()) ;
                            taxReturnAmount = taxReturnAmount.subtract(soReturnReceiveDetail.getTaxReturnAmount());
                            returnAmountLocalCurrency = returnAmountLocalCurrency.subtract(soReturnReceiveDetail.getReturnAmountLocalCurrency());
                            taxReturnAmountLocalCurrency = taxReturnAmountLocalCurrency.subtract(soReturnReceiveDetail.getTaxReturnAmountLocalCurrency());
                        }
                        detailEntity.setReturnAmount(returnAmount);
                        detailEntity.setTaxReturnAmount(taxReturnAmount);
                        detailEntity.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
                        detailEntity.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
                    }
                }
                list.add(detailEntity);
                //修改操作日志
                if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                    SoReturnReceiveDetailEntity old = this.getById(detailEntity.getId());
                    operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), dto.getId(),"",String.format("【%s】",old.getSkuNo()));
                }
            }
            //添加操作日志
            if (CollectionUtils.isNotEmpty(addList)) {
                List<SoReturnReceiveDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
                List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), addPairList, "编辑操作");
            }
            return this.saveOrUpdateBatch(list);
        }
    }

    private Boolean updateB2c(SoReturnReceiveDTO.Update dto) {
        //如果有退货订单号
        if (CharSequenceUtil.isNotBlank(dto.getSourceId())) {
            List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
            //获取退货单详情表id
            List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
            List<SoReturnDetailEntity> soReturnDetailEntities = soReturnFeign.listDetailByIds(returnDetailIds);
            List<SoB2cReturnDetailEntity> soB2cReturnDetailEntityList = FeignQuery.getByIds(SoB2cReturnDetailEntity.class,returnDetailIds);
            List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
            List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = this.listDetailBySourceIds(Collections.singletonList(dto.getSourceId()));
            //原明细数据
            List<SoReturnReceiveDetailEntity> oldList = this.listDetailByMainId(dto.getId());
            List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                List<SoReturnReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
                //操作日志
                List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),pairList,"编辑操作");
                this.removeByIds(deleteIds);
            }
            for (SoReturnReceiveDetailDTO.Update detailDto : dto.getDetailList()) {
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();

                //此单历史签收数量
                Integer historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (CharSequenceUtil.isNotBlank(detailDto.getId())) {
                    detailEntity.setId(detailDto.getId());
                    historyReceiveQty = soReturnReceiveDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                if("B2C".equals(dto.getType())){
                    SoB2cReturnDetailEntity soReturnDetailEntity = soB2cReturnDetailEntityList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(null);
                    if (ObjectUtil.isEmpty(soReturnDetailEntity)) {
                        throw new ServiceException(ApiError.ERROR_92023, detailDto.getSkuNo());
                    }
                    Integer returnQty = soB2cReturnDetailEntityList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).map(SoB2cReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (detailDto.getReceiveQty() + historyReceiveQty > returnQty) {
                        throw new ServiceException(ApiError.ERROR_92020);
                    }
                    detailEntity.setSkuId(soReturnDetailEntity.getSkuId());
                    detailEntity.setSkuNo(soReturnDetailEntity.getSkuNo());
                }
                detailEntity.setId(detailDto.getId());
                detailEntity.setMainId(dto.getId());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
                detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
                list.add(detailEntity);
                //修改操作日志
                if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                    SoReturnReceiveDetailEntity old = this.getById(detailEntity.getId());
                    operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
                }
            }
            //添加操作日志
            if (CollectionUtils.isNotEmpty(addList)) {
                List<SoReturnReceiveDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
                List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), addPairList, "编辑操作");
            }
            return this.saveOrUpdateBatch(list);
        } else {
            List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());

            List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
            List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);

            //原明细数据
            List<SoReturnReceiveDetailEntity> oldList = this.listDetailByMainId(dto.getId());
            List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                List<SoReturnReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
                //操作日志
                List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),pairList,"编辑操作");
                this.removeByIds(deleteIds);
            }
            for (SoReturnReceiveDetailDTO.Update detailDto : dto.getDetailList()) {
                SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
                SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
                detailEntity.setId(detailDto.getId());
                detailEntity.setMainId(dto.getId());
                detailEntity.setSkuId(detailDto.getSkuId());
                detailEntity.setSkuNo(skuVO.getSkuNo());
                detailEntity.setReturnQty(detailDto.getReturnQty());
                detailEntity.setReceiveQty(detailDto.getReceiveQty());
                detailEntity.setRemark(detailDto.getRemark());
                detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
                detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
                detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
                list.add(detailEntity);
                //修改操作日志
                if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                    SoReturnReceiveDetailEntity old = this.getById(detailEntity.getId());
                    operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
                }
            }
            //添加操作日志
            if (CollectionUtils.isNotEmpty(addList)) {
                List<SoReturnReceiveDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
                List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), addPairList, "编辑操作");
            }
            this.saveOrUpdateBatch(list);
            //标记SKU
            plmTaskFeign.updateOccupyStatus(skuIds);
            return Boolean.TRUE;
        }
    }

    /**
     * 无退货订单修改
     * @Author Luo_WG
     * @Date 2023/7/24 14:47
     * @param dto
     * @return java.util.List<com.erp.model.wms.entity.SoReturnReceiveDetailEntity>
     **/
    private Boolean notReturnOrderUpdate(SoReturnReceiveDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> CharSequenceUtil.isBlank(c.getId())).map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());

        List<SoReturnReceiveDetailEntity> list = new ArrayList<>();
        List<String> skuIds = dto.getDetailList().stream().map(SoReturnReceiveDetailDTO.Update::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);

        //原明细数据
        List<SoReturnReceiveDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnReceiveDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        //sku对照表
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setCutomerId(dto.getCustomerId());
        List<String> skuNos = skuInfoByIds.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
        skuParamDTO.setSkuNoList(skuNos);
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingFeign.listSkuBySkuNos(skuParamDTO);
        for (SoReturnReceiveDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnReceiveDetailEntity detailEntity = new SoReturnReceiveDetailEntity();
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(detailDto.getSkuId())).findFirst().orElse(new SkuVO());
            detailEntity.setId(detailDto.getId());
            detailEntity.setMainId(dto.getId());
            detailEntity.setSkuId(detailDto.getSkuId());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setReturnQty(detailDto.getReturnQty());
            detailEntity.setReceiveQty(detailDto.getReceiveQty());
            detailEntity.setRemark(detailDto.getRemark());
            detailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            detailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            detailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            //获取平台sku
            if(StringUtils.isBlank(detailDto.getPlatformSkuNo())){
                String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(skuVO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
                detailEntity.setPlatformSkuNo(platformSkuNo);
            }else {
                detailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            }
            detailEntity.setIsChildSkuNo(detailDto.getIsChildSkuNo());
            detailEntity.setReturnAmount(detailDto.getReturnAmount());
            detailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            detailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            detailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            if(null == detailDto.getExchangeRate()){
                detailEntity.setExchangeRate(dto.getExchangeRate());
            }else{
                detailEntity.setExchangeRate(detailDto.getExchangeRate());
            }
            list.add(detailEntity);
            //修改操作日志
            if (CharSequenceUtil.isNotBlank(detailEntity.getId())) {
                SoReturnReceiveDetailEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old,detailEntity, ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnReceiveDetailEntity> returnNoticeDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnNoticeDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), addPairList, "编辑操作");
        }
        this.saveOrUpdateBatch(list);
        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
        return Boolean.TRUE;
    }

    private List<String> getDeleteIds(List<SoReturnReceiveDetailDTO.Update> newList, List<SoReturnReceiveDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(SoReturnReceiveDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnReceiveDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnReceiveDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnReceiveDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailBySourceIds(sourceIds);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        if (CollectionUtils.isEmpty(sourceDetailIds)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailByIds(ids);
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainId(String id) {
        return lambdaQuery().eq(SoReturnReceiveDetailEntity::getMainId, id).list();
    }

    @Override
    public List<SoReturnReceiveDetailEntity> listDetailByMainIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listDetailByMainIds(ids);
    }

}
