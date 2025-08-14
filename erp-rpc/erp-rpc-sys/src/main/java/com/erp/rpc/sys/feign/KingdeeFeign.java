package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Lambda
 * @Classname DeptKingFeign
 * @Date 2023-07-07 1:38
 * @Created by yl
 */

@FeignClient(name = "erp-sys", contextId = "kingdeeFeign",configuration = {FeignErrorDecoder.class})
public interface KingdeeFeign {

    @PostMapping("/feign/kingdee/getDeptInfo")
    KingdeeDepartmentEntity getDeptKingdee(@RequestBody DeptKingdeeDTO.FindDeptKingdeeDTO dto);


    @PostMapping("/feign/kingdee/getUserKingdeePost")
    KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePost(@RequestBody KingdeePostDTO.FindUserKingdeePostInfoDTO  getUserKingdeePost);

    /**
     * 获取到岗位信息 根据岗位code
     * @param getUserKingdeePost
     * @return
     */
    @PostMapping("/feign/kingdee/getUserKingdeePostByPostCode")
    KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePostByPostCode(@RequestBody KingdeePostDTO.FindUserKingdeePostDTO  getUserKingdeePost);
    /**
     * 获取到业务员信息
     * @param dto
     * @return
     */
    @PostMapping("/feign/kingdee/getBusinessOperator")
    KingdeeOperatorRefPostDTO.OperatorDTO getBusinessOperator(@RequestBody KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO  dto);

    @PostMapping("/feign/kingdee/listOperatorByUserIdList")
    List<KingdeeOperatorRefPostDTO.OperatorDTO> listBusinessOperatorByUserIdList(@RequestBody  List<String> userIdList);

    /**
     * 业务员列表 用于B2B 销售订单下拉
     */
    @PostMapping("/feign/kingdee/listKingdeeUser")
    public ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> listKingdeeUser (@RequestBody KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto);
}
