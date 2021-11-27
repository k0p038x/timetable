package com.example.timetable.service;

import com.example.timetable.entity.Period;
import com.example.timetable.entity.Professor;
import com.example.timetable.entity.Section;

import java.util.List;

public interface TimeTableService {
    void createTimeTable() throws Exception;
    boolean validate();
}
