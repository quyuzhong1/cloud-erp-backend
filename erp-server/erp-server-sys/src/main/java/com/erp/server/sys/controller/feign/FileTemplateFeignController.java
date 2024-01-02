package com.erp.server.sys.controller.feign;

import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.server.sys.service.FileTemplateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 文件模板控制层
 * @date 2023/12/25 14:41
 */
@RestController
@RequestMapping("feign/fileTemplate")
public class FileTemplateFeignController {

    @Resource
    private FileTemplateService fileTemplateService;

    /**
     * 查询文件模板
     * @author Will
     * @date: 2023/12/25 14:51
     * @param dto
     * @return FileTemplateEntity
     */
    @PostMapping("/getByFileTemplate")
    public FileTemplateEntity getByFileTemplate(@RequestBody @Validated FileTemplateDTO.GetOneDTO  dto){
       return fileTemplateService.getByFileTemplate(dto);
    }
}
