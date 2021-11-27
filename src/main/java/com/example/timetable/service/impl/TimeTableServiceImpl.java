package com.example.timetable.service.impl;

import com.example.timetable.entity.Period;
import com.example.timetable.entity.Professor;
import com.example.timetable.entity.Section;
import com.example.timetable.service.TimeTableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TimeTableServiceImpl implements TimeTableService {
    List<Professor> professors;
    Map<String, Section> sectionMap;
    Map<String, Period> periodMap;
    List<Period> periods;
    int assignedCount;

    public TimeTableServiceImpl(List<Professor> professors, Map<String, Section> sectionMap, Map<String, Period> periodMap, List<Period> periods) {
        this.professors = professors;
        this.sectionMap = sectionMap;
        this.periodMap = periodMap;
        this.periods = periods;
        this.assignedCount = 0;
    }

    @Override
    public void createTimeTable() throws Exception {
        System.out.println("Constructing..");
        boolean status = constructTimeTable(0, 0);
        if (!status) {
            throw new Exception("Input seems wrong. Please check and try again!!");
        }
        validate();
    }

    private boolean constructTimeTable(int profIdx, int classDetailIdx) throws Exception {
        if (doneWithAllProfessors(profIdx))
            return true;
        Professor professor = professors.get(profIdx);

        if (doneWithAllClassForProfessor(classDetailIdx, professor))
             return nextProfessor(profIdx);
        Professor.ClassDetail classDetail = professor.getClassDetails().get(classDetailIdx);
//        System.out.println("Professor: " + professor.getProfessorName() + " | Class: " + classDetail.getSectionNames() + " | Diff: " + (classDetail.getWeeklyHours() - classDetail.getAllottedHours()));
        if (doneWithThisClassForProfessor(classDetail))
            return nextClass(profIdx, classDetailIdx);
        List<Period> preferredPeriods = getPreferredPeriods(classDetail);
        List<String> curSectionNames = classDetail.getSectionNames();
        List<Section> curSections = curSectionNames.stream().map(sectionName -> sectionMap.get(sectionName)).collect(Collectors.toList());

        for (Period period : preferredPeriods) {
            int req = classDetail.getWeeklyHours() - classDetail.getAllottedHours();
            Integer[][] dayWiseAllocations = getDayWiseAlloc(req);
            for (Integer[] dayWiseAlloc : dayWiseAllocations) {
                if (!freeSlot(dayWiseAlloc, period, curSections, professor))
                    continue;

                CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                    professor.setAllotment(dayWiseAlloc, period.getPeriodId(), curSectionNames);
                });
                CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                    setAllotmentForSections(professor, curSections, dayWiseAlloc, period);
                });


                CompletableFuture<Void> wait1 = CompletableFuture.allOf(future1, future2);
                try {
                    wait1.get(); // this line waits for all to be completed
                } catch (Exception e) {
                    throw new Exception(e);
                }

                if (validate() && constructTimeTable(profIdx, classDetailIdx)) {
                    return true;
                } else {
                    CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
                        professor.unsetAllotment(dayWiseAlloc, period.getPeriodId());
                    });
                    CompletableFuture<Void> future4 = CompletableFuture.runAsync(() -> {
                        unsetAllotmentForSections(curSections, dayWiseAlloc, period);
                    });


                    CompletableFuture<Void> wait2 = CompletableFuture.allOf(future3, future4);
                    try {
                        wait2.get(); // this line waits for all to be completed
                    } catch (Exception e) {
                        throw new Exception(e);
                    }
                }
            }
        }
        return false;
    }

    private Integer[][] getDayWiseAlloc(int req) {
        assert req == 3 || req >= 6;
        Integer[][] ans;
        if (req == 3) {
            ans = new Integer[][]{{1, 0, 1, 0, 1, 0}, {0, 1, 0, 1, 0, 1}};
        } else {
            ans = new Integer[][]{{1, 1, 1, 1, 1, 1}};
        }
        return ans;
    }

    private boolean nextProfessor(int profIdx) throws Exception {
        return constructTimeTable(profIdx+1, 0);
    }

    private boolean nextClass(int profIdx, int classDetailIdx) throws Exception {
        return constructTimeTable(profIdx, classDetailIdx + 1);
    }

    private boolean freeSlot(Integer[] dayWiseAlloc, Period period, List<Section> curSections, Professor professor) {
        for (Section section : curSections) {
            if (!section.possibleAllotment(dayWiseAlloc, period.getPeriodId()))
                return false;
        }
        return (professor.possibleAllotment(dayWiseAlloc, period.getPeriodId(), curSections.stream().map(section -> section.getSectionName()).collect(Collectors.toList())));
    }

    private void unsetAllotmentForSections(List<Section> curSections, Integer[] dayWiseAlloc, Period period) {
        for (Section section : curSections)
            section.unsetAllotment(dayWiseAlloc, period.getPeriodId());
    }

    private void setAllotmentForSections(Professor professor, List<Section> curSections, Integer[] dayWiseAlloc, Period period) {
        for (Section section : curSections)
            section.setAllotment(dayWiseAlloc, period.getPeriodId(), professor.getProfessorName());
    }

    private List<Period> getPreferredPeriods(Professor.ClassDetail classDetail) {
        try {
            List<Period> preferredPeriods = classDetail.getPreferredPeriodIds().stream().map(periodId -> periodMap.get(periodId)).collect(Collectors.toList());
            return preferredPeriods;
        } catch (NullPointerException e) {
//            System.out.println("Looks like no preferred periods set. Considering all..");
            return periods;
        }
    }

    private boolean doneWithThisClassForProfessor(Professor.ClassDetail classDetail) {
        return classDetail.getWeeklyHours() == classDetail.getAllottedHours();
    }

    private boolean doneWithAllClassForProfessor(int classDetailIdx, Professor professor) {
        return classDetailIdx >= professor.getClassDetails().size();
    }

    private boolean doneWithAllProfessors(int profIdx) {
        return profIdx >= professors.size();
    }


    @Override
    public boolean validate() {
        for (Professor professor : professors) {
            if (!professor.validate(new ArrayList<>(periodMap.keySet())))
                return false;
        }

        for (Section section : new ArrayList<>(sectionMap.values())) {
            if (!section.validate(new ArrayList<>(periodMap.keySet())))
                return false;
        }
        return true;
    }

}
