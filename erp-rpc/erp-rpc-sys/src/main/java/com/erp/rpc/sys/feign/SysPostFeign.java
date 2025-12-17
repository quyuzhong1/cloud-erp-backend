package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.model.sys.entity.SysPostUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-sys", contextId = "sysPostFeign",configuration = {FeignErrorDecoder.class})
public interface SysPostFeign {

    /**
     * 根据Ids查询岗位
     * @Author Luo_WG
     * @Date 2024/1/11 19:51
     * @param ids
     * @return java.util.List<com.erp.model.sys.entity.SysPostEntity>
     **/
    @PostMapping("/feign/post/listById")
    List<SysPostEntity> listById(@RequestBody List<String> ids);


    /**
     * 根据id查询岗位
     * @Author Luo_WG
     * @Date 2024/1/11 19:51
     * @param id
     * @return com.erp.model.sys.entity.SysPostEntity
     **/
    @GetMapping("/feign/post/getById")
    SysPostEntity getById(@RequestParam("id") String id);

    /**
     * 根据用户id查询用户岗位
     * @Author Luo_WG
     * @Date 2024/1/12 10:57
     * @param userId
     * @return java.util.List<com.erp.model.sys.entity.SysPostUserEntity>
     **/
    @GetMapping("/feign/post/getPostUserByUserId")
    List<SysPostUserEntity> getPostUserByUserId(@RequestParam("userId") String userId);


    /**
     * 根据岗位id集合查询岗位用户关联信息
     * @author Will
     * @date: 2024/4/10 16:40
     * @param postIdList
     * @return List<SysPostUserEntity>
     */
    @GetMapping("/feign/post/listPostUserByPostIdList")
    List<SysPostUserEntity> listPostUserByPostIdList(@RequestParam("postIdList") List<String> postIdList);


    /**
     * 通过岗位id查询岗位下全部人员
     **/
    @PostMapping("/feign/post/getUserIdByPostIds")
    List<SysPostUserEntity> getUserIdByPostIds(@RequestBody List<String> ids);
}
