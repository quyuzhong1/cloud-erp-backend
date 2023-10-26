package com.erp.server.sys.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.DeptKingdeeEntity;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.server.sys.service.DeptKingdeeService;
import com.erp.server.sys.service.KingdeeBusinessOperatorService;
import com.erp.server.sys.service.UserKingdeePostService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@RestController
@RequestMapping("/feign/kingdee")
public class KingdeeFeignController extends BaseController {


    @Resource
    private DeptKingdeeService deptKingdeeService;

    @Resource
    private UserKingdeePostService userKingdeePostService;

    @Resource
    private KingdeeBusinessOperatorService kingdeeBusinessOperatorService;

    /**
     * 获取部门信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getDeptInfo")
    public DeptKingdeeEntity getInfo(@RequestBody DeptKingdeeDTO.FindDeptKingdeeDTO dto) {
        return deptKingdeeService.getInfo(dto);
    }


    /**
     * 获取员工任刚信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getUserKingdeePost")
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePost(@RequestBody KingdeePostDTO.FindUserKingdeePostInfoDTO dto) {
        KingdeePostDTO.UserKingdeePostInfoDTO result = userKingdeePostService.getUserKingdeePost(dto);
        return result;
    }

    /**
     * 获取到业务员信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getBusinessOperator")
    public KingdeeBusinessOperatorEntity getUserKingdeePost(@RequestBody KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto) {
        KingdeeBusinessOperatorEntity result = kingdeeBusinessOperatorService.find(dto);
        return result;
    }


    /**
     * 获取员工任岗信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getUserKingdeePostByPostCode")
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePostByInfo(@RequestBody KingdeePostDTO.FindUserKingdeePostDTO dto) {
        KingdeePostDTO.UserKingdeePostInfoDTO result = userKingdeePostService.getUserKingdeePostInfoByPostCode(dto);
        return result;
    }


    /**
     * 根据用户获取到业务员信息
     *
     * @param userIdList
     * @return
     */
    @PostMapping("/listBusinessOperatorByUserIdList")
    public List<KingdeeBusinessOperatorEntity> listBusinessOperatorByUserIdList(@RequestBody List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return kingdeeBusinessOperatorService.list();
        }
        return kingdeeBusinessOperatorService.lambdaQuery().in(KingdeeBusinessOperatorEntity::getErpUserId, userIdList).list();
    }

}
