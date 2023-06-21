package com.erp.rpc.sys.feign;

import com.erp.model.sys.dto.DictBasicDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Classname: SysDictFeign
 * @Description: 字典feign接口
 * @CreateTime: 2023-06-21  15:10
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-sys", contextId = "dictBasic")
public interface SysDictFeign {

    /**
     * 获取字典数据 根据属性
     * @param type
     * @return
     */
    @GetMapping("/feign/dictBasic/getByType")
    List<DictBasicDTO.ViewDTO> getByType(@RequestParam(value = "type") String type);

}
