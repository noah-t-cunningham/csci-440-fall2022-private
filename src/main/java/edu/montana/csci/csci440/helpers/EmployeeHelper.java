package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.*;

public class EmployeeHelper {

    public static String makeEmployeeTree() {
        // TODO, change this to use a single query operation to get all employees
        Employee employee = Employee.find(1); // root employee
        // and use this data structure to maintain reference information needed to build the tree structure
        Map<Long, List<Employee>> employeeMap = new HashMap<>();
        List<Employee> all = Employee.all();
       //Map<Long, Long>  reportsToList = new HashMap<>();
        for(Employee e: all){
            long e_reportsTo = e.getReportsTo();
            List<Employee> peons = new LinkedList<>();
            for(Employee e2: all) {
                long e2_reportsTo = e2.getReportsTo();
                if(e2_reportsTo == e.getEmployeeId()){
                    peons.add(e2);
                }
            }
            employeeMap.put(e.getEmployeeId(), peons);
        }
        return "<ul>" + makeTree(employee, employeeMap) + "</ul>";
    }

    // TODO - currently this method just uses the employee.getReports() function, which
    //  issues a query.  Change that to use the employeeMap variable instead
    public static String makeTree(Employee employee, Map<Long, List<Employee>> employeeMap) {
        String list = "<li><a href='/employees" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>";
        List<Employee> reports = employeeMap.get(employee.getEmployeeId());
        for (Employee report : reports) {
            list += makeTree(report, employeeMap);
        }
        return list + "</ul></li>";
    }
}
