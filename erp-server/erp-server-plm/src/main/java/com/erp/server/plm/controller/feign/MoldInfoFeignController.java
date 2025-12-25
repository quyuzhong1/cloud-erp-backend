package com.erp.server.plm.controller.feign;

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
import java.util.List;
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
}
