package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.config.VirtualInventoryHelper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.VirtualInventoryStockService;
import com.erp.server.wms.service.VirtualInventoryTransCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 虚拟库存交易核心处理类
 * @author will
 * @date 2024/6/5 17:44
 */
@Service
public class VirtualInventoryTransCoreServiceImpl implements VirtualInventoryTransCoreService {


    @Resource
    private VirtualInventoryHelper virtualInventoryHelper;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DictBasicService dictBasicService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(VirtualInventoryStockDTO.StockParamDTO dto) {
        ValidatorUtil.validateEntity(dto);
        //BOM拆分
        List<VirtualInventoryStockDTO.OutInStockDTO> outInStockList = splitBom(dto.getParamList(), dto.getIsSplitBom());
        if (CollUtil.isEmpty(outInStockList)) {
            outInStockList = dto.getParamList();
        }
        VirtualInventoryStockService virtualInventoryStockService = virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        virtualInventoryStockService.approve(outInStockList, dto.getRules(), VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), CollUtil.isEmpty(dto.getRules()) ? true : false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(VirtualInventoryStockDTO.TransferParamDTO dto) {
        ValidatorUtil.validateEntity(dto);
        VirtualInventoryStockService virtualInventoryStockService = virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.TRANSFER_STOCK);
        virtualInventoryStockService.approve(dto.getParamList(), dto.getRules(), VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), CollUtil.isEmpty(dto.getRules()) ? true : false);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK)
                .unApprove(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUnApprove(InventoryBatchUnApproveDTO dto) {
        ValidatorUtil.validateEntity(dto);
        VirtualInventoryStockService virtualInventoryStockService = virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        dto.getBillIds().forEach(billId->{
            InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
            inventoryUnApproveDTO.setSourceType(dto.getSourceType());
            inventoryUnApproveDTO.setBillId(billId);
            virtualInventoryStockService.unApprove(inventoryUnApproveDTO);
        });
    }

    /**
     * 拆分BOM
     * @author will
     * @date 2024/7/21 16:02
     * @param paramList
     * @return List<OutInStockDTO>
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> splitBom (List<VirtualInventoryStockDTO.OutInStockDTO> paramList,Boolean isSplitBom) {
        List<VirtualInventoryStockDTO.OutInStockDTO> resultList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(isSplitBom) && !isSplitBom) {
            return resultList;
        }
        //是否拆分bom
        List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(DictBasicEnum.VIRTUAL_SPLIT_BOM.getKey());
        if (CollUtil.isEmpty(list) || !Boolean.valueOf(list.get(0).getValue())) {
            return resultList;
        }

        List<String> skuIdList = paramList.stream().map(VirtualInventoryStockDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);
        if (CollUtil.isEmpty(bomChildrenSkuList)) {
            return resultList;
        }
        List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        if (CollUtil.isEmpty(bomList)) {
            return resultList;
        }
        for (VirtualInventoryStockDTO.OutInStockDTO outInStockDTO : paramList) {
            //bom信息
            List<BomChildrenSkuDTO> childList = bomList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), outInStockDTO.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childList)) {
                resultList.add(outInStockDTO);
                continue;
            }
            //优先取录入的bom版本，没有则取最新bom版本
            if (StrUtil.isNotBlank(outInStockDTO.getBomVersion())) {
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),outInStockDTO.getBomVersion())).collect(Collectors.toList());
            } else {
                String bomVersion = childList.stream().max(Comparator.comparing(BomChildrenSkuDTO::getBomVersion)).map(BomChildrenSkuDTO::getBomVersion).get();
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),bomVersion)).collect(Collectors.toList());
            }
            if (CollUtil.isEmpty(childList)) {
                throw new ServiceException(CharSequenceUtil.format("SKU【】未找到版本为【{}】的BOM",outInStockDTO.getSkuId(),outInStockDTO.getBomVersion()));
            }
            for (BomChildrenSkuDTO bomChildrenSkuDTO: childList) {
                VirtualInventoryStockDTO.OutInStockDTO newOutInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                BeanMapperUtils.copy(outInStockDTO,newOutInStockDTO);
                newOutInStockDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                newOutInStockDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                newOutInStockDTO.setQty(bomChildrenSkuDTO.getQuantity() * outInStockDTO.getQty());
                resultList.add(newOutInStockDTO);
            }
        }
        return resultList;
    }
}