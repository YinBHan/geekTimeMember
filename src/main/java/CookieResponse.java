import lombok.Data;
import org.openqa.selenium.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class CookieResponse {

    private String md5;

    private List<Cookie> cookieList;

}
