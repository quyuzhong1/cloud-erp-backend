package com.erp.server.sys;

import com.erp.server.file.ErpServerFileApplication;
import com.erp.server.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerFileApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerFileApplicationTests {

    @Resource
    private FileService fileFeign;

    @Test
    public void contextLoads() {
//        String url = "https://img.fxiaoke.com/FilesOne/Sign?Fid=ADE9CE2897D0F91BBC70E5C1540E7C4F7940D3DE6D66C3D7E65A6C72F7381276535AB9D35DABA0B1C3874C79D61F1E6C9E8D9005299B220989DD3293BBC7EC6AA7F6D505B26B3E42AC4AA54C5D15F26DFF7B08E7A9B6C8C96CCD9183DF00E2DF81722EDA9B01F67EE8683FC172FA8E8A882DE0E0C604DE67&Acid=820109.1000&Ets=1760326196439&Ak=x1jRJt5O1anXnMZZadkSTIc2&Fn=0179.png&Sig=sVNkQiubwW9Svfm2vfAKL0vssTc=&Ds=MdmEzFXL1yUCvVMuo8YDSQ==&linkId=E-E.820109_sandbox.1000-92241581";
//        MultipartFile multipartFile = FileUtil.fileUrlToMultipartFileWithCorrectName(url);
//        String fileUrl = fileFeign.uploadFile(multipartFile);
//        System.out.println(fileUrl);

    }

}
