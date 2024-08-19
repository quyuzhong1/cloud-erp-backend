package com.erp.rpc.dmp.feign;

import com.erp.model.dmp.dto.DmpPushWdtDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 推送旺店通中间表Feign
 * @date 2024-07-25
 * @author tanmujin
 */
@FeignClient(value = "erp-dmp", path = "/feign/dmpPushWdt", contextId = "dmpPushWdt")
public interface DmpPushWdtFeign {

    /**
     * 新增旺店通中间表数据
     */
    @PostMapping("/add")
    String add(@RequestBody DmpPushWdtDTO.AddDTO dto);

    /**
     * 批量新增旺店通中间表数据
     * @param addDTO
     * @return id
     * @date: 2024-07-25
     * @author: tanmujin
     */
    @PostMapping("/addBatch")
    String addBatch(@RequestBody List<DmpPushWdtDTO.AddDTO> addDTO);

    /**
     * 根据ID查询中间表数据
     * @param ids
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    @PostMapping("/listByIds")
    List<DmpPushWdtDTO.ViewDTO> listByIds(@RequestBody List<String> ids);
}
