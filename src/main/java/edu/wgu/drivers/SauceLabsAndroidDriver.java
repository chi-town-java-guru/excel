package edu.wgu.drivers;

import edu.wgu.links.CleanURL;
import io.appium.java_client.android.AndroidDriver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.openqa.selenium.By;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

public class SauceLabsAndroidDriver extends DeviceDriver {

    private int runId = 1;

    public SauceLabsAndroidDriver(List<String> urls,
            Consumer<CleanURL> callback, int batchId, boolean isReal) {
        super(urls, callback, batchId, isReal);
    }


    @Override
    protected DesiredCapabilities getCapabilities() {
        if (!isReal) {
            return getCapabilitiesForEmulator();
        }
        final DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("testobject_api_key", "EAD5087035864D2287569C6058A07417");
        caps.setCapability("platformName", "Android"); // Will only run on the specified device
        caps.setCapability("platformVersion", "9"); // Will only run on the specified device
        caps.setCapability("disableWindowAnimation", true);
        caps.setCapability("name", "Android-Session-Batch-" + this.batchId + "-Run-" + runId++);
        caps.setCapability("maxDuration", 10000);
        return caps;
    }

    protected DesiredCapabilities getCapabilitiesForEmulator() {
        DesiredCapabilities caps = DesiredCapabilities.android();
        caps.setCapability("deviceName","Samsung Galaxy S9 Plus WQHD GoogleAPI Emulator");
        caps.setCapability("deviceOrientation", "portrait");
        caps.setCapability("browserName", "Chrome");
        caps.setCapability("platformVersion", "8.1");
        caps.setCapability("platformName","Android");
        caps.setCapability("autoAcceptAlerts", true);
        caps.setCapability("autoGrantPermissions", true);
        caps.setCapability("disableWindowAnimation", true);
        caps.setCapability("name", "Android-Session-Batch-" + this.batchId + "-Run-" + runId++);
        caps.setCapability("maxDuration", 10000);
        return caps;
    }

    @Override
    protected void ssoLogin() {
        super.ssoLogin();
        if (!isReal) {
            //handle file permissions for emulators. For real devices, its already in place
            driver.navigate().to("http://ajcc.aacnjournals.org/content/15/6/541.full.pdf");
            String webContext = ((AndroidDriver) driver).getContext();
            Set<String> contexts = ((AndroidDriver) driver).getContextHandles();
            for (String context : contexts) {
                if (context.contains("NATIVE_APP")) {
                    ((AndroidDriver) driver).context(context);
                    break;
                }
            }
            driver.findElement(By.id("android:id/button1")).click();
            contexts = ((AndroidDriver) driver).getContextHandles();
            for (String context : contexts) {
                if (context.contains("NATIVE_APP")) {
                    ((AndroidDriver) driver).context(context);
                    break;
                }
            }
            driver.findElement(By.id("com.android.packageinstaller:id/permission_allow_button")).click();
            ((AndroidDriver) driver).context(webContext);
        }
        checkAndRestartSessionIfEnded();
    }

    @Override
    protected void checkAndRestartSessionIfEnded() {
        try {
            ((AndroidDriver) driver).isBrowser();
        } catch (UnsupportedCommandException ex) {
            if (ex.getMessage().contains("has already finished")) {
                this.init();
            }
        }
    }

    @Override
    protected WebDriver getDriver() {
        try {
            final AndroidDriver driver = new AndroidDriver(new URL(getUrl()), getCapabilities());
            driver.manage().timeouts().implicitlyWait(200, TimeUnit.MICROSECONDS);
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            return driver;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getDriverName() {
        return "Android ver 8.1 Samsung Galaxy S9";
    }
}
