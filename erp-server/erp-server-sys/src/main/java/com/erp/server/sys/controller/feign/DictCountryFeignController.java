package com.erp.server.sys.controller.feign;

import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCountryOrgEntity;
import com.erp.server.sys.service.DictCountryOrgService;
import com.erp.server.sys.service.DictCountryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 获取国家信息feign
 * @Author Luo_WG
 * @Date 2023/5/26 10:49
 **/
@RestController
@RequestMapping("/feign/dictCountry")
public class DictCountryFeignController {

    @Resource
    private DictCountryService dictCountryService;
    @Resource
    private DictCountryOrgService dictCountryOrgService;

    /**
     * 根据id获取国家信息
     * @Author Luo_WG
     * @Date 2023/5/26 10:45
     * @param id
     * @return java.util.List<com.erp.model.sys.entity.DictCountryEntity>
     **/
    @PostMapping("/getCountryById")
    public DictCountryEntity getCountryById(@RequestBody String id) {
        return dictCountryService.getById(id);
    }


    /**
     * 获取国家列表
     * @param
     * @return
     */
    @GetMapping("/list")
    public List<DictCountryDTO.ListDTO> list() {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountry();
        return list;
    }


    /**
     * 根据ids 获取到国家列表
     * @param
     * @return
     */
    @PostMapping("/listCountryByIds")
    public List<DictCountryEntity> listCountryByIds(@RequestBody List<String> ids) {
        List<DictCountryEntity>list = dictCountryService.listCountryByIds(ids);
        return list;
    }


    /**
     * 根据国家名 获取到国家列表
     * @param
     * @return
     */
    @PostMapping("/listCountryByNames")
    public List<DictCountryEntity> listCountryByNames(@RequestBody List<String> names) {
        List<DictCountryEntity>list = dictCountryService.listCountryByNames(names);
        return list;
    }

    /**
     * 根据国家组织获取国家列表
     * @param
     * @return
     */
    @PostMapping("/listCountryOrgByOrgCode")
    public List<DictCountryOrgEntity> listCountryOrgByOrgCode(@RequestBody String orgCode) {
        return dictCountryOrgService.listCountryByOrgCode(orgCode);
    }


    /**
     * 根据国家id或三字码 集合 获取到国家列表
     */
    @PostMapping("/listCountryByIdsOrAlpha3")
    public List<DictCountryEntity> listCountryByIdsOrAlpha3(@RequestBody List<String> codeList) {
        return dictCountryService.listCountryByIdsOrAlpha3(codeList);
    }
}
