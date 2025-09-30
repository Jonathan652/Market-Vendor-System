/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package bse_oop2_2025;

import java.awt.HeadlessException;
import java.sql.*;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import javax.swing.DefaultListModel;

/**
 *
 * @author jonah
 */
public class FeeCollection extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FeeCollection.class.getName());

    /**
     * Creates new form FeeCollection
     */
    public FeeCollection() {
        initComponents();
        setupDatabase();
        loadVendors();
        loadFeeTypes();
        loadTodaysFees();
        calculateTotals();
        loadPendingFeesList();
        setupListSelection();
    }

    private void setupDatabase() {
        try {
            Connection conn = DbConnection.getConnection();
            Statement stmt = conn.createStatement();

            // Check if fee_type column exists, if not add it
            try {
                stmt.executeUpdate("ALTER TABLE daily_fees ADD COLUMN fee_type VARCHAR(50) DEFAULT 'Daily Revenue'");
                System.out.println("Added fee_type column to database");
            } catch (SQLException e) {
                System.out.println("Column may already exist: " + e.getMessage());
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("Database setup error: " + e.getMessage());
        }
    }

    private void loadFeeTypes() {
        comboxfeetype.removeAllItems();
        comboxfeetype.addItem("Select Fee Type");
        comboxfeetype.addItem("Monthly Rent");
        comboxfeetype.addItem("Cleaning Fee");
        comboxfeetype.addItem("Daily Revenue");
    }

    private double getFeeAmount(String feeType) {
        switch (feeType) {
            case "Monthly Rent":
                return 50000.0;
            case "Cleaning Fee":
                return 5000.0;
            case "Daily Revenue":
                return 1000.0;
            default:
                return 0.0;
        }
    }

    private void setupListSelection() {
        lstpending.addListSelectionListener((javax.swing.event.ListSelectionEvent evt) -> {
            if (!evt.getValueIsAdjusting()) {
                handlePendingFeeSelection();
            }
        });
    }

    public void loadPendingFeesList() {
        try {
            Connection conn = DbConnection.getConnection();

            // SIMPLE QUERY WITHOUT FEE_TYPE FIRST
            String sql = "SELECT f.fee_id, CONCAT(v.first_name, ' ', v.last_name) AS vendor_name, "
                    + "f.amount, f.fee_date, DATEDIFF(CURDATE(), f.fee_date) AS days_overdue "
                    + "FROM daily_fees f "
                    + "JOIN vendors v ON f.vendor_id = v.vendor_id "
                    + "WHERE f.payment_status = 'Pending' "
                    + "ORDER BY f.fee_date ASC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            DefaultListModel<String> listModel = new DefaultListModel<>();

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;

                String vendorName = rs.getString("vendor_name");
                double amount = rs.getDouble("amount");
                String feeDate = rs.getDate("fee_date").toString();
                int daysOverdue = rs.getInt("days_overdue");

                // SIMPLE DISPLAY WITHOUT FEE_TYPE
                String listItem = vendorName + " - UGX " + String.format("%.0f", amount) + " (" + feeDate + ")";

                if (daysOverdue > 0) {
                    listItem += " [" + daysOverdue + " days overdue]";
                }

                listModel.addElement(listItem);
            }

            if (!hasData) {
                listModel.addElement("No pending fees found");
            }

            lstpending.setModel(listModel);
            conn.close();

        } catch (Exception e) {
            // SIMPLE ERROR HANDLING
            JOptionPane.showMessageDialog(this, "Error loading fees. Please check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePendingFeeSelection() {
        // SIMPLIFIED VERSION
        String selectedItem = lstpending.getSelectedValue();
        if (selectedItem != null && !selectedItem.equals("No pending fees found")) {
            JOptionPane.showMessageDialog(this, "Selected: " + selectedItem);
        }
    }

    public void loadVendors() {
        try {
            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT vendor_id, CONCAT(first_name, ' ', last_name) AS vendor_name FROM vendors WHERE status = 'Active'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                cmbvendor.removeAllItems();
                cmbvendor.addItem("Select Vendor");

                while (rs.next()) {
                    String item = rs.getInt("vendor_id") + " - " + rs.getString("vendor_name");
                    cmbvendor.addItem(item);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading vendors: " + e.getMessage());
        }
    }

    private void loadVendorStallRent(int vendorId) {
        try {
            Connection conn = DbConnection.getConnection();
            String sql = "SELECT s.monthly_rent FROM stall_assignments sa "
                    + "JOIN stalls s ON sa.stall_id = s.stall_id "
                    + "WHERE sa.vendor_id = ? AND sa.status = 'Active' "
                    + "ORDER BY sa.assignment_date DESC LIMIT 1";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, vendorId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                double monthlyRent = rs.getDouble("monthly_rent");
                txtamount.setText(String.valueOf(monthlyRent));
            } else {
                txtamount.setText("1000"); // Default if no stall assigned
            }

            conn.close();
        } catch (Exception e) {
            txtamount.setText("1000"); // Default on error
            System.out.println("Error loading stall rent: " + e.getMessage());
        }
    }

    private String getSelectedDate() {
        if (cndate.getDate() != null) {
            java.util.Date selectedDate = cndate.getDate();
            java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
            return sqlDate.toString();
        } else {
            return java.time.LocalDate.now().toString();
        }
    }

    public void loadTodaysFees() {
        try {
            Connection conn = DbConnection.getConnection();
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading fees: " + e.getMessage());
        }
    }

    public void calculateTotals() {
        try {
            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT "
                        + "SUM(CASE WHEN payment_status = 'Paid' THEN amount ELSE 0 END) AS total_collected, "
                        + "SUM(CASE WHEN payment_status = 'Pending' THEN amount ELSE 0 END) AS total_pending "
                        + "FROM daily_fees WHERE fee_date = CURDATE()";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    double totalCollected = rs.getDouble("total_collected");
                    double totalPending = rs.getDouble("total_pending");

                    lbCollected.setText("Total Collected: UGX " + totalCollected);
                    lbpending.setText("Total Pending: UGX " + totalPending);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error calculating totals: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbdate = new javax.swing.JLabel();
        lbvendor = new javax.swing.JButton();
        cmbvendor = new javax.swing.JComboBox<>();
        lbfee = new javax.swing.JLabel();
        btngeneratefee = new javax.swing.JButton();
        lbpending = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstpending = new javax.swing.JList<>();
        btnviewpayments = new javax.swing.JButton();
        btnback = new javax.swing.JButton();
        btndelete = new javax.swing.JButton();
        lbCollected = new javax.swing.JLabel();
        txtcollected = new javax.swing.JTextField();
        cndate = new com.toedter.calendar.JCalendar();
        btnClear = new javax.swing.JButton();
        lbamount = new javax.swing.JLabel();
        txtamount = new javax.swing.JTextField();
        comboxfeetype = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lbdate.setText("Date");

        lbvendor.setText("Vendor");

        cmbvendor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbvendor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbvendorActionPerformed(evt);
            }
        });

        lbfee.setText(" Fee Type");

        btngeneratefee.setText("Generate Fee");
        btngeneratefee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btngeneratefeeActionPerformed(evt);
            }
        });

        lbpending.setText("Pending");

        lstpending.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lstpending);

        btnviewpayments.setText("View Payments");
        btnviewpayments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnviewpaymentsActionPerformed(evt);
            }
        });

        btnback.setText("Back");
        btnback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnbackActionPerformed(evt);
            }
        });

        btndelete.setText("Delete");
        btndelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btndeleteActionPerformed(evt);
            }
        });

        lbCollected.setText(" Collected");

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        lbamount.setText("Amount");

        comboxfeetype.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboxfeetype.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboxfeetypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btndelete)
                .addGap(115, 115, 115)
                .addComponent(btnback)
                .addGap(32, 32, 32))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(199, 199, 199)
                        .addComponent(btngeneratefee))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(639, 639, 639)
                        .addComponent(btnClear)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbCollected)
                            .addComponent(lbpending))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(348, 348, 348)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtcollected, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(369, 369, 369)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(comboxfeetype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtamount, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(216, 216, 216)
                        .addComponent(btnviewpayments))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(26, 26, 26)
                                    .addComponent(lbdate)
                                    .addGap(395, 395, 395))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lbvendor)
                                    .addGap(366, 366, 366)))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lbamount)
                                    .addComponent(lbfee))
                                .addGap(381, 381, 381)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cndate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbvendor, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(359, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cndate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbvendor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(comboxfeetype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtamount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbamount))
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(162, 162, 162)
                        .addComponent(lbdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                        .addComponent(lbvendor)
                        .addGap(18, 18, 18)
                        .addComponent(lbfee)
                        .addGap(59, 59, 59)))
                .addComponent(btngeneratefee)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbCollected)
                    .addComponent(txtcollected, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lbpending, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(122, 122, 122))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21)
                        .addComponent(btnClear)
                        .addGap(28, 28, 28)
                        .addComponent(btnviewpayments)
                        .addGap(8, 8, 8)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnback)
                    .addComponent(btndelete))
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btngeneratefeeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btngeneratefeeActionPerformed
        // TODO add your handling code here:
        if (cmbvendor.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a vendor!");
            return;
        }

        if (comboxfeetype.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a fee type!");
            return;
        }

        String amountText = txtamount.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter fee amount!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String vendorInfo = cmbvendor.getSelectedItem().toString();
            String feeType = comboxfeetype.getSelectedItem().toString();
            int vendorId = Integer.parseInt(vendorInfo.split(" - ")[0]);

            Connection conn = DbConnection.getConnection();
            String sql = "INSERT INTO daily_fees (vendor_id, fee_date, fee_type, amount, payment_status) VALUES (?, ?, ?, ?, 'Pending')";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, vendorId);
            pst.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            pst.setString(3, feeType);
            pst.setDouble(4, amount);

            int result = pst.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Fee generated successfully!");
                comboxfeetype.setSelectedIndex(0);
                cmbvendor.setSelectedIndex(0);
                txtamount.setText("");
                loadPendingFeesList();
            }

            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }

    }//GEN-LAST:event_btngeneratefeeActionPerformed

    private void btnbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbackActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        new MainDashBoard().setVisible(true);

    }//GEN-LAST:event_btnbackActionPerformed

    private void btndeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndeleteActionPerformed
        // TODO add your handling code here:

        int feeId = 0;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this fee?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                try (Connection conn = DbConnection.getConnection()) {
                    String sql = "DELETE FROM daily_fees WHERE fee_id = ?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setInt(1, feeId);

                    int result = pst.executeUpdate();

                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "Fee deleted successfully!");
                        loadTodaysFees(); // Refresh table
                    }
                }
            } catch (HeadlessException | SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting fee: " + e.getMessage());
            }
        }

    }//GEN-LAST:event_btndeleteActionPerformed

    private void cmbvendorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbvendorActionPerformed
        // TODO add your handling code here
        if (cmbvendor.getSelectedIndex() > 0) { 
            String vendorInfo = cmbvendor.getSelectedItem().toString();
            int vendorId = Integer.parseInt(vendorInfo.split(" - ")[0]);

            // Auto-populate monthly rent based on assigned stall
            loadVendorStallRent(vendorId);
        } else {
            txtamount.setText("1000"); // Default amount when no vendor selected
        }


    }//GEN-LAST:event_cmbvendorActionPerformed

    private void btnviewpaymentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnviewpaymentsActionPerformed
        // TODO add your handling code here:
        try {
            Connection conn = DbConnection.getConnection();
            String sql = "SELECT p.payment_id, CONCAT(v.first_name, ' ', v.last_name) AS vendor_name, "
                    + "p.amount_paid, p.payment_date, p.payment_method "
                    + "FROM payments p "
                    + "JOIN daily_fees f ON p.fee_id = f.fee_id "
                    + "JOIN vendors v ON f.vendor_id = v.vendor_id "
                    + "ORDER BY p.payment_date DESC LIMIT 20";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            String paymentList = "RECENT PAYMENTS:\n";
            paymentList += "ID | Vendor | Amount | Date | Method\n";

            while (rs.next()) {
                paymentList += rs.getInt("payment_id") + " | "
                        + rs.getString("vendor_name") + " | UGX "
                        + rs.getDouble("amount_paid") + " | "
                        + rs.getDate("payment_date") + " | "
                        + rs.getString("payment_method") + "\n";
            }

            JOptionPane.showMessageDialog(this, paymentList, "Payment History", JOptionPane.INFORMATION_MESSAGE);
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading payments: " + e.getMessage());

        }
    }//GEN-LAST:event_btnviewpaymentsActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        String selectedItem = lstpending.getSelectedValue();

        if (selectedItem == null || selectedItem.equals("No pending fees found")
                || selectedItem.startsWith("Error loading")) {
            JOptionPane.showMessageDialog(this, "Please select a pending fee to clear!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this pending fee?\n" + selectedItem,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Extract information from selected item
                String[] parts = selectedItem.split(" - ");
                String vendorName = parts[0];
                String[] nameParts = vendorName.split(" ");
                String firstName = nameParts[0];
                String lastName = nameParts.length > 1 ? nameParts[1] : "";

                try (Connection conn = DbConnection.getConnection()) {
                    String sql = "DELETE f FROM daily_fees f "
                            + "JOIN vendors v ON f.vendor_id = v.vendor_id "
                            + "WHERE f.payment_status = 'Pending' AND v.first_name = ? "
                            + (lastName.isEmpty() ? "" : "AND v.last_name = ?");

                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, firstName);
                    if (!lastName.isEmpty()) {
                        pst.setString(2, lastName);
                    }

                    int result = pst.executeUpdate();

                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "Pending fee deleted successfully!");
                        loadPendingFeesList();
                    } else {
                        JOptionPane.showMessageDialog(this, "No matching fee found to delete!");
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting fee: " + e.getMessage());
            }
        }


    }//GEN-LAST:event_btnClearActionPerformed

    private void comboxfeetypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboxfeetypeActionPerformed
        // TODO add your handling code here:
        if (comboxfeetype.getSelectedIndex() > 0) {
            String feeType = comboxfeetype.getSelectedItem().toString();
            double amount = getFeeAmount(feeType);
            txtamount.setText(String.valueOf(amount));
        }
    }//GEN-LAST:event_comboxfeetypeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FeeCollection().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnback;
    private javax.swing.JButton btndelete;
    private javax.swing.JButton btngeneratefee;
    private javax.swing.JButton btnviewpayments;
    private javax.swing.JComboBox<String> cmbvendor;
    private com.toedter.calendar.JCalendar cndate;
    private javax.swing.JComboBox<String> comboxfeetype;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbCollected;
    private javax.swing.JLabel lbamount;
    private javax.swing.JLabel lbdate;
    private javax.swing.JLabel lbfee;
    private javax.swing.JLabel lbpending;
    private javax.swing.JButton lbvendor;
    private javax.swing.JList<String> lstpending;
    private javax.swing.JTextField txtamount;
    private javax.swing.JTextField txtcollected;
    // End of variables declaration//GEN-END:variables

}
