package com.example.timetable.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.map.MultiKeyMap;

import java.io.Serializable;
import java.util.List;

@Data
public class Professor implements Serializable {
    private String professorName;

    @Data
    public static class ClassDetail implements Serializable {
        private List<String> sectionNames;
        private String subject;
        private List<String> preferredPeriodIds;
        private int weeklyHours;
        private int allottedHours;

        public ClassDetail() {
            this.allottedHours = 0;
        }
        public ClassDetail(List<String> sectionNames, List<String> periodIds, int weeklyHours) {
            this.sectionNames = sectionNames;
            this.preferredPeriodIds = periodIds;
            this.weeklyHours = weeklyHours;
            this.allottedHours = 0;
        }
    }

    List<ClassDetail> classDetails;
    MultiKeyMap allotment;

    public Professor() {
        this.allotment = new MultiKeyMap();
    }

    public Professor(String professorName, List<ClassDetail> classDetails) {
        this.professorName = professorName;
        this.classDetails = classDetails;
        allotment = new MultiKeyMap();
    }

    private ClassDetail getClassDetail(List<String> sectionNames) {
        for (ClassDetail classDetail : classDetails) {
            if (classDetail.getSectionNames().equals(sectionNames))
                return classDetail;
        }
        return null;
    }

    public void setAllotment(Integer[] dayWiseAlloc, String periodId, List<String> sectionNames) {
        for (int day = 0; day < 6; day++) {
            if (dayWiseAlloc[day] == 0) continue;
            allotment.put(day, periodId, sectionNames);
            ClassDetail classDetail = getClassDetail(sectionNames);
            assert classDetail != null;
            classDetail.setAllottedHours(classDetail.getAllottedHours() + 1);
        }
    }

    public boolean possibleAllotment(Integer[] dayWiseAlloc, String periodId, List<String> sectionNames) {
        for (int day = 0; day < 6; day++) {
            if (dayWiseAlloc[day] == 0) continue;
            if (allotment.get(day, periodId) != null)
                return false;
        }
        return true;
    }

    public void unsetAllotment(Integer[] dayWiseAlloc, String periodId) {
        for (int day = 0; day < 6; day++) {
            if (dayWiseAlloc[day] == 0) continue;
            List<String> sectionNames = (List<String>) allotment.get(day, periodId);
            allotment.remove(day, periodId);
            ClassDetail classDetail = getClassDetail(sectionNames);
            assert classDetail != null;
            classDetail.setAllottedHours(classDetail.getAllottedHours() - 1);
        }
    }


    public List<String> getAllotment(int day, String periodId) {
        return (List<String>) allotment.get(day, periodId);
    }

    private boolean symmetric(List<String> periodIds) {
        int dayL = 1;
        int dayR = 4;

        while (dayL < 4) {
            for (String periodId : periodIds) {
                List<String> sectionL = getAllotment(dayL, periodId);
                List<String> sectionR = getAllotment(dayR, periodId);
                if (sectionL != null && sectionR != null && !sectionL.equals(sectionR))
                    return false;
            }
            dayL++;
            dayR++;
        }
        return true;
    }

    public boolean validate(List<String> periodIds) {
        return (symmetric(periodIds));
    }
}
