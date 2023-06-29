package com.erp.server.wms.rocketmq.sync.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.ThirdPartySystemEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpTransferInfoDetailEntity;
import com.erp.model.dmp.entity.DmpTransferInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.entity.TransferInfoDetailEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.rocketmq.sync.SyncTransferInfoService;
import com.erp.server.wms.service.TransferInfoService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description: 同步直接调拨单业务层
 * @author Will
 * @date: 2023/6/29 11:09
 */
@Service
public class SyncTransferInfoServiceImpl implements SyncTransferInfoService {

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKingdeeTransferInfo(DmpTransferInfoEntity entity) {

        TransferInfoDTO.ViewDTO viewDTO = transferInfoService.viewTransferInfoByCode(entity.getCode());
        //数据格式化
        TransferInfoEntity transferInfoEntity = handleWmsTransferInfo(entity,viewDTO);
        if (ObjectUtils.isEmpty(viewDTO)) {
            //非已审核数据无需新增
            if (!ApproveStatusEnum.APPROVE.getStatus().equals(transferInfoEntity.getApproveStatus())) {
                return;
            }
            //不存在则新增
            TransferInfoDTO.AddDTO addDTO = BeanMapperUtils.map(TransferInfoDTO.AddDTO.class, transferInfoEntity);
            List<TransferInfoDetailDTO.AddDTO> addDetailList = BeanMapperUtils.copyList(TransferInfoDetailDTO.AddDTO.class, transferInfoEntity.getDetailList());
            addDTO.setDetailList(addDetailList);
            String id = transferInfoService.add(addDTO);
            //提交
            Boolean submit = transferInfoService.submit(Arrays.asList(id));
            if (!submit) {
                throw new ServiceException(ApiError.ERROR_1042);
            }
            //审核
            BaseApproveParamDTO paramDTO = new BaseApproveParamDTO();
            paramDTO.setIds(Arrays.asList(id));
            paramDTO.setType(WmsConstant.PASS);
            transferInfoService.approve(paramDTO);
        } else {
            //判断现有状态


            //存在则更新
            TransferInfoDTO.UpdateDTO updateDTO = BeanMapperUtils.map(TransferInfoDTO.UpdateDTO.class, transferInfoEntity);
            List<TransferInfoDetailDTO.UpdateDTO> updateDetailList = BeanMapperUtils.copyList(TransferInfoDetailDTO.UpdateDTO.class, transferInfoEntity.getDetailList());
            updateDTO.setDetailList(updateDetailList);
            transferInfoService.update(updateDTO);
        }




    }




    /**
     * @description: 处理wms需要的数据
     * @author Will
     * @date: 2023/6/29 15:10
     * @param entity
     * @return TransferInfoEntity
     */
    private TransferInfoEntity handleWmsTransferInfo (DmpTransferInfoEntity entity,TransferInfoDTO.ViewDTO viewDTO) {
        TransferInfoEntity resultEntity = new TransferInfoEntity();

        List<DmpTransferInfoDetailEntity> dmpDetailList = entity.getDetailList();
        if (CollectionUtils.isEmpty(dmpDetailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }
        //仓库信息
        List<WarehouseEntity> warehouseList = warehouseService.listByKingdeeCodeList(Arrays.asList(entity.getInWarehouseCode(), entity.getOutWarehouseCode()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99076,entity.getInWarehouseCode().concat(",").concat(entity.getOutWarehouseCode()));
        }
        //核算公司信息
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.listAccountingCompanyByCodeList(Arrays.asList(entity.getInOrgCode(), entity.getOutOrgCode()));

        //仓管员信息
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByKingdeePostCodes(Arrays.asList(entity.getWarehouseKeeperCode()));

        //主表id赋值
        if (ObjectUtils.isNotEmpty(viewDTO)) {
            resultEntity.setId(viewDTO.getId());
        }

        //编码
        resultEntity.setCode(entity.getCode());

        //平台来源
        if (entity.getCode().substring(0,2).equals("SP")) {
            resultEntity.setThirdPartySystem(ThirdPartySystemEnum.ENUM_MB.getCode());
        } else {
            resultEntity.setThirdPartySystem(ThirdPartySystemEnum.ENUM_OTHER.getCode());
        }

        //调拨类型
        resultEntity.setType(TransferTypeEnum.getCodeByKingdeeCode(entity.getTransferTypeCode()));
        //调拨方向
        resultEntity.setTransferDirection(TransferDirectionEnum.getCodeByKingdeeCode(entity.getTransferDirection()));

        resultEntity.setSourceId(entity.getSourceId());
        resultEntity.setSourceCode(entity.getCode());
        resultEntity.setSourceType(SourceTypeEnum.STK_TRANSFERDIRECT.getCode());

        //调入仓库
        String inWarehouseId = warehouseList.stream().filter(obj -> obj.getKingdeeWarehouseCode().equals(entity.getInWarehouseCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (StringUtils.isBlank(inWarehouseId)) {
            throw new ServiceException(ApiError.ERROR_99076,entity.getInWarehouseCode());
        }
        resultEntity.setInWarehouseId(inWarehouseId);
        //调出仓库
        String outWarehouseId = warehouseList.stream().filter(obj -> obj.getKingdeeWarehouseCode().equals(entity.getOutWarehouseCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (StringUtils.isBlank(outWarehouseId)) {
            throw new ServiceException(ApiError.ERROR_99076,entity.getOutWarehouseCode());
        }
        resultEntity.setOutWarehouseCode(outWarehouseId);

        if (CollectionUtils.isNotEmpty(companyList)) {
            //入库组织
            String inWarehouseCode = companyList.stream().filter(obj -> obj.getCode().equals(entity.getInOrgCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            resultEntity.setInWarehouseCode(inWarehouseCode);
            //出库组织
            String outWarehouseCode = companyList.stream().filter(obj -> obj.getCode().equals(entity.getInOrgCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            resultEntity.setOutWarehouseCode(outWarehouseCode);
        }
        //仓管员
        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            String warehousekeeperId = userKingdeePostInfoList.stream().filter(obj -> obj.getKingdeePostCode().equals(entity.getWarehouseKeeperCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            resultEntity.setWarehouseKeeperId(warehousekeeperId);
        }
        //产品信息
        List<String> skuNoList = dmpDetailList.stream().map(DmpTransferInfoDetailEntity::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);


        List<TransferInfoDetailEntity> detailList = new ArrayList<>();
        for (DmpTransferInfoDetailEntity dmpDetailEntity : dmpDetailList) {
            TransferInfoDetailEntity detailEntity = new TransferInfoDetailEntity();
            String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(dmpDetailEntity.getSkuNo())).
                    findFirst().map(SkuVO::getSkuId).orElse("");
            if (StringUtils.isBlank(skuId)) {
                throw new ServiceException(ApiError.ERROR_NOT_FOUND_SKU,dmpDetailEntity.getSkuNo());
            }
            //明细id赋值
            if (ObjectUtils.isNotEmpty(viewDTO)) {
                List<TransferInfoDetailDTO.ViewDTO> viewDetailList = viewDTO.getDetailList();
                if (CollectionUtils.isEmpty(viewDetailList)) {
                    throw new ServiceException(ApiError.ERROR_99048);
                }
                String detailId = viewDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(dmpDetailEntity.getSourceDetailId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
                detailEntity.setId(detailId);
            }
            detailEntity.setSkuId(skuId);
            detailEntity.setSkuNo(dmpDetailEntity.getSkuNo());
            detailEntity.setQty(dmpDetailEntity.getQty());
            detailEntity.setInWarehouseLocation(dmpDetailEntity.getWarehouseLocation());
            detailEntity.setSourceDetailId(dmpDetailEntity.getSourceDetailId());
            detailList.add(detailEntity);
        }
        resultEntity.setDetailList(detailList);
        return resultEntity;

    }

}

