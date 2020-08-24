package com.wikiwalks.wikiwalks;
import java.util.Date;

class DistanceGoal {

    int GoalDistance;
    int RemainingDistance;
    int CurrentDistance;

    Date StartDate;
    Date EndDate;

    public DistanceGoal(int GoalDistance, int RemainingDistance, int CurrentDistance, Date StartDate, Date EndDate){

        this.GoalDistance = GoalDistance;
        this.CurrentDistance = CurrentDistance;
        this.RemainingDistance = RemainingDistance;
        this.StartDate = StartDate;
        this.EndDate = EndDate;

    }

    public int getGoalDistance() {
        return GoalDistance;
    }

    public int getCurrentDistance() {
        return CurrentDistance;
    }

    public int getRemainingDistance() {
        return RemainingDistance;
    }

    public Date getStartDate() {
        return StartDate;
    }

    public Date getEndDate() {
        return EndDate;
    }

    public void setGoalDistance(int goalDistance) {
        GoalDistance = goalDistance;
    }

    public void setRemainingDistance(int remainingDistance) {
        RemainingDistance = remainingDistance;
    }

    public void setCurrentDistance(int currentDistance) {
        CurrentDistance = currentDistance;
    }

    public void setStartDate(Date startDate) {
        StartDate = startDate;
    }

    public void setEndDate(Date endDate) {
        EndDate = endDate;
    }
}
