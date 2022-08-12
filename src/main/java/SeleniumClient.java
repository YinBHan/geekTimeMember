import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
public class SeleniumClient {


    /**
     * token
     */
    private static String TOKEN;

    /**
     * MD5校验
     */
    private static volatile String MD5 = "DEFAULTMD5";


    /**
     * chrome 窗口
     */
    private static volatile String WINDOW_HANDLE;

    /**
     * 获取cookie
     */
    private static String QUERY_COOKIE_URL = "http://parse.natapp1.cc/cookie/queryCookie?token={token}&md5={md5}";

    /**
     * 定时刷新cookie线程池
     */
    private static ScheduledExecutorService REFESH_THREAD = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.contains("DriverPath")) {
                System.setProperty("webdriver.chrome.driver", arg.split("DriverPath=")[1]);
            } if (arg.contains("token")) {
                TOKEN = arg.split("token=")[1];
            }
        }
        //下载驱动
//        CommonUtils.downloadChromeDriver();
        ChromeDriver chromeDriver = new ChromeDriver(getChromeOptions());
        try {
            chromeDriver.get("https://time.geekbang.org/");
            String body = HttpUtil.createGet(QUERY_COOKIE_URL.replace("{token}", TOKEN)).execute().body();
            WebResponse webResponse = JSONObject.parseObject(body, WebResponse.class);
            JSONObject jsonData = (JSONObject) webResponse.getData();
            CookieResponse cookieResponse = jsonData.toJavaObject(CookieResponse.class);
            MD5 = cookieResponse.getMd5();
            List<Cookie> cookieList = cookieResponse.getCookieList();
            for (Cookie cookie : cookieList) {
                chromeDriver.manage().addCookie(cookie);
            }
            Thread.sleep(2000);
            chromeDriver.get("https://time.geekbang.org/");
            WINDOW_HANDLE = chromeDriver.getWindowHandle();
            REFESH_THREAD.scheduleWithFixedDelay(() -> {
                refeshCookie(chromeDriver);
            }, 30, 60, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
            chromeDriver.quit();
        }
    }

    private static void refeshCookie(ChromeDriver chromeDriver) {
        try {
            log.info("刷新Cookie-----");
            String body = HttpUtil.createGet(QUERY_COOKIE_URL.replace("{token}", TOKEN).replace("{md5}", MD5)).execute().body();
            WebResponse webResponse = JSONObject.parseObject(body, WebResponse.class);
            JSONObject jsonData = (JSONObject) webResponse.getData();
            CookieResponse cookieResponse = jsonData.toJavaObject(CookieResponse.class);
            String md5 = cookieResponse.getMd5();
            if (!MD5.equals(md5)) {
                List<Cookie> cookieList = cookieResponse.getCookieList();
                if (!chromeDriver.getWindowHandles().contains(WINDOW_HANDLE)) {
                    chromeDriver.switchTo().newWindow(WindowType.TAB);
                    WINDOW_HANDLE = chromeDriver.getWindowHandle();
                    chromeDriver.switchTo().window(WINDOW_HANDLE);
                }
                chromeDriver.get("https://time.geekbang.org/");
                chromeDriver.manage().deleteAllCookies();
                for (Cookie cookie : cookieList) {
                    try {
                        chromeDriver.manage().addCookie(cookie);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
            MD5 = md5;
            log.info("刷新Cookie完成-----");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取加载驱动插件
     *
     * @return
     */
    private static ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        //加载插件
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--start-maximized");
        //绕过平台driver检测配置
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);
        return chromeOptions;
    }

}
