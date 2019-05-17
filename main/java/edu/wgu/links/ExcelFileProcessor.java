package edu.wgu.links;

import edu.wgu.drivers.DeviceDriver;
import edu.wgu.drivers.SauceLabsAndroidDriver;
import edu.wgu.drivers.SauceLabsIos12FirefoxDriver;
import edu.wgu.drivers.SauceLabsIosDriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFileProcessor {

    private final int BATCH_SIZE = 500;
    private final int threadCount;
    private final String resultPath;
    private final String platform;
    private final boolean realDevice;

    public ExcelFileProcessor(int threadCount, String resultPath, String platform, Boolean realDevice) {
        this.threadCount = threadCount;
        this.resultPath = resultPath;
        this.platform = platform;
        this.realDevice = realDevice;
    }

    private static final String BASE_URL = new File("src/main/java/edu/wgu/links/").getAbsolutePath();

    private Path finalResultsFilePath;

    public static void main(String[] args) throws Exception {

        ArgumentParser parser = ArgumentParsers.newFor("url test").build()
                .description("Process some integers.");
        parser.addArgument("--platform")
                .type(String.class)
                .dest("platform")
                .nargs("?")
                .help("Platform to run tests on (iOs, Android");
        parser.addArgument("--outputLocation")
                .type(String.class)
                .dest("outputLocation")
                .nargs("?")
                .help("Path to store the output location");
        parser.addArgument("--threadCount")
                .type(Integer.class)
                .dest("threadCount")
                .nargs("?")
                .setDefault(0)
                .help("Path to store the output location");
        parser.addArgument("--realDevice")
                .type(Boolean.class)
                .dest("realDevice")
                .nargs("?")
                .setDefault(Boolean.TRUE)
                .help("Path to store the output location");
        try {
            Namespace res = parser.parseArgs(args);
            String platform = res.get("platform").toString();
            String outputLocation = res.get("outputLocation").toString();
            Integer threadCount = res.get("threadCount");
            Boolean realDevice = res.get("realDevice");

            new ExcelFileProcessor(threadCount, outputLocation, platform, realDevice).execute();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            return;
        }
        return;
    }

    private void execute() throws InterruptedException, IOException {
        finalResultsFilePath = Paths.get(this.resultPath + "/FinalResults-" + platform + "-"
                + DateTimeFormatter.ofPattern("yy_MM_dd").format(LocalDate.now()) + ".csv");
        if (!Files.exists(finalResultsFilePath)) {
            Files.createDirectories(finalResultsFilePath.getParent());
            Files.createFile(finalResultsFilePath);
        }
        Files.write(finalResultsFilePath,
                new String("URL, Landed URL, Platform, Compatible" + System.lineSeparator()).getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING);

        Set<String> inputUrls = loadFromExcelFile();
        inputUrls.removeIf(c -> !c.startsWith("http") || c.contains(" ") || c.contains("text/html"));

        ExecutorService executor = Executors.newFixedThreadPool(this.threadCount);
        final AtomicInteger counter = new AtomicInteger(0);
        Collection<List<String>> urlBatches = inputUrls.stream()
                .collect(Collectors.groupingBy(s -> counter.getAndIncrement() / BATCH_SIZE)).values();
        AtomicInteger batchId = new AtomicInteger();
        urlBatches.stream().forEach(batch -> {
            executor.submit(getDriver(batch, batchId.incrementAndGet(), this.platform, this.realDevice));
        });
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.DAYS);
    }

    private DeviceDriver getDriver(List<String> batch, int batchId, String platform, boolean isReal) {
        switch (platform) {
            case "Android":
                return new SauceLabsAndroidDriver(batch, this::writeBackToExcel, batchId, isReal);
            case "iOs":
                return new SauceLabsIosDriver(batch, this::writeBackToExcel, batchId, isReal);
            case "iOsFirefox":
                return new SauceLabsIos12FirefoxDriver(batch, this::writeBackToExcel, batchId, isReal);
        }
        return null;
    }

    private Set<String> loadFromExcelFile() {
        Set<String> output = new TreeSet<>();
        try (FileInputStream file = new FileInputStream(new File(BASE_URL + "/LRPS.xlsx"));
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
            sheet = workbook.getSheetAt(1);

            // Iterate through each rows one by one
            rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    if (cell.getColumnIndex() == 0) {
                        output.add(cell.getStringCellValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    private void writeBackToExcel(CleanURL cleanURL) {
        try {
            Files.write(finalResultsFilePath, cleanURL.toString().getBytes(),
                    StandardOpenOption.APPEND);
            Files.write(finalResultsFilePath, "\r\n".getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
