package com.vv.personal.diurnal.dbi.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Vivek
 * @since 03/03/21
 */
@RunWith(JUnit4.class)
public class DataControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataControllerTest.class);

    public static String readFileFromLocation(String src) {
        StringBuilder data = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(src))) {
            String line;
            while ((line = in.readLine()) != null) data.append(line.trim()).append("\n");
        } catch (IOException e) {
            LOGGER.error("Failed to read file contents from '{}'. ", src, e);
        }
        //LOGGER.info("Data read in => \n{}", data);
        return data.toString();
    }

    @Test
    public void testPushWholeBackup() {
        String testData = readFileFromLocation("src/test/resources/sample.backup.txt");
        System.out.println(testData);
    }

}