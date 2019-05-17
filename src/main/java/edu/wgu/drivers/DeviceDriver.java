package edu.wgu.drivers;

import edu.wgu.links.CleanURL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class DeviceDriver implements Runnable {

    public static final String USERNAME = "gsugavanam";
    public static final String ACCESS_KEY = "5ba57e8d-1337-460d-ba93-6f4ca75d7e24";
    private static final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.saucelabs.com:443/wd/hub";
    private static final String URL_REAL_DEVICE = "http://us1.appium.testobject.com/wd/hub";
    protected List<String> urls;
    protected Consumer<CleanURL> callback;
    private ExecutorService service;
    protected WebDriver driver;
    private Set<Cookie> cookies;
    protected int batchId;
    protected final boolean isReal;

    public DeviceDriver(List<String> urls, Consumer<CleanURL> callback, int batchId, boolean isReal) {
        this.urls = urls;
        this.callback = callback;
        this.batchId = batchId;
        this.isReal = isReal;
    }

    private static String clean(String currentUrl, String url) {
        return currentUrl.contains("data:text/html") ? url : currentUrl;
    }

    public void init() {
        this.driver = getDriver();
        ssoLogin();
    }

    @Override
    public void run() {
        init();
        runUrls();
    }

    public void runUrls() {
        try {
            performIosMobileTesting(cookies);
            ((JavascriptExecutor) driver).executeScript("sauce:job-result=passed");
        } catch (Throwable e) {
            e.printStackTrace();
            ((JavascriptExecutor) driver).executeScript("sauce:job-result=failed");
        } finally {
            driver.quit();
        }
    }

    private List<CleanURL> performIosMobileTesting(Set<Cookie> cookies) {
        List<CleanURL> result = new ArrayList<>();
        try {
            Iterator<String> it = this.urls.iterator();
            while (it.hasNext()) {
                CleanURL cleanURL = executeUrl(it.next(), cookies);
                this.callback.accept(cleanURL);
                result.add(cleanURL);
            }
        } finally {
            driver.close();
        }
        return result;
    }

    protected abstract DesiredCapabilities getCapabilities();

    protected abstract WebDriver getDriver();

    protected void ssoLogin() {
        driver.navigate().to("https://lrps.wgu.edu/provision/46959891");

        WebDriverWait wait = new WebDriverWait(driver, 40, 1000);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("userName")));
        WebElement un = driver.findElement(By.id("userName"));
        un.sendKeys("gsenior");
        WebElement pwd = driver.findElement(By.id("password"));
        pwd.sendKeys("B+3_z90G1ome$5L4");
        WebElement signin = driver.findElement(By.id("loginButton"));
        signin.submit();

        driver.navigate().to("https://lrps.wgu.edu/provision/46959891");
        WebDriverWait wait2 = new WebDriverWait(driver, 40, 1000);
        wait2.until(ExpectedConditions.elementToBeClickable(By.id("login-username")));
        WebElement un2 = driver.findElement(By.id("login-username"));
        un2.sendKeys("gsenior");
        WebElement pwd2 = driver.findElement(By.id("login-password"));
        pwd2.sendKeys("B+3_z90G1ome$5L4");
        WebElement signin2 = driver.findElement(By.className("btn"));
        signin2.click();

        cookies = driver.manage().getCookies();
    }

    private CleanURL executeUrl(String url, Set<Cookie> cookies) {
        System.out.println("URL under test :" + url);
        try {
            if (driver.manage().getCookies().size() != cookies.size()) {
                driver.navigate().to("https://lrps.wgu.edu/provision/46959891");
                cookies.forEach(c -> driver.manage().addCookie(c));
            }
            driver.navigate().to(url);
            System.out.println("Landed URL :" + driver.getCurrentUrl());
            System.out.println("URL mobile compatible:" + url);
            return new CleanURL(url, clean(driver.getCurrentUrl(), url), getDriverName(), true);
        } catch (Throwable e) {
            e.printStackTrace();
            checkAndRestartSessionIfEnded();
            return new CleanURL(url, clean(url, url), getDriverName(), false);
        }
    }

    protected abstract String getDriverName();

    protected void checkAndRestartSessionIfEnded() {
        //Do nothing here.
    }

    protected String getUrl() {
        return this.isReal ? URL_REAL_DEVICE : URL;
    }
}
