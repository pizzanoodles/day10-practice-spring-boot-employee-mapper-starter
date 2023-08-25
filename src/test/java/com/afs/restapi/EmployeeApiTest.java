package com.afs.restapi;

import com.afs.restapi.entity.Employee;
import com.afs.restapi.repository.EmployeeRepository;
import com.afs.restapi.service.dto.EmployeeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void should_find_employees() throws Exception {
        Employee bob = employeeRepository.save(getEmployeeBob());
        mockMvc.perform(get("/employees"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(bob.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(bob.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(bob.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(bob.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(bob.getSalary()));
    }

    @Test
    void should_find_employee_by_gender() throws Exception {
        Employee bob = employeeRepository.save(getEmployeeBob());
        Employee susan = employeeRepository.save(getEmployeeSusan());

        mockMvc.perform(get("/employees?gender={0}", "Male"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(bob.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(bob.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(bob.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(bob.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(bob.getSalary()));
    }

    @Test
    void should_create_employee() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Alice", 20, "Female", 50000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String employeeRequestJSON = objectMapper.writeValueAsString(employeeRequest);
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeRequestJSON))
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(employeeRequest.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(employeeRequest.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(employeeRequest.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").doesNotExist());
    }

    @Test
    void should_update_employee_age_and_salary() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Jens", 23, "Male", 20000, null);
        ObjectMapper postObjectMapper = new ObjectMapper();
        String employeeRequestJSON = postObjectMapper.writeValueAsString(employeeRequest);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeRequestJSON));

        EmployeeRequest employeeUpdateRequest = new EmployeeRequest("Json", 24, "Male", 2000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String updatedEmployeeJson = objectMapper.writeValueAsString(employeeUpdateRequest);
        mockMvc.perform(put("/employees/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployeeJson))
                .andExpect(MockMvcResultMatchers.status().is(204));

        Optional<Employee> optionalEmployee = employeeRepository.findById(1L);
        assertTrue(optionalEmployee.isPresent());
        Employee updatedEmployee = optionalEmployee.get();
        Assertions.assertEquals(employeeUpdateRequest.getAge(), updatedEmployee.getAge());
        Assertions.assertEquals(employeeUpdateRequest.getSalary(), updatedEmployee.getSalary());
        Assertions.assertEquals(1L, updatedEmployee.getId());
        Assertions.assertEquals(employeeRequest.getName(), updatedEmployee.getName());
        Assertions.assertEquals(employeeRequest.getGender(), updatedEmployee.getGender());
    }

    @Test
    void should_find_employee_by_id() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Jens", 23, "Male", 50000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String employeeRequestJSON = objectMapper.writeValueAsString(employeeRequest);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeRequestJSON));

        mockMvc.perform(get("/employees/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(employeeRequest.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(employeeRequest.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gender").value(employeeRequest.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(employeeRequest.getSalary()));
    }

    @Test
    void should_find_employees_by_page() throws Exception {
        Employee bob = employeeRepository.save(getEmployeeBob());
        Employee susan = employeeRepository.save(getEmployeeSusan());
        Employee lily = employeeRepository.save(getEmployeeLily());

        mockMvc.perform(get("/employees")
                        .param("pageNumber", "1")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(bob.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(bob.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(bob.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(bob.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(bob.getSalary()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(susan.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(susan.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].age").value(susan.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].gender").value(susan.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].salary").value(susan.getSalary()));
    }

    @Test
    void should_delete_employee_by_id() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest("Jens", 23, "Male", 50000, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String employeeRequestJSON = objectMapper.writeValueAsString(employeeRequest);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(employeeRequestJSON));

        mockMvc.perform(delete("/employees/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().is(204));

        assertTrue(employeeRepository.findById(1L).isEmpty());
    }

    private static Employee getEmployeeBob() {
        Employee employee = new Employee();
        employee.setName("Bob");
        employee.setAge(22);
        employee.setGender("Male");
        employee.setSalary(10000);
        return employee;
    }

    private static Employee getEmployeeSusan() {
        Employee employee = new Employee();
        employee.setName("Susan");
        employee.setAge(23);
        employee.setGender("Female");
        employee.setSalary(11000);
        return employee;
    }

    private static Employee getEmployeeLily() {
        Employee employee = new Employee();
        employee.setName("Lily");
        employee.setAge(24);
        employee.setGender("Female");
        employee.setSalary(12000);
        return employee;
    }
}