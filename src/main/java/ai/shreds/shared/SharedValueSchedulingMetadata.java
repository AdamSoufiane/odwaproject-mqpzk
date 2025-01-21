package ai.shreds.shared.value_objects;

public class SharedValueSchedulingMetadata {
    private String startTime;

    public SharedValueSchedulingMetadata() {
    }

    public SharedValueSchedulingMetadata(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
