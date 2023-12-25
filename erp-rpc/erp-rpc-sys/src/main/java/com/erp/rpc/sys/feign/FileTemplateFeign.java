package com.erp.rpc.sys.feign;

import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description: 文件模板
 * @author Will
 * @date: 2023/12/25 14:39
 */
@FeignClient(name = "erp-sys", contextId = "fileTemplate")
public interface FileTemplateFeign {

    /**
     * @description: fastdfs文件新增或修改
     * @author Will
     * @date: 2023/12/25 14:53
     * @param dto
     */
    @PostMapping("feign/fileTemplate/fastdfsAddOrUpdate")
    void fastdfsAddOrUpdate(@RequestBody @Validated FileTemplateDTO.FastdfsAddOrUpdateDTO dto);

    /**
     * @description: 查询模板文件
     * @author Will
     * @date: 2023/12/25 14:52
     * @param dto
     * @return FileTemplateEntity
     */
    @PostMapping("feign/fileTemplate/getByFileTemplate")
    FileTemplateEntity getByFileTemplate(@RequestBody @Validated FileTemplateDTO.GetOneDTO dto);

}
