package com.erp.rpc.dmp.feign;


import cn.hutool.json.JSONObject;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.dto.KingdeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Will
 * @description: DMP远程调用接口
 * @date: 2023/1/12 16:54
 */
@FeignClient("erp-dmp")
public interface DmpTaskFeign {

    //店铺id查询店铺
    @PostMapping("feign/getShopById")
    DmpShopInfoDTO getShopById(@RequestBody String shopId);

    /**
     * 根据条件获详情
     * @author yl
     * @date 2023-06-07 10:42
     * @param dto
     * @return cn.hutool.json.JSONObject
     */
    @PostMapping("feign/getByKingdeeId")
    JSONObject getByKingdeeId(@RequestBody KingdeeDTO dto );

    /**
     * 生成金蝶销售变更单
     * @param paramMap
     */
    @PostMapping("feign/createkingdeeSoChange")
    String createkingdeeSoChange(@RequestBody Map<String, Object> paramMap);
}