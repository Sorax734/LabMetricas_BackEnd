package com.labMetricas.LabMetricas.maintenance.model.dto;

import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.user.model.User;

import java.util.List;

public class MaintenanceInitDataDto {
    private List<UserSummaryDto> users;
    private List<MaintenanceTypeSummaryDto> maintenanceTypes;
    private List<EquipmentSummaryDto> equipments;

    public MaintenanceInitDataDto() {}

    public MaintenanceInitDataDto(List<UserSummaryDto> users, 
                                  List<MaintenanceTypeSummaryDto> maintenanceTypes,
                                  List<EquipmentSummaryDto> equipments) {
        this.users = users;
        this.maintenanceTypes = maintenanceTypes;
        this.equipments = equipments;
    }

    // User Summary DTO
    public static class UserSummaryDto {
        private String id;
        private String name;
        private String email;
        private String roleName;

        public UserSummaryDto() {}

        public UserSummaryDto(User user) {
            this.id = user.getId().toString();
            this.name = user.getName();
            this.email = user.getEmail();
            this.roleName = user.getRole().getName();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }

    // Maintenance Type Summary DTO
    public static class MaintenanceTypeSummaryDto {
        private String id;
        private String name;

        public MaintenanceTypeSummaryDto() {}

        public MaintenanceTypeSummaryDto(MaintenanceType type) {
            this.id = type.getId().toString();
            this.name = type.getName();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Equipment Summary DTO
    public static class EquipmentSummaryDto {
        private String id;
        private String name;
        private String code;
        private String brand;
        private String model;

        public EquipmentSummaryDto() {}

        public EquipmentSummaryDto(com.labMetricas.LabMetricas.equipment.model.Equipment equipment) {
            this.id = equipment.getId().toString();
            this.name = equipment.getName();
            this.code = equipment.getCode();
            this.brand = equipment.getBrand();
            this.model = equipment.getModel();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    // Getters and Setters
    public List<UserSummaryDto> getUsers() { return users; }
    public void setUsers(List<UserSummaryDto> users) { this.users = users; }
    public List<MaintenanceTypeSummaryDto> getMaintenanceTypes() { return maintenanceTypes; }
    public void setMaintenanceTypes(List<MaintenanceTypeSummaryDto> maintenanceTypes) { this.maintenanceTypes = maintenanceTypes; }
    public List<EquipmentSummaryDto> getEquipments() { return equipments; }
    public void setEquipments(List<EquipmentSummaryDto> equipments) { this.equipments = equipments; }
} 