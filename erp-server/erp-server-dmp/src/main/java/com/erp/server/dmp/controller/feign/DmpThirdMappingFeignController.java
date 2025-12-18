package com.erp.server.dmp.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.server.dmp.service.ThirdMappingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.erp.model.dmp.dto.ThirdMappingDTO;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 第三方仓库/店铺映射Feign控制器
 * @date 2024-05-27
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/dmp/thirdMapping")
public class DmpThirdMappingFeignController {

    @Resource
    private ThirdMappingService thirdMappingService;

    @GetMapping("/getBySysId")
    public ThirdWarehouseEntity getBySysId(@RequestParam String sysId, @RequestParam String sysType) {
        return thirdMappingService.getBySysId(sysId, sysType);
    }
    @GetMapping("/getListBySysIds")
    public List<ThirdMappingEntity> getListBySysIds(@RequestParam List<String> sysIds){
        return thirdMappingService.getListBySysIds(sysIds);
    }
    @GetMapping("/getVwListBySysIds")
    public List<ThirdMappingEntity> getVwListBySysIds(@RequestParam List<String> sysIds){
        return thirdMappingService.getVwListBySysIds(sysIds);
    }

    @PostMapping("/getWhetherBind")
    public Boolean getWhetherBind(@RequestBody ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        return thirdMappingService.getWhetherBind(viewParamDTO);
    }

    @PostMapping("/getByThirdId")
    public List<ThirdMappingEntity> getByThirdId(@RequestBody ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        return thirdMappingService.getByThirdId(viewParamDTO);
    }

    @PostMapping("/batchAdd")
    public BaseResultDTO.AddDTO batchAdd(@RequestBody ThirdMappingDTO.FeignMappingDTO feignMappingDTO) {
        return thirdMappingService.batchAdd(feignMappingDTO);
    }

    @PostMapping("/add")
    public BaseResultDTO.AddDTO add(@RequestBody @Validated ThirdMappingDTO.AddDTO dto) {
        return thirdMappingService.add(dto);
    }

    /**
     * 查询三方仓库映射
     */
    @GetMapping("/listMappingBySysIds")
    List<ThirdMappingDTO.WarehouseMappingDTO> listMappingBySysIds(@RequestParam List<String> warehouseIdList, @RequestParam String sysType){
        return thirdMappingService.listMappingBySysIds(warehouseIdList, sysType);
    }

    @PostMapping("/view")
    public ThirdMappingDTO.MappingViewDTO view(@RequestBody @Validated ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        return thirdMappingService.view(viewParamDTO);
    }

    /**
     * 查询三方仓库映射
     */
    @GetMapping("/getShopByThirdCode")
    ThirdMappingEntity getShopByThirdCode(@RequestParam String thirdCode, @RequestParam String sysType){
        return thirdMappingService.getShopByThirdCode(thirdCode, sysType);
    }
}