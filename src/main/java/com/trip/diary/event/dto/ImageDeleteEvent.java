package com.trip.diary.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ImageDeleteEvent {
    private List<String> imagePaths;
}
