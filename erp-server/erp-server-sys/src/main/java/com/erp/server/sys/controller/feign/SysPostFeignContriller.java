package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.server.sys.service.SysPostService;
import com.erp.server.sys.service.SysPostUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/post")
public class SysPostFeignContriller extends BaseController {
    @Resource
    private SysPostService sysPostService;
    @Resource
    private SysPostUserService sysPostUserService;

    /**
     * 根据Ids查询岗位
     * @Author Luo_WG
     * @Date 2024/1/11 19:51
     * @param ids
     * @return java.util.List<com.erp.model.sys.entity.SysPostEntity>
     **/
    @PostMapping("/listById")
    public List<SysPostEntity> listById(@RequestBody List<String> ids) {
        return sysPostService.listByIds(ids);
    }


    /**
     * 根据id查询岗位
     * @Author Luo_WG
     * @Date 2024/1/11 19:51
     * @param id
     * @return com.erp.model.sys.entity.SysPostEntity
     **/
    @GetMapping("/getById")
    public SysPostEntity getById(@RequestParam("id") String id) {
        return sysPostService.getById(id);
    }

    /**
     * 根据用户id查询用户岗位
     * @Author Luo_WG
     * @Date 2024/1/12 10:57
     * @param userId
     * @return java.util.List<com.erp.model.sys.entity.SysPostUserEntity>
     **/
    @GetMapping("/getPostUserByUserId")
    public List<SysPostUserEntity> getPostUserByUserId(@RequestParam("userId") String userId) {
        return sysPostUserService.getByUserId(userId);
    }

    /**
     * 通过岗位id查询岗位下全部人员
     **/
    @PostMapping("/getUserIdByPostIds")
    public List<SysPostUserEntity> getUserIdByPostIds(@RequestBody List<String> ids) {
        return sysPostUserService.getUserIdByPostIds(ids);
    }
}
