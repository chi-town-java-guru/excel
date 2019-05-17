package edu.wgu.drivers;

import edu.wgu.links.CleanURL;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.windows.WindowsDriver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.bytebuddy.implementation.bytecode.Throw;
import org.openqa.selenium.By;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SauceLabsIos12FirefoxDriver extends DeviceDriver {

    private int runId = 1;

    public SauceLabsIos12FirefoxDriver(List<String> urls,
            Consumer<CleanURL> callback, int batchId, boolean isReal) {
        super(urls, callback, batchId, isReal);
    }


    @Override
    protected DesiredCapabilities getCapabilities() {
        DesiredCapabilities caps = DesiredCapabilities.firefox();
        caps.setCapability("platform", "Windows 10");
        caps.setCapability("version", "66.0");
        caps.setCapability("maxDuration", 10000);
        return caps;
    }

    @Override
    protected WebDriver getDriver() {
        try {
            WebDriver driver = new RemoteWebDriver(new URL(getUrl()), getCapabilities());
            driver.manage().timeouts().implicitlyWait(200, TimeUnit.MICROSECONDS);
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            return driver;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getDriverName() {
        return "iOS 12.2 Firefox";
    }
}
