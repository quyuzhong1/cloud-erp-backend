package com.erp.server.scm.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.SyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 字典数据
 * @author Will
 * @date: 2023/10/19 11:05
 */
@RestController
@RequestMapping("feign/dict")
public class ScmDictController {

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 根据key查询字典数据
     * @author Will
     * @date: 2024/1/23 16:46
     * @param key
     * @return List<DictBasicDTO>
     */
    @PostMapping("/listDictByKey")
    public List<DictBasicDTO> listDictByKey(@RequestBody String key) {
        List<DictBasicDTO> list =  dictBasicService.getByKey(key);
        return list;
    }

    /**
     * 根据ids查询
     * @author Will
     * @date: 2024/1/25 10:21
     * @param idList
     * @return List<DictBasicDTO>
     */
    @PostMapping("/listDictByIdList")
    public List<DictBasicEntity> listDictByIdList(@RequestBody List<String> idList) {
        List<DictBasicEntity> list =  dictBasicService.listByIds(idList);
        return list;
    }
}
