package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.model.sys.entity.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Classname: SysDictFeign
 * @Description: 字典feign接口
 * @CreateTime: 2023-06-21  15:10
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-sys", contextId = "dictBasic",configuration = {FeignErrorDecoder.class})
public interface SysDictFeign {

    /**
     * 获取字典数据 根据属性
     * @param type
     * @return
     */
//    @GetMapping("/feign/dictBasic/getByType")
//    List<DictBasicDTO.ViewDTO> getByType(@RequestParam(value = "type") String type);


    /**
     * 根据国家id 集合 获取到国家列表
     * @param ids
     * @return
     */
    @PostMapping("feign/dictCountry/listCountryByIds")
    List<DictCountryEntity> listCountryByIds(@RequestBody List<String> ids);

    /**
     * 根据国家名 集合 获取到国家列表
     * @param names
     * @return
     */
    @PostMapping("feign/dictCountry/listCountryByNames")
    List<DictCountryEntity> listCountryByNames(@RequestBody List<String> names);

    /**
     * 根据国家名/Id 集合 获取到国家列表
     * @param codeList
     * @return
     */
    @PostMapping("feign/dictCountry/listCountryByNamesOrIds")
    List<DictCountryEntity> listCountryByNamesOrIds(@RequestBody List<String> codeList);

    /**
     * 根据组织编码获取国家组织关系列表
     * @param orgCode
     * @return
     */
    @PostMapping("feign/dictCountry/listCountryOrgByOrgCode")
    List<DictCountryOrgEntity> listCountryOrgByOrgCode(@RequestBody String orgCode);


    /**
     * 根据地区id 集合 获取到地区列表
     * @param ids
     * @return
     */
    @PostMapping("feign/globalArea/listGlobalAreaByIds")
    List<DictGlobalAreaEntity> listGlobalAreaByIds(@RequestBody List<String> ids);


    /**
     * 获取城市
     * @param idList
     * @return
     */
    @PostMapping("/feign/city/listByIdList")
    List<DictCityEntity> listCityByIdList(List<String> idList);

    /**
     * 获取第三方城市
     * @param dictIds
     * @return
     */
    @GetMapping("/feign/city/listThirdCityByDictIdList")
    List<DictThirdCity> listThirdCityByDictIdList(@RequestParam(value = "dictIds") List<String> dictIds, @RequestParam(value = "platform")String platform);


    @GetMapping("/feign/dict/kingdee/getByCode")
    DictKingdeeDTO.ListDTO getByCode(@RequestParam(value = "typeName") String typeName, @RequestParam(value = "code") String code);

    @GetMapping("/feign/dict/kingdee/listByTypeName")
    List<DictKingdeeDTO.ListDTO> listByTypeName(@RequestParam(value = "typeName")String typeName);


    /**
     * 根据国家id或三字码 集合 获取到国家列表
     */
    @PostMapping("/feign/dictCountry/listCountryByIdsOrAlpha3")
    List<DictCountryEntity> listCountryByIdsOrAlpha3(@RequestBody List<String> code);


    /**
     * 根据id获取示例用户字典值
     */
    @PostMapping("feign/sampleUseUser/getByIds")
    List<BaseIdDTO> getByIds(@RequestBody List<String> ids);


    /**
     * 全局字段分页查询，默认SYS
     */
    @PostMapping("feign/dictBasic/paging")
    PagingVO<DictBasicAllDTO.ViewDTO> paging(@RequestBody PagingDTO< DictBasicAllDTO.PagingParamDTO > dto);

}
