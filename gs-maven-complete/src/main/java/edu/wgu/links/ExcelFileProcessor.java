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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExcelFileProcessor {

	private List<String> cookies;
	private HttpsURLConnection conn;

	private final String USER_AGENT = "Mozilla/5.0";

	private static final String BASE_URL = "/Users/guru/Documents/workspace-spring-tool-suite-4-4.1.2.RELEASE/gs-maven-complete/src/main/java/edu/wgu/links/";

	public static void main(String[] args) throws Exception {

		String ssoLoginUrl = "https://sso.wgu.edu/WGULogin/wgulogin";
		List<String> urls = loadFromExcelFile();
		urls.remove(0);

		ExcelFileProcessor http = new ExcelFileProcessor();

		// make sure cookies is turned on
		CookieHandler.setDefault(new CookieManager());

		// 1. Send a "GET" request, so that you can extract the form's data.
		String page = http.getPageHtml(ssoLoginUrl);
		String postParams = http.getFormParams(page, "ttes365", "4hL-h552XQMmwU#3");

		// 2. Construct above post's content and then send a POST request for
		// authentication
		http.sendPost(ssoLoginUrl, postParams);

		// 3. success then go to LRPS link.
		List<String> errorUrls = new ArrayList<>();
		List<String> cleanUrls = new ArrayList<>();
		for (String url : urls) {
			try {
				String result = http.getPageHtml(url);
				cleanUrls.add(url);
				// System.out.println(result);
			} catch (Exception e) {
				errorUrls.add(url);
				// System.out.println("BAD URL="+url);
			}
		}
		writeCleanUrlsToFile(cleanUrls);
		writeErrorUrlsToFile(errorUrls);
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

		try {
			FileOutputStream out = new FileOutputStream(new File(BASE_URL + "ErrorURLs.xlsx"));
			workbook.write(out);

			workbook.close();
			out.close();

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

		try {
			FileOutputStream out = new FileOutputStream(new File(BASE_URL + "CleanURLs.xlsx"));
			workbook.write(out);

			workbook.close();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static List<String> loadFromExcelFile() {
		List<String> output = new ArrayList<>();
		try {
			FileInputStream file = new FileInputStream(new File(BASE_URL + "LRPS2.xlsx"));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

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
						output.add(cell.getStringCellValue().replace("http://https", "https").replace("http://",
								"https://"));
					}
				}
			}
			workbook.close();
			file.close();
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
