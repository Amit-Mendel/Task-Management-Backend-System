package Management;

import java.util.List;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
	
    public static void main(String[] args) {
		
        System.out.println("=== Booting Task Management System ===");
        TaskManager manager = new TaskManager();

        System.out.println("Connecting to Database...");

        DBTest.loadEmployees(manager); 
        
        List<Task> allTasks = DBTest.loadTasks(manager, manager.getAllEmployeesList()); 
        
        int mainTasksCount = 0;
        int subTasksCount = 0;

        for (Task task : allTasks) {
            if (task.getTempParentId() == 0) {
                try {
                    manager.addTask(task, task.getCreator());
                    mainTasksCount++;
                } catch (Exception e) {
                    System.out.println("Warning: Skipped loading Task ID " + task.getTaskID() + " due to logic/permission error.");
                }
            }
        }
        
        for (Task subTask : allTasks) {
            if (subTask.getTempParentId() != 0) {
                Task parent = allTasks.stream()
                        .filter(t -> t.getTaskID() == subTask.getTempParentId())
                        .findFirst().orElse(null);
                        
                if (parent != null) {
                    try {
                        subTask.setParentTask(parent); 
                        manager.addSubTask(parent, subTask, subTask.getCreator());
                        subTasksCount++;
                    } catch (Exception e) {
                        System.out.println("Warning: Skipped loading SubTask ID " + subTask.getTaskID() + " due to logic/permission error.");
                    }
                }
            }
        }
        
        System.out.println("SUCCESS: Loaded " + mainTasksCount + " Main Tasks and " + subTasksCount + " Sub-Tasks from SQL!");
        System.out.println("System is ready for use!");
        
        System.out.println("Starting REST API Server on port 8080...");
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            server.createContext("/api/hello", new HelloHandler());
            server.createContext("/api/employees", new EmployeesHandler(manager));
            server.createContext("/api/tasks", new TasksHandler(manager));
            
            server.setExecutor(null); 
            server.start();
            
            System.out.println("Server is running! Try opening http://localhost:8080/api/employees in your browser.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Welcome to your awesome Task Management API!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class EmployeesHandler implements HttpHandler {
        private TaskManager manager;

        public EmployeesHandler(TaskManager manager) {
            this.manager = manager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Employee> employees = manager.getAllEmployeesList();
                
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{\n");
                jsonBuilder.append("  \"employees\": [\n");
                
                for (int i = 0; i < employees.size(); i++) {
                    Employee emp = employees.get(i);
                    jsonBuilder.append("    {\n");
                    jsonBuilder.append("      \"id\": ").append(emp.getID_number()).append(",\n");
                    jsonBuilder.append("      \"full_name\": \"").append(emp.getFullName()).append("\",\n");
                    jsonBuilder.append("      \"role\": \"").append(emp.getRole().getType()).append("\"\n");
                    
                    if (i < employees.size() - 1) {
                        jsonBuilder.append("    },\n");
                    } else {
                        jsonBuilder.append("    }\n");
                    }
                }
                
                jsonBuilder.append("  ]\n");
                jsonBuilder.append("}");
                
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] responseBytes = jsonBuilder.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
                
            } else if ("POST".equals(method)) {
                    try {
                        // 1. קריאת המידע שנשלח מפייתון
                        InputStream is = exchange.getRequestBody();
                        String requestBody = new String(is.readAllBytes());
                        System.out.println(">>> Received JSON from Python: " + requestBody);
                        
                        // 2. המרה של ה-JSON לאובייקט
                        Gson gson = new Gson();
                        Employee newEmployee = gson.fromJson(requestBody, Employee.class);
                        
                        // --- בדיקת רנטגן 1: האם ה-ID עבר בהצלחה או הפך ל-0? ---
                        System.out.println(">>> X-RAY 1: Parsed Employee ID is: " + newEmployee.getID_number());
                        // -----------------------------------------------------

                        // 3. ניסיון הוספה
                        manager.addEmployee(newEmployee); 
                        
                        // --- בדיקת רנטגן 2: האם העובד באמת נכנס לזיכרון של השרת? ---
                        Employee checkInMem = manager.getEmployeeByID(newEmployee.getID_number());
                        System.out.println(">>> X-RAY 2: Is Employee in RAM now? " + (checkInMem != null));
                        // -----------------------------------------------------------
                        
                        System.out.println(">>> New Employee added successfully via Python!");
                        String response = "Java Server: Employee created and saved to DB successfully!";
                        exchange.sendResponseHeaders(201, response.getBytes().length); 
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    
                } catch (Exception e) {
                    // תפסנו את ההתרסקות!
                    System.out.println(">>> SERVER CRASH CAUGHT IN POST!");
                    e.printStackTrace(); 
                    
                    String errorMsg = (e.getMessage() != null) ? e.getMessage() : "Unknown Crash";
                    String errorResponse = "Error from Java: " + errorMsg;
                    exchange.sendResponseHeaders(409, errorResponse.getBytes().length); 
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                }
            }
        }
    }
    
    static class TasksHandler implements HttpHandler {
        private TaskManager manager;

        public TasksHandler(TaskManager manager) {
            this.manager = manager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("POST".equals(method)) {
                try {
                    // 1. קריאת המידע מהפייתון
                    InputStream is = exchange.getRequestBody();
                    String requestBody = new String(is.readAllBytes());
                    System.out.println(">>> Received JSON Task: " + requestBody);
                    
                    Gson gson = new Gson();
                    Task newTask = gson.fromJson(requestBody, Task.class);
                    
                    Employee creator = manager.getEmployeeByID(newTask.getCreator().getID_number());
                    Employee assignee = manager.getEmployeeByID(newTask.getAssignedEmployee().getID_number());
                    
                    if (creator == null || assignee == null) {
                        throw new IllegalArgumentException("Creator or Assignee ID does not exist in the system!");
                    }
                    
                    newTask.setCreator(creator);
                    newTask.setAssignedEmployee(assignee);
                    
                    DBTest.saveTask(newTask);
                    manager.addTask(newTask, creator);
                    
                    System.out.println(">>> New Task saved successfully via Python!");
                    String response = "Java Server: Task created successfully!";
                    exchange.sendResponseHeaders(201, response.getBytes().length); 
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    
                } catch (Exception e) {
                    System.out.println(">>> SERVER CRASH CAUGHT IN POST TASKS!");
                    e.printStackTrace(); 
                    
                    String errorMsg = (e.getMessage() != null) ? e.getMessage() : "Unknown Crash";
                    String errorResponse = "Error from Java: " + errorMsg;
                    exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(errorResponse.getBytes());
                    os.close();
                }
            } else {
                String response = "GET Tasks is not supported yet.";
                exchange.sendResponseHeaders(405, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}