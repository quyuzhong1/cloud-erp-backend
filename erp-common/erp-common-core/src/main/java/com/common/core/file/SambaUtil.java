package com.common.core.file;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @description: samba工具类
 * @author Will
 * @date: 2024/4/3 14:13
 */
public class SambaUtil { 

    public static void delete(String filepath, String username, String pwd)
        {
       SmbFile f = null;
       try {
        f = new SmbFile("smb://" + username + ":" + pwd + "@"
          + filepath);
            try {
                 if (f.exists()) {
                  f.delete();
                 }
            } catch (SmbException e) {
                e.printStackTrace();
            }
       } catch (MalformedURLException e) {
        e.printStackTrace();
       }

    }

    public static boolean exists(String filepath, String username, String pwd)
        throws Exception {
       SmbFile file = new SmbFile("smb://" + username + ":" + pwd + "@"
         + filepath);
       try {
        return file.exists();
       } catch (Exception e) {
        e.printStackTrace();
        return false;
       }
    }

    public static boolean fileRename(String filepath, String newFilename,
        String username, String pwd) {
       try {
        SmbFile f = new SmbFile("smb://" + username + ":" + pwd + "@"
          + filepath);
        if (f.isFile()) {
         String str = filepath.substring(0, filepath.lastIndexOf("/"));
         str = "smb://" + username + ":" + pwd + "@" + str + "/"
           + newFilename;
         f.renameTo(new SmbFile(str));
        } else if (f.isDirectory()) {
         String str = filepath.substring(0, filepath.length() - 1);
         str = filepath.substring(0, str.lastIndexOf("/"));
         str = "smb://" + username + ":" + pwd + "@" + str + "/"
           + newFilename;
         f.renameTo(new SmbFile(str));
        }
        return true;
       } catch (Exception e) {
        e.printStackTrace();
        return false;
       }
    }

    public static void mkDir(String dir, String username, String pwd) {
       try {
        SmbFile f = new SmbFile("smb://" + username + ":" + pwd + "@" + dir);
        if (!f.exists()) {
         f.mkdir();
        }
       } catch (Exception e) {
        e.printStackTrace();
       }
    }

    public static void mkFile(String filepath, String username, String pwd) {
       try {
        SmbFile f = new SmbFile("smb://" + username + ":" + pwd + "@"
          + filepath);
            if (!f.exists()) {
             f.createNewFile();
            }
       } catch (Exception e) {
            e.printStackTrace();
       }
    }

    public static void mkFile(String filepath, String username, String pwd,
        String content) {
       try {
            SmbFile f = new SmbFile("smb://" + username + ":" + pwd + "@"
              + filepath);
            if (!f.exists()) {
                f.createNewFile();
            }
            writefile(filepath, content, username, pwd);
       } catch (Exception e) {
            e.printStackTrace();
       }
    }

    public static String readfile(String filepath, String username, String pwd) {
       StringBuffer sb = new StringBuffer("");
       try {
        SmbFile f = new SmbFile("smb://" + username + ":" + pwd + "@"
          + filepath);
            if (f.exists() && f.isFile()) {
             int length = f.getContentLength();
             // 得到文件的大小
             byte buffer[] = new byte[length];

             SmbFileInputStream in = new SmbFileInputStream(f);
             while ((in.read(buffer)) != -1) {
              sb.append(new String(buffer));
             }
             in.close();
            }
       } catch (Exception e) {
        e.printStackTrace();
       }
       return sb.toString();
    }

    public static boolean isDir(String filepath, String username, String pwd) throws Exception {
       String dir = "smb://" + username + ":" + pwd + "@" + filepath;
       SmbFile f = new SmbFile(dir);
       return f.isDirectory();
    }

    public static void writefile(String filepath, String content,
        String username, String pwd) {
       try {
            SmbFile to = new SmbFile("smb://" + username + ":" + pwd + "@"
              + filepath);
            SmbFileOutputStream out = new SmbFileOutputStream(to);
            out.write(content.getBytes());
            out.close();
       } catch (Exception e) {
            e.printStackTrace();
       }
    }

    /**
     * 转换MultipartFile
     */
    public static MultipartFile toMultipartFile(String filepath, String username, String pwd) throws Exception {
        // 按路径段做编码，兼容中文、空格、+ 等字符
        String fileUrl = encodeSmbPath(filepath);

        SmbFile smbFile = new SmbFile("smb://" + username + ":" + pwd + "@"
                + fileUrl);

        InputStream inputStream = smbFile.getInputStream();

        // 读取输入流中的数据并存储到 ByteArrayOutputStream 中
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // 关闭输入流
        inputStream.close();

        // 从 ByteArrayOutputStream 中获取 byte 数组
        byte[] bytes = outputStream.toByteArray();

        // 关闭 ByteArrayOutputStream
        outputStream.close();

        // 创建 MockMultipartFile 对象，并设置 originalFilename
        return new MockMultipartFile(
                "file",
                smbFile.getName(),
                null,
                new ByteArrayInputStream(bytes)
        );
    }

    /**
     * SMB路径编码（仅编码host之后的路径段）
     */
    private static String encodeSmbPath(String filepath) {
        String path = filepath.replace("\\", "/").trim();
        path = path.replaceFirst("(?i)^smb://", "");
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        int firstSlashIdx = path.indexOf('/');
        if (firstSlashIdx <= 0 || firstSlashIdx >= path.length() - 1) {
            return path;
        }
        String host = path.substring(0, firstSlashIdx);
        String[] subSegments = path.substring(firstSlashIdx + 1).split("/");
        if (subSegments.length == 0) {
            return path;
        }
        // share name 不能编码，否则 treeConnect 可能报参数错误
        String shareName = subSegments[0];
        if (subSegments.length == 1) {
            return host + "/" + shareName;
        }
        String encodedSubPath = Arrays.stream(subSegments)
                .skip(1)
                .filter(segment -> segment != null && !segment.isEmpty())
                .map(SambaUtil::escapeSmbSegment)
                .collect(Collectors.joining("/"));
        if (encodedSubPath.isEmpty()) {
            return host + "/" + shareName;
        }
        return host + "/" + shareName + "/" + encodedSubPath;
    }

    /**
     * 仅转义SMB URL中必要字符，保留中文等可读字符
     */
    private static String escapeSmbSegment(String segment) {
        return segment
                .replace("#", "%23")
                .replace("?", "%3F");
    }

    /**
     * 删除前缀
     */
    public static String removePrefix(String path,String pattern) {
        // 将路径中以字母开头的部分替换为空字符串
        return path.replaceFirst(pattern, "");
    }
} 
