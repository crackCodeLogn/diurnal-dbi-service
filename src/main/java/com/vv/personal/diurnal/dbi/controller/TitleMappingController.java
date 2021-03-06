package com.vv.personal.diurnal.dbi.controller;

import com.google.protobuf.AbstractMessage;
import com.vv.personal.diurnal.artifactory.generated.TitleMappingProto;
import com.vv.personal.diurnal.dbi.interactor.diurnal.DiurnalTableTitleMapping;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vivek
 * @since 24/02/21
 */
@RestController("title-mapping-controller")
@RequestMapping("/diurnal/mapping-title")
public class TitleMappingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TitleMappingController.class);

    @Autowired
    @Qualifier("DiurnalTableTitleMapping")
    private DiurnalTableTitleMapping diurnalTableTitleMapping;

    @ApiOperation(value = "create title", hidden = true)
    @PostMapping("/create/title")
    public Integer createTitleMapping(@RequestBody TitleMappingProto.TitleMapping titleMapping) {
        LOGGER.info("Creating new title mapping: {} x {} -> {}", titleMapping.getMobile(), titleMapping.getDate(), titleMapping.getTitle());
        Integer sqlResult = diurnalTableTitleMapping.pushNewEntity(titleMapping);
        LOGGER.info("Result of new title creation: {}", sqlResult);
        return sqlResult;
    }

    @GetMapping("/create/manual/title")
    public Integer createTitleMappingManually(@RequestParam Long mobile,
                                              @RequestParam Integer date,
                                              @RequestParam String title) {
        LOGGER.info("Obtained manual req for new title creation: {} x {} -> {}", mobile, date, title);
        return createTitleMapping(TitleMappingProto.TitleMapping.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setTitle(title)
                .build());
    }

    @ApiOperation(value = "delete title", hidden = true)
    @PostMapping("/delete/title")
    public Integer deleteTitleMapping(@RequestBody TitleMappingProto.TitleMapping titleMapping) {
        LOGGER.info("Deleting title mapping: {} x {}", titleMapping.getMobile(), titleMapping.getDate());
        Integer sqlResult = diurnalTableTitleMapping.deleteEntity(titleMapping);
        LOGGER.info("Result of title deletion: {}", sqlResult);
        return sqlResult;
    }

    @GetMapping("/delete/manual/title")
    public Integer deleteTitleMappingManually(@RequestParam Long mobile,
                                              @RequestParam Integer date) {
        LOGGER.info("Obtained manual req for title deletion: {} x {}", mobile, date);
        return deleteTitleMapping(TitleMappingProto.TitleMapping.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .build());
    }

    @ApiOperation(value = "update title", hidden = true)
    @PostMapping("/update/title")
    public Integer updateTitleMapping(@RequestBody TitleMappingProto.TitleMapping titleMapping) {
        LOGGER.info("Updating title mapping: {} x {} -> {}", titleMapping.getMobile(), titleMapping.getDate(), titleMapping.getTitle());
        Integer sqlResult = diurnalTableTitleMapping.updateEntity(titleMapping);
        LOGGER.info("Result of title updation: {}", sqlResult);
        return sqlResult;
    }

    @GetMapping("/update/manual/title")
    public Integer updateTitleMappingManually(@RequestParam Long mobile,
                                              @RequestParam Integer date,
                                              @RequestParam String updatedTitle) {
        LOGGER.info("Obtained manual req for title updation: {} x {} -> {}", mobile, date, updatedTitle);
        return updateTitleMapping(TitleMappingProto.TitleMapping.newBuilder()
                .setMobile(mobile)
                .setDate(date)
                .setTitle(updatedTitle)
                .build());
    }

    @ApiOperation(value = "retrieve all titles", hidden = true)
    @GetMapping("/retrieve/all/titles")
    public TitleMappingProto.TitleMappingList retrieveAllTitleMappings() {
        LOGGER.info("Retrieving all title mappings");
        TitleMappingProto.TitleMappingList titleMappingList = diurnalTableTitleMapping.retrieveAll();
        LOGGER.info("Result of retrieving all title mappings: {} entries", titleMappingList.getTitleMappingsCount());
        return titleMappingList;
    }

    @GetMapping("/retrieve/all/manual/titles")
    public List<String> retrieveAllTitleMappingsManually() {
        LOGGER.info("Obtained manual req for retrieving all title mappings");
        return retrieveAllTitleMappings().getTitleMappingsList().stream()
                .map(AbstractMessage::toString)
                .collect(Collectors.toList());
    }
}
