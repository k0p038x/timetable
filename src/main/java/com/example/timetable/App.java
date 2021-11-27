package com.example.timetable;

import com.example.timetable.entity.Period;
import com.example.timetable.entity.Professor;
import com.example.timetable.entity.Section;
import com.example.timetable.service.TimeTableService;
import com.example.timetable.service.impl.TimeTableServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.jakewharton.fliptables.FlipTableConverters;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {
    public static Map<String, Professor> professorMap;
    public static Map<String, Period> periodMap;
    public static Map<String, Section> sectionMap;
    public static List<Professor> professors;
    public static List<Section> sections;
    public static List<Period> periods;
    public static ObjectMapper objectMapper;

    private static void initPeriods() throws IOException {
        URL url = Resources.getResource("periods.json");
        String periodsJson = Resources.toString(url, Charset.defaultCharset());
        periods = objectMapper.readValue(periodsJson, new TypeReference<List<Period>>(){});

        for (Period period : periods) {
            periodMap.put(period.getPeriodId(), period);
        }
    }

    private static void initSections() throws IOException {
        URL url = Resources.getResource("sections.json");
        String sectionsJson = Resources.toString(url, Charset.defaultCharset());
        sections = objectMapper.readValue(sectionsJson, new TypeReference<List<Section>>(){});

        for (Section section : sections) {
            sectionMap.put(section.getSectionName(), section);
        }
    }

    private static void initProfessors() throws IOException {
        URL url = Resources.getResource("professors.json");
        String professorsJson = Resources.toString(url, Charset.defaultCharset());
        professors = objectMapper.readValue(professorsJson, new TypeReference<List<Professor>>(){});

        for (Professor professor : professors) {
            professorMap.put(professor.getProfessorName(), professor);
        }
    }

    private static String[][] appendDayAsRowHeader(String[][] data) {
        int r = data.length;
        int c = data[0].length;
        String[][] modifiedData = new String[r][c + 1];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++)
                modifiedData[i][j + 1] = data[i][j];
        }
        modifiedData[0][0] = "Mon";
        modifiedData[1][0] = "Tue";
        modifiedData[2][0] = "Wed";
        modifiedData[3][0] = "Thu";
        modifiedData[4][0] = "Fri";
        modifiedData[5][0] = "Sat";
        return modifiedData;
    }

    private static void init() throws IOException {
        objectMapper = new ObjectMapper();
        periodMap = new HashMap<>();
        sectionMap = new HashMap<>();
        professorMap = new HashMap<>();
        initPeriods();
        initSections();
        initProfessors();

        for (Professor professor : professors) {
            int hours = 0;
            for (Professor.ClassDetail classDetail : professor.getClassDetails()) {
                hours += classDetail.getWeeklyHours();
            }
            System.out.println(professor.getProfessorName() + ": " + hours);
        }

        HashMap<String, Integer> sectionHrs = new HashMap<>();
        for (Section section : sections) {
            sectionHrs.put(section.getSectionName(), 0);
        }
        for (Professor professor : professors) {
            for (Professor.ClassDetail classDetail : professor.getClassDetails()) {
                for (String sectionName : classDetail.getSectionNames()) {
                    sectionHrs.put(sectionName, sectionHrs.get(sectionName) + classDetail.getWeeklyHours());
                }
            }
        }

        for (String sectionName : sectionHrs.keySet()) {
            System.out.println(sectionName + ": " + sectionHrs.get(sectionName));
        }
    }

    private static void compute() throws Exception {
        TimeTableService timeTableService = new TimeTableServiceImpl(professors, sectionMap, periodMap, periods);
        timeTableService.createTimeTable();
    }

    private static void display() {
         String[] periodHeader = {"Day/Period", "P1", "P2", "P3", "P4", "P5", "P6", "P7"};
         for (Professor professor : professors) {
            System.out.println("Professor: " + professor.getProfessorName());
            String[][] professorAllotment = new String[6][7];

            for (int day = 0; day < 6; ++day) {
                for (int p = 0; p < periods.size(); ++p)
                    professorAllotment[day][p] = String.valueOf(professor.getAllotment(day, periods.get(p).getPeriodId()));
            }
            String[][] data = appendDayAsRowHeader(professorAllotment);
            System.out.println(FlipTableConverters.fromObjects(periodHeader, data));
        }

        for (Section section : sections) {
            System.out.println("Section: " + section.getSectionName());
            String[][] sectionAllotment = new String[6][7];

            for (int day = 0; day < 6; ++day) {
                for (int p = 0; p < periods.size(); ++p) {
                    sectionAllotment[day][p] = String.valueOf(section.getAllotment(day, periods.get(p).getPeriodId()));
                }
            }
            String[][] data = appendDayAsRowHeader(sectionAllotment);
            System.out.println(FlipTableConverters.fromObjects(periodHeader, data));
        }
    }

    public static void main( String[] args ) throws Exception {
        init();
        compute();
        display();
        return;





    }

}
