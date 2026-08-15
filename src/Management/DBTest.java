package Management;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DBTest {
    
    private static final String URL = "jdbc:sqlserver://db:1433;databaseName=TaskDB;user=sa;password=YourPassword123!;encrypt=true;trustServerCertificate=true;";
    
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(URL);
            
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Driver JAR file was not found in the Classpath!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("ERROR: Failed to connect to SQL Server.");
            e.printStackTrace();
        }
        return conn;
    }

    
    public static void saveEmployee(Employee emp) {
    	if (isEmployeeExists(emp.getID_number())) {
    		throw new IllegalArgumentException("Employee with ID " + emp.getID_number() + " already exists in SQL.");
        }
    	
    	String sql = "INSERT INTO employees (id_number, first_name, last_name, role, working_since) VALUES (?, ?, ?, ?, ?)";
    	try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
               
               String[] nameParts = emp.getFullName().split(" ", 2);
               String firstName = nameParts[0];
               String lastName = (nameParts.length > 1) ? nameParts[1] : "";
               pstmt.setInt(1, emp.getID_number());
               pstmt.setString(2, firstName);
               pstmt.setString(3, lastName);
               pstmt.setString(4, emp.getRole().getType().name());
               pstmt.setInt(5, emp.getStartingDate());
               
               pstmt.executeUpdate();
               System.out.println("SUCCESS: Employee '" + firstName + "' was saved to SQL Server!");
    	} catch (SQLException e) {
    		throw new RuntimeException("Database error: " + e.getMessage());
        }
    }
    
    public static boolean isEmployeeExists(int id) {
        String sql = "SELECT COUNT(*) FROM employees WHERE id_number = ?"; 
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean isTaskExists(String description, int creatorId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE task_description = ? AND creator_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
        	pstmt.setString(1, description);
            pstmt.setInt(2, creatorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static void saveTask(Task task) {
    	
    	if (isTaskExists(task.getTask(), task.getCreator().getID_number())) {
            System.out.println("INFO: Task '" + task.getTask() + "' already exists. Skipping save.");
            return;
        }
    	
    	String sql = "INSERT INTO tasks (task_description, status, started, creator_id, assigned_employee_id, parent_task_id) VALUES (?, ?, ?, ?, ?, ?)";
            
    	try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
    			pstmt.setString(1, task.getTask());
                pstmt.setString(2, task.getStatus());
                pstmt.setInt(3, task.getStarted());
                pstmt.setInt(4, task.getCreator().getID_number());
                pstmt.setInt(5, task.getAssignedEmployee().getID_number());
                
                if (task.getParentTask() != null) {
                    pstmt.setInt(6, task.getParentTask().getTaskID());
                } else {
                    pstmt.setNull(6, Types.INTEGER);
                }
                pstmt.executeUpdate();       
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int realSqlId = generatedKeys.getInt(1);                        
                        task.setTaskID(realSqlId); 
                        
                        System.out.println("SUCCESS: Task '" + task.getTask() + "' was saved with SQL ID: " + realSqlId);
                    }
                }
                
            } catch (SQLException e) {
                System.out.println("ERROR: Could not save task to SQL.");
                e.printStackTrace();
            }
        }
    
    public static List<Task> loadTasks(TaskManager manager, List<Employee> allEmployees) {
        List<Task> loadedTasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("task_id");
                String desc = rs.getString("task_description");
                String status = rs.getString("status");
                int started = rs.getInt("started");
                int creatorId = rs.getInt("creator_id");
                int assigneeId = rs.getInt("assigned_employee_id");
                
                // מציאת העובדים לפי ה-ID ששמור ב-SQL
                Employee creator = findEmployeeById(allEmployees, creatorId);
                Employee assignee = findEmployeeById(allEmployees, assigneeId);
                
                int parentId = rs.getInt("parent_task_id");
                Task task = new Task(id, desc, status, started, creator, assignee);
                task.setTempParentId(parentId); 
                
                loadedTasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loadedTasks;
    }

    private static Employee findEmployeeById(List<Employee> employees, int id) {
        return employees.stream().filter(e -> e.getID_number()== id).findFirst().orElse(null);
    }
    
    public static void loadEmployees(TaskManager manager) {
        String sql = "SELECT * FROM employees";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            
            while (rs.next()) {
                int id = rs.getInt("id_number");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String roleName = rs.getString("role");
                int workingSince = rs.getInt("working_since");
                
                Roles roleEnum = Roles.valueOf(roleName); 
                Role empRole = new Role(roleEnum, 0, roleName); 
                
                String fullName = firstName + " " + lastName;
                Employee emp = new Employee(fullName, roleName, id, workingSince, empRole);
                
                manager.loadEmployeeToMemory(emp);
                count++;
            }
            System.out.println("SUCCESS: Loaded " + count + " employees from SQL!");
            
        } catch (Exception e) {
            System.out.println("ERROR: Could not load employees from SQL.");
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) {
        getConnection();
    }
}