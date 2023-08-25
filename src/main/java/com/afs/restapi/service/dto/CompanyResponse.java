package com.afs.restapi.service.dto;

public class CompanyResponse {
    private String name;
    private Integer employeesCount;

    public CompanyResponse() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEmployeesCount() {
        return employeesCount;
    }

    public void setEmployeesCount(Integer employeesCount) {
        this.employeesCount = employeesCount;
    }


}
