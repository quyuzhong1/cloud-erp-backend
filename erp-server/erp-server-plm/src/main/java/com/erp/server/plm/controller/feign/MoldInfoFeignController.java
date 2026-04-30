package com.erp.server.plm.controller.feign;

import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.erp.model.scm.dto.AssetNoticeDetailDTO;
import com.erp.server.plm.mapper.MoldInfoMapper;
import com.erp.server.plm.service.MoldInfoService;
import com.erp.server.plm.service.MoldRefSkuService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/10/29 10:58
 * @Param:
 * @Return:
 * @Description:
 **/
@RestController
@RequestMapping("/feign/moldInfo")
public class MoldInfoFeignController {

    @Resource
    private MoldInfoService moldInfoService;

    @Resource
    private MoldInfoMapper moldInfoMapper;

    @Resource
    private MoldRefSkuService moldRefSkuService;

    @PostMapping("/getMoldInfo")
    MoldInfoEntity getMoldInfoByCode(@RequestBody String moldCode){
        return moldInfoService.lambdaQuery().eq(MoldInfoEntity::getCode,moldCode).one();
    }

    @PostMapping("/listMoldInfoByCodes")
    List<MoldInfoEntity> listMoldInfoByCodes(@RequestBody List<String> moldCodes){
        if (moldCodes == null || moldCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return moldInfoService.lambdaQuery().in(MoldInfoEntity::getCode, moldCodes).list();
    }

    @PostMapping("/listMoldCodesByProjectName")
    List<String> listMoldCodesByProjectName(@RequestBody String projectName){
        if (projectName == null || projectName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<MoldInfoEntity> moldInfoList = moldInfoService.lambdaQuery()
                .like(MoldInfoEntity::getProjectName, projectName)
                .list();
        return moldInfoList.stream()
                .map(MoldInfoEntity::getCode)
                .filter(code -> code != null && !code.trim().isEmpty())
                .collect(Collectors.toList());
    }

    @PostMapping("/searchMoldRefSkuByAssetId")
    List<AssetNoticeDetailDTO.AssetDetailRefSkuDTO> searchMoldRefSkuByAssetId(@RequestBody String assetId){
        return moldInfoMapper.searchMoldRefSkuByAssetId(assetId);
    }

    @PostMapping("/listInvalidMoldCodesForRefSku")
    List<String> listInvalidMoldCodesForRefSku(@RequestBody List<String> moldCodes) {
        if (moldCodes == null || moldCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> distinctMoldCodes = moldCodes.stream()
                .filter(code -> code != null && !code.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (distinctMoldCodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> codeToMoldIdMap = moldInfoService.lambdaQuery()
                .select(MoldInfoEntity::getId, MoldInfoEntity::getCode)
                .in(MoldInfoEntity::getCode, distinctMoldCodes)
                .list()
                .stream()
                .collect(Collectors.toMap(
                        MoldInfoEntity::getCode,
                        MoldInfoEntity::getId,
                        (oldValue, newValue) -> oldValue
                ));
        List<String> moldIds = codeToMoldIdMap.values().stream().distinct().collect(Collectors.toList());

        Map<String, List<MoldRefSkuEntity>> refSkuMap = Collections.emptyMap();
        if (!moldIds.isEmpty()) {
            refSkuMap = moldRefSkuService.lambdaQuery()
                    .select(MoldRefSkuEntity::getMoldId, MoldRefSkuEntity::getApproveStatus)
                    .in(MoldRefSkuEntity::getMoldId, moldIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(MoldRefSkuEntity::getMoldId));
        }

        Map<String, List<MoldRefSkuEntity>> finalRefSkuMap = refSkuMap;
        return distinctMoldCodes.stream()
                .filter(moldCode -> {
                    String moldId = codeToMoldIdMap.get(moldCode);
                    if (moldId == null) {
                        return true;
                    }
                    List<MoldRefSkuEntity> refSkuList = finalRefSkuMap.get(moldId);
                    return refSkuList == null || refSkuList.isEmpty()
                            || refSkuList.stream().anyMatch(refSku -> !ApproveStatusEnum.APPROVE.getStatus().equals(refSku.getApproveStatus()));
                })
                .collect(Collectors.toList());
    }

    /**
     * 批量通过模具code获取供应商信息
     * @param moldCodes 模具编码列表
     * @return Map<String, MoldInfoDTO.SupplierInfoByCodeDTO> key为模具编码，value为供应商信息
     */
    @PostMapping("/batchGetSupplierInfoByCodes")
    Map<String, MoldInfoDTO.SupplierInfoByCodeDTO> batchGetSupplierInfoByCodes(@RequestBody List<String> moldCodes) {
        if (moldCodes == null || moldCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        // 去重
        List<String> distinctCodes = moldCodes.stream().distinct().filter(code -> code != null && !code.trim().isEmpty()).collect(Collectors.toList());
        if (distinctCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 批量查询模具信息
        List<MoldInfoEntity> moldInfoList = moldInfoService.lambdaQuery()
                .in(MoldInfoEntity::getCode, distinctCodes)
                .list();
        
        // 转换为Map，key为模具编码，value为供应商信息
        Map<String, MoldInfoDTO.SupplierInfoByCodeDTO> resultMap = new HashMap<>();
        for (MoldInfoEntity moldInfo : moldInfoList) {
            if (moldInfo.getCode() != null) {
                resultMap.put(moldInfo.getCode(), new MoldInfoDTO.SupplierInfoByCodeDTO(
                        moldInfo.getSupplierId(),
                        moldInfo.getSupplierCode(),
                        moldInfo.getSupplierName()
                ));
            }
        }
        return resultMap;
    }
}
