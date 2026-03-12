package com.erp.server.plm.smb;

import com.common.core.file.SambaUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.Console;
import java.lang.reflect.Method;
import java.util.Scanner;

/**
 * Local fast checks for SMB path behavior.
 *
 * Run only unit checks:
 * mvn -pl erp-server/erp-server-plm -Dtest=SambaUtilPathTest#shouldNotEncodeShareNameAndShouldEncodeSubPath test
 *
 * Run live NAS check (only when properties are provided):
 * mvn -pl erp-server/erp-server-plm -Dtest=SambaUtilPathTest#shouldReadSmbFileWhenPropsProvided test ^
 *   -Dsmb.user=sdc-erp -Dsmb.pwd=*** -Dsmb.path=\\\\172.16.100.252\\共享文件夹\\产品认证报告共享文件\\docs\\DSP25110279-1-MSDS.pdf
 */
public class SambaUtilPathTest {

    @Test
    public void shouldNotEncodeShareNameAndShouldEncodeSubPath() throws Exception {
        String rawPath = "172.16.100.252/共享文件夹/产品认证报告共享文件/docs/产品报告认证/电池CCC/日晖达/11款证书+总报告 03.17.zip";
        String encoded = callEncodeSmbPath(rawPath);

        Assert.assertTrue("host/share should stay readable",
                encoded.startsWith("172.16.100.252/共享文件夹/"));
        Assert.assertTrue("plus sign should stay readable", encoded.contains("11款证书+总报告 03.17.zip"));
        Assert.assertTrue("space should stay readable", encoded.contains("01. 产品认证"));
        Assert.assertTrue("chinese path should stay readable", encoded.contains("产品报告认证/电池CCC/日晖达"));
        Assert.assertFalse("share name must not be URL encoded",
                encoded.startsWith("172.16.100.252/%E5%85%B1%E4%BA%AB"));
    }

    @Test
    public void shouldReadSmbFileWhenPropsProvided() throws Exception {
        String user = "sdc-erp";
        String pwd = "BvuZVUxy4ulbrzsx";
        String path = "\\\\172.16.100.252\\共享文件夹\\产品认证报告共享文件\\docs\\产品报告认证\\电池CCC\\日晖达\\11款证书+总报告 03.17.zip";

        Assume.assumeTrue("SMB username is required", user != null && !user.trim().isEmpty());
        Assume.assumeTrue("SMB password is required", pwd != null && !pwd.trim().isEmpty());
        Assume.assumeTrue("SMB file path is required", path != null && !path.trim().isEmpty());

        System.out.println("SMB file path(input): " + path);
        System.out.println("SMB file path(encoded): " + callEncodeSmbPath(path));

        MultipartFile multipartFile = SambaUtil.toMultipartFile(path, user, pwd);
        Assert.assertNotNull(multipartFile);
        Assert.assertNotNull(multipartFile.getOriginalFilename());
        Assert.assertFalse(multipartFile.getOriginalFilename().trim().isEmpty());
        Assert.assertTrue("file content should not be empty", multipartFile.getSize() > 0L);
    }

    private String resolveInput(String key, String prompt) {
        String value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        // Interactive mode: read from local console when available.
        Console console = System.console();
        if (console != null) {
            String input;
            if ("smb.pwd".equals(key)) {
                char[] pwd = console.readPassword("%s: ", prompt);
                input = pwd == null ? null : new String(pwd);
            } else {
                input = console.readLine("%s: ", prompt);
            }
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            return null;
        }
        // Fallback for IDE runs that still attach stdin.
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print(prompt + ": ");
            String input = scanner.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String callEncodeSmbPath(String path) throws Exception {
        Method method = SambaUtil.class.getDeclaredMethod("encodeSmbPath", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, path);
    }
}
