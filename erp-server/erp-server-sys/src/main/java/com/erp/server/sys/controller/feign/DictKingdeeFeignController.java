package com.erp.server.sys.controller.feign;

import com.common.business.constant.DictKindgeeConstant;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.server.sys.service.DictKingdeeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * 金蝶字典
 */
@RestController
@RequestMapping("feign/dict/kingdee")
public class DictKingdeeFeignController {

    @Resource
    private DictKingdeeService dictKingdeeService;

    @GetMapping("/getByCode")
    public DictKingdeeDTO.ListDTO getByCode(@RequestParam(value = "typeName") String typeName, @RequestParam(value = "code") String code) {
        List<DictKingdeeDTO.ListDTO> list = dictKingdeeService.dropDown(typeName);
        return list.stream().filter(v->v.getCode().equals(code)).findFirst().orElse(new DictKingdeeDTO.ListDTO());
    }

    @GetMapping("/listByTypeName")
    public List<DictKingdeeDTO.ListDTO> listByTypeName(@RequestParam(value = "typeName")String typeName) {
        List<DictKingdeeDTO.ListDTO> list = dictKingdeeService.dropDown(typeName);
        return list;
    }
}
