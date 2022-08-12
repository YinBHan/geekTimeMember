import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.SystemPropsUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CommonUtils {


    //下载镜像的地址
    private static final String MIRRORS_URL="https://registry.npmmirror.com/-/binary/chromedriver/";


    private static HashMap<@Nullable OsTypeEnums, @Nullable List<String>> OS_PROCESSBUILDER_MAP = Maps.newHashMap();

    static {

        //window
        ArrayList<@Nullable String> windowCommondList = Lists.newArrayList();
        windowCommondList.add("reg");
        windowCommondList.add("query");
        windowCommondList.add("HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\\");
        windowCommondList.add("/v");
        windowCommondList.add("Version");
        OS_PROCESSBUILDER_MAP.put(OsTypeEnums.MAC,windowCommondList);
    }


    public static String queryOsName(){
        String osName = SystemPropsUtil.get("os.name");
        return osName;
    }



    public static String queryChromeVersion(OsTypeEnums osTypeEnums){
        List<String> commondList = OS_PROCESSBUILDER_MAP.get(osTypeEnums);
        ProcessBuilder processBuilder = new ProcessBuilder();
        ProcessBuilder command = processBuilder.command(commondList);
        Process start = null;
        try {
            start = command.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = start.getInputStream();
        String commondResultStr = IoUtil.read(inputStream, UTF_8);
        osTypeEnums.processCommondResult(commondResultStr);
        return osTypeEnums.processCommondResult(commondResultStr);
    }


    public static void downloadChromeDriver(){
        String osName = queryOsName();
        OsTypeEnums osType = OsTypeEnums.getOsType(osName);
        String chromeVersion = queryChromeVersion(OsTypeEnums.getOsType(osName));
        chromeVersion=chromeVersion.split("\\.")[0];
        File file = new File("/chromedriver/chromedriver/");
        if (FileUtil.exist(file)){
            String chromeDiverPath = file.getAbsolutePath()+File.separator+osType.getDirverName();
            if (FileUtil.exist(chromeDiverPath)){
                if (chromeVersion.equals(queryChromeDriverVersion(chromeDiverPath))){
                    System.setProperty("webdriver.chrome.driver", chromeDiverPath);
                    return;
                }

            }
        }
        String chromeDriverPath = downloadChromeDriver(chromeVersion, osType, "/chromedriver/");
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
    }


    private static String queryChromeDriverVersion(String chromeDiverPath){
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = null;
        try {
            process = processBuilder.command(chromeDiverPath, "-v").start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = process.getInputStream();
        String commondResultStr = IoUtil.read(inputStream, UTF_8);
       String allVersion=commondResultStr.replace("ChromeDriver","").trim()
                .split("\\(")[0];
       return allVersion.split("\\.")[0];
    }



    public static String downloadChromeDriver(String localChromeVersion,OsTypeEnums osTypeEnums,String savePath){
        String body = HttpUtil.createGet(MIRRORS_URL).execute().body();
        List<MirrorObj> mirrorObjs = JSON.parseArray(body, MirrorObj.class);
        String mirrorChildUrl= StrUtil.EMPTY;
        for (MirrorObj mirrorObj : mirrorObjs) {
            Integer bigVersion= null;
            try {
                bigVersion = Integer.valueOf(mirrorObj.getName().split("\\.")[0]);
            } catch (Exception e) {
                continue;
            }
            if (localChromeVersion.equals(bigVersion.toString())){
                mirrorChildUrl=mirrorObj.getUrl();
            }
        }
        if (StrUtil.isNotEmpty(mirrorChildUrl)){
            List<MirrorObj> mirrorObjList = JSON.parseArray(HttpUtil.createGet(mirrorChildUrl).execute().body(), MirrorObj.class);
            for (MirrorObj mirrorObj : mirrorObjList) {
                String fileSavePath = osTypeEnums.getSavePath(savePath);
                if (mirrorObj.getName().contains(osTypeEnums.name().toLowerCase())){
                    HttpUtil.downloadFileFromUrl(mirrorObj.getUrl(),new File(fileSavePath));
                    File unzip = ZipUtil.unzip(fileSavePath);
                    FileUtil.del(fileSavePath);
                    System.out.println(unzip.getAbsolutePath());
                    return unzip.getAbsolutePath()+File.separator+"chromedriver.exe";
                }
            }
        }
        return StrUtil.EMPTY;
    }





    @Data
    static class MirrorObj{
        private String id;

        private String category;

        private String name;

        private String date;

        private String type;

        private String url;

        private String modified;
    }



}
