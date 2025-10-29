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
import java.util.List;

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

    @PostMapping("/searchMoldRefSkuByAssetId")
    List<AssetNoticeDetailDTO.AssetDetailRefSkuDTO> searchMoldRefSkuByAssetId(@RequestBody String assetId){
        return moldInfoMapper.searchMoldRefSkuByAssetId(assetId);
    }
}
