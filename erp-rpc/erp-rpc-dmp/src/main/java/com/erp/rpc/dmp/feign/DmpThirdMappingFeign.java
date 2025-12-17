package com.erp.rpc.dmp.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * DMP远程调用ThirdMapping接口
 *
 * @author tanmujin
 * @date 2024-05-27
 */
@FeignClient(value = "erp-dmp", path = "/feign/dmp/thirdMapping", contextId = "dmpThirdMappingFeign",configuration = {FeignErrorDecoder.class})
public interface DmpThirdMappingFeign {

    @GetMapping("/getBySysId")
    ThirdWarehouseEntity getBySysId(@RequestParam String sysId, @RequestParam String sysType);

    @GetMapping("/getListBySysIds")
    List<ThirdMappingEntity> getListBySysIds(@RequestParam List<String> sysIds);

    @GetMapping("/getVwListBySysIds")
    List<ThirdMappingEntity> getVwListBySysIds(@RequestParam List<String> sysIds);

    /**
     * 查询绑定关系
     */
    @PostMapping("/getWhetherBind")
    Boolean getWhetherBind(@RequestBody ThirdMappingDTO.ViewParamDTO viewParamDTO);

    /**
     * 查询绑定关系
     */
    @PostMapping("/getByThirdId")
    List<ThirdMappingEntity> getByThirdId(@RequestBody ThirdMappingDTO.ViewParamDTO viewParamDTO);

    /**
     * 店铺id查询绑定关系
     */
    @PostMapping("/batchAdd")
    BaseResultDTO.AddDTO batchAdd(@RequestBody @Validated ThirdMappingDTO.FeignMappingDTO feignMappingDTO);

    /**
     * 查询三方仓库映射
     */
    @GetMapping("/listMappingBySysIds")
    List<ThirdMappingDTO.WarehouseMappingDTO> listMappingBySysIds(@RequestParam List<String> warehouseIdList, @RequestParam String sysType);
    /**
     * 新增、编辑
     */
    @PostMapping("/add")
    BaseResultDTO.AddDTO add(@RequestBody @Validated ThirdMappingDTO.AddDTO dto);

    @PostMapping("/view")
    ThirdMappingDTO.MappingViewDTO view(@RequestBody @Validated ThirdMappingDTO.ViewParamDTO viewParamDTO);
}
