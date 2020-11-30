package com.aniu.downvideo.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

public interface VideoStatusService {

    boolean saveExcelData(String filePath) throws IOException, InvalidFormatException;

}
