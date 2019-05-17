package edu.wgu.drivers;

import edu.wgu.links.CleanURL;
import java.util.List;

public class ExecutionResult {

    private final List<CleanURL> cleanUrls;

    public ExecutionResult(List<CleanURL> cleanUrls) {
        this.cleanUrls = cleanUrls;
    }
}
