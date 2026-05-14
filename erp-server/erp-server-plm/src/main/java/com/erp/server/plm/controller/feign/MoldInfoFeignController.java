package com.erp.server.plm.controller.feign;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.scm.dto.AssetNoticeDetailDTO;
import com.erp.server.plm.mapper.MoldInfoMapper;
import com.erp.server.plm.service.MoldInfoService;
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

    /**
     * 高级查询模具档案：调用方把 advanceQueryDTOList 的 field 用 mi.xxx 别名传入，
     * 经 @WebAdvanceQuery 切面解析后写入 container.sqlMap，
     * 由 mapper 用 ${params.sqlMap.default} 拼接，完整支持 EQ / CONTAINS / STARTS_WITH
     * 等所有比较符及大小写不敏感匹配。
     */
    @PostMapping("/listMoldInfoAdvanceQuery")
    @WebAdvanceQuery
    List<MoldInfoEntity> listMoldInfoAdvanceQuery(@RequestBody AdvanceQueryContainer advanceQueryContainer) {
        return moldInfoMapper.listMoldInfoAdvanceQuery(advanceQueryContainer);
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
