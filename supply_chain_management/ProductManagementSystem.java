import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProductManagementSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/supply?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root"; // Replace with your MySQL username
    private static final String DB_PASS = "Ganc@2005"; // Replace with your MySQL password

    private Connection connection;

    public ProductManagementSystem() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database.");
            e.printStackTrace();
            System.exit(1);
        }

        createGUI();
    }

    private void createGUI() {
        JFrame frame = new JFrame("Supply Chain Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Supply Chain Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        frame.add(titlePanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 3, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton customerButton = createStyledButton("Manage Customers");
        JButton productButton = createStyledButton("Manage Products");
        JButton purchaseButton = createStyledButton("Purchase Products");
        JButton viewPurchasesButton = createStyledButton("View Purchases");
        JButton searchButton = createStyledButton("Search Products");
        JButton viewCustomersButton = createStyledButton("View Customers");
        JButton viewProductsButton = createStyledButton("View Products");
        JButton updateCustomerButton = createStyledButton("Update Customer");
        JButton updateProductButton = createStyledButton("Update Product");

        buttonPanel.add(customerButton);
        buttonPanel.add(productButton);
        buttonPanel.add(purchaseButton);
        buttonPanel.add(viewPurchasesButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(viewCustomersButton);
        buttonPanel.add(viewProductsButton);
        buttonPanel.add(updateCustomerButton);
        buttonPanel.add(updateProductButton);

        frame.add(buttonPanel, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(220, 220, 220));
        JLabel footerLabel = new JLabel("© 2024 Product Management System");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerPanel.add(footerLabel);
        frame.add(footerPanel, BorderLayout.SOUTH);
        
        // Button Actions
        customerButton.addActionListener(e -> manageCustomers());
        productButton.addActionListener(e -> manageProducts());
        purchaseButton.addActionListener(e -> purchaseProduct());
        viewPurchasesButton.addActionListener(e -> viewPurchases());
        searchButton.addActionListener(e -> searchProduct());
        viewCustomersButton.addActionListener(e -> viewCustomers());
        viewProductsButton.addActionListener(e -> viewProducts());
        updateCustomerButton.addActionListener(e -> updateCustomer());
        updateProductButton.addActionListener(e -> updateProduct());
        
        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }

    private void manageCustomers() {
        String name = JOptionPane.showInputDialog("Enter Customer Name:");
        String email = JOptionPane.showInputDialog("Enter Customer Email:");
        String contact = JOptionPane.showInputDialog("Enter Customer Contact:");

        String query = "INSERT INTO Customers (Name, Email, Contact) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, contact);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Customer added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to add customer.");
            e.printStackTrace();
        }
    }

    private void manageProducts() {
        String name = JOptionPane.showInputDialog("Enter Product Name:");
        String priceStr = JOptionPane.showInputDialog("Enter Product Price:");
        String stockStr = JOptionPane.showInputDialog("Enter Product Stock:");

        double price = Double.parseDouble(priceStr);
        int stock = Integer.parseInt(stockStr);

        String query = "INSERT INTO Products (Name, Price, Stock) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Product added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to add product.");
            e.printStackTrace();
        }
    }

    private void purchaseProduct() {
        String customerIdStr = JOptionPane.showInputDialog("Enter Customer ID:");
        String productIdStr = JOptionPane.showInputDialog("Enter Product ID:");
        String quantityStr = JOptionPane.showInputDialog("Enter Quantity:");
    
        int customerId = Integer.parseInt(customerIdStr);
        int productId = Integer.parseInt(productIdStr);
        int quantity = Integer.parseInt(quantityStr);
    
        try {
            // Check the stock of the product
            String checkStockQuery = "SELECT Stock FROM Products WHERE ProductID = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkStockQuery)) {
                checkStmt.setInt(1, productId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int currentStock = rs.getInt("Stock");
                    if (currentStock < quantity) {
                        JOptionPane.showMessageDialog(null, "Insufficient stock! Available stock: " + currentStock);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Product not found.");
                    return;
                }
            }
    
            // Proceed with the purchase
            String purchaseQuery = "INSERT INTO Purchases (CustomerID, ProductID, Quantity) VALUES (?, ?, ?)";
            try (PreparedStatement purchaseStmt = connection.prepareStatement(purchaseQuery)) {
                purchaseStmt.setInt(1, customerId);
                purchaseStmt.setInt(2, productId);
                purchaseStmt.setInt(3, quantity);
                purchaseStmt.executeUpdate();
            }
    
            // Deduct the stock
            String updateStockQuery = "UPDATE Products SET Stock = Stock - ? WHERE ProductID = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateStockQuery)) {
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, productId);
                updateStmt.executeUpdate();
            }
    
            JOptionPane.showMessageDialog(null, "Purchase successful! Stock updated.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to complete the purchase.");
            e.printStackTrace();
        }
    }
    

    private void viewPurchases() {
        String query = "SELECT * FROM Purchases";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            StringBuilder sb = new StringBuilder("Purchases:\n");
            while (rs.next()) {
                sb.append("PurchaseID: ").append(rs.getInt("PurchaseID"))
                  .append(", CustomerID: ").append(rs.getInt("CustomerID"))
                  .append(", ProductID: ").append(rs.getInt("ProductID"))
                  .append(", Quantity: ").append(rs.getInt("Quantity"))
                  .append(", Date: ").append(rs.getTimestamp("PurchaseDate"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to retrieve purchases.");
            e.printStackTrace();
        }
    }

    private void searchProduct() {
        String keyword = JOptionPane.showInputDialog("Enter Product Name or ID:");
        String query = "SELECT * FROM Products WHERE Name LIKE ? OR ProductID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, keyword);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Search Results:\n");
            while (rs.next()) {
                sb.append("ProductID: ").append(rs.getInt("ProductID"))
                  .append(", Name: ").append(rs.getString("Name"))
                  .append(", Price: ").append(rs.getDouble("Price"))
                  .append(", Stock: ").append(rs.getInt("Stock"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to search products.");
            e.printStackTrace();
        }
    }

    private void viewCustomers() {
        String query = "SELECT * FROM Customers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            StringBuilder sb = new StringBuilder("Customer Details:\n");
            while (rs.next()) {
                sb.append("CustomerID: ").append(rs.getInt("CustomerID"))
                  .append(", Name: ").append(rs.getString("Name"))
                  .append(", Email: ").append(rs.getString("Email"))
                  .append(", Contact: ").append(rs.getString("Contact"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to retrieve customers.");
            e.printStackTrace();
        }
    }

    private void viewProducts() {
        String query = "SELECT * FROM Products";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            StringBuilder sb = new StringBuilder("Product Details:\n");
            while (rs.next()) {
                sb.append("ProductID: ").append(rs.getInt("ProductID"))
                  .append(", Name: ").append(rs.getString("Name"))
                  .append(", Price: ").append(rs.getDouble("Price"))
                  .append(", Stock: ").append(rs.getInt("Stock"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to retrieve products.");
            e.printStackTrace();
        }
    }

    private void updateCustomer() {
        String customerIdStr = JOptionPane.showInputDialog("Enter Customer ID to Update:");
        if (customerIdStr == null || customerIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Customer ID is required!");
            return;
        }
    
        int customerId = Integer.parseInt(customerIdStr);
    
        String newName = JOptionPane.showInputDialog("Enter New Name (leave blank to keep unchanged):");
        String newEmail = JOptionPane.showInputDialog("Enter New Email (leave blank to keep unchanged):");
        String newContact = JOptionPane.showInputDialog("Enter New Contact (leave blank to keep unchanged):");
    
        String query = "UPDATE Customers SET Name = COALESCE(NULLIF(?, ''), Name), " +
                       "Email = COALESCE(NULLIF(?, ''), Email), " +
                       "Contact = COALESCE(NULLIF(?, ''), Contact) WHERE CustomerID = ?";
    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setString(2, newEmail);
            stmt.setString(3, newContact);
            stmt.setInt(4, customerId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Customer details updated successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Customer ID not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to update customer details.");
            e.printStackTrace();
        }
    }

    private void updateProduct() {
        String productIdStr = JOptionPane.showInputDialog("Enter Product ID to Update:");
        if (productIdStr == null || productIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Product ID is required!");
            return;
        }
    
        int productId = Integer.parseInt(productIdStr);
    
        String newName = JOptionPane.showInputDialog("Enter New Name (leave blank to keep unchanged):");
        String newPriceStr = JOptionPane.showInputDialog("Enter New Price (leave blank to keep unchanged):");
        String newStockStr = JOptionPane.showInputDialog("Enter New Stock (leave blank to keep unchanged):");
    
        String query = "UPDATE Products SET Name = COALESCE(NULLIF(?, ''), Name), " +
                       "Price = COALESCE(NULLIF(?, ''), Price), " +
                       "Stock = COALESCE(NULLIF(?, ''), Stock) WHERE ProductID = ?";
    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setString(2, newPriceStr.isEmpty() ? null : newPriceStr);
            stmt.setString(3, newStockStr.isEmpty() ? null : newStockStr);
            stmt.setInt(4, productId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Product details updated successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Product ID not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to update product details.");
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        new ProductManagementSystem();
    }
}