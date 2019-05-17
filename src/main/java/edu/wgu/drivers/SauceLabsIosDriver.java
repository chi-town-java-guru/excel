package edu.wgu.drivers;

import edu.wgu.links.CleanURL;
import io.appium.java_client.ios.IOSDriver;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class SauceLabsIosDriver extends DeviceDriver {


    public SauceLabsIosDriver(List<String> urls, Consumer<CleanURL> callback, int batchId, boolean isReal) {
        super(urls, callback, batchId, isReal);
    }


    @Override
    protected DesiredCapabilities getCapabilities() {
        DesiredCapabilities caps = DesiredCapabilities.iphone();
        caps.setCapability("name", "IOs-Session-Batch-" + this.batchId);
        caps.setCapability("deviceOrientation", "portrait");
        caps.setCapability("platformName", "iOS");
        caps.setCapability("browserName", "Safari");
        caps.setCapability("maxDuration", 10000);

        if(isReal) {
            caps.setCapability("testobject_api_key", "EAD5087035864D2287569C6058A07417");
        } else {
            caps.setCapability("deviceName", "iPhone 8 Simulator");
            caps.setCapability("platformVersion", "12.0");
        }


        return caps;
    }

    @Override
    protected WebDriver getDriver() {
        try {
            return new IOSDriver<>(new URL(getUrl()), getCapabilities());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }


    protected String getDriverName() {
        return "iOS ver 12.0 iPhone 8";
    }

}
