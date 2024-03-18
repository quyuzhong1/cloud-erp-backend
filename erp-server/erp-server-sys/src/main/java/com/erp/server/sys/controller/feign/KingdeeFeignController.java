package com.erp.server.sys.controller.feign;


import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.DeptKingdeeDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.KingdeeOperatorRefPostService;
import com.erp.server.sys.service.KingdeeUserRefPostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    private KingdeeUserRefPostService kingdeeUserRefPostService;


    @Resource
    private KingdeeOperatorRefPostService kingdeeOperatorRefPostService;

    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;

    /**
     * 获取部门信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getDeptInfo")
    public KingdeeDepartmentEntity getInfo(@RequestBody DeptKingdeeDTO.FindDeptKingdeeDTO dto) {
        return kingdeeDepartmentService.getInfo(dto);
    }


    /**
     * 获取员工任刚信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getUserKingdeePost")
    public KingdeePostDTO.UserKingdeePostInfoDTO getUserKingdeePost(@RequestBody KingdeePostDTO.FindUserKingdeePostInfoDTO dto) {
        KingdeePostDTO.UserKingdeePostInfoDTO result = kingdeeUserRefPostService.getUserKingdeePost(dto);
        return result;
    }

    /**
     * 获取到业务员信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/getBusinessOperator")
    public KingdeeOperatorRefPostDTO.OperatorDTO getUserKingdeePost(@RequestBody KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO dto) {
        KingdeeOperatorRefPostDTO.OperatorDTO result = kingdeeOperatorRefPostService.find(dto);
        return result;
    }



}
