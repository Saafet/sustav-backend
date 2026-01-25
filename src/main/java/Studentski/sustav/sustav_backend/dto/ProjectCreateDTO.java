package Studentski.sustav.sustav_backend.dto;

import java.util.List;

public class ProjectCreateDTO {
    private String name;
    private String description;
    private List<Long> members;

    // Getteri i setteri
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Long> getMembers() { return members; }
    public void setMembers(List<Long> members) { this.members = members; }
}
