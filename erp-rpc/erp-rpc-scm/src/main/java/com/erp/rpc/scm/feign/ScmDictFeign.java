package com.erp.rpc.scm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
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
 * @description: 字典rpc
 * @author Will
 * @date: 2024/1/23 16:39
 */
@FeignClient(name = "erp-scm", contextId = "scmDict",configuration = {FeignErrorDecoder.class})
public interface ScmDictFeign {

   /**
    * @description: 根据key查询字典数据
    * @author Will
    * @date: 2024/1/23 16:47
    * @param key
    * @return List<DictBasicDTO>
    */
    @PostMapping("/feign/dict/listDictByKey")
    List<DictBasicDTO> listDictByKey(@RequestBody String key);

    /**
     * @description: 根据id集合查询字典数据
     * @author Will
     * @date: 2024/1/23 16:47
     * @param idList
     * @return List<DictBasicEntity>
     */
    @PostMapping("/feign/dict/listDictByIdList")
    List<DictBasicEntity> listDictByIdList(@RequestBody List<String> idList);

}
