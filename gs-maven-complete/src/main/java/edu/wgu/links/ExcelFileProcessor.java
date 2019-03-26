package edu.wgu.links;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.IOSMobileCapabilityType;

public class ExcelFileProcessor {

	private List<String> cookies;
	private HttpsURLConnection conn;
	private static WebDriver driver;

	private final String USER_AGENT = "Mozilla/5.0";

	private static final String BASE_URL = "/Users/guru/Documents/workspace-spring-tool-suite-4-4.1.2.RELEASE/gs-maven-complete/src/main/java/edu/wgu/links/";

	public static final String USERNAME = "gsugavanam";
	public static final String ACCESS_KEY = "5ba57e8d-1337-460d-ba93-6f4ca75d7e24";
	public static final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.saucelabs.com:443/wd/hub";

	public static void main(String[] args) throws Exception {

		String ssoLoginUrl = "https://sso.wgu.edu/WGULogin/wgulogin";
		Set<String> urls = loadFromExcelFile();
		// urls.remove("CL_FULL_URL");
		urls.removeIf(c -> !c.startsWith("https://"));

		ExcelFileProcessor http = new ExcelFileProcessor();

		// make sure cookies is turned on
		CookieHandler.setDefault(new CookieManager());

//		// 1. Send a "GET" request, so that you can extract the form's data.
//		String page = http.getPageHtml(ssoLoginUrl);
//		String postParams = http.getFormParams(page, "gsenior", "B+3_z90G1ome$5L4");
//
//		// 2. Construct above post's content and then send a POST request for
//		// authentication
//		http.sendPost(ssoLoginUrl, postParams);

		// 3. success then go to LRPS link.
		List<String> errorUrls = new ArrayList<>();
		List<String> cleanUrls = new ArrayList<>();
		for (String url : urls) {
			try {
				// String result = http.getPageHtml(url);
				cleanUrls.add(url);
				System.out.println(url);
			} catch (Exception e) {
				errorUrls.add(url);
				// System.out.println("BAD URL="+url);
			}
		}
		writeCleanUrlsToFile(cleanUrls);
		// writeErrorUrlsToFile(errorUrls);
		performIosMobileTesting(cleanUrls);
	}

	private static void performIosMobileTesting(List<String> cleanUrls) throws Exception {
		DesiredCapabilities caps = DesiredCapabilities.iphone();
		caps.setCapability("deviceName", "iPhone 8 Simulator");
		caps.setCapability("deviceOrientation", "portrait");
		caps.setCapability("platformVersion", "12.0");
		caps.setCapability("platformName", "iOS");
		caps.setCapability("browserName", "Safari");

		driver = new IOSDriver<>(new URL(URL), caps);

		/*
		 * driver.get("https://sso.wgu.edu/WGULogin/wgulogin"); WebElement un =
		 * driver.findElement(By.id("userName")); un.sendKeys("gsenior"); WebElement pwd
		 * = driver.findElement(By.id("password")); pwd.sendKeys("B+3_z90G1ome$5L4");
		 * WebElement signin = driver.findElement(By.id("loginButton")); signin.click();
		 */
		cleanUrls.forEach(c -> doIt(c));

		driver.quit();

	}

	private static void doIt(String url) {
		String copy = new String(url);
		// driver.manage().timeouts().pageLoadTimeout(-1, TimeUnit.SECONDS);
		driver.navigate().to(url);

		if (driver.getCurrentUrl().contains("sso.wgu.edu")) {
			WebElement un = driver.findElement(By.id("userName"));
			un.sendKeys("gsenior");
			WebElement pwd = driver.findElement(By.id("password"));
			pwd.sendKeys("B+3_z90G1ome$5L4");
			WebElement signin = driver.findElement(By.id("loginButton"));
			signin.click();
 
		}driver.navigate().to(url);

		if (driver.getCurrentUrl().contains("access.wgu.edu")) {
			WebElement un = driver.findElement(By.id("login-username"));
			un.sendKeys("gsenior");
			WebElement pwd = driver.findElement(By.id("login-password"));
			pwd.sendKeys("B+3_z90G1ome$5L4");
			WebElement signin = driver.findElement(By.className("btn"));
			signin.click();
			// driver.manage().timeouts().pageLoadTimeout(-1, TimeUnit.SECONDS);

		}

		System.out.println("URL  :" + driver.getCurrentUrl());
		System.out.println("URL mobile compatible:" + url);
	}

	private static void writeErrorUrlsToFile(List<String> errorUrls) {
		System.out.println("Entering Error url file creation");

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Error URLs that are for our Info only");

		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("URL");
		errorUrls.remove(0);
		int i = 1;

		for (String url : errorUrls) {
			Row dataRow = sheet.createRow(i++);
			dataRow.createCell(0).setCellValue(url);
		}

		try (FileOutputStream out = new FileOutputStream(new File(BASE_URL + "ErrorURLs.xlsx"));) {
			workbook.write(out);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void writeCleanUrlsToFile(List<String> cleanUrls) {
		System.out.println("Entering Clean url file creation");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Clean URLs ready to submit to SauceLabs");

		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("URL");

		int i = 1;

		for (String url : cleanUrls) {
			Row dataRow = sheet.createRow(i++);
			dataRow.createCell(0).setCellValue(url);
		}

		try (FileOutputStream out = new FileOutputStream(new File(BASE_URL + "CleanURLs.xlsx"));) {
			workbook.write(out);
			workbook.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Set<String> loadFromExcelFile() {
		Set<String> output = new TreeSet<>();
		try (FileInputStream file = new FileInputStream(new File(BASE_URL + "LRPS.xlsx"));

				// Create Workbook instance holding reference to .xlsx file
				XSSFWorkbook workbook = new XSSFWorkbook(file);) {

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {

					Cell cell = cellIterator.next();

					// Check the cell type and format accordingly
					if (cell.getColumnIndex() == 3) {
						output.add(cell.getStringCellValue().replace("http://https", "https")
								.replace("http://", "https://").replace("https://https://", "https://")
								.replace("https://lhttps://", "https://"));
					}
				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}

		return output;
	}

	private void sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", url);
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", "https://sso.wgu.edu/WGULogin/wgulogin");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		// System.out.println("\nSending 'POST' request to URL : " + url);
		// System.out.println("Post parameters : " + postParams);
		// System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// System.out.println(response.toString());

	}

	private String getPageHtml(String url) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		// System.out.println("\nSending 'GET' request to URL : " + url);
		// System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Get the response cookies
		setCookies(conn.getHeaderFields().get("Set-Cookie"));

		return response.toString();

	}

	public String getFormParams(String html, String username, String password) throws UnsupportedEncodingException {

		// System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		// Google form id
		Element loginform = doc.getElementsByTag("form").first();
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		paramList.add("username" + "=" + URLEncoder.encode(username, "UTF-8"));
		paramList.add("password" + "=" + URLEncoder.encode(password, "UTF-8"));
//	for (Element inputElement : inputElements) {
//		String key = inputElement.attr("username");
//		String value = inputElement.attr("password");
//
//		if (key.equals("username"))
//			value = username;
//		else if (key.equals("password"))
//			value = password;
//		paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
//	}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

}