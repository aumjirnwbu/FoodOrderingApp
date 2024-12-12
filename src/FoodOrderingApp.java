import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.lang.String;

public class FoodOrderingApp {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private int currentUserId; // User ID of logged-in user
    // List to hold the order details
    private final DefaultListModel<String> orderList = new DefaultListModel<>();

    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + File.separator + "food_ordering.db";

    // เมธอดสำหรับเชื่อมต่อกับฐานข้อมูล
    private Connection connectToDatabase() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    public static void main(String[] args) {
        FoodOrderingApp app = new FoodOrderingApp();
        app.createDatabaseIfNotExists();
        EventQueue.invokeLater(() -> {
            try {
                FoodOrderingApp window = new FoodOrderingApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public FoodOrderingApp() {
        initialize();
    }
    private void createDatabaseIfNotExists() {
        File dbFile = new File(DB_URL);  // เส้นทางของไฟล์ฐานข้อมูล
        if (!dbFile.exists()) {
            try (Connection conn = connectToDatabase()) {
                // สร้างคำสั่ง SQL สำหรับสร้างตาราง
                String createUsersTableSQL =
                        "CREATE TABLE IF NOT EXISTS users ("
                                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + "username VARCHAR(255) UNIQUE, "
                                + "password VARCHAR(255), "
                                + "cash_balance DOUBLE DEFAULT 0, "
                                + "points DOUBLE DEFAULT 0, "
                                + "totalspents DOUBLE DEFAULT 0"
                                + ");";

                String createFoodItemsTableSQL =
                        "CREATE TABLE IF NOT EXISTS food_items ("
                                + "food_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + "name VARCHAR(100) NOT NULL, "
                                + "description TEXT, "
                                + "price DOUBLE NOT NULL"
                                + ");";

                String createOrdersTableSQL =
                        "CREATE TABLE IF NOT EXISTS orders ("
                                + "order_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + "user_id INTEGER, "
                                + "food_id INTEGER, "
                                + "quantity INTEGER NOT NULL, "
                                + "order_status VARCHAR(50) DEFAULT 'Pending', "
                                + "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                                + "FOREIGN KEY(user_id) REFERENCES users(user_id), "
                                + "FOREIGN KEY(food_id) REFERENCES food_items(food_id)"
                                + ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createFoodItemsTableSQL);  // สร้างตาราง food_items
                    System.out.println("food_items table created successfully!");

                    // เพิ่มเมนู 10 เมนูเข้าไปในตาราง food_items
                    String insertFoodItemsSQL =
                            "INSERT INTO food_items (name, description, price) VALUES "
                                    + "('Pizza', 'Delicious cheese pizza', 9.99), "
                                    + "('Burger', 'Juicy beef burger with cheese', 5.49), "
                                    + "('Pasta', 'Pasta with marinara sauce', 7.99), "
                                    + "('Salad', 'Fresh garden salad', 4.99), "
                                    + "('Sushi', 'Assorted sushi rolls', 12.99), "
                                    + "('Steak', 'Grilled steak with vegetables', 15.99), "
                                    + "('Sandwich', 'Ham and cheese sandwich', 3.99), "
                                    + "('Ice Cream', 'Vanilla ice cream with chocolate sauce', 2.99), "
                                    + "('Tacos', 'Beef tacos with toppings', 6.49), "
                                    + "('Fried Chicken', 'Crispy fried chicken pieces', 8.49);";

                    // ทำการ insert เมนูลงในตาราง
                    stmt.executeUpdate(insertFoodItemsSQL);  // เพิ่มข้อมูล 10 เมนู
                    System.out.println("10 food items inserted successfully!");
                }
                // สร้างตารางต่างๆ ในฐานข้อมูล
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createUsersTableSQL);
                    stmt.execute(createFoodItemsTableSQL);
                    stmt.execute(createOrdersTableSQL);
                    System.out.println("Database and tables created successfully at " + dbFile.getAbsolutePath());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Database already exists at " + dbFile.getAbsolutePath());
        }
    }

    private void initialize() {
        frame = new JFrame("Food Ordering App");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createLoginPanel(), "Login");
        cardPanel.add(createRegisterPanel(), "Register");
        cardPanel.add(createHomePanel(), "Home");

        frame.getContentPane().add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Padding between components

        // Create components
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);  // Adjust width of the text field
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);  // Adjust width of the password field

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(_ -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (validateLogin(username, password)) {
                cardLayout.show(cardPanel, "Home"); // Switch to home after login
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.");
            }
        });

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(_ -> cardLayout.show(cardPanel, "Register"));

        // Set grid constraints for each component
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(registerButton, gbc);

        // Center align the components in the panel
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel(""), gbc); // Add empty label for spacing at the bottom

        return panel;
    }


    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Padding between components

        // Create components
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);  // Adjust width of the text field
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);  // Adjust width of the password field

        JButton registerButton = getjButton(usernameField, passwordField);

        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(_ -> cardLayout.show(cardPanel, "Login"));

        // Set grid constraints for each component
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(registerButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(backButton, gbc);

        // Center align the components in the panel
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel(""), gbc); // Add empty label for spacing at the bottom

        return panel;
    }


    private JButton getjButton(JTextField usernameField, JPasswordField passwordField) {
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(_ -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (registerUser(username, password)) {
                JOptionPane.showMessageDialog(frame, "Registration successful. Please login.");
                cardLayout.show(cardPanel, "Login");
            } else {
                JOptionPane.showMessageDialog(frame, "Username already exists.");
            }
        });
        return registerButton;
    }

    private boolean validateLogin(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(password)) {
                    currentUserId = rs.getInt("user_id");
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean registerUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());  // ใช้ BorderLayout สำหรับจัดตำแหน่งของ welcomeLabel และปุ่ม

        // สร้างข้อความต้อนรับ (welcome label)
        JLabel welcomeLabel = new JLabel("Welcome to Food Ordering App", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 24));

        // สร้างแผงสำหรับปุ่มต่างๆ
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());  // ใช้ GridBagLayout สำหรับจัดตำแหน่งปุ่ม
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // เพิ่มช่องว่างระหว่างปุ่ม

        // สร้างปุ่ม
        JButton orderButton = new JButton("Order");
        orderButton.addActionListener(_ -> showOrderPanel());

        JButton topUpButton = new JButton("Top-Up");
        topUpButton.addActionListener(_ -> showTopUpPanel());

        JButton historyButton = new JButton("History");
        historyButton.addActionListener(_ -> showHistoryPanel(currentUserId));

        JButton pointsButton = new JButton("Points");
        pointsButton.addActionListener(_ -> showPointsPanel(currentUserId));

        // ตั้งตำแหน่งปุ่มต่างๆ ลงใน GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(orderButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        buttonPanel.add(topUpButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(historyButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        buttonPanel.add(pointsButton, gbc);

        // เพิ่มข้อความต้อนรับ (welcome label) ไปที่ตำแหน่งเหนือสุด (North) ของ panel
        panel.add(welcomeLabel, BorderLayout.NORTH);  // ข้อความจะอยู่ที่ด้านบนตรงกลาง
        panel.add(buttonPanel, BorderLayout.CENTER);  // ปุ่มต่างๆ จะอยู่ตรงกลางด้านล่างของข้อความ

        return panel;
    }



    // แสดงประวัติการสั่งซื้อของผู้ใช้เฉพาะ
    private void showHistoryPanel(int userId) {
        // สร้าง JPanel สำหรับแสดงข้อมูล
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());

        // เพิ่ม title หรือ label สำหรับหน้าจอ history
        JLabel historyTitle = new JLabel("Order History", JLabel.CENTER);
        historyTitle.setFont(new Font("Serif", Font.BOLD, 24));

        // สมมติว่าเรามีเมธอด getUserOrderHistory() ที่ดึงประวัติการสั่งซื้อในรูปแบบ List<String> ตาม userId
        List<String> orderHistory = getUserOrderHistory(userId);  // ดึงประวัติการสั่งซื้อจาก userId

        // เปลี่ยน List<String> เป็น DefaultListModel สำหรับ JList
        DefaultListModel<String> historyListModel = new DefaultListModel<>();
        for (String order : orderHistory) {
            historyListModel.addElement(order);
        }

        // สร้าง JList สำหรับแสดงประวัติการสั่งซื้อ
        JList<String> historyList = new JList<>(historyListModel);
        JScrollPane scrollPane = new JScrollPane(historyList);  // ทำให้สามารถเลื่อนดูรายการได้

        // เพิ่มปุ่มกลับเพื่อไปยังหน้าหลัก
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Home"));

        // เพิ่มคอมโพเนนต์ลงใน panel
        historyPanel.add(historyTitle, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        // เพิ่มปุ่มกลับในส่วนล่าง
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);

        historyPanel.add(buttonPanel, BorderLayout.SOUTH);

        // เพิ่ม panel ลงใน cardPanel
        cardPanel.add(historyPanel, "History");

        // แสดงหน้า History
        cardLayout.show(cardPanel, "History");
    }

    // แสดงแต้มสะสมของผู้ใช้
    private void showPointsPanel(int userId) {
        // Panel to hold the UI components
        JPanel pointsPanel = new JPanel();
        pointsPanel.setLayout(new BorderLayout());

        // Fetch user’s current points from the database
        final double[] userPoints = {getUserPoints(userId)};

        // Display current points
        JLabel pointsLabel = new JLabel("Your current points: " + userPoints[0]);
        pointsPanel.add(pointsLabel, BorderLayout.NORTH);

        // Create a list of products that can be redeemed with points
        // Example: Map of products and their point requirements
        Map<String, Integer> redeemableProducts = new HashMap<>();
        redeemableProducts.put("Free Coffee", 100);
        redeemableProducts.put("Discount Voucher", 250);
        redeemableProducts.put("Gift Card", 500);

        // Create a JList to display the products
        DefaultListModel<String> productListModel = new DefaultListModel<>();
        for (Map.Entry<String, Integer> entry : redeemableProducts.entrySet()) {
            productListModel.addElement(entry.getKey() + " - " + entry.getValue() + " points");
        }

        JList<String> productJList = new JList<>(productListModel);
        JScrollPane scrollPane = new JScrollPane(productJList);
        pointsPanel.add(scrollPane, BorderLayout.CENTER);

        // Button to redeem the selected product
        JButton redeemButton = new JButton("Redeem");
        redeemButton.addActionListener(e -> {
            int selectedIndex = productJList.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedProduct = productListModel.get(selectedIndex);
                String[] parts = selectedProduct.split(" - ");
                String productName = parts[0];
                int pointsRequired = Integer.parseInt(parts[1].replace(" points", ""));

                if (userPoints[0] >= pointsRequired) {
                    // Deduct points from user balance
                    userPoints[0] -= pointsRequired;
                    updateUserPoints(userId, userPoints[0]);

                    // Show success message
                    JOptionPane.showMessageDialog(frame, "You successfully redeemed " + productName + "!");

                    // Update the UI to reflect the new points balance
                    pointsLabel.setText("Your current points: " + userPoints[0]);
                } else {
                    JOptionPane.showMessageDialog(frame, "You don't have enough points for " + productName + ".");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a product to redeem.");
            }
        });

        // Button to go back
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            // Switch back to the home screen or the appropriate panel
            cardLayout.show(cardPanel, "Home");
        });

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(redeemButton);
        buttonPanel.add(backButton);

        // Add the button panel to the pointsPanel
        pointsPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the pointsPanel to the card layout and display
        cardPanel.add(pointsPanel, "Points");
        cardLayout.show(cardPanel, "Points");
    }

    // ฟังก์ชันดึงประวัติการสั่งซื้อจากฐานข้อมูลสำหรับผู้ใช้
    private List<String> getUserOrderHistory(int userId) {
        List<String> orderHistory = new ArrayList<>();
        String query = "SELECT order_id, food_id, quantity, order_status, order_date FROM orders WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the userId parameter
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                // ดึงข้อมูลจากฐานข้อมูล
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    int foodId = rs.getInt("food_id");
                    int quantity = rs.getInt("quantity");
                    String orderStatus = rs.getString("order_status");
                    Timestamp orderDate = rs.getTimestamp("order_date");

                    String formattedDate = formatTimestamp(orderDate);
                    // Get the food name using the food_id
                    String foodName = getFoodNameById(foodId);

                    // Format the order detail string
                    String orderDetail = String.format("Order #%d: %s x%d, Status: %s, Date: %s",
                            orderId, foodName, quantity, orderStatus, formattedDate);

                    // Add to the history list
                    orderHistory.add(orderDetail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderHistory;
    }
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "Unknown Date";

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(new Date(timestamp.getTime()));
    }
    private String getFoodNameById(int foodId) {
        String foodName = null;
        String query = "SELECT name FROM food_items WHERE food_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, foodId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    foodName = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foodName != null ? foodName : "Unknown Food";  // Return a default value if food not found
    }


    // ฟังก์ชันดึงยอดใช้จ่ายทั้งหมดของผู้ใช้
    private double getUserTotalSpent(int userId) {
        double totalSpent = 0;
        String query = "SELECT o.food_id, o.quantity, f.price FROM orders o " +
                "JOIN food_items f ON o.food_id = f.food_id " +
                "WHERE o.user_id = ? AND o.order_status = 'Completed'"; // assuming we only count completed orders

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the userId parameter
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                // ดึงข้อมูลจากฐานข้อมูลและคำนวณรวมทั้งหมด
                while (rs.next()) {
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");

                    // Add the total for this order to the total spent
                    totalSpent += price * quantity;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalSpent;
    }


    private void showOrderPanel() {
        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(new BorderLayout());

        DefaultComboBoxModel<String> foodComboBoxModel = new DefaultComboBoxModel<>();
        JComboBox<String> foodComboBox = new JComboBox<>(foodComboBoxModel);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM food_items")) {
            while (rs.next()) {
                foodComboBoxModel.addElement(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTextField quantityField = new JTextField(5);

        JButton addOrderButton = new JButton("Add to Order");
        DefaultListModel<String> orderListModel = new DefaultListModel<>();
        JList<String> orderJList = new JList<>(orderListModel);
        JScrollPane orderScrollPane = new JScrollPane(orderJList);

        addOrderButton.addActionListener(e -> {
            String selectedFood = (String) foodComboBox.getSelectedItem();
            String quantityText = quantityField.getText();

            // ตรวจสอบให้แน่ใจว่า quantity เป็นตัวเลขเท่านั้น
            if (isValidNumber(quantityText)) {
                int quantity = Integer.parseInt(quantityText);  // แปลงจาก String เป็น int
                String orderDetails = selectedFood + " x" + quantity;
                orderListModel.addElement(orderDetails);  // เพิ่มรายการใน order list
            } else {
                // ถ้าไม่ใช่ตัวเลขแสดงข้อความเตือน
                JOptionPane.showMessageDialog(null, "Please enter a valid number for quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton checkoutButton = new JButton("Go to Checkout");
        checkoutButton.addActionListener(_ -> showCheckoutPanel(orderListModel, currentUserId));

        JPanel controlsPanel = new JPanel();
        controlsPanel.add(new JLabel("Select Food:"));
        controlsPanel.add(foodComboBox);
        controlsPanel.add(new JLabel("Quantity:"));
        controlsPanel.add(quantityField);
        controlsPanel.add(addOrderButton);
        controlsPanel.add(checkoutButton);

        orderPanel.add(controlsPanel, BorderLayout.NORTH);
        orderPanel.add(orderScrollPane, BorderLayout.CENTER);

        cardPanel.add(orderPanel, "Order");
        cardLayout.show(cardPanel, "Order");
    }

    // Relevant code refactor inside FoodOrderingApp
    private boolean isValidNumber(String text) {
        try {
            Integer.parseInt(text);  // พยายามแปลงจาก String เป็น Integer
            return true;  // ถ้าแปลงได้เป็นตัวเลข
        } catch (NumberFormatException e) {
            return false;  // ถ้าแปลงไม่ได้จะเป็น NumberFormatException
        }
    }
    private void showCheckoutPanel(DefaultListModel<String> orderListModel, int userId) {
        JPanel checkoutPanel = new JPanel();
        checkoutPanel.setLayout(new BorderLayout());

        // JTextArea ที่จะแสดงรายละเอียดการสั่งซื้อและราคาทั้งหมด
        JTextArea checkoutTextArea = new JTextArea(10, 30);
        checkoutTextArea.setEditable(false);

        double totalPrice = 0.0;

        // คำนวณราคาและแสดงรายการใน checkoutTextArea
        for (int i = 0; i < orderListModel.size(); i++) {
            String orderDetails = orderListModel.get(i);
            String[] parts = orderDetails.split(" x");
            String foodName = parts[0];
            int quantity = Integer.parseInt(parts[1]);

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement("SELECT price FROM food_items WHERE name = ?")) {
                stmt.setString(1, foodName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    totalPrice += price * quantity;
                    checkoutTextArea.append(orderDetails + " - Price: $" + (price * quantity) + "\n");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // แสดงราคาทั้งหมด
        checkoutTextArea.append("\nTotal Price: $" + totalPrice);
        var ref = new Object() {
            double currentCash = getUserCashBalance(userId);
        };
        checkoutTextArea.append("\nYour Balance: $" + ref.currentCash);
        // ปุ่มต่างๆ ในหน้าชำระเงิน
        JPanel buttonPanel = new JPanel();
        JButton editQuantityButton = new JButton("Edit Quantity");
        JButton deleteButton = new JButton("Delete");
        JButton proceedToPaymentButton = new JButton("Proceed to Payment");
        JButton backToHomeButton = new JButton("Back");

        // ใช้ JList แสดงรายการอาหารที่สั่ง
        JList<String> orderJList = new JList<>(orderListModel);
        JScrollPane orderScrollPane = new JScrollPane(orderJList);

        // Edit Quantity functionality
        editQuantityButton.addActionListener(_ -> {
            int selectedIndex = orderJList.getSelectedIndex(); // Get selected index from JList
            if (selectedIndex != -1) {
                String selectedOrder = orderListModel.get(selectedIndex);
                String[] parts = selectedOrder.split(" x");
                String foodName = parts[0];

                // Ask user for new quantity
                String input = JOptionPane.showInputDialog(frame, "Enter new quantity for " + foodName + ":", "Edit Quantity", JOptionPane.PLAIN_MESSAGE);

                try {
                    int newQuantity = Integer.parseInt(input);
                    if (newQuantity > 0) {
                        // Update the quantity in the order list
                        orderListModel.set(selectedIndex, foodName + " x" + newQuantity);
                        JOptionPane.showMessageDialog(frame, "Quantity updated!");
                        // Recalculate total price in the checkout panel
                        showCheckoutPanel(orderListModel, userId);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Quantity must be greater than 0.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid quantity entered.");
                }
            }
        });
        backToHomeButton.addActionListener(e -> {
            // This switches to the home screen (make sure there's a panel labeled "Home")
            cardLayout.show(cardPanel, "Home");
        });
        double finalTotalPrice = totalPrice;

        proceedToPaymentButton.addActionListener(_ -> {
            // Check if the available cash is enough
            if (ref.currentCash >= finalTotalPrice) {
                // Deduct the total price from current cash
                ref.currentCash -= finalTotalPrice;
                updateUserCashBalance(userId, ref.currentCash); // Update the new cash balance in the database

                // Calculate reward points (10% of the total price)
                double rewardPoints = finalTotalPrice * 0.10;
                updateUserPoints(userId, rewardPoints); // Update the user’s reward points in the database

                // Update totalspents in the users table
                updateTotalSpent(userId, finalTotalPrice);

                // Insert the order into the orders table
                logOrder(userId, orderListModel, finalTotalPrice);

                // Show the message to the user
                JOptionPane.showMessageDialog(frame, "Payment successful! Remaining balance: $" + ref.currentCash + "\nReward Points Earned: " + rewardPoints);

                // Clear the order list after payment
                orderListModel.clear();
                showCheckoutPanel(orderListModel, userId); // Refresh the checkout panel to show the updated list
            } else {
                // If not enough cash, prompt the user to top up
                int topUpAmount = Integer.parseInt(JOptionPane.showInputDialog(frame, "Your balance is insufficient! Enter amount to top up:", "Top Up", JOptionPane.PLAIN_MESSAGE));
                if (topUpAmount > 0) {
                    ref.currentCash += topUpAmount; // Add the top-up amount
                    updateUserCashBalance(userId, ref.currentCash); // Update the new cash balance in the database
                    checkoutTextArea.append("\nTopup Success Your Balance: $" + ref.currentCash);
                    JOptionPane.showMessageDialog(frame, "Top up successful! New balance: $" + ref.currentCash);
                } else {
                    JOptionPane.showMessageDialog(frame, "Top up amount must be greater than 0.");
                }
            }
        });

        // Delete functionality
        deleteButton.addActionListener(_ -> {
            int selectedIndex = orderJList.getSelectedIndex(); // Get selected index from JList
            if (selectedIndex != -1) {
                orderListModel.remove(selectedIndex); // Remove the selected order item
                JOptionPane.showMessageDialog(frame, "Order item removed!");
                // Recalculate total price in the checkout panel
                showCheckoutPanel(orderListModel, userId);
            }
        });

        // Add the buttons to the panel
        buttonPanel.add(editQuantityButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(proceedToPaymentButton);
        buttonPanel.add(backToHomeButton);

        // Add components to the checkout panel
        checkoutPanel.add(new JScrollPane(checkoutTextArea), BorderLayout.CENTER); // Display the order details in text area
        checkoutPanel.add(buttonPanel, BorderLayout.SOUTH);
        checkoutPanel.add(orderScrollPane, BorderLayout.NORTH);  // Add the order list on top

        // Add the checkout panel to the card layout and display
        cardPanel.add(checkoutPanel, "Checkout");
        cardLayout.show(cardPanel, "Checkout");
    }
    private void updateUserCashBalance(int userId, double currentCash) {
        String updateSQL = "UPDATE users SET cash_balance = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setDouble(1, currentCash); // Set the new cash balance
            stmt.setInt(2, userId); // Set the user ID
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User cash balance updated successfully!");
            } else {
                System.out.println("Failed to update cash balance for user ID: " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private double getUserCashBalance(int userId) {
        double cashBalance = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT cash_balance FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cashBalance = rs.getDouble("cash_balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cashBalance;
    }

    private void showTopUpPanel() {
        JPanel topUpPanel = new JPanel();
        topUpPanel.setLayout(new BorderLayout());

        // Create a JComboBox with predefined top-up amounts
        Integer[] topUpOptions = {50, 100, 200, 500, 1000}; // Example options
        JComboBox<Integer> topUpAmountComboBox = new JComboBox<>(topUpOptions);

        // Adjust the size of the JComboBox to match the button size
        topUpAmountComboBox.setPreferredSize(new Dimension(150, 30));  // Adjust width to 150 and height to 30 (same as button)
        topUpAmountComboBox.setSelectedIndex(0); // Set default selection to the first option

        // Create the Top-Up button
        JButton topUpButton = new JButton("Top-Up");
        topUpButton.setPreferredSize(new Dimension(150, 30));  // Set button size
        topUpButton.addActionListener(_ -> {
            // Get the selected top-up amount from the JComboBox
            int topUpAmount = (int) topUpAmountComboBox.getSelectedItem();

            if (topUpAmount > 0) {
                addBalance(currentUserId, topUpAmount);
                JOptionPane.showMessageDialog(frame, "Top-up successful!");
            } else {
                JOptionPane.showMessageDialog(frame, "Top-up amount must be greater than zero.");
            }
        });

        // Create the Back button
        JButton backToHomeButton = new JButton("Back");
        backToHomeButton.setPreferredSize(new Dimension(150, 30));  // Set button size to match others
        backToHomeButton.addActionListener(e -> {
            // This switches to the home screen (make sure there's a panel labeled "Home")
            cardLayout.show(cardPanel, "Home");
        });

        // Create a panel for the form content
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());  // Use GridBagLayout for better control over component alignment
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding between components

        // Label for the combo box
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Select top-up amount:"), gbc);

        // ComboBox for the top-up amount
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(topUpAmountComboBox, gbc);

        // Top-Up button
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(topUpButton, gbc);

        // Add the form panel to the center of the topUpPanel
        topUpPanel.add(formPanel, BorderLayout.CENTER);

        // Create a panel for the Back button at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the "Back" button
        buttonPanel.add(backToHomeButton);

        // Add the button panel to the south of the topUpPanel (bottom)
        topUpPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add the top-up panel to the card panel with the identifier "TopUp"
        cardPanel.add(topUpPanel, "TopUp");

        // Show the "TopUp" panel
        cardLayout.show(cardPanel, "TopUp");
    }

    private void updateUserPoints(int userId, double rewardPoints) {
        String query = "UPDATE users SET points = points + ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, rewardPoints); // Add the calculated reward points
            stmt.setInt(2, userId); // Specify the user ID
            stmt.executeUpdate(); // Execute the update query

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addBalance(int userId, double amount) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET cash_balance = cash_balance + ? WHERE user_id = ?")) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getFoodId(String foodName) {
        int foodId = -1;
        String query = "SELECT food_id FROM food_items WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, foodName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                foodId = rs.getInt("food_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foodId;
    }

    // Method to update totalspents for the user
    private void updateTotalSpent(int userId, double amount) {
        String updateSQL = "UPDATE users SET totalspents = totalspents + ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setDouble(1, amount); // Add the amount to totalspents
            stmt.setInt(2, userId); // Specify the user ID
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void logOrder(int userId, DefaultListModel<String> orderListModel, double totalPrice) {
        String insertOrderSQL = "INSERT INTO orders (user_id, food_id, quantity, order_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(insertOrderSQL)) {

            for (int i = 0; i < orderListModel.getSize(); i++) {
                String orderItem = orderListModel.get(i);
                String[] parts = orderItem.split(" x");
                String foodName = parts[0];
                int quantity = Integer.parseInt(parts[1]);

                // Fetch the food_id for the food item
                int foodId = getFoodId(foodName);

                // Insert the order into the database
                stmt.setInt(1, userId);
                stmt.setInt(2, foodId);
                stmt.setInt(3, quantity);
                stmt.setString(4, "Completed"); // Set order status to "Completed"
                stmt.executeUpdate();
            }

            // Optionally, insert the total price as part of the order if needed
            // You can store the total price in a separate column if required.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private double getUserPoints(int userId) {
        double points = 0;
        String query = "SELECT points FROM users WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    points = rs.getDouble("points");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }
    private String getUserName(int userId) {
        String username = "Guest"; // ค่าปริยาย
        String query = "SELECT username FROM users WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);  // ตั้งค่ารหัสผู้ใช้
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                username = rs.getString("username");
                System.out.println("Fetched username: " + username); // ตรวจสอบค่าที่ได้
            } else {
                System.out.println("No user found with userId: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching username for userId: " + userId);
            e.printStackTrace();
        }

        return username;
    }


}
