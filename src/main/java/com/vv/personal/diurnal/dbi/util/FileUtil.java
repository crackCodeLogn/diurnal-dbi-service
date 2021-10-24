package com.vv.personal.diurnal.dbi.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author Vivek
 * @since 14/02/21
 */
@Slf4j
public class FileUtil {
    public static String readFileFromLocation(String src) {
        StringBuilder data = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(src))) {
            String line;
            while ((line = in.readLine()) != null) data.append(line.trim()).append("\n");
        } catch (IOException e) {
            log.error("Failed to read file contents from '{}'. ", src, e);
        }
        //log.info("Data read in => \n{}", data);
        return data.toString();
    }

    public static String readFileFromLocation(InputStream inputStream) {
        StringBuilder data = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = in.readLine()) != null) data.append(line.trim()).append("\n");
        } catch (IOException e) {
            log.error("Failed to read file contents from '{}'. ", inputStream, e);
        }
        log.debug("Data read in => \n{}", data);
        return data.toString();
    }
}