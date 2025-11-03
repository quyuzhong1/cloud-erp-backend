package com.erp.server.wms.rocketmq.sync.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.ThirdPartySystemEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;
import com.erp.model.dmp.dto.DmpTransferInfoDetailDTO;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.plm.vo.SkuVO;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @DataIdempotent(keyIdName = "entity.code", leaseTime = 30, waitTime = 20)
    public void syncKingdeeTransferInfo(DmpTransferInfoDTO entity) {

        TransferInfoDTO.ViewDTO oldTransferInfo = transferInfoService.viewTransferInfoByCode(entity.getCode());
        //数据格式化
        TransferInfoEntity newTransferInfo = handleWmsTransferInfo(entity,oldTransferInfo);
        if (ObjectUtils.isEmpty(oldTransferInfo)) {
            //非已审核数据无需新增
            if (!ApproveStatusEnum.APPROVE.getStatus().equals(newTransferInfo.getApproveStatus())) {
                return;
            }
            //不存在则新增
            TransferInfoDTO.AddDTO addDTO = BeanMapperUtils.map(TransferInfoDTO.AddDTO.class, newTransferInfo);
            List<TransferInfoDetailDTO.AddDTO> addDetailList = BeanMapperUtils.copyList(TransferInfoDetailDTO.AddDTO.class, newTransferInfo.getDetailList());
            addDTO.setDetailList(addDetailList);
            String id = transferInfoService.add(addDTO);
            //提交并审核
            submitAndApprove(id);
        } else {
            /**
             * 判断现有状态
             * 1、现有状态为已审核或审核中时需要反审核后更新数据
             * 2、如果拉取数据非已审核数据则修改数据后无需提交审核
             */
            if (!oldTransferInfo.getSourceType().equals(newTransferInfo.getSourceType())) {
                throw new ServiceException(ApiError.ERROR_99077,oldTransferInfo.getCode());
            }

            if (ApproveStatusEnum.APPROVE.getStatus().equals(oldTransferInfo.getApproveStatus())) {
                TransferInfoEntity transferInfoEntity = transferInfoService.getById(oldTransferInfo.getId());
                transferInfoService.disApprove(transferInfoEntity, Boolean.FALSE, Boolean.TRUE);
            }
            if (ApproveStatusEnum.APPROVE_ING.getStatus().equals(oldTransferInfo.getApproveStatus())) {
                transferInfoService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(Collections.singletonList(oldTransferInfo.getId())));
            }
            //存在则更新
            TransferInfoDTO.UpdateDTO updateDTO = BeanMapperUtils.map(TransferInfoDTO.UpdateDTO.class, newTransferInfo);
            List<TransferInfoDetailDTO.UpdateDTO> updateDetailList = BeanMapperUtils.copyList(TransferInfoDetailDTO.UpdateDTO.class, newTransferInfo.getDetailList());
            updateDTO.setDetailList(updateDetailList);
            transferInfoService.update(updateDTO);
            //审核
            if (ApproveStatusEnum.APPROVE.getStatus().equals(newTransferInfo.getApproveStatus())) {
                //提交并审核
                submitAndApprove(oldTransferInfo.getId());
            }
        }
    }

    /**
     * @description: 提交并审核
     * @author Will
     * @date: 2023/6/29 16:19
     * @param id
     */
    private void submitAndApprove(String id) {
        TransferInfoEntity entity = transferInfoService.getById(id);
        if (ObjUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }

        //提交
        BatchResultDTO submit = transferInfoService.submit(entity, Boolean.FALSE);
        if (!submit.getSuccess()) {
            throw new ServiceException(ApiError.ERROR_1042);
        }
        TransferInfoEntity approveEntity = transferInfoService.getById(id);
        if (ObjUtil.isEmpty(approveEntity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        //审核
        transferInfoService.approve(approveEntity,WmsConstant.PASS, "", null,Boolean.TRUE, Boolean.FALSE);
    }


    /**
     * @description: 处理wms需要的数据
     * @author Will
     * @date: 2023/6/29 15:10
     * @param entity
     * @return TransferInfoEntity
     */
    private TransferInfoEntity handleWmsTransferInfo (DmpTransferInfoDTO entity, TransferInfoDTO.ViewDTO viewDTO) {
        TransferInfoEntity resultEntity = new TransferInfoEntity();

        List<DmpTransferInfoDetailDTO> dmpDetailList = entity.getDetailList();
        if (CollectionUtils.isEmpty(dmpDetailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }

        //核算公司信息
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.listAccountingCompanyByCodeList(Arrays.asList(entity.getInOrgCode(), entity.getOutOrgCode()));

        //仓管员信息
        List<FindUserDTO> userList = sysUserFeign.listUserByCodeList(Collections.singletonList(entity.getWarehouseKeeperCode()));

        //主表id赋值
        if (ObjectUtils.isNotEmpty(viewDTO)) {
            resultEntity.setId(viewDTO.getId());
        }

        //编码
        resultEntity.setCode(entity.getCode());

        //平台来源
        if (entity.getCode().startsWith("SP") || entity.getCode().startsWith("FBA") || entity.getCode().startsWith("RV")) {
            resultEntity.setThirdPartySystem(ThirdPartySystemEnum.ENUM_MB.getCode());
        } else {
            resultEntity.setThirdPartySystem(ThirdPartySystemEnum.ENUM_KINGDEE.getCode());
        }
        //审核状态
        if (KingdeeDocStatusEnum.APPROVED.getCode().equals(entity.getApproveStatus())) {
            resultEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        } else if (KingdeeDocStatusEnum.REAPPROVE.getCode().equals(entity.getApproveStatus())) {
            resultEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        } else if (KingdeeDocStatusEnum.APPROVING.getCode().equals(entity.getApproveStatus())) {
            resultEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
        }


        //调拨类型
        resultEntity.setType(TransferTypeEnum.getCodeByKingdeeCode(entity.getTransferTypeCode()));
        //调拨方向
        resultEntity.setTransferDirection(TransferDirectionEnum.getCodeByKingdeeCode(entity.getTransferDirection()));

        resultEntity.setSourceId(entity.getSourceId());
        resultEntity.setSyncKingdeeId(entity.getSourceId());
        resultEntity.setSourceCode(entity.getCode());
        resultEntity.setSourceType(SourceTypeEnum.STK_TRANSFERDIRECT.getCode());
        resultEntity.setBillDate(entity.getBillDate().toLocalDate());

        if (CollectionUtils.isNotEmpty(companyList)) {
            //入库组织
            String inOrgId = companyList.stream().filter(obj -> obj.getCode().equals(entity.getInOrgCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            resultEntity.setInOrgId(inOrgId);
            //出库组织
            String outOrgId = companyList.stream().filter(obj -> obj.getCode().equals(entity.getOutOrgCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            resultEntity.setOutOrgId(outOrgId);
        }
        //仓管员
        if (CollectionUtils.isNotEmpty(userList)) {
            String warehousekeeperId = userList.stream().filter(obj -> obj.getCode().equals(entity.getWarehouseKeeperCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            resultEntity.setWarehouseKeeperId(warehousekeeperId);
        }
        //产品信息
        List<String> skuNoList = dmpDetailList.stream().map(DmpTransferInfoDetailDTO::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);

        //仓库信息
        List<String> warehouseCodeList = dmpDetailList.stream().flatMap(obj -> Stream.of(obj.getInWarehouseCode(), obj.getOutWarehouseCode())).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByKingdeeCodeList(warehouseCodeList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99076, JSONUtil.toJsonStr(warehouseCodeList));
        }

        List<TransferInfoDetailEntity> detailList = new ArrayList<>();
        for (DmpTransferInfoDetailDTO dmpDetailEntity : dmpDetailList) {
            TransferInfoDetailEntity detailEntity = new TransferInfoDetailEntity();
            String skuId = skuList.stream().filter(s -> s.getSkuNo().equals(dmpDetailEntity.getSkuNo())).
                    findFirst().map(SkuVO::getSkuId).orElse("");
            if (CharSequenceUtil.isBlank(skuId)) {
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

            //调入仓库
            String inWarehouseId = warehouseList.stream().filter(obj -> obj.getKingdeeWarehouseCode().equals(dmpDetailEntity.getInWarehouseCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (CharSequenceUtil.isBlank(inWarehouseId)) {
                throw new ServiceException(ApiError.ERROR_99076,dmpDetailEntity.getInWarehouseCode());
            }
            detailEntity.setInWarehouseId(inWarehouseId);
            //调出仓库
            String outWarehouseId = warehouseList.stream().filter(obj -> obj.getKingdeeWarehouseCode().equals(dmpDetailEntity.getOutWarehouseCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (CharSequenceUtil.isBlank(outWarehouseId)) {
                throw new ServiceException(ApiError.ERROR_99076,dmpDetailEntity.getOutWarehouseCode());
            }
            detailEntity.setOutWarehouseId(outWarehouseId);

            detailEntity.setSkuId(skuId);
            detailEntity.setSkuNo(dmpDetailEntity.getSkuNo());
            detailEntity.setQty(dmpDetailEntity.getQty());
            detailEntity.setInWarehouseLocation(StrUtils.strNullToEmpty(dmpDetailEntity.getInWarehouseLocation()));
            detailEntity.setOutWarehouseLocation(StrUtils.strNullToEmpty(dmpDetailEntity.getOutWarehouseLocation()));
            detailEntity.setSourceDetailId(dmpDetailEntity.getSourceDetailId());
            detailList.add(detailEntity);
        }
        resultEntity.setDetailList(detailList);
        return resultEntity;

    }

}

