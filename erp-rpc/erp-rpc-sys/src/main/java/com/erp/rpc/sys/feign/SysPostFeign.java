package com.erp.rpc.sys.feign;

import com.erp.model.sys.entity.SysPostEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-sys", contextId = "post")
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
}
