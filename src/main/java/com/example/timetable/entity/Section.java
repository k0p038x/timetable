package com.example.timetable.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.map.MultiKeyMap;

import java.io.Serializable;
import java.util.List;

@Data
public class Section implements Serializable {
    private String sectionName;
    private MultiKeyMap allotment;

    public Section() {
        allotment = new MultiKeyMap();
    }

    public void setAllotment(Integer[] dayWiseAlloc, String periodId, String professorName) {
        for (int day = 0; day < 6; ++day) {
            if (dayWiseAlloc[day] == 0) continue;;
            allotment.put(day, periodId, professorName);
        }
    }

    public boolean possibleAllotment(Integer[] dayWiseAlloc, String periodId) {
        for (int day = 0; day < 6; ++day) {
            if (dayWiseAlloc[day] == 0) continue;
            if (allotment.get(day, periodId) != null)
                return false;
        }
        return true;
    }

    public void unsetAllotment(Integer[] dayWiseAlloc, String periodId) {
        for (int day = 0; day < 6; ++day) {
            if (dayWiseAlloc[day] == 0) continue;;
            allotment.remove(day, periodId);
        }
    }

    public String getAllotment(int day, String periodId) {
        return (String) allotment.get(day, periodId);
    }

    public boolean validate(List<String> periodIds) {
        for (int day = 1; day <= 6; ++day) {
            for (int i = 0; i < periodIds.size(); i++) {
                if (getAllotment(day, periodIds.get(i)) == null)
                    continue;

                for (int j = i-2; j >= 0; j--) {
                    if (getAllotment(day, periodIds.get(i)).equals(getAllotment(day, periodIds.get(j))))
                        return false;
                }
            }
        }
        return true;
    }
}
