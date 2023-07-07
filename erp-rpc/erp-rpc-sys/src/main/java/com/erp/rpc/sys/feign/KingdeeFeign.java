package com.erp.rpc.sys.feign;

import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.DeptKingdeeEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Lambda
 * @Classname DeptKingFeign
 * @Description TODO
 * @Date 2023-07-07 1:38
 * @Created by yl
 */

@FeignClient(name = "erp-sys", contextId = "kingdee")
public interface KingdeeFeign {

    @PostMapping("/feign/kingdee/getInfo")
    DeptKingdeeEntity getKingdee(@RequestBody DeptKingdeeDTO.FindDeptKingdeeDTO dto);


    @PostMapping("/feign/kingdee/getUserKingdeePost")
    KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePost(@RequestBody KingdeePostDTO.FindUserKingdeePostInfoDTO  getUserKingdeePost);
}
