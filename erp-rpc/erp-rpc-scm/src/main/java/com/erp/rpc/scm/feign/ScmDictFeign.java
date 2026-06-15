package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.SupplierEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @description: 字典rpc
 * @date: 2024/1/23 16:39
 */
@FeignClient(name = "erp-scm", contextId = "scmDict", configuration = {FeignErrorDecoder.class})
public interface ScmDictFeign {

    /**
     * @param key
     * @return List<DictBasicDTO>
     * @description: 根据key查询字典数据
     * @author Will
     * @date: 2024/1/23 16:47
     */
    @PostMapping("/feign/dict/listDictByKey")
    List<DictBasicDTO> listDictByKey(@RequestBody String key);

    /**
     * @param idList
     * @return List<DictBasicEntity>
     * @description: 根据id集合查询字典数据
     * @author Will
     * @date: 2024/1/23 16:47
     */
    @PostMapping("/feign/dict/listDictByIdList")
    List<DictBasicEntity> listDictByIdList(@RequestBody List<String> idList);

    /**
     * @description: 获取供应商下拉列表
     * @return
     */
    @PostMapping("/feign/dict/listALLSupplierDropDown")
    ApiResult<List<BaseDropDownDTO.RemarkDTO>> listALLSupplierDropDown();


}
